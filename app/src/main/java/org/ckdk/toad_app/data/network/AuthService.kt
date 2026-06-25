package org.ckdk.toad_app.data.network

import org.ckdk.toad_app.data.network.model.LoginRequest
import org.ckdk.toad_app.data.network.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("api/auth/login")
    // Suspends the function (in the background) while waiting for the server response
    // in that way the user can keep scrolling or doing stuff while the backend is processing the request
    // When the server respond the function reactivates again
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
