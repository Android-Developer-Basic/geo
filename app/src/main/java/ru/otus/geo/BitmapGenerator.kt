package ru.otus.geo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object BitmapGenerator {
    fun generateBitmap(context: Context, @ColorInt color: Int): BitmapDescriptor {
        val id = bitmaps.random()
        val vectorDrawable = ResourcesCompat.getDrawable(context.resources, id, null) ?: return BitmapDescriptorFactory.defaultMarker()
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

@DrawableRes
private val bitmaps: List<Int> = listOf(
    android.R.drawable.ic_menu_mylocation,
    android.R.drawable.ic_menu_compass,
    android.R.drawable.ic_menu_mapmode,
    android.R.drawable.ic_menu_zoom,
    android.R.drawable.ic_menu_myplaces,
    android.R.drawable.ic_menu_search,
    android.R.drawable.ic_menu_directions,
    android.R.drawable.ic_menu_gallery
)