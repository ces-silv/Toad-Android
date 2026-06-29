package org.ckdk.toad_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ckdk.toad_app.data.database.converter.Converters
import org.ckdk.toad_app.data.database.dao.ReportDao
import org.ckdk.toad_app.data.database.entity.ReportEntity

@Database(entities = [ReportEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var DB_INSTANCE: AppDatabase? = null

        @Volatile
        var appContext: Context? = null

        fun getDatabase(context: Context): AppDatabase {
            appContext = context.applicationContext
            // If instance already exists then return it
            // if not we use synchronized to make sure that only one thread at a time can create a database
            return DB_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "toad_app_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                DB_INSTANCE = instance
                instance
            }
        }
    }
}