package com.kilagbe.fakegps

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class MockLocationService : Service() {

    companion object {
        const val ACTION_START = "com.kilagbe.fakegps.action.START"
        const val ACTION_SWITCH = "com.kilagbe.fakegps.action.SWITCH"
        const val ACTION_STOP = "com.kilagbe.fakegps.action.STOP"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
        const val EXTRA_NAME = "extra_name"
        const val NOTIF_ID = 1001

        private val PROVIDERS: List<String>
            get() {
                val list = mutableListOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    list.add(LocationManager.FUSED_PROVIDER)
                } else {
                    list.add("fused")
                }
                return list
            }
    }

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var currentLat = 23.8103
    private var currentLng = 90.4125
    private var currentName: String = "কাস্টম"
    private var jitterEnabled = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_SWITCH -> {
                currentLat = intent.getDoubleExtra(EXTRA_LAT, currentLat)
                currentLng = intent.getDoubleExtra(EXTRA_LNG, currentLng)
                currentName = intent.getStringExtra(EXTRA_NAME) ?: currentName
                startForeground(NOTIF_ID, NotificationHelper.build(this, true, currentName))
                startFeeding()
                runBlocking {
                    LocationRepository(applicationContext)
                        .setActive(true, currentLat, currentLng, currentName)
                }
            }
            ACTION_STOP -> {
                stopFeeding()
                runBlocking {
                    LocationRepository(applicationContext)
                        .setActive(false, currentLat, currentLng, currentName)
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startFeeding() {
        job?.cancel()
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager

        for (provider in PROVIDERS) {
            try {
                lm.addTestProvider(
                    provider,
                    false, false, false, false,
                    true, true, true,
                    android.location.Criteria.POWER_LOW,
                    android.location.Criteria.ACCURACY_FINE
                )
            } catch (_: Exception) { }
            try {
                lm.setTestProviderEnabled(provider, true)
            } catch (_: Exception) { }
        }

        job = scope.launch {
            while (true) {
                pushLocation(lm)
                NotificationManagerCompat.from(this@MockLocationService)
                    .notify(NOTIF_ID, NotificationHelper.build(this@MockLocationService, true, currentName))
                delay(2000)
            }
        }
    }

    private fun pushLocation(lm: LocationManager) {
        val lat = if (jitterEnabled) currentLat + Random.nextDouble(-0.00005, 0.00005) else currentLat
        val lng = if (jitterEnabled) currentLng + Random.nextDouble(-0.00005, 0.00005) else currentLng

        for (provider in PROVIDERS) {
            val location = Location(provider).apply {
                latitude = lat
                longitude = lng
                altitude = 0.0
                accuracy = 5f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bearingAccuracyDegrees = 0.1f
                    verticalAccuracyMeters = 0.1f
                    speedAccuracyMetersPerSecond = 0.01f
                }
            }
            try {
                lm.setTestProviderLocation(provider, location)
            } catch (_: Exception) { }
        }
    }

    private fun stopFeeding() {
        job?.cancel()
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        for (provider in PROVIDERS) {
            try {
                lm.removeTestProvider(provider)
            } catch (_: Exception) { }
        }
    }

    override fun onDestroy() {
        stopFeeding()
        super.onDestroy()
    }
}
