package ru.otus.geo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.otus.domain.location.LocationService
import ru.otus.domain.location.data.LocationData
import ru.otus.domain.track.UserTrackingService
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


@AndroidEntryPoint
class LocationTrackingService : LifecycleService() {
    // Messenger for receiving messages client -> server
    private lateinit var messenger: Messenger

    // Client messenger channel server -> client
    // Only one client is supported for simplicity
    private var client: WeakReference<Messenger>? = null

    private var job: Job? = null

    @Inject
    lateinit var locationService: LocationService

    @Inject
    lateinit var trackingService: UserTrackingService

    override fun onCreate() {
        Log.i(TAG, "Starting location tracking service")
        super.onCreate()
        messenger = Messenger(IncomingHandler(WeakReference(this)))
        startForeground()
        startTracking()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "Binding...")
        super.onBind(intent)
        return messenger.binder
    }

    override fun onDestroy() {
        Log.i(TAG, "Stopping location tracking service")
        job?.cancel()
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun startTracking() {
        Log.i(TAG, "Starting location tracking")
        job = lifecycleScope.launch {
            locationService.reportCurrentLocation(2.seconds)
                .onEach { location ->
                    Log.i(TAG, "Location: $location")
                    sendMessage(location)
                    trackingService.trackLocation(location)
                }
                .launchIn(lifecycleScope)
        }
    }

    // Send location to the client
    private fun sendMessage(location: LocationData) {
        client?.get()?.send(
            Message.obtain(null, MSG_LOCATION).apply {
                data = bundleOf(KEY_LOCATION to location)
            }
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Unbinding...")
        client = null
        return false
    }

    private fun startForeground() {
        val channel = NotificationChannel(CHANNEL_ID, "Geo tracking", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Geo app location tracking"

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location tracking")
            .setContentText("Tracking your location")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )
    }

    // Handler of incoming messages from client
    // Client sends us registration and unregistration messages
    private class IncomingHandler(private val service: WeakReference<LocationTrackingService>) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER -> {
                    service.get()?.client = WeakReference(msg.replyTo)
                }
                MSG_UNREGISTER -> {
                    service.get()?.client = null
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "geo"
        private const val TAG = "LocationTrackingService"
        const val MSG_REGISTER = 1
        const val MSG_UNREGISTER = 2
        const val MSG_LOCATION = 3
        const val KEY_LOCATION = "location"
    }
}