package edu.temple.convoy

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationListenerService : Service(){

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    enum class Actions {
        START, STOP
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(){
        val notification = NotificationCompat.Builder(this, "Convoy")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tracking Location")
            .setContentText("Test Description")
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient.getLocationUpdates(10000L)
            .catch { e -> e.printStackTrace() }
            .onEach {
                val lat = it.latitude
                val long = it.longitude
                val intent = Intent(LOCATION_UPDATE)
                val newNotification = notification.setContentText("Location: ($lat, $long)")
                notificationManager.notify(1, newNotification.build())
                intent.putExtra("LATITUDE", lat)
                intent.putExtra("LONGITUDE", long)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }.launchIn(serviceScope)

        startForeground(1, notification.build())

    }

    private fun stop(){
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }



}