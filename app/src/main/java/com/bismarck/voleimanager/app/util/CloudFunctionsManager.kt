package com.bismarck.voleimanager.app.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.functions
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Papel concedido por um código de convite de grupo em nuvem — espelha `JoinRole` no backend
 *  (`volei_manager_backend/functions/src/groups/joinCodes.ts`). */
enum class JoinRole { AUXILIAR, ESPECTADOR }

/** Limite de caracteres para o campo de código digitado manualmente pelo usuário — os códigos
 *  gerados são bem mais curtos que isso; o limite existe só para barrar colagens absurdamente
 *  longas antes de enviar ao backend. */
const val MAX_JOIN_CODE_LENGTH = 20

/** Código de convite recém-gerado, pronto para compartilhar (expira em 30 minutos). */
data class GeneratedJoinCode(val code: String, val expiresAtMillis: Long)

/** Resultado de trocar o código permanente de Espectador de um grupo: o novo código e quantos
 *  espectadores tiveram o acesso revogado na troca (precisarão resgatar o novo código de novo). */
data class RegeneratedSpectatorCode(val code: String, val revokedCount: Int)

/** Resultado de resgatar um código de convite: grupo em nuvem + papel concedido + nome real do
 *  grupo (para não depender de um placeholder local com o código dentro). */
data class RedeemedJoinCode(val cloudGroupId: String, val role: JoinRole, val groupName: String?)

/**
 * Fachada sobre as Cloud Functions "callable" do backend (`volei_manager_backend`) responsáveis
 * pela sincronização em nuvem (`createJoinCode`, `redeemJoinCode`, `switchPremiumGroup`). Segue o
 * mesmo cuidado de [AuthManager]/[TelemetryManager]: se o Firebase não estiver configurado (sem
 * `google-services.json`) ou a chamada falhar, retorna uma mensagem de erro amigável em vez de
 * derrubar o app — quem decide se um erro é bloqueante (ex.: sem assinatura ativa ainda, já que o
 * pagamento chega em uma fase seguinte) é quem chama esta fachada.
 */
object CloudFunctionsManager {
    private const val TAG = "CloudFunctionsManager"

    /** Segundos de espera antes de desistir de uma chamada e devolver erro amigável, evitando que
     *  o app fique travado indefinidamente esperando um Cloud Function lento ou sem rede. */
    private const val CALL_TIMEOUT_SECONDS = 15L

    private fun functionsOrNull(): FirebaseFunctions? {
        if (isRunningInUnitTest) return null
        return try {
            Firebase.functions
        } catch (e: Exception) {
            Log.d(TAG, "Firebase Functions indisponível: ${e.message}")
            null
        }
    }

    /** Marca um grupo local como sincronizado em nuvem no backend (cria/atualiza o documento
     *  `cloudGroups/{cloudGroupId}`) — exige uma assinatura premium ativa validada no servidor
     *  (`activeEntitlement`), respeitando o limite de grupos do plano e o intervalo de 15 dias
     *  entre trocas. Antes de existir cobrança real, essa chamada tende a falhar com
     *  `failed-precondition` (sem entitlement) para todo mundo — é esperado, e a simulação de
     *  premium em debug continua liberando a UI localmente sem depender deste retorno. Retorna
     *  `null` em caso de sucesso, ou uma mensagem amigável em caso de falha. */
    suspend fun switchPremiumGroup(localGroupPublicId: String, groupName: String): String? {
        val functions = functionsOrNull() ?: return null
        return try {
            call(functions, "switchPremiumGroup", mapOf("localGroupPublicId" to localGroupPublicId, "groupName" to groupName))
            null
        } catch (e: Exception) {
            friendlyMessage(e)
        }
    }

