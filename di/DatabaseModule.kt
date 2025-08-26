package com.aalay.app.di

import android.content.Context
import androidx.room.Room
import com.aalay.app.data.local.AalayDatabase
import com.aalay.app.data.local.dao.AccommodationDao
import com.aalay.app.data.local.dao.LocationDao
import com.aalay.app.data.local.dao.StudentPreferencesDao
import com.aalay.app.data.local.dao.TrafficCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAalayDatabase(
        @ApplicationContext context: Context
    ): AalayDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AalayDatabase::class.java,
            "aalay_database"
        )
            .fallbackToDestructiveMigration() // For development - use proper migrations in production
            .build()
    }

    @Provides
    fun provideAccommodationDao(
        database: AalayDatabase
    ): AccommodationDao {
        return database.accommodationDao()
    }

    @Provides
    fun provideLocationDao(
        database: AalayDatabase
    ): LocationDao {
        return database.locationDao()
    }

    @Provides
    fun provideStudentPreferencesDao(
        database: AalayDatabase
    ): StudentPreferencesDao {
        return database.studentPreferencesDao()
    }

    @Provides
    fun provideTrafficCacheDao(
        database: AalayDatabase
    ): TrafficCacheDao {
        return database.trafficCacheDao()
    }
}