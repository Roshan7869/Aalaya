package com.aalay.app.di

import android.content.Context
import com.aalay.app.BuildConfig
import com.aalay.app.config.ConfigManager
import com.aalay.app.data.remote.AccommodationApiService
import com.aalay.app.data.remote.MapboxDirectionsService
import com.aalay.app.data.remote.BhilaiLocationApiService
import com.aalay.app.utils.SecurityConfig
import com.aalay.app.utils.SecurityInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideConfigManager(
        @ApplicationContext context: Context
    ): ConfigManager {
        return ConfigManager(context)
    }

    @Provides
    @Singleton
    fun provideSecurityConfig(
        @ApplicationContext context: Context,
        configManager: ConfigManager
    ): SecurityConfig {
        return SecurityConfig(context, configManager)
    }

    @Provides
    @Singleton
    fun provideSecurityInterceptor(
        securityConfig: SecurityConfig
    ): SecurityInterceptor {
        return SecurityInterceptor(securityConfig)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(
        configManager: ConfigManager
    ): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (configManager.isNetworkLoggingEnabled) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    @Named("mapbox_api_key")
    fun provideMapboxApiKey(
        configManager: ConfigManager
    ): String {
        return configManager.mapboxAccessToken
    }

    @Provides
    @Singleton
    fun provideMapboxAuthInterceptor(
        @Named("mapbox_api_key") apiKey: String
    ): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val url = originalRequest.url.newBuilder()
                .addQueryParameter("access_token", apiKey)
                .build()
            
            val newRequest = originalRequest.newBuilder()
                .url(url)
                .build()
            
            chain.proceed(newRequest)
        }
    }

    @Provides
    @Singleton
    @Named("aalay_okhttp")
    fun provideAalayOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        securityInterceptor: SecurityInterceptor,
        configManager: ConfigManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(configManager.apiTimeout, TimeUnit.SECONDS)
            .readTimeout(configManager.apiTimeout, TimeUnit.SECONDS)
            .writeTimeout(configManager.apiTimeout, TimeUnit.SECONDS)
            .addInterceptor(securityInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                // Add common headers for Aalay API
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    @Named("mapbox_okhttp")
    fun provideMapboxOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        mapboxAuthInterceptor: Interceptor,
        configManager: ConfigManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(configManager.apiTimeout, TimeUnit.SECONDS)
            .readTimeout(configManager.apiTimeout, TimeUnit.SECONDS)
            .writeTimeout(configManager.apiTimeout, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(mapboxAuthInterceptor)
            .addInterceptor { chain ->
                // Add common headers for Mapbox API
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    @Named("aalay_retrofit")
    fun provideAalayRetrofit(
        @Named("aalay_okhttp") okHttpClient: OkHttpClient,
        configManager: ConfigManager
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(configManager.apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("mapbox_retrofit")
    fun provideMapboxRetrofit(
        @Named("mapbox_okhttp") okHttpClient: OkHttpClient,
        configManager: ConfigManager
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(configManager.mapboxDirectionsApiUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAccommodationApiService(
        @Named("aalay_retrofit") retrofit: Retrofit
    ): AccommodationApiService {
        return retrofit.create(AccommodationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMapboxDirectionsService(
        @Named("mapbox_retrofit") retrofit: Retrofit
    ): MapboxDirectionsService {
        return retrofit.create(MapboxDirectionsService::class.java)
    }

    @Provides
    @Singleton
    fun provideBhilaiLocationApiService(
        @Named("aalay_retrofit") retrofit: Retrofit
    ): BhilaiLocationApiService {
        return retrofit.create(BhilaiLocationApiService::class.java)
    }
}