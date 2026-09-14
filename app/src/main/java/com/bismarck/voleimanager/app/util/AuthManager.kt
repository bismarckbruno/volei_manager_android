package com.bismarck.voleimanager.app.util

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Usuário autenticado, já convertido para um tipo simples (sem depender do SDK do Firebase fora
 *  desta camada). [nickname] é o nome público exibido no topo do app (definido pelo próprio
 *  usuário no cadastro/edição de perfil, pode ser diferente do nome completo); [fullName] e
 *  [birthDate] (ISO `yyyy-MM-dd`) ficam guardados para uma futura verificação de elegibilidade de
 *  compra premium. [photoBase64] é uma miniatura JPEG já reduzida (ver [encodeAvatarBase64]),
 *  guardada no documento Firestore `users/{uid}` — não usamos Firebase Storage para isso. */
data class AppAuthUser(
    val uid: String,
    val email: String?,
    val nickname: String?,
    val fullName: String?,
    val birthDate: String?,
    val photoBase64: String?,
    val photoUrl: String?
)

private const val USERS_COLLECTION = "users"
private const val FIELD_NICKNAME = "nickname"
private const val FIELD_FULL_NAME = "fullName"
private const val FIELD_BIRTH_DATE = "birthDate"
private const val FIELD_PHOTO_BASE64 = "photoBase64"

/**
 * Fachada única sobre o Firebase Authentication (e-mail/senha por enquanto — login com Google
 * fica para uma fase seguinte) e sobre o documento de perfil complementar guardado no Firestore
 * (`users/{uid}`: apelido público, nome completo, data de nascimento e foto reduzida). Segue o
 * mesmo cuidado do [TelemetryManager]: se o app não tiver um `google-services.json` válido, todas
 * as chamadas abaixo falham de forma segura (nunca derruba o app) e [currentUser] simplesmente
 * nunca emite um usuário logado.
 */
object AuthManager {
    private const val TAG = "AuthManager"
    private const val CACHE_PREFS_NAME = "auth_profile_cache"

    /** Guardado apenas para acessar o cache local (SharedPreferences) de perfil — ver [init].
     *  Sempre um `applicationContext`, então não há risco de vazamento de memória. */
    private var appContext: Context? = null

    /** Deve ser chamado uma vez na inicialização do app (feito em `VoleiViewModel`), para que o
     *  perfil (apelido/nome/data de nascimento/foto) fique disponível localmente mesmo quando o
     *  Firestore ainda não confirmou a última escrita — ver [localOverrides]. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun authOrNull(): FirebaseAuth? = try {
        Firebase.auth
    } catch (e: Exception) {
        Log.d(TAG, "Firebase Auth indisponível: ${e.message}")
        null
    }

    private fun firestoreOrNull(): FirebaseFirestore? = try {
        Firebase.firestore
    } catch (e: Exception) {
        Log.d(TAG, "Firestore indisponível: ${e.message}")
        null
    }

    /** Alterações de perfil pendentes de confirmação pelo Firestore (ou definitivas, se o
     *  Firestore não estiver disponível/configurado). Existe para que editar apelido/nome/data de
     *  nascimento/foto funcione instantaneamente e de forma confiável mesmo offline ou com o
     *  Firestore ainda não provisionado no console do Firebase — a sincronização em nuvem
     *  acontece "por baixo", sem bloquear a experiência local. Também persistido em
     *  SharedPreferences (por uid) para sobreviver a reinícios do app antes do primeiro snapshot
     *  do Firestore chegar. */
    private val localOverrides = MutableStateFlow<Map<String, String?>>(emptyMap())
    private var localOverridesUid: String? = null

