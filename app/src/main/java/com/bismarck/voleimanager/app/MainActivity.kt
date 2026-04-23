package com.bismarck.voleimanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.bismarck.voleimanager.app.data.AppDatabase
import com.bismarck.voleimanager.app.data.VoleiRepository
import com.bismarck.voleimanager.app.ui.VoleiManagerApp
import com.bismarck.voleimanager.app.ui.theme.AppTheme
import com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = com.bismarck.voleimanager.app.data.AppDatabase.getDatabase(this)
        val repository =
            com.bismarck.voleimanager.app.data.VoleiRepository(database.voleiDao())
        val viewModelFactory =
            com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModelFactory(
                application,
                repository
            )
        val viewModel = ViewModelProvider(this, viewModelFactory)[com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel::class.java]

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode.LIGHT -> false
                com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode.DARK -> true
            }
            com.bismarck.voleimanager.app.ui.theme.AppTheme(
                darkTheme = darkTheme
            ) {
                val view = LocalView.current
                val statusBarColor = MaterialTheme.colorScheme.surface.toArgb()
                val navBarColor = MaterialTheme.colorScheme.surfaceContainerLow.toArgb()

                SideEffect {
                    window.navigationBarColor = navBarColor
                    window.statusBarColor = statusBarColor

                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.isAppearanceLightNavigationBars = !darkTheme
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                }

                com.bismarck.voleimanager.app.ui.VoleiManagerApp(
                    viewModel,
                    darkTheme
                )
            }
        }
    }
}
