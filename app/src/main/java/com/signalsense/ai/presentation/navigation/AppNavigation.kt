package com.signalsense.ai.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.signalsense.ai.presentation.dashboard.DashboardScreen
import com.signalsense.ai.presentation.dashboard.DashboardViewModel
import com.signalsense.ai.presentation.map.MapScreen
import com.signalsense.ai.presentation.speedtest.SpeedTestScreen
import com.signalsense.ai.presentation.speedtest.SpeedTestViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Map : Screen("map", "Map", Icons.Default.Map)
    object SpeedTest : Screen("speedtest", "Speed", Icons.Default.Speed)
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    speedTestViewModel: SpeedTestViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(dashboardViewModel)
        }
        composable(Screen.Map.route) {
            MapScreen()
        }
        composable(Screen.SpeedTest.route) {
            SpeedTestScreen(speedTestViewModel)
        }
    }
}
