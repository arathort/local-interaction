package com.arathort.localinteraction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arathort.localinteraction.presentation.navigation.AppNavigation
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalInteractionTheme {
                AppNavigation()
            }
        }
    }
}
