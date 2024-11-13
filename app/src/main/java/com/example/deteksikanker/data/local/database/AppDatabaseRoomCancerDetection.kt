package com.example.deteksikanker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryCancerRecord::class], version = 1, exportSchema = false)
abstract class AppDatabaseRoomCancerDetection : RoomDatabase() {
    abstract fun historyCancerDao(): HistoryCancerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabaseRoomCancerDetection? = null

        fun getDatabase(context: Context): AppDatabaseRoomCancerDetection {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabaseRoomCancerDetection::class.java,
                    "app_database_room_cancer_detection"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
