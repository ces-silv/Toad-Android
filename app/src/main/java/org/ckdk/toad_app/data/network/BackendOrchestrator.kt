package org.ckdk.toad_app.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.ckdk.toad_app.data.model.LoginResult
import org.ckdk.toad_app.data.model.User
import org.ckdk.toad_app.data.network.model.LoginRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackendOrchestrator {

    // block for constants and static methods
    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/"
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()) // Converts JSON to Kotlin
        .build()

    // Retrofit needs Java Interface Reference to create the implementation of the HTTP request
    private val authService: AuthService = retrofit.create(AuthService::class.java)

    suspend fun login(username: String, password: String): LoginResult {
        return try {
            val response = authService.login(LoginRequest(username, password))
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        LoginResult.Success(
                            User(username = body.username, token = body.token)
                        )
                    } else {
                        LoginResult.NetworkError("Empty response from server")
                    }
                }
                response.code() == 401 || response.code() == 403 -> {
                    LoginResult.InvalidCredentials
                }
                else -> {
                    LoginResult.NetworkError("Server error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            LoginResult.NetworkError("No connection: ${e.message}")
        } catch (e: Exception) {
            LoginResult.NetworkError("Unexpected error: ${e.message}")
        }
    }
}
