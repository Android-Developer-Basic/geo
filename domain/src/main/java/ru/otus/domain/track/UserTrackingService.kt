package ru.otus.domain.track

import ru.otus.domain.location.data.LocationData

/**
 * User track service
 */
interface UserTrackingService {
    /**
     * Track user location
     */
    suspend fun trackLocation(location: LocationData)
}