package com.kilagbe.fakegps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, MockLocationService::class.java).apply {
            action = intent.action
            putExtras(intent)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
