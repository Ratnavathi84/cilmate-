package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val current: CurrentWeather,
    val hourly: HourlyWeather?,
    val daily: DailyWeather?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val temperature_2m: Double,
    val relative_humidity_2m: Double,
    val apparent_temperature: Double,
    val precipitation: Double,
    val weather_code: Int,
    val wind_speed_10m: Double,
    val wind_direction_10m: Double?,
    val surface_pressure: Double?,
    val cloud_cover: Double?
)

@JsonClass(generateAdapter = true)
data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>,
    val uv_index_max: List<Double>
)

@JsonClass(generateAdapter = true)
data class AirQualityResponse(
    val current: CurrentAirQuality
)

@JsonClass(generateAdapter = true)
data class CurrentAirQuality(
    val european_aqi: Double?,
    val pm10: Double?,
    val pm2_5: Double?,
    val uv_index: Double?
)

@JsonClass(generateAdapter = true)
data class MarineResponse(
    val current: CurrentMarine?
)

@JsonClass(generateAdapter = true)
data class CurrentMarine(
    val wave_height: Double?,
    val wave_period: Double?
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?
)

interface OpenMeteoApi {
    @GET("v1/forecast?current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m,surface_pressure,cloud_cover&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max&timezone=auto")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): WeatherResponse
}

interface OpenMeteoAqiApi {
    @GET("v1/air-quality?current=european_aqi,pm10,pm2_5,uv_index")
    suspend fun getAirQuality(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): AirQualityResponse
}

interface OpenMeteoMarineApi {
    @GET("v1/marine?current=wave_height,wave_period")
    suspend fun getMarine(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): MarineResponse
}

interface OpenMeteoGeocodingApi {
    @GET("v1/search?count=10&language=en&format=json")
    suspend fun search(
        @Query("name") name: String
    ): GeocodingResponse
}

object ApiClient {
    val geocodingApi: OpenMeteoGeocodingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenMeteoGeocodingApi::class.java)
    }

    val weatherApi: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }

    val aqiApi: OpenMeteoAqiApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenMeteoAqiApi::class.java)
    }

    val marineApi: OpenMeteoMarineApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://marine-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenMeteoMarineApi::class.java)
    }
}
