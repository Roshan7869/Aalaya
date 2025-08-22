package com.aalay.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.aalay.app.data.local.dao.AccommodationDao
import com.aalay.app.data.local.dao.StudentPreferencesDao
import com.aalay.app.data.local.dao.TrafficCacheDao
import com.aalay.app.data.local.entities.AccommodationEntity
import com.aalay.app.data.local.entities.StudentPreferencesEntity
import com.aalay.app.data.local.entities.TrafficCacheEntity
import com.aalay.app.data.local.converters.Converters

/**
 * Main Room database for Aalay app
 * Handles offline storage for accommodations, student preferences, and traffic data
 */
@Database(
    entities = [
        AccommodationEntity::class,
        StudentPreferencesEntity::class,
        TrafficCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AalayDatabase : RoomDatabase() {
    
    abstract fun accommodationDao(): AccommodationDao
    abstract fun studentPreferencesDao(): StudentPreferencesDao
    abstract fun trafficCacheDao(): TrafficCacheDao
    
    companion object {
        @Volatile
        private var INSTANCE: AalayDatabase? = null
        
        fun getDatabase(context: Context): AalayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AalayDatabase::class.java,
                    "aalay_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}