    /** Gera um código de convite (PIN de 6 caracteres) TEMPORÁRIO para o papel Auxiliar de um
     *  grupo já sincronizado em nuvem (uso único, válido por 30 minutos; papel hoje oculto no
     *  app — ver `hide-auxiliar-role-temporarily`). O código de Espectador não usa mais esta
     *  função: é permanente, criado automaticamente pelo backend na primeira ativação do grupo e
     *  só muda via [regenerateSpectatorCode]. */
    suspend fun createJoinCode(cloudGroupId: String, role: JoinRole): Result<GeneratedJoinCode> {
        val functions = functionsOrNull()
            ?: return Result.failure(Exception("Serviço de nuvem indisponível no momento."))
        return try {
            val data = call(functions, "createJoinCode", mapOf("cloudGroupId" to cloudGroupId, "role" to role.name))
            val code = data["code"] as? String
            val expiresAt = (data["expiresAt"] as? Number)?.toLong()
            if (code == null || expiresAt == null) {
                Result.failure(Exception("Resposta inesperada do servidor."))
            } else {
                Result.success(GeneratedJoinCode(code, expiresAt))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyMessage(e)))
        }
    }

    /** "Atualizar código": troca o código permanente de Espectador de [cloudGroupId] por um novo
     *  e revoga de fato o acesso de quem já tinha entrado com o código anterior (o backend apaga
     *  os membros Espectador do grupo — ver `regenerateSpectatorCode` em
     *  `volei_manager_backend`) — use só quando o organizador quiser resetar os acessos por
     *  segurança, já que todo mundo precisará resgatar o novo código de novo. */
    suspend fun regenerateSpectatorCode(cloudGroupId: String): Result<RegeneratedSpectatorCode> {
        val functions = functionsOrNull()
            ?: return Result.failure(Exception("Serviço de nuvem indisponível no momento."))
        return try {
            val data = call(functions, "regenerateSpectatorCode", mapOf("cloudGroupId" to cloudGroupId))
            val code = data["code"] as? String
            val revokedCount = (data["revokedCount"] as? Number)?.toInt() ?: 0
            if (code == null) {
                Result.failure(Exception("Resposta inesperada do servidor."))
            } else {
                Result.success(RegeneratedSpectatorCode(code, revokedCount))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyMessage(e)))
        }
    }

    /** Resgata um código de convite recebido (digitado ou lido via QR Code), entrando como
     *  auxiliar ou espectador do grupo correspondente. */
    suspend fun redeemJoinCode(code: String): Result<RedeemedJoinCode> {
        val functions = functionsOrNull()
            ?: return Result.failure(Exception("Serviço de nuvem indisponível no momento."))
        return try {
            val data = call(functions, "redeemJoinCode", mapOf("code" to code))
            val cloudGroupId = data["cloudGroupId"] as? String
            val role = (data["role"] as? String)?.let { roleName ->
                try {
                    JoinRole.valueOf(roleName)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            if (cloudGroupId == null || role == null) {
                Result.failure(Exception("Resposta inesperada do servidor."))
            } else {
                Result.success(RedeemedJoinCode(cloudGroupId, role, data["groupName"] as? String))
            }
        } catch (e: Exception) {
            Result.failure(Exception(friendlyMessage(e)))
        }
    }

    /** Chama uma Cloud Function "callable" e devolve seu payload como mapa, seguindo o mesmo
     *  padrão `suspendCancellableCoroutine` + `addOnCompleteListener` usado em [AuthManager] (sem
     *  depender da lib `kotlinx-coroutines-play-services`, ausente deste projeto). */
    private suspend fun call(functions: FirebaseFunctions, name: String, data: Map<String, Any?>): Map<String, Any?> =
        suspendCancellableCoroutine { cont ->
            val callable = functions.getHttpsCallable(name).withTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            callable.call(data).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    @Suppress("UNCHECKED_CAST")
                    val result = task.result?.data as? Map<String, Any?> ?: emptyMap()
                    cont.resume(result)
                } else {
                    cont.resumeWith(Result.failure(task.exception ?: Exception("Falha desconhecida.")))
                }
            }
        }

    private fun friendlyMessage(e: Throwable): String {
        val functionsException = e as? FirebaseFunctionsException
        return when (functionsException?.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED -> "Você precisa estar logado para usar a nuvem."
            FirebaseFunctionsException.Code.NOT_FOUND -> "Código inválido ou grupo não encontrado."
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED -> "Tempo esgotado ou código expirado. Verifique sua conexão e tente novamente."
            FirebaseFunctionsException.Code.FAILED_PRECONDITION,
            FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED,
            FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                functionsException.message ?: "Não foi possível concluir a operação."
            else -> e.message ?: "Não foi possível concluir a operação."
        }
    }
}
