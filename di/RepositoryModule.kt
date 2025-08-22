package com.aalay.app.di

import com.aalay.app.data.repository.AccommodationRepository
import com.aalay.app.data.repository.AccommodationRepositoryImpl
import com.aalay.app.data.repository.DirectionsRepository
import com.aalay.app.data.repository.DirectionsRepositoryImpl
import com.aalay.app.data.repository.StudentAuthRepository
import com.aalay.app.data.repository.StudentAuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccommodationRepository(
        accommodationRepositoryImpl: AccommodationRepositoryImpl
    ): AccommodationRepository

    @Binds
    @Singleton
    abstract fun bindStudentAuthRepository(
        studentAuthRepositoryImpl: StudentAuthRepositoryImpl
    ): StudentAuthRepository

    @Binds
    @Singleton
    abstract fun bindDirectionsRepository(
        directionsRepositoryImpl: DirectionsRepositoryImpl
    ): DirectionsRepository
}