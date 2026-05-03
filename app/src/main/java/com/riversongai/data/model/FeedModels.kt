package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class NewsArticle(
    val title: String,
    val url: String,
    val source: String = "",
    @SerializedName("published_at") val publishedAt: String? = null,
    val summary: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class WeatherCurrent(
    @SerializedName("temp_c") val tempC: Float = 0f,
    @SerializedName("temp_f") val tempF: Float = 0f,
    @SerializedName("condition_text") val conditionText: String = "",
    val humidity: Int = 0,
    @SerializedName("wind_kph") val windKph: Float = 0f,
    @SerializedName("feelslike_c") val feelsLikeC: Float = 0f
)

data class WeatherData(
    val current: WeatherCurrent = WeatherCurrent(),
    val forecast: List<Any?> = emptyList(),
    val location: Any? = null
)

data class StockQuote(
    val symbol: String,
    val name: String? = null,
    val price: Float = 0f,
    val change: Float = 0f,
    @SerializedName("change_percent") val changePercent: Float = 0f,
    val currency: String? = null
)

data class SportsEvent(
    @SerializedName("home_team") val homeTeam: String = "",
    @SerializedName("away_team") val awayTeam: String = "",
    @SerializedName("home_score") val homeScore: Int? = null,
    @SerializedName("away_score") val awayScore: Int? = null,
    val status: String = "",
    val date: String? = null,
    val competition: String? = null
)

data class FeedPreferences(
    @SerializedName("news_sources") val newsSources: List<String> = emptyList(),
    @SerializedName("weather_lat") val weatherLat: Double? = null,
    @SerializedName("weather_lon") val weatherLon: Double? = null,
    @SerializedName("weather_unit") val weatherUnit: String = "celsius",
    @SerializedName("sport_teams") val sportTeams: List<String> = emptyList(),
    @SerializedName("stock_tickers") val stockTickers: List<String> = emptyList()
)
