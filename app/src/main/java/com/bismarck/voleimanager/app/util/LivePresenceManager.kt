package com.bismarck.voleimanager.app.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val PRESENCE_ROOT = "presence"
private const val FIELD_AT = "at"

/**
 * Presença "ao vivo" por grupo em nuvem (`live-viewer-count`): usa o Firebase Realtime Database
 * (não o Firestore) porque só o RTDB tem `onDisconnect()`, que detecta de verdade quando o app
 * fecha ou a conexão cai e remove o nó sozinho no servidor — sem isso, teríamos que manter um
 * heartbeat manual com TTL para "limpar" presenças de quem já saiu. Ver `database.rules.json` em
 * `volei_manager_backend` para as regras (leitura liberada a qualquer usuário autenticado, já que
 * o dado exposto aqui é só uma contagem, sem conteúdo sensível do jogo).
 *
 * Estrutura: `/presence/{cloudGroupId}/{uid} = { at: <server timestamp> }` — usa o uid do Firebase
 * Auth como chave (não um id de dispositivo aleatório), então o mesmo usuário logado em dois
 * aparelhos conta como uma presença só, o que é aceitável para esta métrica aproximada.
 *
 * Segue o mesmo padrão defensivo do [CloudSyncManager]/[CloudFunctionsManager]: sem
 * `google-services.json` configurado (ou em testes de unidade), todas as chamadas abaixo falham
 * de forma silenciosa em vez de travar o app.
 */
object LivePresenceManager {
    private const val TAG = "LivePresenceManager"

    private fun databaseOrNull(): FirebaseDatabase? {
        if (isRunningInUnitTest) return null
        return try {
            Firebase.database
        } catch (e: Exception) {
            Log.d(TAG, "Realtime Database indisponível: ${e.message}")
            null
        }
    }

    /** Marca [uid] como presente em [cloudGroupId] (tela ao vivo aberta agora) e agenda a remoção
     *  automática via `onDisconnect()` para quando o app fechar ou a conexão cair sem passar por
     *  [clearPresence]. Chamar de novo com o mesmo par é idempotente (só atualiza o timestamp). */
    fun markPresent(cloudGroupId: String, uid: String) {
        val database = databaseOrNull() ?: return
        val ref = database.getReference(PRESENCE_ROOT).child(cloudGroupId).child(uid)
        ref.onDisconnect().removeValue()
        ref.setValue(mapOf(FIELD_AT to ServerValue.TIMESTAMP))
            .addOnFailureListener { e -> Log.d(TAG, "Falha ao marcar presença (best-effort): ${e.message}") }
    }

    /** Remove a presença de [uid] em [cloudGroupId] imediatamente — chamado ao sair de propósito
     *  da tela ao vivo, sem precisar esperar o `onDisconnect()` (ex.: o usuário troca de grupo mas
     *  mantém o app aberto). */
    fun clearPresence(cloudGroupId: String, uid: String) {
        val database = databaseOrNull() ?: return
        database.getReference(PRESENCE_ROOT).child(cloudGroupId).child(uid)
            .removeValue()
            .addOnFailureListener { e -> Log.d(TAG, "Falha ao limpar presença (best-effort): ${e.message}") }
    }

    /** Observa em tempo real quantas sessões estão presentes em [cloudGroupId] agora — usado para
     *  mostrar "N assistindo agora" na tela Premium do organizador. */
    fun observePresenceCount(cloudGroupId: String): Flow<Int> = callbackFlow {
        val database = databaseOrNull()
        if (database == null) {
            trySend(0)
            awaitClose { }
            return@callbackFlow
        }
        val ref = database.getReference(PRESENCE_ROOT).child(cloudGroupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.childrenCount.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Falha ao observar presença: ${error.message}")
                trySend(0)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
