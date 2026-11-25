package com.mrwind.vn

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.math.roundToInt

object WeatherService {

    data class SearchLocationResult(
        val displayName: String,
        val lat: Double,
        val lon: Double
    )

    suspend fun searchLocationsByName(query: String): List<SearchLocationResult> =
        withContext(Dispatchers.IO) {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) return@withContext emptyList()
            try {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                val urlStr =
                    "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=8&language=vi&format=json"
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                }
                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    conn.disconnect()
                    return@withContext emptyList()
                }
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val root = JSONObject(body)
                val arr = root.optJSONArray("results") ?: return@withContext emptyList()
                val out = mutableListOf<SearchLocationResult>()
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    val name = item.optString("name", "").trim()
                    if (name.isEmpty()) continue
                    val admin2 = item.optString("admin2", "").trim().takeIf { it.isNotEmpty() }
                    val admin1 = item.optString("admin1", "").trim().takeIf { it.isNotEmpty() }
                    val country = item.optString("country", "").trim().takeIf { it.isNotEmpty() }
                    val parts = listOfNotNull(
                        name,
                        admin2?.takeIf { it != name },
                        admin1?.takeIf { it != name && it != admin2 },
                        country?.takeIf { it != "Việt Nam" && it != "Vietnam" }
                    )
                    val lat = item.optDouble("latitude", Double.NaN)
                    val lon = item.optDouble("longitude", Double.NaN)
                    if (!lat.isNaN() && !lon.isNaN()) {
                        out.add(
                            SearchLocationResult(
                                displayName = parts.joinToString(", "),
                                lat = lat,
                                lon = lon
                            )
                        )
                    }
                }
                out
            } catch (_: Exception) {
                emptyList()
            }
        }

    suspend fun syncWeather(context: Context): WeatherCache = withContext(Dispatchers.IO) {
        val currentCache = AppPrefs.getWeather(context)
        var lat = currentCache.latitude
        var lon = currentCache.longitude
        var locName = currentCache.locationName

        if (AppPrefs.isAutoLocation(context)) {
            val deviceLoc = getBestDeviceLocation(context)
            if (deviceLoc != null) {
                lat = deviceLoc.latitude
                lon = deviceLoc.longitude
                locName = resolveLocationName(context, lat, lon) ?: locName.ifBlank { "Vị trí hiện tại" }
            }
        }

        val fetched = fetchOpenMeteo(
            lat = lat,
            lon = lon,
            locationName = locName
        )

        if (fetched != null) {
            AppPrefs.saveWeather(context, fetched)
            WeatherLunarWidget.updateAllWidgets(context)
            fetched
        } else {
            currentCache
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getBestDeviceLocation(context: Context): Location? {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return null

        val client = LocationServices.getFusedLocationProviderClient(context)

        val lastLoc = suspendCancellableCoroutine<Location?> { cont ->
            client.lastLocation
                .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        }
        if (lastLoc != null) return lastLoc

        val cts = CancellationTokenSource()
        return suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { cts.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveLocationName(context: Context, lat: Double, lon: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale("vi", "VN"))
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val addr = addresses?.firstOrNull() ?: return null
            val subAdmin = addr.subAdminArea
            val admin = addr.adminArea
            val locality = addr.locality
            listOfNotNull(
                locality?.takeIf { it.isNotBlank() },
                subAdmin?.takeIf { it.isNotBlank() && it != locality },
                admin?.takeIf { it.isNotBlank() }
            ).take(2).joinToString(", ").ifBlank { addr.featureName }
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchOpenMeteo(lat: Double, lon: Double, locationName: String): WeatherCache? {
        return try {
            val urlStr =
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m" +
                    "&hourly=temperature_2m,weather_code" +
                    "&daily=temperature_2m_max,temperature_2m_min" +
                    "&timezone=Asia%2FBangkok&forecast_days=2"

            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }

            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return null
            }

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val root = JSONObject(body)
            val current = root.optJSONObject("current") ?: return null
            val daily = root.optJSONObject("daily")
            val hourly = root.optJSONObject("hourly")

            val tempNow = current.optDouble("temperature_2m", Double.NaN)
            val humidityNow = current.optInt("relative_humidity_2m", -1)
            val windNow = current.optDouble("wind_speed_10m", Double.NaN)
            val wCode = current.optInt("weather_code", 0)

            val maxArr = daily?.optJSONArray("temperature_2m_max")
            val minArr = daily?.optJSONArray("temperature_2m_min")
            val maxToday = maxArr?.optDouble(0, Double.NaN) ?: Double.NaN
            val minToday = minArr?.optDouble(0, Double.NaN) ?: Double.NaN

            val slots = extractNextFourHours(hourly)

            WeatherCache(
                locationName = locationName,
                latitude = lat,
                longitude = lon,
                currentTemp = if (tempNow.isNaN()) "--°C" else "${tempNow.roundToInt()}°C",
                maxTemp = if (maxToday.isNaN()) "--°" else "${maxToday.roundToInt()}°",
                minTemp = if (minToday.isNaN()) "--°" else "${minToday.roundToInt()}°",
                description = describeWeatherCode(wCode),
                humidity = if (humidityNow >= 0) "$humidityNow%" else "--%",
                windSpeed = if (windNow.isNaN()) "--km/h" else "${windNow.roundToInt()}km/h",
                weatherCode = wCode,
                hour1 = slots.getOrElse(0) { "--:-- --°" },
                hour2 = slots.getOrElse(1) { "--:-- --°" },
                hour3 = slots.getOrElse(2) { "--:-- --°" },
                hour4 = slots.getOrElse(3) { "--:-- --°" },
                lastUpdated = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun extractNextFourHours(hourly: JSONObject?): List<String> {
        if (hourly == null) return emptyList()
        val times = hourly.optJSONArray("time") ?: return emptyList()
        val temps = hourly.optJSONArray("temperature_2m") ?: return emptyList()

        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:00", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        }
        val nowSlot = fmt.format(Date())

        var startIndex = 0
        for (i in 0 until times.length()) {
            val t = times.optString(i, "")
            if (t >= nowSlot) {
                startIndex = i
                break
            }
        }

        val result = mutableListOf<String>()
        var idx = startIndex
        while (idx < times.length() && result.size < 4) {
            val tStr = times.optString(idx, "")
            val tempVal = temps.optDouble(idx, Double.NaN)
            val hhMm = if (tStr.length >= 16) tStr.substring(11, 16) else "--:--"
            val tempText = if (tempVal.isNaN()) "--°" else "${tempVal.roundToInt()}°"
            result.add("$hhMm $tempText")
            idx += 2
        }
        return result
    }

    fun describeWeatherCode(code: Int): String = when (code) {
        0 -> "Trời quang"
        1 -> "Ít mây"
        2 -> "Mây rải rác"
        3 -> "Nhiều mây"
        45, 48 -> "Có sương mù"
        51, 53, 55 -> "Mưa phùn"
        56, 57 -> "Mưa phùn lạnh"
        61, 63, 65 -> "Có mưa"
        66, 67 -> "Mưa lạnh"
        71, 73, 75, 77 -> "Tuyết / Mưa đá"
        80, 81, 82 -> "Mưa rào"
        85, 86 -> "Mưa rào tuyết"
        95 -> "Có dông"
        96, 99 -> "Dông kèm mưa đá"
        else -> "Thời tiết ổn định"
    }

    fun scheduleTwoHourlySync(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(2, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weather_two_hourly_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}

class WeatherSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        WeatherService.syncWeather(applicationContext)
        return Result.success()
    }
}
