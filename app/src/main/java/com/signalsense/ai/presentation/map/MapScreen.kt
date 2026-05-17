package com.signalsense.ai.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.signalsense.ai.core.theme.DeepBlue
import com.signalsense.ai.core.theme.ElectricBlue
import androidx.compose.ui.Alignment
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.signalsense.ai.presentation.components.ScanningPulse

@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val towers by viewModel.towerLocations.collectAsState(initial = emptyList())
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(12.9716, 77.5946)) // Default to Bangalore for demo
                    setMultiTouchControls(true)
                    
                    // Add Heatmap Overlay
                    overlays.add(HeatmapOverlay(viewModel.heatmapPoints.value))
                }
            },
            update = { mapView ->
                mapView.overlays.clear()
                
                // Re-add Heatmap
                mapView.overlays.add(HeatmapOverlay(viewModel.heatmapPoints.value))
                
                towers.forEach { tower ->
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(tower.lat, tower.lng)
                    marker.title = "${tower.carrier} Tower"
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Panels
        Column(modifier = Modifier.padding(16.dp)) {
            AIRecommendationCard(viewModel)
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ScanningPulse()
            }
        }
    }
}

@Composable
fun AIRecommendationCard(viewModel: MapViewModel) {
    Surface(
        color = DeepBlue.copy(alpha = 0.8f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI RECOMMENDATION", color = ElectricBlue, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Best Carrier: Jio (92% Confidence)", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Action: Move 30m East for 5G optimization", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
