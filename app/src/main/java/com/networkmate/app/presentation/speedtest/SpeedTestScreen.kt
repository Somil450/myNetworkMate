package com.networkmate.app.presentation.speedtest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.networkmate.app.core.theme.ElectricBlue
import com.networkmate.app.data.repository.SpeedResultState
import com.networkmate.app.presentation.dashboard.GlassCard

@Composable
fun SpeedTestScreen(viewModel: SpeedTestViewModel) {
    val testState by viewModel.testState.collectAsState()
    val currentSpeed by viewModel.currentSpeed.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0E21), Color(0xFF1A1A2E))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SPEED INTELLIGENCE",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElectricBlue,
                letterSpacing = 2.sp
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        Speedometer(speed = currentSpeed)

        Spacer(modifier = Modifier.height(32.dp))

        // Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem("PING", if (testState is SpeedResultState.LatencyMeasured) "${(testState as SpeedResultState.LatencyMeasured).ping} ms" else "--")
            MetricItem("JITTER", if (testState is SpeedResultState.LatencyMeasured) "${(testState as SpeedResultState.LatencyMeasured).jitter} ms" else "--")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (testState is SpeedResultState.Finished) {
            QualityScoresSection()
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.startSpeedTest() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {
            Text("START TEST", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
fun QualityScoresSection() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("NETWORK SUITABILITY", fontWeight = FontWeight.Bold, color = ElectricBlue)
            Spacer(modifier = Modifier.height(12.dp))
            QualityRow("Gaming", "Excellent")
            QualityRow("Streaming", "4K Ultra HD")
            QualityRow("Video Call", "Very Stable")
        }
    }
}

@Composable
fun QualityRow(label: String, score: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.7f))
        Text(score, color = ElectricBlue, fontWeight = FontWeight.SemiBold)
    }
}
