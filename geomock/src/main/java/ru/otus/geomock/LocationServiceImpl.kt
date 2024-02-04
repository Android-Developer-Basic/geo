package ru.otus.geomock

import android.app.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import ru.otus.domain.location.LocationService
import ru.otus.domain.location.data.LocationData
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.time.Duration

class LocationServiceImpl @Inject constructor(private val application: Application) : LocationService {
    /**
     * Check if the app has the location permission
     */
    override fun hasLocationPermissions(permissions: List<String>): Boolean = permissions.all {
        application.checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Retrieves the last known location if available
     */
    override suspend fun getLastLocation(): LocationData? = if (Random.nextBoolean()) {
        locations.random()
    } else {
        null
    }

    /**
     * Reports the current location at the specified interval
     */
    override fun reportCurrentLocation(interval: Duration): Flow<LocationData> = flow {
        while (coroutineContext.isActive) {
            emit(locations.random())
            delay(interval)
        }
    }
}

/**
 * Mock location data
 */
private val locations = listOf(
    LocationData(37.7749, -122.4194),
    LocationData(40.7128, -74.0060),
    LocationData(34.0522, -118.2437),
    LocationData(41.8781, -87.6298),
    LocationData(29.7604, -95.3698),
    LocationData(33.4484, -112.0740),
    LocationData(32.7157, -117.1611),
    LocationData(39.9526, -75.1652),
    LocationData(30.2672, -97.7431),
    LocationData(47.6062, -122.3321)
)