package org.ckdk.toad_app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ckdk.toad_app.data.network.model.ReportImageResponse
import org.ckdk.toad_app.data.network.model.ReportResponse

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val status: String,
    val userId: String,
    val reporterName: String,
    val createdAt: String,
    val images: List<ReportImageResponse>?,
    val cachedByUsername: String
)

fun ReportResponse.toEntity(cachedByUsername: String) = ReportEntity(
    id = id,
    description = description,
    latitude = latitude,
    longitude = longitude,
    address = address,
    status = status,
    userId = userId,
    reporterName = reporterName,
    createdAt = createdAt,
    images = images,
    cachedByUsername = cachedByUsername
)

fun ReportEntity.toResponse() = ReportResponse(
    id = id,
    description = description,
    latitude = latitude,
    longitude = longitude,
    address = address,
    status = status,
    userId = userId,
    reporterName = reporterName,
    createdAt = createdAt,
    images = images
)
