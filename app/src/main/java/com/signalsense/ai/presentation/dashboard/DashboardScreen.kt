package com.signalsense.ai.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import com.signalsense.ai.core.theme.ElectricBlue
import com.signalsense.ai.core.theme.GlassWhite
import com.signalsense.ai.domain.model.TowerInfo

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val connectedTower by viewModel.connectedTower.collectAsState()
    val nearbyTowers by viewModel.towerInfoList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1A2E))))
            .padding(16.dp)
    ) {
        Text(
            text = "SIGNAL SENSE AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ElectricBlue,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Current Connection Card
        connectedTower?.let { tower ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CURRENT CONNECTION", color = ElectricBlue.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(tower.carrier, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("Tower ID: ${tower.id}", color = Color.Gray)
                        }
                        Text(
                            text = tower.type.name.replace("_", ""),
                            color = ElectricBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SignalMeter(level = tower.signalLevel, dbm = tower.signalStrength)
                }
            }
            
            AIAnalyticsPanel()
            Spacer(modifier = Modifier.height(16.dp))
            
            OptimizationAdviceCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "NEARBY TOWER INTELLIGENCE",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(nearbyTowers.filter { !it.isConnected }) { tower ->
                TowerComparisonItem(tower)
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
fun SignalMeter(level: Int, dbm: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(16.dp + (index * 4).dp)
                    .padding(horizontal = 1.dp)
                    .background(
                        if (index < level) ElectricBlue else Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text("${dbm} dBm", color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AIAnalyticsPanel() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI INTELLIGENCE", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Congestion Forecast", color = Color.Gray, fontSize = 10.sp)
                    Text("LOW in 20 mins", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Best Carrier Rank", color = Color.Gray, fontSize = 10.sp)
                    Text("1. Jio  2. Airtel", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = 0.8f,
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = ElectricBlue,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
    }
}

@Composable
fun OptimizationAdviceCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Optimize",
                    tint = com.signalsense.ai.core.theme.WarningOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SMART OPTIMIZATION", color = com.signalsense.ai.core.theme.WarningOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Gaming Optimization Available", color = Color.White, fontWeight = FontWeight.SemiBold)
            Text("Switch to LTE (4G Only) in system settings. The nearby LTE tower has a stronger signal and likely lower latency.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun TowerComparisonItem(tower: TowerInfo) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(tower.carrier, fontWeight = FontWeight.Bold)
                Text("Type: ${tower.type.name}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${tower.estimatedSpeedMbps.toInt()} Mbps", color = ElectricBlue, fontWeight = FontWeight.Bold)
                Text("Latency: 28ms", fontSize = 10.sp, color = Color.Gray) // Dummy latency for UI
            }
        }
    }
}
