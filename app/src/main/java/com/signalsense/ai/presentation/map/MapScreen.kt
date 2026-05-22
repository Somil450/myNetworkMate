package com.signalsense.ai.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.signalsense.ai.core.theme.DeepBlue
import com.signalsense.ai.core.theme.ElectricBlue
import com.signalsense.ai.data.remote.OpenCelliDCell
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val towers by viewModel.allNearbyTowers.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isLoading by viewModel.isLoadingTowers.collectAsState()
    val selectedTower by viewModel.selectedTower.collectAsState()
    val currentTower by viewModel.currentTower.collectAsState()
    val targetTower by viewModel.targetTower.collectAsState()

    val centerLat = userLocation?.lat ?: 12.9716
    val centerLng = userLocation?.lng ?: 77.5946

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(centerLat, centerLng))
                    setMultiTouchControls(true)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()
                mapView.controller.setCenter(GeoPoint(centerLat, centerLng))

                // Add user location marker
                userLocation?.let { loc ->
                    val userMarker = Marker(mapView)
                    userMarker.position = GeoPoint(loc.lat, loc.lng)
                    userMarker.title = "📍 You Are Here"
                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(userMarker)
                }

                // Add all carrier towers from OpenCelliD
                towers.forEach { tower ->
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(tower.lat, tower.lon)
                    marker.title = "${carrierEmoji(tower.carrierName)} ${tower.carrierName} Tower"
                    marker.snippet = "${tower.networkType} • Signal: ${tower.averageSignal} dBm"
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    if (tower == currentTower) {
                        ContextCompat.getDrawable(mapView.context, android.R.drawable.presence_online)?.let {
                            marker.icon = it
                        }
                    } else if (tower == targetTower) {
                        ContextCompat.getDrawable(mapView.context, android.R.drawable.presence_away)?.let {
                            marker.icon = it
                        }
                    }

                    marker.setOnMarkerClickListener { m, _ ->
                        viewModel.selectTower(tower)
                        m.showInfoWindow()
                        true
                    }
                    mapView.overlays.add(marker)
                }

                // Add Connection Lines
                userLocation?.let { loc ->
                    val userGeo = GeoPoint(loc.lat, loc.lng)
                    currentTower?.let { ct ->
                        val line = Polyline(mapView)
                        line.setPoints(listOf(userGeo, GeoPoint(ct.lat, ct.lon)))
                        line.outlinePaint.color = AndroidColor.GREEN
                        line.outlinePaint.strokeWidth = 8f
                        mapView.overlays.add(line)
                    }
                    targetTower?.let { tt ->
                        val line = Polyline(mapView)
                        line.setPoints(listOf(userGeo, GeoPoint(tt.lat, tt.lon)))
                        line.outlinePaint.color = AndroidColor.YELLOW
                        line.outlinePaint.strokeWidth = 8f
                        mapView.overlays.add(line)
                    }
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top overlay: Loading + tower count
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) {
                Surface(
                    color = DeepBlue.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = ElectricBlue,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetching towers from OpenCelliD...", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else if (towers.isNotEmpty()) {
                Surface(
                    color = DeepBlue.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "📡 ${towers.size} towers found nearby",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Carrier legend
            CarrierLegend(towers)

            Spacer(modifier = Modifier.height(8.dp))

            // Refresh button
            Button(
                onClick = { viewModel.fetchTowersFromCloud() },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔄 Refresh All Towers", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Bottom Sheet for Tower Details
        selectedTower?.let { tower ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.selectTower(null) },
                containerColor = DeepBlue
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Text(
                        "${carrierEmoji(tower.carrierName)} ${tower.carrierName} Tower Details",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Network Type: ${tower.networkType}", color = Color.LightGray)
                    Text("Signal Strength: ${tower.averageSignal} dBm", color = Color.LightGray)
                    Text("Cell ID: ${tower.id}", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        color = ElectricBlue.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Estimated Speed", color = ElectricBlue, fontSize = 14.sp)
                            Text("${tower.estimatedSpeedMbps} Mbps", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(
                            onClick = { viewModel.setCurrentTower(tower) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Set Current", color = Color.White)
                        }
                        Button(
                            onClick = { viewModel.setTargetTower(tower) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                        ) {
                            Text("Set Target", color = Color.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun CarrierLegend(towers: List<OpenCelliDCell>) {
    if (towers.isEmpty()) return

    val carrierCounts = towers.groupBy { it.carrierName }.mapValues { it.value.size }

    Surface(
        color = DeepBlue.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Tower Count by Carrier", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            carrierCounts.forEach { (carrier, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${carrierEmoji(carrier)} $carrier", color = Color.White, fontSize = 12.sp)
                    Text("$count towers", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

fun carrierEmoji(carrier: String): String = when (carrier) {
    "Airtel" -> "🔴"
    "Jio" -> "🔵"
    "Vi" -> "🟣"
    "BSNL" -> "🟢"
    else -> "⚪"
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
