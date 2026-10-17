package com.bismarck.voleimanager.app.util

// Tipos de dados da nuvem/conta/assinatura, sem depend?ncia de Firebase/Billing (usados pelo ViewModel).

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
    val hasPasswordProvider: Boolean,
    /** `true` para a sessão anônima criada por [AuthManager.signInAnonymously] (Espectador que só
     *  quer acompanhar um grupo por código, sem cadastro). Não conta como "logado de verdade" para
     *  fins de menu/UI — ver uso em `VoleiManagerApp.AccountMenu` — porque não tem e-mail/perfil
     *  editável e pode ser substituída a qualquer momento por um login/cadastro real. */
    val isAnonymous: Boolean
)

/**
 * IDs de produto/plano base das assinaturas premium — precisam bater exatamente com o que for
 * cadastrado no Play Console (Monetizar > Produtos > Assinaturas). Os preços em si (incluindo o
 * ajuste de preço por país, ex.: R$ 9,99/R$ 19,99 por mês no Brasil x ~US$1,89/US$3,89 fora dele)
 * ficam inteiramente configurados lá — o app nunca hardcoda valores monetários usados de verdade, só um
 * texto de fallback (ver `cloud_sync_plan_*_price` em strings.xml) para quando a Play Store ainda
 * não respondeu com o preço real (offline, sem os produtos cadastrados, etc.).
 */
object BillingProductIds {
    const val SINGLE_GROUP = "premium_single_group"
    const val MULTI_GROUP = "premium_multi_group"
    const val BASE_PLAN_MONTHLY = "mensal"
    const val BASE_PLAN_ANNUAL = "anual"
}

/** Uma oferta de assinatura comprável: um par (produto, plano base) já com o preço formatado e
 *  localizado devolvido pela própria Play Store (respeita moeda/preço regional configurados no
 *  Play Console, sem o app precisar saber a conversão). */
data class SubscriptionOffer(
    val productId: String,
    val basePlanId: String,
    val offerToken: String,
    val formattedPrice: String,
    /** Período de faturamento ISO-8601 (ex.: "P1M" mensal, "P1Y" anual). */
    val billingPeriodIso: String,
    /** Preço bruto (micros da unidade monetária, ex.: R$ 1,00 = 1_000_000) e código ISO-4217 da
     *  moeda — usados só para calcular localmente o "equivalente por mês" do plano anual (preço
     *  anual / 12) e mostrar a economia real na moeda do próprio usuário, sem o app precisar saber
     *  taxas de câmbio. [formattedPrice] continua sendo o texto oficial mostrado como preço. */
    val priceAmountMicros: Long,
    val priceCurrencyCode: String
)

/** Compra vista pelo Play Billing neste aparelho (nova ou já conhecida de uma sessão anterior),
 *  emitida para o [VoleiViewModel][com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel]
 *  repassar ao backend (`linkPurchaseToken`) e obter um entitlement validado de verdade — ver
 *  `purchase-validation-function`. */
data class AcknowledgedPurchase(val purchaseToken: String, val productId: String)

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

/** Jogador "enxuto" sincronizado em `liveState` — usa [publicId] (estável entre dispositivos) em
 *  vez do id local autoGenerate do Room, que não tem significado fora do aparelho de origem.
 *  Inclui os campos exibidos nos cards da tela "Jogo" (fora tolerância/posição do dia, que não
 *  fazem sentido fora do aparelho que calcula presença localmente). */
