package org.ckdk.toad_app.data.network.model

import com.google.gson.annotations.SerializedName

data class ReportResponse(
    @SerializedName("id") val id: String,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String,
    @SerializedName("status") val status: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("reporterName") val reporterName: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("images") val images: List<ReportImageResponse>?
)

data class ReportImageResponse(
    @SerializedName("id") val id: String,
    @SerializedName("contentType") val contentType: String,
    @SerializedName("base64Data") val base64Data: String?
)
