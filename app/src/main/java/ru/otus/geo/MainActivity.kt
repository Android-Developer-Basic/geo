package ru.otus.geo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.otus.geo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)

        view.location.setOnClickListener {
            LocationActivity.start(this)
        }

        view.service.setOnClickListener {
            ServiceActivity.start(this)
        }

        view.maps.setOnClickListener {
            MapsActivity.start(this)
        }
    }
}