data class RemotePlayerSnapshot(
    val publicId: String = "",
    val name: String = "",
    val elo: Double = 1200.0,
    val isPriority: Boolean = false,
    val matchesPlayed: Int = 0,
    val victories: Int = 0,
    val preferredPosition: String? = null,
    val secondaryPosition: String? = null,
    /** Tolerância de atraso (pedágio) e a data em que foi calculada, já resolvidas pelo
     *  organizador — só o valor final é replicado (o cálculo em si continua acontecendo apenas
     *  no aparelho que controla a presença localmente), para que Auxiliar/Espectador também
     *  vejam o mesmo badge de pedágio que o organizador. */
    val dailyToll: Int = 0,
    val tollDate: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "publicId" to publicId,
        "name" to name,
        "elo" to elo,
        "isPriority" to isPriority,
        "matchesPlayed" to matchesPlayed,
        "victories" to victories,
        "preferredPosition" to preferredPosition,
        "secondaryPosition" to secondaryPosition,
        "dailyToll" to dailyToll,
        "tollDate" to tollDate
    )

    companion object {
        fun fromMap(map: Map<*, *>): RemotePlayerSnapshot = RemotePlayerSnapshot(
            publicId = map["publicId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            elo = (map["elo"] as? Number)?.toDouble() ?: 1200.0,
            isPriority = map["isPriority"] as? Boolean ?: false,
            matchesPlayed = (map["matchesPlayed"] as? Number)?.toInt() ?: 0,
            victories = (map["victories"] as? Number)?.toInt() ?: 0,
            preferredPosition = map["preferredPosition"] as? String,
            secondaryPosition = map["secondaryPosition"] as? String,
            dailyToll = (map["dailyToll"] as? Number)?.toInt() ?: 0,
            tollDate = map["tollDate"] as? String ?: ""
        )
    }
}

/** Espelha o jogo em andamento de um grupo premium sincronizado: times em quadra, fila de espera
 *  e placar — é isto que um(a) Espectador(a) (ou Auxiliar) vê em tempo real na tela "Jogo (Ao
 *  vivo)". Também carrega o canal de comando usado por um(a) Auxiliar para pedir que o
 *  organizador finalize a partida em andamento (só o dispositivo organizador tem os registros
 *  locais de jogador no Room para calcular Elo/histórico de verdade, ver
 *  [pendingFinishWinner]/[pendingFinishRequestId] e `role-permission-matrix`). */
