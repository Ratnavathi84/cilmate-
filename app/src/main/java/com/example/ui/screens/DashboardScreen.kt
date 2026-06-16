package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AirQualityResponse
import com.example.data.MarineResponse
import com.example.data.WeatherResponse
import com.example.ui.theme.*
import com.example.viewmodel.ClimateViewModel
import com.example.viewmodel.DashboardState
import com.example.viewmodel.LocationPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import com.example.ui.components.GlassCard

@Composable
fun DashboardScreen(viewModel: ClimateViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredLocations by viewModel.filteredLocations.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "CloudMovement")
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CloudOffset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic Background based on weather state
        val bgRes = when (val state = uiState) {
            is DashboardState.Success -> getBackgroundForWeather(state.weather.current.weather_code)
            else -> com.example.R.drawable.nature_night_background_1781618169463
        }

        Image(
            painter = painterResource(id = bgRes),
            contentDescription = "Nature Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Floating Clouds Overlay
        Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .size(300.dp)
                .offset(x = cloudOffset.dp - 100.dp, y = 100.dp)
                .graphicsLayer { alpha = 0.5f }
        )

        Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .size(200.dp)
                .offset(x = (cloudOffset * 0.5f).dp + 200.dp, y = 300.dp)
        )

        // Darker overlay for nature contrast
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            SearchBar(
                query = searchQuery,
                isSearching = isSearching,
                onQueryChange = viewModel::onSearchQueryChange,
                isSearchActive = isSearchActive,
                onSearchActiveChange = { isSearchActive = it },
                locations = filteredLocations,
                onLocationSelected = { loc ->
                    viewModel.loadDataForLocation(loc)
                    isSearchActive = false
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is DashboardState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is DashboardState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${state.message}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is DashboardState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            WeatherMainCard(state.currentLocation, state.weather)
                        }
                        item {
                            WeatherDetailsGrid(state.weather, state.aqi)
                        }
                        if (state.marine != null && state.marine.current != null) {
                            item {
                                CoastalFeaturesCard(state.marine)
                            }
                        }
                        item {
                            HourlyForecastScroll(state.weather)
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    locations: List<LocationPoint>,
    onLocationSelected: (LocationPoint) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                onSearchActiveChange(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            placeholder = { Text("Search any Visakhapatnam area...", color = TextOnDark.copy(alpha = 0.7f)) },
            leadingIcon = { 
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SoftGreen)
                } else {
                    Icon(Icons.Default.Eco, contentDescription = "Nature Search", tint = SoftGreen)
                }
            },
            trailingIcon = {
                if (isSearchActive && query.isNotEmpty()) {
                    IconButton(onClick = { 
                        onQueryChange("")
                        onSearchActiveChange(false)
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextOnDark)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SoftGreen,
                unfocusedBorderColor = TextOnDark.copy(alpha = 0.3f),
                focusedTextColor = TextOnDark,
                unfocusedTextColor = TextOnDark,
                cursorColor = SoftGreen
            ),
            shape = RoundedCornerShape(32.dp)
        )

        AnimatedVisibility(
            visible = isSearchActive && locations.isNotEmpty(),
            enter = fadeIn(spring()),
            exit = fadeOut(spring()),
            modifier = Modifier.padding(top = 64.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(locations) { loc ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationSelected(loc) }
                                .padding(12.dp)
                        ) {
                            Text(text = loc.name, color = TextOnDark, style = MaterialTheme.typography.titleMedium)
                            if (loc.admin1 != null || loc.country != null) {
                                Text(
                                    text = listOfNotNull(loc.admin1, loc.country).joinToString(", "),
                                    color = TextOnDark.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        HorizontalDivider(color = TextOnDark.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMainCard(location: LocationPoint, weather: WeatherResponse) {
    val current = weather.current
    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextOnDark)
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextOnDark
                    )
                    if (location.country != null) {
                        Text(
                            text = listOfNotNull(location.admin1, location.country).joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnDark.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${current.temperature_2m}",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 72.sp),
                    color = TextOnDark
                )
                Text(
                    text = "°C",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextOnDark,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            Text(
                text = getWeatherDescription(current.weather_code),
                style = MaterialTheme.typography.titleMedium,
                color = SoftGreen
            )
        }
    }
}

@Composable
fun WeatherDetailsGrid(weather: WeatherResponse, aqi: AirQualityResponse) {
    val current = weather.current
    val quality = aqi.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            DetailItem(
                title = "Feels Like",
                value = "${current.apparent_temperature}°C",
                icon = Icons.Outlined.DeviceThermostat,
                modifier = Modifier.weight(1f)
            )
            DetailItem(
                title = "Wind",
                value = "${current.wind_speed_10m} km/h",
                icon = Icons.Outlined.Air,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            DetailItem(
                title = "Humidity",
                value = "${current.relative_humidity_2m}%",
                icon = Icons.Outlined.WaterDrop,
                modifier = Modifier.weight(1f)
            )
            DetailItem(
                title = "AQI",
                value = "${quality.european_aqi?.toInt() ?: "--"}",
                icon = Icons.Outlined.Cloud,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            DetailItem(
                title = "Precipitation",
                value = "${current.precipitation} mm",
                icon = Icons.Outlined.Umbrella,
                modifier = Modifier.weight(1f)
            )
            DetailItem(
                title = "Pressure",
                value = "${current.surface_pressure ?: "--"} hPa",
                icon = Icons.Outlined.Speed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CoastalFeaturesCard(marine: MarineResponse) {
    val m = marine.current
    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Waves, contentDescription = null, tint = SoftGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Coastal conditions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "Wave Height", color = Color.White.copy(alpha = 0.7f))
                    Text(text = "${m?.wave_height ?: "--"} m", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = "Wave Period", color = Color.White.copy(alpha = 0.7f))
                    Text(text = "${m?.wave_period ?: "--"} s", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastScroll(weather: WeatherResponse) {
    if (weather.hourly == null) return
    Column {
        Text(
            text = "Today",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val count = minOf(24, weather.hourly.time.size)
            items(count) { index ->
                val timeRaw = weather.hourly.time[index]
                val hour = try {
                    LocalDateTime.parse(timeRaw, DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(DateTimeFormatter.ofPattern("HH:mm"))
                } catch (e: Exception) { timeRaw.takeLast(5) }
                
                GlassCard(
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = hour, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = getWeatherIcon(weather.hourly.weather_code[index]),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${weather.hourly.temperature_2m[index]}°", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SoftGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = TextOnDark.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                Text(text = value, color = TextOnDark, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        95 -> "Thunderstorm"
        else -> "Unknown"
    }
}

fun getWeatherIcon(code: Int): ImageVector {
    return when (code) {
        0 -> Icons.Outlined.WbSunny
        1, 2, 3 -> Icons.Outlined.Cloud
        45, 48 -> Icons.Outlined.FilterDrama
        51, 53, 55, 61, 63, 65 -> Icons.Outlined.WaterDrop
        95 -> Icons.Outlined.FlashOn
        else -> Icons.Outlined.WbSunny
    }
}

fun getBackgroundForWeather(code: Int): Int {
    return when (code) {
        0 -> com.example.R.drawable.nature_night_background_1781618169463
        1, 2, 3, 45, 48 -> com.example.R.drawable.nature_cloudy_background_1781618146758
        51, 53, 55, 61, 63, 65, 95 -> com.example.R.drawable.nature_rainy_background_1781618129000
        else -> com.example.R.drawable.nature_night_background_1781618169463
    }
}
