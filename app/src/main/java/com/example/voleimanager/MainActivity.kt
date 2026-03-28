package com.example.voleimanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.voleimanager.data.AppDatabase
import com.example.voleimanager.data.VoleiRepository
import com.example.voleimanager.ui.VoleiManagerApp
import com.example.voleimanager.ui.theme.AppTheme
import com.example.voleimanager.ui.viewmodel.ThemeMode
import com.example.voleimanager.ui.viewmodel.VoleiViewModel
import com.example.voleimanager.ui.viewmodel.VoleiViewModelFactory

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
                 VoleiManagerApp(
                    viewModel,
                    darkTheme
                )
            }
        }
    }
}