data class LiveGameState(
    val groupName: String = "",
    val teamA: List<RemotePlayerSnapshot> = emptyList(),
    val teamB: List<RemotePlayerSnapshot> = emptyList(),
    val waitingList: List<RemotePlayerSnapshot> = emptyList(),
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val currentStreak: Int = 0,
    val streakOwner: String? = null,
    val updatedAt: Long = 0L,
    /** Time ("A"/"B") que um Auxiliar pediu para vencer a partida em andamento; só o
     *  organizador processa este campo (chamando sua lógica real de fim de partida) e volta a
     *  publicá-lo como `null` assim que atender o pedido. */
    val pendingFinishWinner: String? = null,
    /** Identificador único do pedido acima, usado pelo organizador para não processar o mesmo
     *  pedido duas vezes caso receba a mesma atualização mais de uma vez. */
    val pendingFinishRequestId: String? = null,
    /** Jogadores marcados como presentes/selecionados no aparelho do organizador *antes* de uma
     *  partida começar (ainda sem times formados). Publicado só pelo organizador (único
     *  dispositivo com o roster real do grupo no Room); Auxiliar apenas ecoa o último valor
     *  recebido ao publicar suas próprias atualizações, para não apagar essa lista sem querer.
     *  Usado pelo Espectador para ver "quem está presente agora" antes do jogo começar. */
    val presentPlayers: List<RemotePlayerSnapshot> = emptyList(),
    /** Lista completa do elenco do grupo (sem filtro de presença), publicada só pelo organizador
     *  (único com o roster real no Room) e ecoada por um Auxiliar remoto — usada para o Auxiliar
     *  enxergar a lista de jogadores completa, sem as restrições de edição do Espectador. */
    val allPlayers: List<RemotePlayerSnapshot> = emptyList(),
    /** Instante (epoch millis) em que a partida em andamento começou, usado para calcular tempo de
     *  partida/tempo médio de duração em todos os papéis. */
    val matchStartTimestamp: Long? = null,
    /** Time ("A"/"B") que fez o ponto mais recente, para o indicador visual no placar. */
    val lastScoringTeam: String? = null,
    /** Time ("A"/"B") que deve fazer rodízio, para o indicador visual no placar. */
    val rotationRequiredForTeam: String? = null,
    /** publicId do jogador cuja presença um Auxiliar remoto pediu para alternar; só o organizador
     *  processa este campo (aplicando sua própria lógica local de presença/fila) e volta a
     *  publicá-lo como `null` assim que atender o pedido — mesmo padrão de
     *  [pendingFinishWinner]/[pendingFinishRequestId]. */
    val pendingPresenceTogglePublicId: String? = null,
    /** Identificador único do pedido acima, ver [pendingFinishRequestId]. */
    val pendingPresenceToggleRequestId: String? = null,
    /** Identificador único de sessão do dispositivo que publicou esta atualização (gerado uma vez
     *  por processo/instalação da ViewModel). Usado para detectar auto-eco de forma confiável —
     *  ao contrário de comparar [updatedAt] (relógio do aparelho, sujeito a variação entre
     *  dispositivos), cada dispositivo só ignora atualizações que ele mesmo publicou, nunca as de
     *  outro aparelho, mesmo que os relógios estejam dessincronizados (ver `fix-admin-aux-sync-races`). */
    val writerSessionId: String? = null,
    /** Quantidade de partidas já disputadas hoje por cada jogador (chave = publicId), calculada
     *  pelo organizador a partir dos registros locais de Elo/histórico — replicada para que
     *  Auxiliar/Espectador vejam o mesmo indicador "jogos hoje"/ordem de fila que o organizador. */
    val gamesPlayedToday: Map<String, Int> = emptyMap(),
    /** Posição atribuída a cada jogador na formação em quadra (chave = publicId, valor = nome do
     *  enum [com.bismarck.voleimanager.app.data.model.PlayerPosition]) — só relevante para grupos
     *  `FIXED_POSITIONS`. Calculada só pelo organizador (dono do algoritmo [util.PositionAssigner])
     *  e replicada para Auxiliar/Espectador exibirem os mesmos selos de posição. */
    val assignedPositions: Map<String, String> = emptyMap(),
    /** Índice do slot de composição atribuído a cada jogador (chave = publicId), ver [assignedPositions]. */
    val assignedSlotIndices: Map<String, Int> = emptyMap(),
    /** Se a composição do time está incompleta (algum slot preenchido abaixo do nível
     *  secundário) — replicado para o aviso "composição incompleta" também aparecer para
     *  Auxiliar/Espectador. */
    val compositionIncomplete: Boolean = false,
    /** Se a última partida deste grupo já terminou (times zerados aguardando a próxima rodada) —
     *  replicado para que o banner de "time vencedor" (`EmptyStateCard`) apareça também para
     *  Auxiliar/Espectador, que não têm essa informação calculada localmente. */
    val hasPreviousMatch: Boolean = false,
    /** Jogadores do time vencedor da última partida encerrada, ver [hasPreviousMatch]. */
    val lastWinners: List<RemotePlayerSnapshot> = emptyList()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "groupName" to groupName,
        "teamA" to teamA.map { it.toMap() },
        "teamB" to teamB.map { it.toMap() },
        "waitingList" to waitingList.map { it.toMap() },
        "scoreA" to scoreA,
        "scoreB" to scoreB,
        "currentStreak" to currentStreak,
        "streakOwner" to streakOwner,
        "updatedAt" to updatedAt,
        "pendingFinishWinner" to pendingFinishWinner,
        "pendingFinishRequestId" to pendingFinishRequestId,
        "presentPlayers" to presentPlayers.map { it.toMap() },
        "allPlayers" to allPlayers.map { it.toMap() },
        "matchStartTimestamp" to matchStartTimestamp,
        "lastScoringTeam" to lastScoringTeam,
        "rotationRequiredForTeam" to rotationRequiredForTeam,
        "pendingPresenceTogglePublicId" to pendingPresenceTogglePublicId,
        "pendingPresenceToggleRequestId" to pendingPresenceToggleRequestId,
        "writerSessionId" to writerSessionId,
        "gamesPlayedToday" to gamesPlayedToday,
        "assignedPositions" to assignedPositions,
        "assignedSlotIndices" to assignedSlotIndices,
        "compositionIncomplete" to compositionIncomplete,
        "hasPreviousMatch" to hasPreviousMatch,
        "lastWinners" to lastWinners.map { it.toMap() }
    )
}

/** Entrada de histórico "enxuta" (equivalente a um subconjunto de [com.bismarck.voleimanager.app.data.model.MatchHistory])
 *  publicada em `cloudGroups/{id}/history` — só visível a observadores quando
 *  `visibility.shareHistoryWithObservers` estiver ligado. */
