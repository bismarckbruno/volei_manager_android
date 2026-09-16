package com.bismarck.voleimanager.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.bismarck.voleimanager.app.data.VoleiRepository
import com.bismarck.voleimanager.app.data.model.BalancingMode
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_COMPLETE
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Testes da matriz de permissões/regras de negócio da sincronização premium
 * (`role-permission-matrix`, `testing-migrations`): limite de grupos simultâneos por pacote,
 * cooldown de 15 dias para trocar o grupo premium, e a simulação local (debug) de entrar num
 * grupo remoto via código de convite (usada como fallback enquanto não existe uma assinatura
 * real — ver [VoleiViewModel.joinGroupWithCode]).
 */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CloudSyncPermissionsTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setGroupCloudSynced_withoutPremiumAccess_doesNothing() = runBlocking {
        val env = createEnv()
        env.vm.setGroupCloudSynced(DEFAULT_GROUP_NAME, true)
        awaitUntil { env.repo.getGroupConfig(DEFAULT_GROUP_NAME) != null }

        val config = env.repo.getGroupConfig(DEFAULT_GROUP_NAME)
        assertFalse(config?.isCloudSynced ?: false)
    }

    @Test
    fun setGroupCloudSynced_singleTier_allowsOnlyOneSyncedGroup() = runBlocking {
        val env = createEnv()
        env.vm.setDebugPremiumPlanTier(CloudPlanTier.SINGLE)
        env.vm.setDebugPremiumOverride(true)

        env.repo.saveGroupConfig(GroupConfig(groupName = "Segundo Grupo", onboardingStep = ONBOARDING_STEP_COMPLETE))

        env.vm.setGroupCloudSynced(DEFAULT_GROUP_NAME, true)
        awaitUntil { env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.isCloudSynced == true }
        assertTrue(env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.isCloudSynced ?: false)

        // Já tem 1 grupo sincronizado (limite do plano SINGLE) — o segundo deve ser recusado.
        env.vm.setGroupCloudSynced("Segundo Grupo", true)
        awaitUntil { env.vm.uiMessage.value != null }
        assertFalse(env.repo.getGroupConfig("Segundo Grupo")?.isCloudSynced ?: false)
        assertNotNull(env.vm.uiMessage.value)
    }

    @Test
    fun setGroupCloudSynced_multiTier_allowsUpToFiveSyncedGroups() = runBlocking {
        val env = createEnv()
        env.vm.setDebugPremiumPlanTier(CloudPlanTier.MULTI)
        env.vm.setDebugPremiumOverride(true)

        val groupNames = listOf(DEFAULT_GROUP_NAME, "G2", "G3", "G4", "G5")
        groupNames.drop(1).forEach { name ->
            env.repo.saveGroupConfig(GroupConfig(groupName = name, onboardingStep = ONBOARDING_STEP_COMPLETE))
        }
        groupNames.forEach { name ->
            env.vm.setGroupCloudSynced(name, true)
            awaitUntil { env.repo.getGroupConfig(name)?.isCloudSynced == true }
        }
        groupNames.forEach { name ->
            assertTrue("$name should be synced", env.repo.getGroupConfig(name)?.isCloudSynced ?: false)
        }

        // Um sexto grupo excede o limite do pacote MULTI (5).
        env.repo.saveGroupConfig(GroupConfig(groupName = "G6", onboardingStep = ONBOARDING_STEP_COMPLETE))
        env.vm.setGroupCloudSynced("G6", true)
        awaitUntil { env.vm.uiMessage.value != null }
        assertFalse(env.repo.getGroupConfig("G6")?.isCloudSynced ?: false)
    }

    @Test
    fun setGroupCloudSynced_respectsFifteenDayCooldownBetweenSwitches() = runBlocking {
        val env = createEnv()
        env.vm.setDebugPremiumPlanTier(CloudPlanTier.SINGLE)
        env.vm.setDebugPremiumOverride(true)

        env.repo.saveGroupConfig(GroupConfig(groupName = "Segundo Grupo", onboardingStep = ONBOARDING_STEP_COMPLETE))

        env.vm.setGroupCloudSynced(DEFAULT_GROUP_NAME, true)
        awaitUntil { env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.isCloudSynced == true }
        assertTrue(env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.isCloudSynced ?: false)

        // Libera a vaga desativando o primeiro grupo — desativar nunca é bloqueado.
        env.vm.setGroupCloudSynced(DEFAULT_GROUP_NAME, false)
        awaitUntil { env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.isCloudSynced == false }
        assertFalse(env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.isCloudSynced ?: true)

        // Tentar sincronizar outro grupo antes de 15 dias desde a última troca deve ser recusado.
        env.vm.setGroupCloudSynced("Segundo Grupo", true)
        awaitUntil { env.vm.uiMessage.value != null }
        assertFalse(env.repo.getGroupConfig("Segundo Grupo")?.isCloudSynced ?: false)
        assertNotNull(env.vm.uiMessage.value)
    }

    @Test
    fun joinGroupWithCode_debugFallback_auxiliarPrefix_isTemporarilyUnavailable() = runBlocking {
        // Papel Auxiliar temporariamente oculto/desativado para o lançamento (ver
        // `hide-auxiliar-role-temporarily`) — mesmo o fallback local de debug deve recusar.
        val env = createEnv()
        val error = env.vm.joinGroupWithCodeAwait("AUX-ABC123")

        assertNotNull(error)
        val remoteGroup = env.repo.getAllGroupConfigs().firstOrNull { it.remoteRole == UserProfileType.AUXILIAR.name }
        assertNull(remoteGroup)
    }

    @Test
    fun joinGroupWithCode_debugFallback_espectadorPrefix_createsRemoteEspectadorGroup() = runBlocking {
        val env = createEnv()
        val error = env.vm.joinGroupWithCodeAwait("ESP-XYZ789")

        assertNull(error)
        val remoteGroup = env.repo.getAllGroupConfigs().firstOrNull { it.remoteRole == UserProfileType.ESPECTADOR.name }
        assertNotNull(remoteGroup)
    }

    @Test
    fun joinGroupWithCode_invalidCode_returnsError() = runBlocking {
        val env = createEnv()
        val error = env.vm.joinGroupWithCodeAwait("not-a-real-code")

        assertNotNull(error)
    }

    @Test
    fun joinGroupWithCode_sameCodeTwice_secondAttemptReturnsAlreadyJoinedError() = runBlocking {
        val env = createEnv()
        val firstError = env.vm.joinGroupWithCodeAwait("ESP-SAME1")
        assertNull(firstError)

        val secondError = env.vm.joinGroupWithCodeAwait("ESP-SAME1")
        assertNotNull(secondError)
    }

    @Test
    fun requestGroupOwnershipTransfer_onRemoteGroup_isRejected() = runBlocking {
        val env = createEnv()
        env.repo.saveGroupConfig(
            GroupConfig(
                groupName = "Remoto",
                onboardingStep = ONBOARDING_STEP_COMPLETE,
                isCloudSynced = true,
                cloudGroupId = "ABC123",
                remoteRole = UserProfileType.AUXILIAR.name
            )
        )
        env.vm.requestGroupOwnershipTransfer("Remoto", "alguem@example.com")
        awaitUntil { env.vm.uiMessage.value != null }

        val config = env.repo.getGroupConfig("Remoto")
        assertNull(config?.pendingOwnershipTransferTo)
    }

    @Test
    fun requestGroupOwnershipTransfer_onOwnSyncedGroup_registersPendingTransfer() = runBlocking {
        val env = createEnv()
        env.vm.setDebugPremiumPlanTier(CloudPlanTier.SINGLE)
        env.vm.setDebugPremiumOverride(true)
        env.vm.setGroupCloudSynced(DEFAULT_GROUP_NAME, true)
        awaitUntil { env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.isCloudSynced == true }

        env.vm.requestGroupOwnershipTransfer(DEFAULT_GROUP_NAME, "auxiliar@example.com")
        awaitUntil { env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.pendingOwnershipTransferTo != null }

        val config = env.repo.getGroupConfig(DEFAULT_GROUP_NAME)
        assertEquals("auxiliar@example.com", config?.pendingOwnershipTransferTo)

        env.vm.cancelGroupOwnershipTransfer(DEFAULT_GROUP_NAME)
        awaitUntil { env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.pendingOwnershipTransferTo == null }
        assertNull(env.repo.getGroupConfig(DEFAULT_GROUP_NAME)?.pendingOwnershipTransferTo)
    }

    /** Aguarda [check] ficar verdadeiro, dando tempo para coroutines lançadas em
     *  `Dispatchers.IO` (dispatcher real, não afetado pelo [UnconfinedTestDispatcher] usado
     *  como Main) terminarem — mesmo padrão de `awaitFinishGamePersistence` em
     *  [VoleiViewModelRestingIntegrationTest]. */
    private suspend fun awaitUntil(timeoutMs: Long = 5_000, check: suspend () -> Boolean) {
        withTimeout(timeoutMs) {
            while (!check()) {
                delay(10)
            }
        }
    }

    /** Chama [VoleiViewModel.joinGroupWithCode] e suspende até o callback ser invocado,
     *  independente de em qual dispatcher a coroutine interna roda. */
    private suspend fun VoleiViewModel.joinGroupWithCodeAwait(code: String): String? {
        val result = CompletableDeferred<String?>()
        joinGroupWithCode(code) { result.complete(it) }
        return withTimeout(5_000) { result.await() }
    }

    private fun createEnv(): TestEnv {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("volei", Context.MODE_PRIVATE).edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)
        val vm = VoleiViewModel(app, repo)

        runBlocking {
            repo.saveGroupConfig(
                GroupConfig(
                    groupName = DEFAULT_GROUP_NAME,
                    teamSize = 2,
                    victoryLimit = 2,
                    priorityEnabled = false,
                    scoreEnabled = true,
                    balancingMode = BalancingMode.REST.name,
                    groupType = GroupType.RECREATIONAL.name,
                    onboardingStep = ONBOARDING_STEP_COMPLETE
                )
            )
        }
        vm.loadGroupConfig(DEFAULT_GROUP_NAME)
        return TestEnv(vm, repo)
    }
}
