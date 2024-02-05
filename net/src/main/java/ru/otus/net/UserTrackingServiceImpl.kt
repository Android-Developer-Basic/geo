package ru.otus.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.otus.domain.location.data.LocationData
import ru.otus.domain.track.UserTrackingService
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * User track service implementation
 */
class UserTrackingServiceImpl @Inject constructor() : UserTrackingService {
    companion object {
        private const val TAG = "UserTrackServiceImpl"
    }

    override suspend fun trackLocation(location: LocationData) {
        withContext(Dispatchers.IO) {
            delay(1.seconds)
            println("$TAG: Tracking location: $location")
        }
    }
}