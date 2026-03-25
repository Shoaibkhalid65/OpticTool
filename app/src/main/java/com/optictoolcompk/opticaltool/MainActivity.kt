package com.optictoolcompk.opticaltool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.optictoolcompk.opticaltool.data.auth.AuthState
import com.optictoolcompk.opticaltool.navigation.AppNavGraph
import com.optictoolcompk.opticaltool.ui.auth.viewmodel.AuthViewModel
import com.optictoolcompk.opticaltool.ui.theme.OpticalToolTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.Auth

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.authState.value is AuthState.Loading
        }
        setContent {
            val authState = authViewModel.authState
            OpticalToolTheme {
                AppNavGraph()
            }
        }
    }
}

