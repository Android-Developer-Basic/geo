package ru.otus.geo

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.scopes.ActivityScoped
import ru.otus.domain.location.LocationService
import javax.inject.Inject

@ActivityScoped
class LocationPermissionHelper @Inject constructor(
    activity: Activity,
    private val locationService: LocationService
) {

    private val componentActivity = activity as ComponentActivity

    private var toRun: Pair<List<String>, () -> Unit>? = null

    private val launcher = componentActivity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        toRun?.takeIf { (requested, _) -> requested.all { result.getOrDefault(it, false) }}?.let { (_, block) -> block() }
    }

    operator fun invoke(vararg permissions: String, block: () -> Unit) {
        val listPermissions = permissions.toList()
        if (locationService.hasLocationPermissions(listPermissions)) {
            block()
        } else {
            toRun = listPermissions to block
            requestLocationPermission(listPermissions, block)
        }
    }

    private fun requestLocationPermission(permissions: List<String>, block: () -> Unit) {
        launcher.launch(permissions.toTypedArray())
    }
}