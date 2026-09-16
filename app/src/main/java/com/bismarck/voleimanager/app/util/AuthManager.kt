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
import kotlinx.coroutines.withContext
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
    val photoUrl: String?,
    /** `true` para contas Google (verificadas pelo próprio provedor) ou e-mail/senha já
     *  confirmado pelo link enviado por [AuthManager.signUp]/[AuthManager.resendVerificationEmail].
     *  Gateia a assinatura Premium (ver premium-purchase-gating). */
    val emailVerified: Boolean,
    /** `true` só para contas com login por e-mail/senha (permite alterar e-mail/senha pelo app);
     *  contas exclusivamente Google gerenciam e-mail/senha do lado do Google, então essas opções
     *  ficam ocultas na UI para elas. */
    val hasPasswordProvider: Boolean
)

private const val USERS_COLLECTION = "users"
private const val FIELD_NICKNAME = "nickname"
private const val FIELD_FULL_NAME = "fullName"
private const val FIELD_BIRTH_DATE = "birthDate"
private const val FIELD_PHOTO_BASE64 = "photoBase64"

private const val RATE_LIMIT_PREFS_NAME = "auth_rate_limit"
private const val KEY_SIGNUP_ATTEMPT_COUNT = "signup_attempt_count"
private const val KEY_SIGNUP_LAST_ATTEMPT_AT = "signup_last_attempt_at"
private const val KEY_SIGNUP_WINDOW_START_AT = "signup_window_start_at"
private const val SIGNUP_RATE_LIMIT_WINDOW_MS = 15L * 60L * 1000L
private const val SIGNUP_RATE_LIMIT_FREE_ATTEMPTS = 3
private const val SIGNUP_RATE_LIMIT_BASE_COOLDOWN_MS = 30L * 1000L

