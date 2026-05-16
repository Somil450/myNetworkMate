package com.signalsense.ai.presentation.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.signalsense.ai.domain.usecase.HeatPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class HeatmapOverlay(private val points: List<HeatPoint>) : Overlay() {
    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        val projection = mapView.projection
        val point = android.graphics.Point()

        points.forEach { heatPoint ->
            projection.toPixels(GeoPoint(heatPoint.lat, heatPoint.lng), point)
            
            // Color mapping: Green (Good) -> Yellow (Moderate) -> Red (Poor)
            val color = when {
                heatPoint.intensity > 0.7f -> Color.argb(120, 0, 255, 127) // Neon Green
                heatPoint.intensity > 0.4f -> Color.argb(120, 255, 255, 0)   // Yellow
                else -> Color.argb(120, 255, 69, 0)                        // OrangeRed
            }
            
            paint.color = color
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 40f, paint)
        }
    }
}
