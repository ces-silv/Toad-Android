package org.ckdk.toad_app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ckdk.toad_app.data.model.LoginResult
import org.ckdk.toad_app.data.model.User
import org.ckdk.toad_app.data.network.BackendOrchestrator

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val apiError: String? = null,
    val isLoading: Boolean = false,
    val loggedInUser: User? = null,
    val isOfflineMode: Boolean = false
)

class LoginViewModel(
    private val orchestrator: BackendOrchestrator = BackendOrchestrator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState()) // Private and Mutable Version
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow() // Public version for UI (Not Mutable)

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null, apiError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, apiError = null) }
    }

    fun onPasswordVisibilityToggled() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClicked() {
        val state = _uiState.value
        if (!validate(state)) return

        _uiState.update { it.copy(isLoading = true, apiError = null) }

        viewModelScope.launch {
            val result = orchestrator.login(state.username.trim(), state.password)
            _uiState.update { current ->
                when (result) {
                    is LoginResult.Success -> current.copy(
                        isLoading = false,
                        loggedInUser = result.user
                    )
                    is LoginResult.InvalidCredentials -> current.copy(
                        isLoading = false,
                        apiError = "Usuario o contraseña incorrectos."
                    )
                    is LoginResult.NetworkError -> current.copy(
                        isLoading = false,
                        apiError = result.message
                    )
                }
            }
        }
    }

    fun onLogout() {
        _uiState.update { LoginUiState() }
    }

    fun onLoginOffline() {
        _uiState.update {
            it.copy(
                isOfflineMode = true,
                loggedInUser = null
            )
        }
    }

    private fun validate(state: LoginUiState): Boolean {
        var valid = true

        val usernameError = when {
            state.username.isBlank() -> "El usuario no puede estar vacío."
            else -> null
        }

        val passwordError = when {
            state.password.isBlank() -> "La contraseña no puede estar vacía."
            state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            else -> null
        }

        if (usernameError != null || passwordError != null) {
            valid = false
            _uiState.update {
                it.copy(usernameError = usernameError, passwordError = passwordError)
            }
        }

        return valid
    }
}
