package org.ckdk.toad_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ckdk.toad_app.ui.login.LoginScreen
import org.ckdk.toad_app.ui.login.LoginViewModel
import org.ckdk.toad_app.ui.main.MainScreen
import org.ckdk.toad_app.ui.main.OfflineReportsScreen
import org.ckdk.toad_app.ui.theme.Toad_AppTheme
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge()
        setContent {
            Toad_AppTheme {
                val loginViewModel: LoginViewModel = viewModel()
                val uiState by loginViewModel.uiState.collectAsState()

                if (uiState.isOfflineMode) {
                    OfflineReportsScreen(
                        onBackToLogin = loginViewModel::onLogout
                    )
                } else if (uiState.loggedInUser != null) {
                    MainScreen(
                        user = uiState.loggedInUser!!,
                        onLogout = loginViewModel::onLogout
                    )
                } else {
                    LoginScreen(viewModel = loginViewModel)
                }
            }
        }
    }
}