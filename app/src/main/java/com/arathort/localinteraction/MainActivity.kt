package com.arathort.localinteraction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.arathort.localinteraction.domain.usecase.RestoreSessionUseCase
import com.arathort.localinteraction.presentation.navigation.AppNavigation
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var restoreSessionUseCase: RestoreSessionUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            restoreSessionUseCase()
        }

        setContent {
            LocalInteractionTheme {
                AppNavigation()
            }
        }
    }
}
