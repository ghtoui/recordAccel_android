package com.moritoui.recordaccel.di

import android.content.Context
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.moritoui.recordaccel.BuildConfig
import com.moritoui.recordaccel.model.MotionSensor
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.model.UserListDataRepository
import com.moritoui.recordaccel.model.UserListDataRepositoryImpl
import com.moritoui.recordaccel.network.AccelApiService
import com.moritoui.recordaccel.repositories.SensorDataRepository
import com.moritoui.recordaccel.repositories.SensorDataRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

private const val BASE_URL = BuildConfig.DENO_ENDPOINT

@Module
@InstallIn(SingletonComponent::class)
object Modules {
    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context): SensorManager {
        return context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @Provides
    @Singleton
    fun provideMotionSensor(
        sensorManager: SensorManager,
    ): MotionSensor {
        return MotionSensor(
            sensorManager = sensorManager,
        )
    }

    @Provides
    @Singleton
    fun provideTimeManager(): TimeManager {
        return TimeManager()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile("user_list")
            }
        )
    }

    @Provides
    @Singleton
    fun provideUserListDataRepository(
        dataStore: DataStore<Preferences>,
        moshi: Moshi
    ): UserListDataRepository {
        return UserListDataRepositoryImpl(dataStore, moshi.adapter<MutableList<User>>(Types.newParameterizedType(MutableList::class.java, User::class.java)))
    }

    @Provides
    @Singleton
    fun provideSensorDataRepository(timeManager: TimeManager, motionSensor: MotionSensor, accelApiService: AccelApiService): SensorDataRepository {
        return SensorDataRepositoryImpl(motionSensor = motionSensor, timeManager = timeManager, accelApi = accelApiService)
    }

    @Provides
    @Singleton
    fun provideInterceptor(): OkHttpClient {
        // log出力のためのインターセプターを追加
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            // log出力のためのインターセプターを追加
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideAccelApiService(retrofit: Retrofit): AccelApiService {
        return retrofit
            .create(AccelApiService::class.java)
    }
}
