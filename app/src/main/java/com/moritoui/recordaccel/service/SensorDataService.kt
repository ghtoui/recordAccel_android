package com.moritoui.recordaccel.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.moritoui.recordaccel.R
import javax.inject.Inject

enum class ForegroundState {
    START,
    STOP
}

class SensorDataService @Inject constructor() : Service() {
    private lateinit var context: Context

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ForegroundState.START.name -> startService()
            else -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startService() {
        val notification = NotificationCompat.Builder(
            this,
            "CollectSensorChannel"
        ).setContentTitle("センサ起動中")
            .setContentText("終了ボタンを押すと終了します")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(1,notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    }
}
