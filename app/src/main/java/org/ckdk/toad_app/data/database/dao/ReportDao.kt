package org.ckdk.toad_app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.ckdk.toad_app.data.database.entity.ReportEntity

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports WHERE cachedByUsername = :username ORDER BY createdAt DESC")
    suspend fun getReports(username: String): List<ReportEntity>

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    suspend fun getAllReports(): List<ReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ReportEntity>): List<Long>

    @Query("DELETE FROM reports WHERE cachedByUsername = :username")
    suspend fun clearReports(username: String): Int
}
