package org.ckdk.toad_app.data.network.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("username") val username: String
)
