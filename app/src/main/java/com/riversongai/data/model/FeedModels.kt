package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class NewsArticle(
    val title: String,
    val url: String,
    val source: String = "",
    val category: String = "all",
    @SerializedName("published_at") val publishedAt: String? = null,
    val summary: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class WeatherCurrent(
    val temperature: Float = 0f,
    @SerializedName("feels_like") val feelsLike: Float = 0f,
    val condition: String = "",
    val humidity: Int = 0,
    @SerializedName("wind_speed") val windSpeed: Float = 0f,
    @SerializedName("wind_gusts") val windGusts: Float = 0f,
    @SerializedName("uv_index") val uvIndex: Float = 0f,
    val visibility: Float = 0f,
    val unit: String = "°C"
)

data class WeatherData(
    val current: WeatherCurrent = WeatherCurrent(),
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyForecast> = emptyList(),
    val alerts: List<WeatherAlert> = emptyList(),
    @SerializedName("air_quality") val airQuality: AirQuality? = null,
    @SerializedName("location_name") val locationName: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class AirQuality(
    val aqi: Float = 0f,
    val label: String = "",
    val color: String = "#00cc44",
    @SerializedName("pm2_5") val pm25: Float = 0f,
    val pm10: Float = 0f,
    val ozone: Float = 0f,
    @SerializedName("nitrogen_dioxide") val nitrogenDioxide: Float = 0f,
    @SerializedName("carbon_monoxide") val carbonMonoxide: Float = 0f
)

data class HourlyForecast(
    val time: String,
    val temperature: Float = 0f,
    val condition: String = "",
    @SerializedName("precip_prob") val precipProb: Float = 0f
)

data class DailyForecast(
    val date: String,
    @SerializedName("temp_max") val tempMax: Float = 0f,
    @SerializedName("temp_min") val tempMin: Float = 0f,
    val condition: String = "",
    @SerializedName("uv_index_max") val uvIndexMax: Float = 0f,
    val sunrise: String = "",
    val sunset: String = "",
    @SerializedName("precipitation") val precipitation: Float = 0f
)

data class WeatherAlert(
    val headline: String,
    val severity: String,
    val description: String
)

data class SportsTeam(
    val id: String,
    val name: String,
    @SerializedName("league_id") val leagueId: String = "",
    @SerializedName("league_name") val leagueName: String = "",
    val abbr: String = "",
    val logo: String? = null
)

data class SportsLeague(
    val id: String,
    val name: String,
    val abbr: String = ""
)

data class StandingEntry(
    @SerializedName("team_id") val teamId: String = "",
    val team: String = "",
    val abbr: String = "",
    val rank: Int = 0,
    val played: Int = 0,
    val win: Int = 0,
    val draw: Int = 0,
    val loss: Int = 0,
    @SerializedName("goals_for") val goalsFor: Int = 0,
    @SerializedName("goals_against") val goalsAgainst: Int = 0,
    @SerializedName("goal_diff") val goalDiff: Int = 0,
    val points: Int = 0,
    val form: String = "",
    @SerializedName("badge_url") val badgeUrl: String? = null,
    val stats: Map<String, String> = emptyMap()
)

data class SportsEventStat(
    val label: String,
    val home: String,
    val away: String
)

data class SportsMatch(
    val id: String,
    val league: String = "",
    val date: String = "",
    val time: String? = null,
    @SerializedName("home_team") val homeTeam: String,
    @SerializedName("away_team") val awayTeam: String,
    @SerializedName("home_score") val homeScore: Int?,
    @SerializedName("away_score") val awayScore: Int?,
    @SerializedName("home_badge") val homeBadge: String? = null,
    @SerializedName("away_badge") val awayBadge: String? = null,
    val finished: Boolean = false,
    val status: String = ""
)

data class StockQuote(
    val ticker: String = "",
    val name: String? = null,
    val price: Float = 0f,
    val change: Float = 0f,
    @SerializedName("change_pct") val changePct: Float = 0f,
    val high: String = "",
    val low: String = "",
    val open: String = "",
    @SerializedName("prev_close") val prevClose: String = "",
    val volume: String = "",
    val up: Boolean = true,
    val source: String = ""
)

data class StockChartEntry(
    val date: String,
    val close: Float,
    val high: Float,
    val low: Float,
    val volume: Long
)

data class StockSearchMatch(
    val ticker: String,
    val name: String,
    val type: String,
    val region: String,
    val currency: String
)

data class FeedPreferences(
    @SerializedName("news_sources") val newsSources: List<Map<String, String>> = emptyList(),
    @SerializedName("weather_lat") val weatherLat: Double? = null,
    @SerializedName("weather_lon") val weatherLon: Double? = null,
    @SerializedName("weather_unit") val weatherUnit: String = "celsius",
    @SerializedName("sport_teams") val sportTeams: List<String> = emptyList(),
    @SerializedName("stock_tickers") val stockTickers: List<String> = emptyList(),
    @SerializedName("refresh_news_min") val refreshNewsMins: Int = 30,
    @SerializedName("refresh_weather_min") val refreshWeatherMin: Int = 30,
    @SerializedName("refresh_sports_min") val refreshSportsMin: Int = 60,
    @SerializedName("refresh_stocks_min") val refreshStocksMin: Int = 60
)

data class GoogleAuthUrlResponse(
    @SerializedName("auth_url") val authUrl: String
)

data class CalendarResponse(
    val events: List<CalendarEvent> = emptyList()
)

data class CalendarEvent(
    val id: String,
    val summary: String,
    val start: String,
    val end: String,
    val location: String? = null
)

data class GmailResponse(
    val messages: List<GmailMessage> = emptyList(),
    @SerializedName("unread_count") val unreadCount: Int = 0
)

data class GmailMessage(
    val id: String,
    val threadId: String,
    val subject: String,
    val from: String,
    val date: String,
    val snippet: String
)

data class GoogleStatus(
    val connected: Boolean = false,
    val email: String? = null
)
