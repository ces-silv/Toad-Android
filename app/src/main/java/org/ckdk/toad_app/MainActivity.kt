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
import org.ckdk.toad_app.ui.theme.Toad_AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Toad_AppTheme {
                val loginViewModel: LoginViewModel = viewModel()
                val uiState by loginViewModel.uiState.collectAsState()

                if (uiState.loggedInUser != null) {
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