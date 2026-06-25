package org.ckdk.toad_app.data.network

import org.ckdk.toad_app.data.network.model.ReportRequest
import org.ckdk.toad_app.data.network.model.ReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ReportService {

    @POST("api/reports")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Body request: ReportRequest
    ): Response<ReportResponse>

    @GET("api/reports")
    suspend fun getReports(
        @Header("Authorization") token: String
    ): Response<List<ReportResponse>>

    @GET("api/reports/my-reports")
    suspend fun getMyReports(
        @Header("Authorization") token: String
    ): Response<List<ReportResponse>>
}
