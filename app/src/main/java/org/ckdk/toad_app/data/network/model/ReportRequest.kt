package org.ckdk.toad_app.data.network.model

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String,
    @SerializedName("images") val images: List<ReportImageRequest>?
)

data class ReportImageRequest(
    @SerializedName("contentType") val contentType: String,
    @SerializedName("base64Data") val base64Data: String
)
