package com.moritoui.recordaccel.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.moritoui.recordaccel.BuildConfig
import com.moritoui.recordaccel.MainActivity
import com.moritoui.recordaccel.R

class PushNotificationService : FirebaseMessagingService() {
    companion object {
        const val CHANNEL_ID = 2
        @DrawableRes var NOTIFICATION_ICON: Int = R.drawable.groups
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val notification = NotificationCompat.Builder(
            this,
            BuildConfig.FOREGROUND_SENSOR_CHANNEL_NAME
        ).setSmallIcon(NOTIFICATION_ICON)
            .setContentTitle(remoteMessage.notification!!.title)
            .setContentText(remoteMessage.notification!!.body).setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val channel = NotificationChannel(
            BuildConfig.FOREGROUND_SENSOR_CHANNEL_NAME,
            "Firebase messaging channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        manager.notify(CHANNEL_ID, notification)
    }
}
