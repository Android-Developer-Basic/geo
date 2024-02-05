package ru.otus.geo

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.domain.location.data.LocationData
import ru.otus.geo.databinding.ActivityServiceBinding
import java.lang.ref.WeakReference
import javax.inject.Inject


@AndroidEntryPoint
class ServiceActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ServiceActivity"

        fun start(context: Context) {
            val intent = Intent(context, ServiceActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var checkPermissions: LocationPermissionHelper

    private lateinit var binding: ActivityServiceBinding

    // Messenger for sending messages to the bound service
    private var messenger: WeakReference<Messenger>? = null
    // Messenger for receiving messages from the service
    private val incomingMessenger = Messenger(IncomingHandler(WeakReference(this)))
    // Flag indicating whether we have called bind on the service
    private var bound: Boolean = false

    // Service connection for binding to the service
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            bound = true
            // Register message channel client -> service
            messenger = WeakReference(Messenger(service))
            val message = Message.obtain(null, LocationTrackingService.MSG_REGISTER).apply {
                // Set the reply messenger to receive messages server -> client
                replyTo = incomingMessenger
            }
            // Register client
            messenger?.get()?.send(message)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            messenger = null
        }
    }

    // Handler of incoming messages from service
    // Server sends us location updates
    private class IncomingHandler(private val activity: WeakReference<ServiceActivity>) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                LocationTrackingService.MSG_LOCATION -> {
                    val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        msg.data.getParcelable(LocationTrackingService.KEY_LOCATION, LocationData::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        msg.data.getParcelable(LocationTrackingService.KEY_LOCATION)
                    }

                    Log.i(TAG, "Received location: $location")
                    activity.get()?.updateLocation(location)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            title.setNavigationOnClickListener {
                finish()
            }
            startService.setOnClickListener {
                startService()
            }
            stopService.setOnClickListener {
                stopService()
            }
            subscribeService.setOnClickListener {
                watchLocation()
            }
            unsubscribeService.setOnClickListener {
                unwatchLocation()
            }
        }
    }

    // Start location tracking service which will run in the foreground
    private fun startService() = checkPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION) {
        Log.i(TAG, "Starting location tracking service")
        val intent = Intent(this, LocationTrackingService::class.java)
        startForegroundService(intent)
    }

    // Stop location tracking service
    private fun stopService() {
        Log.i(TAG, "Stopping location tracking service")
        unwatchLocation()
        val intent = Intent(this, LocationTrackingService::class.java)
        stopService(intent)
    }

    // Watch (bind) location updates from the service
    private fun watchLocation() {
        Log.i(TAG, "Watching location...")
        if (bound) {
            return
        }
        val intent = Intent(this, LocationTrackingService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // Unwatch (unbind) location updates from the service
    private fun unwatchLocation() {
        Log.i(TAG, "Unwatching location...")
        if (bound.not()) {
            return
        }
        val message = Message.obtain(null, LocationTrackingService.MSG_UNREGISTER)
        messenger?.get()?.send(message)

        unbindService(connection)
        bound = false
        messenger = null
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission()
    }

    override fun onPause() {
        super.onPause()
        unwatchLocation()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS).let { result ->
                val granted = PackageManager.PERMISSION_GRANTED == result
                setButtonsEnabled(granted)
                if (!granted) {
                    notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
                }
            }
        } else {
            setButtonsEnabled(true)
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        setButtonsEnabled(granted)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        with(binding) {
            startService.isEnabled = enabled
            stopService.isEnabled = enabled
            subscribeService.isEnabled = enabled
            unsubscribeService.isEnabled = enabled
        }
    }

    private fun updateLocation(location: LocationData?) = with(binding) {
        if (null != location) {
            lat.text = getString(R.string.lat_data, location.lat)
            lon.text = getString(R.string.lon_data, location.lon)
        }  else {
            lat.text = getString(R.string.lat)
            lon.text = getString(R.string.lon)
        }
    }
}