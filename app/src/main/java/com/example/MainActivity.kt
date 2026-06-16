package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EcoScreen
import com.example.ui.screens.ForecastScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.theme.MyApplicationTheme

enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    Dashboard("dashboard", "Home", Icons.Filled.Home),
    Forecast("forecast", "Forecast", Icons.Filled.Cloud),
    Eco("eco", "Eco", Icons.Filled.Eco),
    Community("community", "River", Icons.Filled.WaterDrop)
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        val navController = rememberNavController()

        Scaffold(
          bottomBar = {
            NavigationBar {
              val navBackStackEntry by navController.currentBackStackEntryAsState()
              val currentRoute = navBackStackEntry?.destination?.route

              val items = Screen.values()
              items.forEach { screen ->
                NavigationBarItem(
                  icon = { Icon(screen.icon, contentDescription = screen.title) },
                  label = { Text(screen.title) },
                  selected = currentRoute == screen.route,
                  onClick = {
                    navController.navigate(screen.route) {
                      popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                      }
                      launchSingleTop = true
                      restoreState = true
                    }
                  }
                )
              }
            }
          }
        ) { innerPadding ->
          NavHost(navController, startDestination = Screen.Dashboard.route, Modifier.padding(innerPadding)) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Forecast.route) { ForecastScreen() }
            composable(Screen.Eco.route) { EcoScreen() }
            composable(Screen.Community.route) { CommunityScreen() }
          }
        }
      }
    }
  }
}


