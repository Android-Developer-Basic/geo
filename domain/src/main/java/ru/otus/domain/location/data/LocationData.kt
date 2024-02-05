package ru.otus.domain.location.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Location data
 * @property lat Latitude
 * @property lon Longitude
 */
@Parcelize
data class LocationData(val lat: Double, val lon: Double): Parcelable