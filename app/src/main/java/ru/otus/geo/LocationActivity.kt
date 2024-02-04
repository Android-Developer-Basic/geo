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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.otus.domain.location.LocationService
import ru.otus.geo.databinding.ActivityLocationBinding
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class LocationActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LocationActivity"

        fun start(context: Context) {
            val intent = Intent(context, LocationActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var locationService: LocationService

    @Inject
    lateinit var checkPermissions: LocationPermissionHelper

    private lateinit var binding: ActivityLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.setNavigationOnClickListener {
            finish()
        }

        binding.currentLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.watchLocation.setOnClickListener {
            watchLocation()
        }
    }

    private fun getCurrentLocation() = checkPermissions(ACCESS_COARSE_LOCATION) {
        lifecycleScope.launch {
            locationService.getLastLocation()?.let { location ->
                Log.i(TAG, "Location: $location")
                binding.lat.text = getString(R.string.lat_data, location.lat)
                binding.lon.text = getString(R.string.lon_data, location.lon)
            } ?: run {
                Log.i(TAG, "Location: null")
                binding.lat.text = getString(R.string.lat)
                binding.lon.text = getString(R.string.lon)
            }
        }
    }

    private fun watchLocation() = checkPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                locationService.reportCurrentLocation(2.seconds).collect { location ->
                    Log.i(TAG, "Location: $location")
                    binding.lat.text = getString(R.string.lat_data, location.lat)
                    binding.lon.text = getString(R.string.lon_data, location.lon)
                }
            }
        }
    }
}