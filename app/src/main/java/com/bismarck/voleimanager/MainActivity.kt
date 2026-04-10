package com.bismarck.voleimanager

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
import com.bismarck.voleimanager.data.AppDatabase
import com.bismarck.voleimanager.data.VoleiRepository
import com.bismarck.voleimanager.ui.VoleiManagerApp
import com.bismarck.voleimanager.ui.theme.AppTheme
import com.bismarck.voleimanager.ui.viewmodel.ThemeMode
import com.bismarck.voleimanager.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.ui.viewmodel.VoleiViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val repository = VoleiRepository(database.voleiDao())
        val viewModelFactory = VoleiViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[VoleiViewModel::class.java]

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            AppTheme(
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

                VoleiManagerApp(
                    viewModel,
                    darkTheme
                )
            }
        }
    }
}
