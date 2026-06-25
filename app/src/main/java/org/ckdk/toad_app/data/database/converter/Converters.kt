package org.ckdk.toad_app.data.database.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.ckdk.toad_app.data.network.model.ReportImageResponse
import org.ckdk.toad_app.data.database.AppDatabase
import java.io.File

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromImageList(images: List<ReportImageResponse>?): String? {
        if (images == null) return null
        
        val context = AppDatabase.appContext
        if (context == null) {
            return gson.toJson(images)
        }

        val savedImages = images.map { img ->
            val base64 = img.base64Data
            if (!base64.isNullOrBlank() && !base64.startsWith("local_file:")) {
                try {
                    val dir = File(context.filesDir, "report_images")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file = File(dir, "${img.id}.txt")
                    file.writeText(base64)
                    img.copy(base64Data = "local_file:${img.id}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    img
                }
            } else {
                img
            }
        }
        return gson.toJson(savedImages)
    }

    @TypeConverter
    fun toImageList(json: String?): List<ReportImageResponse>? {
        if (json == null) return null
        val type = object : TypeToken<List<ReportImageResponse>>() {}.type
        val imagesList: List<ReportImageResponse>? = gson.fromJson(json, type)
        if (imagesList == null) return null

        val context = AppDatabase.appContext ?: return imagesList

        return imagesList.map { img ->
            val base64 = img.base64Data
            if (base64 != null && base64.startsWith("local_file:")) {
                try {
                    val id = base64.substringAfter("local_file:")
                    val file = File(File(context.filesDir, "report_images"), "$id.txt")
                    if (file.exists()) {
                        img.copy(base64Data = file.readText())
                    } else {
                        img
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    img
                }
            } else {
                img
            }
        }
    }
}
