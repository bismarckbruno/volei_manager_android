package com.bismarck.voleimanager.app.platform

import com.bismarck.voleimanager.app.util.AcknowledgedPurchase
import com.bismarck.voleimanager.app.util.AppAuthUser
import com.bismarck.voleimanager.app.util.AppDateTime
import com.bismarck.voleimanager.app.util.GeneratedJoinCode
import com.bismarck.voleimanager.app.util.GroupVisibility
import com.bismarck.voleimanager.app.util.JoinRole
import com.bismarck.voleimanager.app.util.LiveGameState
import com.bismarck.voleimanager.app.util.RedeemedJoinCode
import com.bismarck.voleimanager.app.util.RegeneratedSpectatorCode
import com.bismarck.voleimanager.app.util.RemoteEloLogEntry
import com.bismarck.voleimanager.app.util.RemoteHistoryEntry
import com.bismarck.voleimanager.app.util.SubscriptionOffer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

// Serviços de nuvem/conta/assinatura usados pelo ViewModel. No Android são implementados pelos
// objetos AuthManager/CloudSyncManager/... (Firebase/Play Billing); onde não há backend (ex.: iOS
// offline) usam-se as versões Unavailable* abaixo. Funções que retornam String? devolvem a
// mensagem de erro (null = sucesso).

interface AuthService {
    val currentUser: Flow<AppAuthUser?>
    suspend fun refreshCurrentUser()
    suspend fun resendVerificationEmail(): String?
    suspend fun signUp(email: String, password: String, fullName: String, nickname: String, birthDate: String): String?
    suspend fun signIn(email: String, password: String): String?
    suspend fun signInAnonymously(): String?
    suspend fun signInWithGoogleIdToken(idToken: String): String?
    fun signOut()
    suspend fun updateProfile(nickname: String, fullName: String, birthDate: String): String?
    suspend fun updateProfilePhoto(base64: String?): String?
    suspend fun changeEmail(currentPassword: String, newEmail: String): String?
    suspend fun changePassword(currentPassword: String, newPassword: String): String?
    suspend fun sendPasswordResetEmail(email: String): String?
    suspend fun deleteAccount(): String?
}

interface BillingService {
    val offers: StateFlow<List<SubscriptionOffer>>
    val activeProductIds: StateFlow<Set<String>>
    val purchaseEvents: SharedFlow<AcknowledgedPurchase>
    fun refreshPurchases()
}

interface CloudFunctionsService {
    suspend fun switchPremiumGroup(localGroupPublicId: String, groupName: String): String?
    suspend fun linkPurchaseToken(purchaseToken: String, productId: String): String?
    suspend fun createJoinCode(cloudGroupId: String, role: JoinRole): Result<GeneratedJoinCode>
    suspend fun regenerateSpectatorCode(cloudGroupId: String): Result<RegeneratedSpectatorCode>
    suspend fun redeemJoinCode(code: String): Result<RedeemedJoinCode>
    suspend fun deleteCloudGroup(cloudGroupId: String): String?
}

interface LivePresenceService {
    fun markPresent(cloudGroupId: String, uid: String)
    fun clearPresence(cloudGroupId: String, uid: String)
    fun observePresenceCount(cloudGroupId: String): Flow<Int>
}

interface CloudSyncService {
    fun pushLiveState(cloudGroupId: String, state: LiveGameState)
    fun observeLiveState(cloudGroupId: String): Flow<LiveGameState?>
    suspend fun pushHistoryEntry(cloudGroupId: String, entry: RemoteHistoryEntry): String?
    suspend fun deleteHistoryEntry(cloudGroupId: String, docId: String)
    suspend fun pushEloLogEntry(cloudGroupId: String, entry: RemoteEloLogEntry): String?
    suspend fun deleteEloLogEntries(cloudGroupId: String, docIds: List<String>)
    suspend fun pushHistoryEntriesBatched(cloudGroupId: String, entries: List<RemoteHistoryEntry>): Boolean
    suspend fun pushEloLogEntriesBatched(cloudGroupId: String, entries: List<RemoteEloLogEntry>): Boolean
    fun observeHistory(cloudGroupId: String): Flow<List<RemoteHistoryEntry>>
    fun observeEloLogs(cloudGroupId: String): Flow<List<RemoteEloLogEntry>>
    fun observeGroupVisibility(cloudGroupId: String): Flow<GroupVisibility?>
    suspend fun setGroupVisibility(
        cloudGroupId: String,
        shareHistory: Boolean,
        showElo: Boolean,
        shareOnlyToday: Boolean = false
    ): String?
    fun setGroupActiveState(cloudGroupId: String, isActive: Boolean)
    fun setActiveAdminDevice(cloudGroupId: String, deviceId: String, since: Long = AppDateTime.nowMillis())
    fun setGroupMeta(cloudGroupId: String, groupType: String, balancingMode: String, teamSize: Int)
    fun setGroupTeamColors(cloudGroupId: String, teamAColorName: String, teamBColorName: String)
}

