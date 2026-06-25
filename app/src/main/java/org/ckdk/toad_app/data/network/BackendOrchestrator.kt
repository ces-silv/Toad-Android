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
//        private const val BASE_URL = "http://10.0.2.2:8080/"
        private const val BASE_URL = "http://192.168.1.32:8080/"
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
    private val reportService: ReportService = retrofit.create(ReportService::class.java)

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

    suspend fun createReport(
        token: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        images: List<org.ckdk.toad_app.data.network.model.ReportImageRequest>?
    ): org.ckdk.toad_app.data.model.ReportResult {
        return try {
            val authHeader = "Bearer $token"
            val response = reportService.createReport(
                authHeader,
                org.ckdk.toad_app.data.network.model.ReportRequest(
                    description,
                    latitude,
                    longitude,
                    address,
                    images
                )
            )
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        org.ckdk.toad_app.data.model.ReportResult.Success(body)
                    } else {
                        org.ckdk.toad_app.data.model.ReportResult.Error("Empty response from server")
                    }
                }
                response.code() == 401 || response.code() == 403 -> {
                    org.ckdk.toad_app.data.model.ReportResult.Unauthorized
                }
                else -> {
                    org.ckdk.toad_app.data.model.ReportResult.Error("Server error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            org.ckdk.toad_app.data.model.ReportResult.Error("No connection: ${e.message}")
        } catch (e: Exception) {
            org.ckdk.toad_app.data.model.ReportResult.Error("Unexpected error: ${e.message}")
        }
    }

    suspend fun getMyReports(token: String): org.ckdk.toad_app.data.model.ReportListResult {
        return try {
            val authHeader = "Bearer $token"
            val response = reportService.getMyReports(authHeader)
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        org.ckdk.toad_app.data.model.ReportListResult.Success(body)
                    } else {
                        org.ckdk.toad_app.data.model.ReportListResult.Success(emptyList())
                    }
                }
                response.code() == 401 || response.code() == 403 -> {
                    org.ckdk.toad_app.data.model.ReportListResult.Unauthorized
                }
                else -> {
                    org.ckdk.toad_app.data.model.ReportListResult.Error("Server error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            org.ckdk.toad_app.data.model.ReportListResult.Error("No connection: ${e.message}")
        } catch (e: Exception) {
            org.ckdk.toad_app.data.model.ReportListResult.Error("Unexpected error: ${e.message}")
        }
    }
}
