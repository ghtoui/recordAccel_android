package com.moritoui.recordaccel.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.moritoui.recordaccel.BuildConfig
import com.moritoui.recordaccel.MainActivity
import com.moritoui.recordaccel.R
import com.moritoui.recordaccel.model.SensorCollectSender
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

enum class ForegroundState {
    START,
    STOP
}

@AndroidEntryPoint
class SensorDataService @Inject constructor() : Service() {
    @Inject lateinit var sensorCollectSender: SensorCollectSender
    companion object {
        const val CHANNEL_ID = 1
        @DrawableRes var NOTIFICATION_ICON: Int = R.drawable.groups
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ForegroundState.START.name -> startSensorService()
            ForegroundState.STOP.name -> stopSensorService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startSensorService() {
        sensorCollectSender.isCollect = true
        sensorCollectSender.updateAndSendAccData()
        val sendIntent = Intent(applicationContext, SensorBroadCastReceiver::class.java).apply {
            action = ForegroundState.STOP.name
        }
        // 通知タップ時でアプリを起動する
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_IMMUTABLE)

        val sendPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            sendIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            this,
            BuildConfig.FOREGROUND_SENSOR_CHANNEL_NAME
        ).setContentTitle("見守り中")
            .setSmallIcon(NOTIFICATION_ICON)
            .addAction(NOTIFICATION_ICON, "停止する", sendPendingIntent)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(CHANNEL_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    }

    private fun stopSensorService() {
        // 通知タップ時でアプリを起動する
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_IMMUTABLE)

        sensorCollectSender.isCollect = false
        val sendIntent = Intent(
            this,
            SensorBroadCastReceiver::class.java
        ).apply {
            action = ForegroundState.START.name
        }
        val sendPendingIntent = PendingIntent.getBroadcast(
            this,
            0, sendIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            this,
            BuildConfig.FOREGROUND_SENSOR_CHANNEL_NAME
        ).setContentTitle("見守り停止中")
            .setSmallIcon(NOTIFICATION_ICON)
            .addAction(NOTIFICATION_ICON, "再開する", sendPendingIntent)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(CHANNEL_ID, notification)
    }
}