/**
 * Fachada única sobre o Firebase Authentication (e-mail/senha e login com Google — ver
 * [signInWithGoogleIdToken] e [com.bismarck.voleimanager.app.util.GoogleSignInHelper]) e sobre o
 * documento de perfil complementar guardado no Firestore (`users/{uid}`: apelido público, nome
 * completo, data de nascimento e foto reduzida). Segue o mesmo cuidado do [TelemetryManager]: se o
 * app não tiver um `google-services.json` válido, todas as chamadas abaixo falham de forma segura
 * (nunca derruba o app) e [currentUser] simplesmente nunca emite um usuário logado.
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

    private fun authOrNull(): FirebaseAuth? {
        // Ver [isRunningInUnitTest]: em Robolectric, nunca tenta abrir um listener real do
        // Firebase Auth (evita travar Dispatchers.IO esperando rede que não existe na JVM de
        // teste). `currentUser` simplesmente nunca emite um usuário logado nesse caso, igual ao
        // comportamento já existente quando o Firebase não está configurado.
        if (isRunningInUnitTest) return null
        return try {
            Firebase.auth
        } catch (e: Exception) {
            Log.d(TAG, "Firebase Auth indisponível: ${e.message}")
            null
        }
    }

    private fun firestoreOrNull(): FirebaseFirestore? {
        if (isRunningInUnitTest) return null
        return try {
            Firebase.firestore
        } catch (e: Exception) {
            Log.d(TAG, "Firestore indisponível: ${e.message}")
            null
        }
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

    /** Incrementado por [refreshCurrentUser] para forçar [currentUser] a reemitir depois de um
     *  [FirebaseUser.reload] — necessário porque a confirmação de e-mail acontece fora do app (o
     *  usuário clica num link recebido por e-mail), então o [FirebaseAuth.AuthStateListener]
     *  sozinho nunca dispara de novo sem essa releitura explícita. */
    private val _reloadTrigger = MutableStateFlow(0)

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
            combine(profileDocFlow(user.uid), localOverrides, _reloadTrigger) { remote, overrides, _ ->
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

    /** Recarrega o [FirebaseUser] logado (via [FirebaseUser.reload]) para refletir mudanças feitas
     *  fora do app — hoje, apenas a confirmação de e-mail por link. Chamado quando o app volta ao
     *  primeiro plano; não falha nada se estiver deslogado/offline, apenas não atualiza. */
    suspend fun refreshCurrentUser() {
        val user = authOrNull()?.currentUser ?: return
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                user.reload().addOnCompleteListener {
                    if (cont.isActive) cont.resume(Unit)
                }
            }
            _reloadTrigger.value += 1
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao recarregar usuário: ${e.message}")
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
        photoUrl = user.photoUrl?.toString(),
        emailVerified = user.isEmailVerified,
        hasPasswordProvider = user.providerData.any {
            it.providerId == com.google.firebase.auth.EmailAuthProvider.PROVIDER_ID
        }
    )

    /** Proteção simples e local contra scripts de criação em massa de contas: não substitui uma
     *  defesa de verdade (App Check/quotas no backend cabem em [purchase-validation-function] e
     *  Cloud Functions), mas já cria fricção crescente contra automações ingênuas batendo direto
     *  no app. Dentro da mesma janela de [SIGNUP_RATE_LIMIT_WINDOW_MS], as primeiras
     *  [SIGNUP_RATE_LIMIT_FREE_ATTEMPTS] tentativas passam livres; a partir daí, o tempo mínimo de
     *  espera até a próxima tentativa dobra a cada nova tentativa (backoff exponencial). Retorna
     *  uma mensagem amigável se a tentativa atual precisar esperar, ou `null` (e já registra a
     *  tentativa) se puder prosseguir. */
    private fun checkAndRecordSignupAttempt(): String? {
        val prefs = appContext?.getSharedPreferences(RATE_LIMIT_PREFS_NAME, Context.MODE_PRIVATE) ?: return null
        val now = System.currentTimeMillis()
        var windowStart = prefs.getLong(KEY_SIGNUP_WINDOW_START_AT, 0L)
        var count = prefs.getInt(KEY_SIGNUP_ATTEMPT_COUNT, 0)
        if (now - windowStart > SIGNUP_RATE_LIMIT_WINDOW_MS) {
            windowStart = now
            count = 0
        }
        if (count >= SIGNUP_RATE_LIMIT_FREE_ATTEMPTS) {
            val cooldownMs = SIGNUP_RATE_LIMIT_BASE_COOLDOWN_MS shl
                (count - SIGNUP_RATE_LIMIT_FREE_ATTEMPTS).coerceAtMost(6)
            val remaining = cooldownMs - (now - prefs.getLong(KEY_SIGNUP_LAST_ATTEMPT_AT, 0L))
            if (remaining > 0) {
                val minutes = (remaining / 60_000L) + 1
                return "Muitas tentativas de cadastro em pouco tempo. Tente novamente em cerca de $minutes minuto(s)."
            }
        }
        prefs.edit()
            .putLong(KEY_SIGNUP_WINDOW_START_AT, windowStart)
            .putInt(KEY_SIGNUP_ATTEMPT_COUNT, count + 1)
            .putLong(KEY_SIGNUP_LAST_ATTEMPT_AT, now)
            .apply()
        return null
    }

    /** Dispara o e-mail de verificação sem bloquear o restante do cadastro em caso de falha (rede
     *  instável, quota do Firebase etc.) — o usuário pode reenviar depois via
     *  [resendVerificationEmail]. */
    private fun sendVerificationEmailSafely(user: FirebaseUser) {
        try {
            user.sendEmailVerification().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d(TAG, "Falha ao enviar e-mail de verificação: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao disparar e-mail de verificação: ${e.message}")
        }
    }

    /** Reenvia o e-mail de confirmação da conta logada. Retorna uma mensagem amigável de erro (ou
     *  de aviso, se o e-mail já estiver confirmado), ou `null` em caso de sucesso. */
    suspend fun resendVerificationEmail(): String? {
        val user = authOrNull()?.currentUser ?: return "Você precisa estar logado para reenviar a confirmação."
        if (user.isEmailVerified) return "Seu e-mail já está confirmado."
        return try {
            suspendCancellableCoroutine<Result<Unit>> { cont ->
                user.sendEmailVerification().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(Result.failure(task.exception ?: Exception("Falha ao reenviar e-mail")))
                    }
                }
            }.getOrThrow()
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível reenviar o e-mail de confirmação."
        }
    }

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
        if (!isValidEmail(email)) return "Informe um e-mail válido."
        if (!isValidPassword(password)) {
            return "A senha deve ter de $MIN_PASSWORD_LENGTH a $MAX_PASSWORD_LENGTH caracteres, com ao menos " +
                "uma letra maiúscula, uma minúscula, um número e um caractere especial."
        }
        if (fullName.length > MAX_FULL_NAME_LENGTH) return "Nome completo muito longo."
        checkAndRecordSignupAttempt()?.let { return it }
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
                sendVerificationEmailSafely(user)
            }
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível criar a conta."
        }
    }

    /** Autentica com e-mail/senha. Retorna uma mensagem de erro amigável em caso de falha, ou
     *  `null` em caso de sucesso. */
    suspend fun signIn(email: String, password: String): String? {
        if (email.length > MAX_EMAIL_LENGTH || password.length > MAX_PASSWORD_LENGTH) {
            return "Entrada inválida."
        }
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

    /** Autentica (ou cria a conta, se for o primeiro acesso) usando um ID token do Google obtido
     *  por [com.bismarck.voleimanager.app.util.GoogleSignInHelper]. No primeiro login, usa nome e
     *  e-mail da própria conta Google como valores iniciais do perfil complementar (apelido =
     *  primeiro nome, nome completo = nome exibido pelo Google) — o usuário pode alterá-los depois
     *  em "Editar perfil". A data de nascimento não vem do Google, então fica em branco até o
     *  usuário preenchê-la manualmente (necessária futuramente para checar elegibilidade de compra
     *  premium). Logins seguintes não sobrescrevem um perfil já existente. Retorna uma mensagem de
     *  erro amigável em caso de falha, ou `null` em caso de sucesso. */
    suspend fun signInWithGoogleIdToken(idToken: String): String? {
        val auth = authOrNull() ?: return "Serviço de conta indisponível no momento."
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = suspendCancellableCoroutine<Result<FirebaseUser?>> { cont ->
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(task.result?.user))
                        } else {
                            cont.resume(Result.failure(task.exception ?: Exception("Falha ao entrar com o Google")))
                        }
                    }
            }
            result.getOrThrow()?.let { user -> initializeProfileIfFirstLogin(user) }
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível entrar com o Google."
        }
    }

    /** Preenche o perfil complementar (`users/{uid}`) com dados da conta Google só quando ele
     *  ainda não existir — evita sobrescrever um apelido/nome/data de nascimento que o usuário já
     *  tenha editado manualmente em um login anterior (com e-mail/senha ou Google). Também baixa e
     *  comprime a foto de perfil do Google (ver [downloadAndCompressAvatarFromUrl]), guardando
     *  apenas a miniatura reduzida — nunca a URL/imagem original pesada. A data de nascimento não
     *  é solicitada aqui (exigiria o escopo adicional da People API, fora do fluxo simples do
     *  Credential Manager), então fica em branco até o usuário preenchê-la manualmente em "Editar
     *  perfil". */
    private suspend fun initializeProfileIfFirstLogin(user: FirebaseUser) {
        val firestore = firestoreOrNull() ?: return
        val alreadyHasProfile = try {
            suspendCancellableCoroutine<Boolean> { cont ->
                firestore.collection(USERS_COLLECTION).document(user.uid).get()
                    .addOnCompleteListener { task ->
                        cont.resume(task.isSuccessful && (task.result?.exists() == true))
                    }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao checar perfil existente: ${e.message}")
            false
        }
        if (alreadyHasProfile) return
        val displayName = user.displayName.orEmpty()
        val firstName = displayName.trim().substringBefore(" ").ifBlank { displayName }
        saveProfileDoc(uid = user.uid, fullName = displayName, nickname = firstName, birthDate = "")
        user.photoUrl?.toString()?.let { photoUrl ->
            val base64 = withContext(kotlinx.coroutines.Dispatchers.IO) {
                downloadAndCompressAvatarFromUrl(photoUrl)
            }
            if (base64 != null) updateProfilePhoto(base64)
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

    /** Reautentica o usuário logado com a senha atual — exigido pelo Firebase antes de operações
     *  sensíveis (trocar e-mail/senha, apagar conta) se o login não for recente. Retorna o
     *  [FirebaseUser] em caso de sucesso, ou uma mensagem de erro amigável. */
    private suspend fun reauthenticateWithPassword(user: FirebaseUser, currentPassword: String): String? {
        val email = user.email ?: return "Esta conta não usa e-mail/senha."
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
        return try {
            suspendCancellableCoroutine<Result<Unit>> { cont ->
                user.reauthenticate(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(Result.failure(task.exception ?: Exception("Falha ao confirmar a senha atual")))
                    }
                }
            }.getOrThrow()
            null
        } catch (e: Exception) {
            "Senha atual incorreta."
        }
    }

    /** Inicia a troca do e-mail da conta: confirma [currentPassword], então envia um link de
     *  confirmação para [newEmail] (via [FirebaseUser.verifyBeforeUpdateEmail]) — o e-mail da
     *  conta só muda de fato depois que o usuário clicar no link recebido na caixa de entrada
     *  nova, então nenhuma escrita local/Firestore é feita aqui (o snapshot de
     *  [FirebaseAuth.AuthStateListener] refletirá o novo e-mail automaticamente quando o usuário
     *  voltar a abrir o app após confirmar). Retorna uma mensagem de erro amigável, ou `null` em
     *  caso de sucesso (a UI deve avisar o usuário para checar a caixa de entrada do novo e-mail). */
    suspend fun changeEmail(currentPassword: String, newEmail: String): String? {
        if (!isValidEmail(newEmail)) return "Informe um e-mail válido."
        val user = authOrNull()?.currentUser ?: return "Você precisa estar logado."
        reauthenticateWithPassword(user, currentPassword)?.let { return it }
        return try {
            suspendCancellableCoroutine<Result<Unit>> { cont ->
                user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(Result.failure(task.exception ?: Exception("Falha ao iniciar a troca de e-mail")))
                    }
                }
            }.getOrThrow()
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível iniciar a troca de e-mail."
        }
    }

    /** Altera a senha da conta logada, exigindo a senha atual por segurança. Retorna uma mensagem
     *  de erro amigável em caso de falha, ou `null` em caso de sucesso. */
    suspend fun changePassword(currentPassword: String, newPassword: String): String? {
        if (!isValidPassword(newPassword)) {
            return "A nova senha deve ter $MIN_PASSWORD_LENGTH-$MAX_PASSWORD_LENGTH caracteres, com maiúscula, " +
                "minúscula, número e caractere especial."
        }
        val user = authOrNull()?.currentUser ?: return "Você precisa estar logado."
        reauthenticateWithPassword(user, currentPassword)?.let { return it }
        return try {
            suspendCancellableCoroutine<Result<Unit>> { cont ->
                user.updatePassword(newPassword).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(Result.failure(task.exception ?: Exception("Falha ao alterar a senha")))
                    }
                }
            }.getOrThrow()
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível alterar a senha."
        }
    }

    /** Dispara o e-mail de "esqueci minha senha" do Firebase (link para redefinir a senha),
     *  usado a partir da tela de login sem precisar estar logado. Retorna uma mensagem amigável
     *  de erro, ou `null` em caso de sucesso. */
    suspend fun sendPasswordResetEmail(email: String): String? {
        if (!isValidEmail(email)) return "Informe um e-mail válido."
        val auth = authOrNull() ?: return "Serviço de conta indisponível no momento."
        return try {
            suspendCancellableCoroutine<Result<Unit>> { cont ->
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(Result.failure(task.exception ?: Exception("Falha ao enviar e-mail de redefinição")))
                    }
                }
            }.getOrThrow()
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível enviar o e-mail de redefinição de senha."
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