    private fun cachePrefs() = appContext?.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE)

    private fun cacheKey(uid: String, field: String) = "${uid}_$field"

    private fun readCachedProfile(uid: String): Map<String, String?> {
        val prefs = cachePrefs() ?: return emptyMap()
        val fields = listOf(FIELD_NICKNAME, FIELD_FULL_NAME, FIELD_BIRTH_DATE, FIELD_PHOTO_BASE64)
        return buildMap {
            fields.forEach { field ->
                if (prefs.contains(cacheKey(uid, field))) put(field, prefs.getString(cacheKey(uid, field), null))
            }
        }
    }

    private fun writeCachedProfile(uid: String, updates: Map<String, String?>) {
        cachePrefs()?.edit()?.apply {
            updates.forEach { (field, value) ->
                val key = cacheKey(uid, field)
                if (value == null) remove(key) else putString(key, value)
            }
        }?.apply()
    }

    /** Aplica uma alteração local imediatamente (otimista) — usado por [saveProfileDoc] e
     *  [updateProfilePhoto] antes mesmo da escrita no Firestore terminar. */
    private fun applyLocalOverride(uid: String, updates: Map<String, String?>) {
        if (localOverridesUid != uid) {
            localOverridesUid = uid
            localOverrides.value = readCachedProfile(uid)
        }
        localOverrides.value = localOverrides.value + updates
        writeCachedProfile(uid, updates)
    }

    /** Emite o usuário autenticado (dados do Firebase Auth + perfil complementar do Firestore),
     *  ou `null`, sempre que o estado de login, o documento de perfil ou uma alteração local
     *  pendente mudar. Enquanto o Firestore não confirma o mesmo valor de um campo alterado
     *  localmente, o valor local prevalece (ver [localOverrides]); assim que o snapshot remoto
     *  alcança o valor local, o override correspondente é liberado (permitindo que futuras
     *  mudanças feitas em outro dispositivo voltem a valer). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<AppAuthUser?> = authStateFlow().flatMapLatest { user ->
        if (user == null) {
            localOverridesUid = null
            localOverrides.value = emptyMap()
            flowOf(null)
        } else {
            if (localOverridesUid != user.uid) {
                localOverridesUid = user.uid
                localOverrides.value = readCachedProfile(user.uid)
            }
            combine(profileDocFlow(user.uid), localOverrides) { remote, overrides ->
                val reconciled = overrides.filterKeys { field -> remote?.get(field) != overrides[field] }
                if (reconciled.size != overrides.size) {
                    localOverrides.value = reconciled
                }
                val merged = (remote ?: emptyMap()).toMutableMap()
                reconciled.forEach { (field, value) -> merged[field] = value }
                buildAppAuthUser(user, merged)
            }
        }
    }

    private fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val auth = authOrNull()
        if (auth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth -> trySend(firebaseAuth.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Escuta em tempo real o documento de perfil complementar do usuário logado. */
    private fun profileDocFlow(uid: String): Flow<Map<String, Any?>?> = callbackFlow {
        val firestore = firestoreOrNull()
        if (firestore == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val registration = firestore.collection(USERS_COLLECTION).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Falha ao observar perfil: ${error.message}")
                    trySend(null)
                } else {
                    trySend(snapshot?.data)
                }
            }
        awaitClose { registration.remove() }
    }

    private fun buildAppAuthUser(user: FirebaseUser, profile: Map<String, Any?>?): AppAuthUser = AppAuthUser(
        uid = user.uid,
        email = user.email,
        nickname = (profile?.get(FIELD_NICKNAME) as? String)?.takeIf { it.isNotBlank() } ?: user.displayName,
        fullName = profile?.get(FIELD_FULL_NAME) as? String,
        birthDate = profile?.get(FIELD_BIRTH_DATE) as? String,
        photoBase64 = profile?.get(FIELD_PHOTO_BASE64) as? String,
        photoUrl = user.photoUrl?.toString()
    )

    /** Cria uma conta gratuita com e-mail/senha, grava o perfil complementar (apelido exibido no
     *  app, nome completo e data de nascimento — usada futuramente para checar elegibilidade de
     *  compra premium) e já efetua o login. Retorna uma mensagem de erro amigável em caso de
     *  falha, ou `null` em caso de sucesso. */
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        nickname: String,
        birthDate: String
    ): String? {
        val auth = authOrNull() ?: return "Serviço de conta indisponível no momento."
        return try {
            val result = suspendCancellableCoroutine<Result<FirebaseUser?>> { cont ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(task.result?.user))
                        } else {
                            cont.resume(Result.failure(task.exception ?: Exception("Falha ao criar conta")))
                        }
                    }
            }
            result.getOrThrow()?.let { user ->
                if (nickname.isNotBlank()) {
                    awaitProfileUpdate(user, nickname)
                }
                saveProfileDoc(user.uid, fullName = fullName, nickname = nickname, birthDate = birthDate)
            }
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível criar a conta."
        }
    }

    /** Autentica com e-mail/senha. Retorna uma mensagem de erro amigável em caso de falha, ou
     *  `null` em caso de sucesso. */
    suspend fun signIn(email: String, password: String): String? {
        val auth = authOrNull() ?: return "Serviço de conta indisponível no momento."
        return try {
            val result = suspendCancellableCoroutine<Result<Unit>> { cont ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(Unit))
                        } else {
                            cont.resume(Result.failure(task.exception ?: Exception("Falha ao entrar")))
                        }
                    }
            }
            result.getOrThrow()
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível entrar. Verifique seu e-mail e senha."
        }
    }

    fun signOut() {
        authOrNull()?.signOut()
    }

    /** Atualiza o apelido público, nome completo e data de nascimento do usuário logado. Aplica a
     *  mudança localmente de imediato (ver [applyLocalOverride]) e só então tenta sincronizar com
     *  o Firestore — assim o app continua funcionando normalmente mesmo se o Firestore ainda não
     *  estiver provisionado ou o dispositivo estiver offline. Retorna uma mensagem de erro amigável
     *  em caso de falha na sincronização com a conta (ex.: sem login), ou `null` em caso de sucesso. */
    suspend fun updateProfile(nickname: String, fullName: String, birthDate: String): String? {
        val user = authOrNull()?.currentUser ?: return "Você precisa estar logado para editar o perfil."
        if (nickname.isNotBlank() && nickname != user.displayName) {
            try {
                awaitProfileUpdate(user, nickname)
            } catch (e: Exception) {
                Log.d(TAG, "Falha ao atualizar displayName: ${e.message}")
            }
        }
        saveProfileDoc(user.uid, fullName = fullName, nickname = nickname, birthDate = birthDate)
        return null
    }

    /** Define (ou remove, se [base64] for `null`) a miniatura de foto de perfil do usuário logado.
     *  Assim como [updateProfile], aplica a mudança localmente de imediato para que a foto apareça
     *  na hora, e sincroniza com o Firestore em segundo plano (sem bloquear nem falhar a operação
     *  local caso o Firestore esteja indisponível). */
    suspend fun updateProfilePhoto(base64: String?): String? {
        val uid = authOrNull()?.currentUser?.uid ?: return "Você precisa estar logado para editar a foto."
        applyLocalOverride(uid, mapOf(FIELD_PHOTO_BASE64 to base64))
        val firestore = firestoreOrNull() ?: return null
        return try {
            suspendCancellableCoroutine<Unit> { cont ->
                firestore.collection(USERS_COLLECTION).document(uid)
                    .set(mapOf(FIELD_PHOTO_BASE64 to base64), com.google.firebase.firestore.SetOptions.merge())
                    .addOnCompleteListener { cont.resume(Unit) }
            }
            null
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao sincronizar foto de perfil com o Firestore: ${e.message}")
            null
        }
    }

    /** Apaga a conta do usuário logado (Firebase Auth + documento de perfil no Firestore + cache
     *  local). Retorna uma mensagem de erro amigável em caso de falha (por exemplo, quando o
     *  Firebase exige um login recente antes de permitir apagar a conta), ou `null` em caso de
     *  sucesso. */
    suspend fun deleteAccount(): String? {
        val user = authOrNull()?.currentUser ?: return "Você precisa estar logado para apagar a conta."
        val uid = user.uid
        return try {
            firestoreOrNull()?.let { firestore ->
                suspendCancellableCoroutine<Unit> { cont ->
                    firestore.collection(USERS_COLLECTION).document(uid).delete()
                        .addOnCompleteListener { cont.resume(Unit) }
                }
            }
            suspendCancellableCoroutine<Result<Unit>> { cont ->
                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(Result.failure(task.exception ?: Exception("Falha ao apagar a conta")))
                    }
                }
            }.getOrThrow()
            clearCachedProfile(uid)
            if (localOverridesUid == uid) localOverrides.value = emptyMap()
            null
        } catch (e: Exception) {
            if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                "Por segurança, entre novamente antes de apagar sua conta."
            } else {
                e.message ?: "Não foi possível apagar a conta."
            }
        }
    }

    /** Persiste o apelido/nome/data de nascimento localmente (imediato) e tenta sincronizar com o
     *  Firestore em segundo plano — sem bloquear nem falhar a operação local caso o Firestore
     *  esteja indisponível/não provisionado. */
    private suspend fun saveProfileDoc(uid: String, fullName: String, nickname: String, birthDate: String) {
        applyLocalOverride(
            uid,
            mapOf(FIELD_FULL_NAME to fullName, FIELD_NICKNAME to nickname, FIELD_BIRTH_DATE to birthDate)
        )
        val firestore = firestoreOrNull() ?: return
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                firestore.collection(USERS_COLLECTION).document(uid)
                    .set(
                        mapOf(
                            FIELD_FULL_NAME to fullName,
                            FIELD_NICKNAME to nickname,
                            FIELD_BIRTH_DATE to birthDate
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    .addOnCompleteListener { cont.resume(Unit) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao sincronizar perfil com o Firestore: ${e.message}")
        }
    }

    private fun clearCachedProfile(uid: String) {
        val prefs = cachePrefs() ?: return
        prefs.edit().apply {
            remove(cacheKey(uid, FIELD_NICKNAME))
            remove(cacheKey(uid, FIELD_FULL_NAME))
            remove(cacheKey(uid, FIELD_BIRTH_DATE))
            remove(cacheKey(uid, FIELD_PHOTO_BASE64))
        }.apply()
    }

    private suspend fun awaitProfileUpdate(user: FirebaseUser, displayName: String) {
        suspendCancellableCoroutine<Unit> { cont ->
            val request = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(request).addOnCompleteListener {
                cont.resume(Unit)
            }
        }
    }
}
