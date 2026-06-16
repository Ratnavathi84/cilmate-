package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AirQualityResponse
import com.example.data.ApiClient
import com.example.data.MarineResponse
import com.example.data.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.data.GeocodingResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

data class LocationPoint(
    val name: String,
    val lat: Double,
    val lon: Double,
    val isCoastal: Boolean = false,
    val country: String? = null,
    val admin1: String? = null
)

val VisakhapatnamLocations = listOf(
    LocationPoint("Visakhapatnam City", 17.6868, 83.2185, true, "India", "Andhra Pradesh"),
    LocationPoint("MVP Colony", 17.7441, 83.3361, true, "India", "Andhra Pradesh"),
    LocationPoint("Madhurawada", 17.8205, 83.3422, true, "India", "Andhra Pradesh"),
    LocationPoint("Gajuwaka", 17.6908, 83.1654, false, "India", "Andhra Pradesh"),
    LocationPoint("Dwaraka Nagar", 17.7280, 83.3039, false, "India", "Andhra Pradesh"),
    LocationPoint("Seethammadhara", 17.7420, 83.3150, false, "India", "Andhra Pradesh"),
    LocationPoint("Rushikonda", 17.7816, 83.3853, true, "India", "Andhra Pradesh"),
    LocationPoint("Bheemunipatnam", 17.8872, 83.4475, true, "India", "Andhra Pradesh"),
    LocationPoint("Arilova", 17.7656, 83.3370, false, "India", "Andhra Pradesh"),
    LocationPoint("Simhachalam", 17.7663, 83.2506, false, "India", "Andhra Pradesh"),
    LocationPoint("Pendurthi", 17.8083, 83.2008, false, "India", "Andhra Pradesh"),
    LocationPoint("Anakapalle", 17.6896, 83.0024, false, "India", "Andhra Pradesh"),
    LocationPoint("Steel Plant", 17.6334, 83.1661, true, "India", "Andhra Pradesh"),
    LocationPoint("Kurmannapalem", 17.6811, 83.1495, false, "India", "Andhra Pradesh"),
    LocationPoint("Muralinagar", 17.7495, 83.2750, false, "India", "Andhra Pradesh"),
    LocationPoint("Akkayyapalem", 17.7297, 83.2980, false, "India", "Andhra Pradesh"),
    LocationPoint("Kancharapalem", 17.7230, 83.2800, false, "India", "Andhra Pradesh"),
    LocationPoint("PM Palem", 17.8085, 83.3465, false, "India", "Andhra Pradesh"),
    LocationPoint("Yendada", 17.7785, 83.3590, true, "India", "Andhra Pradesh"),
    LocationPoint("Sagar Nagar", 17.7650, 83.3650, true, "India", "Andhra Pradesh"),
    LocationPoint("Gopalapatnam", 17.7554, 83.2198, false, "India", "Andhra Pradesh")
)

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val currentLocation: LocationPoint,
        val weather: WeatherResponse,
        val aqi: AirQualityResponse,
        val marine: MarineResponse?
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class ClimateViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filteredLocations = MutableStateFlow(VisakhapatnamLocations)
    val filteredLocations = _filteredLocations.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadDataForLocation(VisakhapatnamLocations.first())
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            _filteredLocations.value = VisakhapatnamLocations
            _isSearching.value = false
            return
        }

        // Local filtering + Remote search
        val locals = VisakhapatnamLocations.filter {
            it.name.contains(query, ignoreCase = true)
        }
        _filteredLocations.value = locals

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            _isSearching.value = true
            try {
                // If query is short and doesn't mention the city, try to bias it towards Visakhapatnam
                val effectiveQuery = if (query.length > 2 && !query.contains("Visakhapatnam", ignoreCase = true)) {
                    "$query Visakhapatnam"
                } else {
                    query
                }
                
                val results = ApiClient.geocodingApi.search(effectiveQuery).results
                val remotePoints = results?.map {
                    LocationPoint(it.name, it.latitude, it.longitude, false, it.country, it.admin1)
                } ?: emptyList()
                
                // Combine and unique by lat/lon
                val combined = (locals + remotePoints).distinctBy { "${it.lat},${it.lon}" }
                _filteredLocations.value = combined
            } catch (e: Exception) {
                // Keep locals if remote fails
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun loadDataForLocation(location: LocationPoint) {
        _uiState.value = DashboardState.Loading
        _searchQuery.value = ""
        _filteredLocations.value = VisakhapatnamLocations
        
        viewModelScope.launch {
            try {
                val weather = ApiClient.weatherApi.getWeather(location.lat, location.lon)
                val aqi = ApiClient.aqiApi.getAirQuality(location.lat, location.lon)
                var marine: MarineResponse? = null
                if (location.isCoastal) {
                    try {
                        marine = ApiClient.marineApi.getMarine(location.lat, location.lon)
                    } catch (e: Exception) {
                        // Ignore marine fetch errors if not fully supported by API for all coordinates
                    }
                }
                
                _uiState.value = DashboardState.Success(
                    currentLocation = location,
                    weather = weather,
                    aqi = aqi,
                    marine = marine
                )
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
