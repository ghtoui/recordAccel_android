package com.moritoui.recordaccel.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SensorBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sendIntent = Intent(context, SensorDataService::class.java).apply {
            action = intent.action
        }
        context.startService(sendIntent)
    }
}
