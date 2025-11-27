package com.example.walert.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("weather") val weather: List<WeatherDescription>,
    @SerialName("main") val main: MainStats,
    @SerialName("name") val cityName: String
)

@Serializable
data class WeatherDescription(
    @SerialName("main") val main: String,
    @SerialName("description") val description: String,
    @SerialName("icon") val icon: String
)

@Serializable
data class MainStats(
    @SerialName("temp") val temperature: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    @SerialName("pressure") val pressure: Int,
    @SerialName("humidity") val humidity: Int
)
