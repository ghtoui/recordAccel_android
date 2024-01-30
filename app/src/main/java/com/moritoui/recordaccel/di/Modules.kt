package com.moritoui.recordaccel.di

import android.content.Context
import android.hardware.SensorManager
import com.moritoui.recordaccel.model.MotionSensor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    fun provideMotionSensor(sensorManager: SensorManager): MotionSensor {
        return MotionSensor(sensorManager = sensorManager)
    }
}
