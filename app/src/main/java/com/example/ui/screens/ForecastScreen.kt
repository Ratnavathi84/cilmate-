package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.TextOnDark
import com.example.ui.theme.SoftGreen

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.ClimateViewModel
import com.example.viewmodel.DashboardState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.vector.ImageVector

import com.example.ui.components.GlassCard

@Composable
fun ForecastScreen(viewModel: ClimateViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val bgRes = when (val state = uiState) {
            is DashboardState.Success -> getBackgroundForWeather(state.weather.current.weather_code)
            else -> com.example.R.drawable.nature_night_background_1781618169463
        }

        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = bgRes),
            contentDescription = "Nature Background",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Nature Timeline",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextOnDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            when (val state = uiState) {
                is DashboardState.Loading -> {
                    item {
                        CircularProgressIndicator(color = SoftGreen)
                    }
                }
                is DashboardState.Error -> {
                    item {
                        Text("Error loading data", color = TextOnDark, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is DashboardState.Success -> {
                    val daily = state.weather.daily
                    if (daily != null) {
                        val count = minOf(7, daily.time.size)
                        items(count) { index ->
                            ForecastItemDay(
                                dateStr = daily.time[index],
                                minTemp = daily.temperature_2m_min[index],
                                maxTemp = daily.temperature_2m_max[index],
                                weatherCode = daily.weather_code[index]
                            )
                        }
                    } else {
                        item {
                            Text("Daily forecast not available.", color = TextOnDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastItemDay(dateStr: String, minTemp: Double, maxTemp: Double, weatherCode: Int) {
    val dayName = try {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        if (date == LocalDate.now()) "Today" else date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        dateStr
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                dayName,
                style = MaterialTheme.typography.titleMedium,
                color = TextOnDark,
                modifier = Modifier.weight(1f)
            )
            Icon(
                getWeatherIcon(weatherCode),
                contentDescription = null,
                tint = TextOnDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                "${maxTemp}° / ${minTemp}°",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextOnDark
            )
        }
    }
}
