package com.bismarck.voleimanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

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

                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    com.bismarck.voleimanager.app.ui.VoleiManagerApp(
                        viewModel,
                        darkTheme
                    )
                }
            }
        }
    }
}


