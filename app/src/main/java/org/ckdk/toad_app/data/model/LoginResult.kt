package org.ckdk.toad_app.data.model

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    object InvalidCredentials : LoginResult()
    data class NetworkError(val message: String) : LoginResult()
}
