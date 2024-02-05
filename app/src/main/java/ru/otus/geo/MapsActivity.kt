package ru.otus.geo

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.otus.domain.location.LocationService
import ru.otus.geo.databinding.ActivityMapsBinding
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class MapsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MapsActivity"

        fun start(context: Context) {
            val intent = Intent(context, MapsActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityMapsBinding

    private var googleMap: GoogleMap? = null
    private inline fun withMaps(block: GoogleMap.() -> Unit) {
        googleMap?.let(block)
    }

    @Inject
    lateinit var locationService: LocationService

    @Inject
    lateinit var checkPermissions: LocationPermissionHelper

    private var followJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            title.setNavigationOnClickListener {
                finish()
            }
            followLocation.setOnClickListener {
                followLocation()
            }
            stopFollowLocation.setOnClickListener {
                stopFollowLocation()
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            Log.i(TAG, "Map is ready")
            onMapReady(googleMap)
        }
    }

    private fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        followMapLocation()
    }

    // Camera movement
    private fun followLocation() = checkPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION) {
        followJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                locationService.reportCurrentLocation(3.seconds).collect { location ->
                    Log.i(TAG, "Location: $location")
                    withMaps {
                        val latLng = LatLng(location.lat, location.lon)
                        val update = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
                        animateCamera(update)
                    }
                }
            }
        }
    }

    private fun stopFollowLocation() {
        followJob?.cancel()
    }

    // Gets map location when camera is idle
    private fun followMapLocation() = withMaps {
        setOnCameraIdleListener {
            val latLng = cameraPosition.target
            Log.i(TAG, "Map Location: $latLng")
        }
    }

}