private const val UNAVAILABLE = "Recurso indisponível nesta plataforma."

object UnavailableAuthService : AuthService {
    override val currentUser: Flow<AppAuthUser?> = flowOf(null)
    override suspend fun refreshCurrentUser() {}
    override suspend fun resendVerificationEmail(): String? = UNAVAILABLE
    override suspend fun signUp(email: String, password: String, fullName: String, nickname: String, birthDate: String): String? = UNAVAILABLE
    override suspend fun signIn(email: String, password: String): String? = UNAVAILABLE
    override suspend fun signInAnonymously(): String? = UNAVAILABLE
    override suspend fun signInWithGoogleIdToken(idToken: String): String? = UNAVAILABLE
    override fun signOut() {}
    override suspend fun updateProfile(nickname: String, fullName: String, birthDate: String): String? = UNAVAILABLE
    override suspend fun updateProfilePhoto(base64: String?): String? = UNAVAILABLE
    override suspend fun changeEmail(currentPassword: String, newEmail: String): String? = UNAVAILABLE
    override suspend fun changePassword(currentPassword: String, newPassword: String): String? = UNAVAILABLE
    override suspend fun sendPasswordResetEmail(email: String): String? = UNAVAILABLE
    override suspend fun deleteAccount(): String? = UNAVAILABLE
}

object UnavailableBillingService : BillingService {
    override val offers: StateFlow<List<SubscriptionOffer>> = MutableStateFlow(emptyList())
    override val activeProductIds: StateFlow<Set<String>> = MutableStateFlow(emptySet())
    override val purchaseEvents: SharedFlow<AcknowledgedPurchase> = MutableSharedFlow()
    override fun refreshPurchases() {}
}

object UnavailableCloudFunctionsService : CloudFunctionsService {
    override suspend fun switchPremiumGroup(localGroupPublicId: String, groupName: String): String? = UNAVAILABLE
    override suspend fun linkPurchaseToken(purchaseToken: String, productId: String): String? = UNAVAILABLE
    override suspend fun createJoinCode(cloudGroupId: String, role: JoinRole): Result<GeneratedJoinCode> =
        Result.failure(IllegalStateException(UNAVAILABLE))
    override suspend fun regenerateSpectatorCode(cloudGroupId: String): Result<RegeneratedSpectatorCode> =
        Result.failure(IllegalStateException(UNAVAILABLE))
    override suspend fun redeemJoinCode(code: String): Result<RedeemedJoinCode> =
        Result.failure(IllegalStateException(UNAVAILABLE))
    override suspend fun deleteCloudGroup(cloudGroupId: String): String? = UNAVAILABLE
}

object UnavailableLivePresenceService : LivePresenceService {
    override fun markPresent(cloudGroupId: String, uid: String) {}
    override fun clearPresence(cloudGroupId: String, uid: String) {}
    override fun observePresenceCount(cloudGroupId: String): Flow<Int> = flowOf(0)
}

object UnavailableCloudSyncService : CloudSyncService {
    override fun pushLiveState(cloudGroupId: String, state: LiveGameState) {}
    override fun observeLiveState(cloudGroupId: String): Flow<LiveGameState?> = flowOf(null)
    override suspend fun pushHistoryEntry(cloudGroupId: String, entry: RemoteHistoryEntry): String? = null
    override suspend fun deleteHistoryEntry(cloudGroupId: String, docId: String) {}
    override suspend fun pushEloLogEntry(cloudGroupId: String, entry: RemoteEloLogEntry): String? = null
    override suspend fun deleteEloLogEntries(cloudGroupId: String, docIds: List<String>) {}
    override suspend fun pushHistoryEntriesBatched(cloudGroupId: String, entries: List<RemoteHistoryEntry>): Boolean = false
    override suspend fun pushEloLogEntriesBatched(cloudGroupId: String, entries: List<RemoteEloLogEntry>): Boolean = false
    override fun observeHistory(cloudGroupId: String): Flow<List<RemoteHistoryEntry>> = emptyFlow()
    override fun observeEloLogs(cloudGroupId: String): Flow<List<RemoteEloLogEntry>> = emptyFlow()
    override fun observeGroupVisibility(cloudGroupId: String): Flow<GroupVisibility?> = flowOf(null)
    override suspend fun setGroupVisibility(cloudGroupId: String, shareHistory: Boolean, showElo: Boolean, shareOnlyToday: Boolean): String? = UNAVAILABLE
    override fun setGroupActiveState(cloudGroupId: String, isActive: Boolean) {}
    override fun setActiveAdminDevice(cloudGroupId: String, deviceId: String, since: Long) {}
    override fun setGroupMeta(cloudGroupId: String, groupType: String, balancingMode: String, teamSize: Int) {}
    override fun setGroupTeamColors(cloudGroupId: String, teamAColorName: String, teamBColorName: String) {}
}