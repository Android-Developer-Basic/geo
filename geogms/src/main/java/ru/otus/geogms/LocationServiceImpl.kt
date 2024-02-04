package ru.otus.geogms

import android.annotation.SuppressLint
import android.app.Application
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await
import ru.otus.domain.location.LocationService
import ru.otus.domain.location.data.LocationData
import javax.inject.Inject
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
    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): LocationData? {
        // Location provider
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
        // Return the last known location
        // Last known location is not guaranteed to be accurate
        // https://developer.android.com/develop/sensors-and-location/location/retrieve-current
        return fusedLocationClient.lastLocation.await()?.let { location ->
            LocationData(
                lat = location.latitude,
                lon = location.longitude
            )
        }
    }

    /**
     * Reports the current location at the specified interval
     */
    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalStdlibApi::class)
    override fun reportCurrentLocation(interval: Duration): Flow<LocationData> = channelFlow {
        // Location provider
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
        // Coroutine dispatcher to execute the location updates
        val dispatcher = coroutineContext[CoroutineDispatcher] ?: Dispatchers.Main
        // Location request
        // High accuracy is used to get the most accurate location
        // https://developer.android.com/develop/sensors-and-location/location/request-updates
        val request = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, interval.inWholeMilliseconds)
            .build()
        // Location update callback. It sends the location data to the channel
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(LocationData(
                        lat = location.latitude,
                        lon = location.longitude
                    ))
                }
            }
        }

        // Request location updates and close the flow if an exception is thrown
        try {
            fusedLocationClient.requestLocationUpdates(request, dispatcher.asExecutor(), callback).await()
        } catch (e: Exception) {
            close(e)
        }

        // Unsuscribe from location updates when the flow is cancelled
        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}