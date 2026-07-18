package com.kilagbe.fakegps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val repo = LocationRepository(context)
        val (wasActive, lat, lng) = runBlocking { repo.activeStateFlow.first() }
        if (!wasActive) return

        val serviceIntent = Intent(context, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_START
            putExtra(MockLocationService.EXTRA_LAT, lat)
            putExtra(MockLocationService.EXTRA_LNG, lng)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
