package ru.otus.geo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import ru.otus.geo.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MapsActivity"

        fun start(context: Context) {
            val intent = Intent(context, MapsActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityMapsBinding

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.setNavigationOnClickListener {
            finish()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            Log.i(TAG, "Map is ready")
            onMapReady(googleMap)
        }
    }

    private fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }
}