data class RemoteHistoryEntry(
    val id: String = "",
    val date: String = "",
    val teamA: String = "",
    val teamB: String = "",
    val winner: String = "",
    val teamAScore: Int? = null,
    val teamBScore: Int? = null,
    val endTimestamp: Long? = null,
    val startTimestamp: Long? = null,
    val eloPoints: Double = 0.0,
    val teamAAverageElo: Double? = null,
    val teamBAverageElo: Double? = null
)

/** Entrada de Elo "enxuta" publicada em `cloudGroups/{id}/eloLogs` — só visível a observadores
 *  quando `visibility.shareHistoryWithObservers` E `visibility.showEloToObservers` estiverem
 *  ligados (ver firestore.rules). */
data class RemoteEloLogEntry(
    val playerNameSnapshot: String = "",
    val date: String = "",
    val elo: Double = 1200.0,
    val won: Boolean = false,
    /** Timestamp de fim da partida que gerou este registro — usado só para ordenar/descobrir o
     *  Elo "mais recente" de cada jogador ao reconstruir o histórico no aparelho remoto, já que a
     *  consulta do Firestore não garante ordem de inserção. */
    val endTimestamp: Long? = null
)

/** Toggles de visibilidade do grupo para observadores, espelhados de `cloudGroups/{id}.visibility`. */
data class GroupVisibility(
    val shareHistoryWithObservers: Boolean = false,
    val showEloToObservers: Boolean = false,
    /** Ver [com.bismarck.voleimanager.app.data.model.GroupConfig.shareOnlyTodayHistory]. */
    val shareOnlyTodayHistory: Boolean = false,
    /** Metadados do grupo espelhados no documento raiz (fora do mapa `visibility`) para que
     *  ícones de tipo/balanceamento/tamanho de time no cabeçalho fiquem sincronizados entre
     *  organizador, auxiliar e espectador — ver `GroupConfig.groupType`/`balancingMode`/`teamSize`. */
    val groupType: String? = null,
    val balancingMode: String? = null,
    val teamSize: Int? = null,
    /** Cores oficiais do Time A/Time B definidas pelo organizador ([TeamAccentColor.name]),
     *  espelhadas aqui para que Espectadores enxerguem a mesma escolha de cores do organizador
     *  (ver [com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel.setGroupTeamColors]).
     *  `null` enquanto o organizador nunca personalizou as cores (usa o padrão azul/amarelo). */
    val teamAColorName: String? = null,
    val teamBColorName: String? = null,
    /** Espelha se o organizador ainda mantém a sincronização em nuvem deste grupo ligada
     *  ([com.bismarck.voleimanager.app.data.model.GroupConfig.isCloudSynced]). Quando o
     *  organizador desliga a sincronização, o código de convite continua válido (o documento
     *  `cloudGroups/{id}` não é apagado), mas `isActive=false` faz Auxiliar/Espectador ocultarem
     *  os dados imediatamente até o grupo voltar a ser sincronizado. Padrão `true` para grupos
     *  antigos que nunca tinham esse campo (nunca foram desativados). */
    val isActive: Boolean = true,
    /** Código permanente de convite de Espectador (`spectatorCode`), sempre visível na tela
     *  Premium para o organizador/auxiliar compartilhar (texto simples/QR) — ver
     *  `spectator-code-client`. `null` até o backend gerá-lo na primeira ativação do grupo
     *  ([ensureSpectatorCode] em `volei_manager_backend`). */
    val spectatorCode: String? = null,
    /** `true` só quando o listener deste documento falhou com PERMISSION_DENIED — ou seja, este
     *  dispositivo (Espectador) teve seu acesso revogado (organizador tocou em "Atualizar
     *  código", ver `regenerateSpectatorCode`). Distinto de `isActive=false`: aqui o grupo pode
     *  continuar ativo, só este dispositivo específico que perdeu o acesso e precisa reentrar com
     *  o novo código. */
    val accessRevoked: Boolean = false,
    /** Ver [com.bismarck.voleimanager.app.data.model.GroupConfig.activeAdminDeviceId] —
     *  `null` = nenhum aparelho reivindicou a sessão ainda (nenhuma restrição). */
    val activeAdminDeviceId: String? = null,
    /** Ver [com.bismarck.voleimanager.app.data.model.GroupConfig.activeAdminSince]. */
    val activeAdminSince: Long? = null
)
