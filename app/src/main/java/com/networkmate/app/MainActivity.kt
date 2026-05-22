package com.networkmate.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.networkmate.app.core.theme.NetworkMateTheme
import com.networkmate.app.presentation.dashboard.DashboardScreen
import com.networkmate.app.presentation.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.networkmate.app.core.theme.ElectricBlue
import com.networkmate.app.presentation.navigation.AppNavigation
import com.networkmate.app.presentation.navigation.Screen
import com.networkmate.app.presentation.speedtest.SpeedTestViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            NetworkMateTheme {
                val navController = rememberNavController()
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                val speedTestViewModel: SpeedTestViewModel = hiltViewModel()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(Unit) {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE
                        )
                    )
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = Color(0xFF0A0E21)) {
                            listOf(Screen.Dashboard, Screen.Map, Screen.SpeedTest).forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ElectricBlue,
                                        selectedTextColor = ElectricBlue,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
                        AppNavigation(navController, dashboardViewModel, speedTestViewModel)
                    }
                }
            }
        }
    }
}
