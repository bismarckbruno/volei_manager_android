package com.bismarck.voleimanager.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.ViewModelProvider
import com.bismarck.voleimanager.app.data.AppDatabase
import com.bismarck.voleimanager.app.data.VoleiRepository
import com.bismarck.voleimanager.app.ui.VoleiManagerApp
import com.bismarck.voleimanager.app.ui.theme.AppTheme
import com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModelFactory
import com.bismarck.voleimanager.app.util.InAppUpdateHelper

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: VoleiViewModel
    private lateinit var inAppUpdateHelper: InAppUpdateHelper

    // Estado hoisted (fora de qualquer @Composable) para sinalizar quando um update FLEXIBLE
    // termina de baixar e está pronto para ser instalado via snackbar "Reiniciar".
    private val flexibleUpdateReady = mutableStateOf(false)

    private val updateFlowLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Log.d("InAppUpdate", "Fluxo de atualização cancelado ou falhou: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        inAppUpdateHelper = InAppUpdateHelper(this)

        val database = AppDatabase.getDatabase(this)
        val repository =
            VoleiRepository(database.voleiDao())
        val viewModelFactory =
            VoleiViewModelFactory(
                application,
                repository
            )
        viewModel = ViewModelProvider(this, viewModelFactory)[VoleiViewModel::class.java]
        handleViewIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            AppTheme(
                darkTheme = darkTheme,
                dynamicColor = false
            ) {
                DisposableEffect(darkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        ) { darkTheme },
                        navigationBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        ) { darkTheme }
                    )
                    onDispose {}
                }

                val snackbarHostState = remember { SnackbarHostState() }
                val isFlexibleUpdateReady by flexibleUpdateReady
                LaunchedEffect(isFlexibleUpdateReady) {
                    if (!isFlexibleUpdateReady) return@LaunchedEffect
                    val result = snackbarHostState.showSnackbar(
                        message = getString(R.string.update_downloaded_message),
                        actionLabel = getString(R.string.update_downloaded_action),
                        duration = SnackbarDuration.Indefinite
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        inAppUpdateHelper.completeFlexibleUpdate()
                    }
                    flexibleUpdateReady.value = false
                }

                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(Modifier.fillMaxSize()) {
                        VoleiManagerApp(
                            viewModel,
                            darkTheme
                        )
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Chamado em todo ponto de entrada do app, como recomendado pela Play Core In-App
        // Update API: cobre tanto a checagem inicial de atualização quanto a retomada de um
        // update IMMEDIATE que tenha ficado parado (ex.: app fechado no meio do fluxo).
        inAppUpdateHelper.checkForUpdate(updateFlowLauncher) {
            flexibleUpdateReady.value = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateHelper.unregister()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleViewIntent(intent)
    }

    /**
     * Se o app foi aberto via ACTION_VIEW (ex.: toque em um arquivo .vlz) ou recebido via
     * ACTION_SEND (quando o gerenciador de arquivos não reconhece a extensão e cai no fluxo de
     * "Compartilhar" em vez de "Abrir com"), sinaliza ao ViewModel para exibir o diálogo de
     * confirmação de importação.
     */
    private fun handleViewIntent(intent: Intent?) {
        val uri = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> intent.getStreamExtraUri()
            else -> null
        }
        uri?.let { viewModel.onExternalFileOpened(it) }
    }

    @Suppress("DEPRECATION")
    private fun Intent.getStreamExtraUri(): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            getParcelableExtra(Intent.EXTRA_STREAM)
        }
}


