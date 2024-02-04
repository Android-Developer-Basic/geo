package ru.otus.domain.location

import kotlinx.coroutines.flow.Flow
import ru.otus.domain.location.data.LocationData
import kotlin.time.Duration

/**
 * Service for retrieving and reporting location data
 */
interface LocationService {
    /**
     * Check if the app has the location permission
     */
    fun hasLocationPermissions(permissions: List<String>): Boolean

    /**
     * Retrieves the last known location if available
     */
    suspend fun getLastLocation(): LocationData?

    /**
     * Reports the current location at the specified interval
     */
    fun reportCurrentLocation(interval: Duration): Flow<LocationData>
}