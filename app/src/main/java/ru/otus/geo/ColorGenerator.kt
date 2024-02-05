package ru.otus.geo

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlin.random.Random

object ColorGenerator {
    @ColorInt
    fun generateColor(): Int {
        val nextInt = Random.nextInt(0xffffff + 1)
        val colorCode = String.format("#ff%06x", nextInt)
        return Color.parseColor(colorCode)
    }
}
