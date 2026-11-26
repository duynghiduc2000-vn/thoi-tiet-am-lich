package com.mrwind.vn

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class WeatherCache(
    val locationName: String = "Chưa có vị trí",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val currentTemp: String = "--°C",
    val weatherCode: Int = -1,
    val description: String = "Chạm nhiệt độ để cập nhật",
    val humidity: String = "--%",
    val windSpeed: String = "--km/h",
    val minTemp: String = "--°",
    val maxTemp: String = "--°",
    val lastUpdate: Long = 0L
)

data class BirthdayItem(
    val id: String,
    val name: String,
    val day: Int,
    val month: Int
)

data class LunarLookupHistoryItem(
    val id: String,
    val solarDay: Int,
    val solarMonth: Int,
    val solarYear: Int,
    val lunarSummary: String
)

data class AgeHistoryItem(
    val id: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val ageSummary: String
)

object AppPrefs {
    private const val PREF_NAME = "mrwind_weather_lunar_prefs"

    // 0: Theo hệ thống, 1: Sáng, 2: Tối
    private const val KEY_APP_THEME_MODE = "app_theme_mode"

    // 0: Kính lỏng tối, 1: Kính lỏng sáng, 2: Trong suốt, 3: Theo chủ đề app, 4: Liquid Glass
    private const val KEY_WIDGET_BG_MODE = "widget_bg_mode"

    private const val KEY_ACCENT_COLOR = "accent_color"
    private const val KEY_SHOW_SECONDS = "show_seconds"
    private const val KEY_SHOW_WEATHER_EFFECT = "show_weather_effect"
    private const val KEY_SHOW_SEVEN_DAY_FORECAST = "show_seven_day_forecast"
    private const val KEY_NEXT_SEVEN_DAYS_FORECAST = "next_seven_days_forecast"
    private const val KEY_SHOW_SIGNATURE = "show_signature"
    private const val KEY_SIGNATURE = "signature"

    // Kích cỡ chữ trên Widget (mức 1..20, mặc định 10)
    private const val KEY_CLOCK_SCALE = "clock_scale"
    private const val KEY_LUNAR_SCALE = "lunar_scale"
    private const val KEY_SOLAR_SCALE = "solar_scale"
    private const val KEY_SIG_SCALE = "sig_scale"

    private const val KEY_AUTO_LOCATION = "auto_location"

    private const val KEY_LOC_NAME = "loc_name"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"
    private const val KEY_TEMP = "temp"
    private const val KEY_CODE = "code"
    private const val KEY_DESC = "desc"
    private const val KEY_HUMIDITY = "humidity"
    private const val KEY_WIND = "wind"
    private const val KEY_MIN_TEMP = "min_temp"
    private const val KEY_MAX_TEMP = "max_temp"
    private const val KEY_LAST_UPDATE = "last_update"

    private const val KEY_BIRTHDAYS = "birthdays_json"
    private const val KEY_LAST_BDAY_NOTIFIED_DATE = "last_bday_notified_date"
    private const val KEY_LAST_BDAY_POPUP_DATE = "last_bday_popup_date"

    private const val KEY_HOLIDAY_REMINDER_ENABLED = "holiday_reminder_enabled"
    private const val KEY_ENABLED_HOLIDAY_IDS = "enabled_holiday_ids_json"
    private const val KEY_LAST_HOLIDAY_NOTIFIED_DATE = "last_holiday_notified_date"
    private const val KEY_LAST_HOLIDAY_ALERT_KEY = "last_holiday_alert_key"

    private const val KEY_LUNAR_HISTORY = "lunar_history_json"
    private const val KEY_AGE_HISTORY = "age_history_json"

    private const val DEFAULT_ACCENT = 0xFF4FC3F7.toInt()

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getAppThemeMode(context: Context): Int =
        prefs(context).getInt(KEY_APP_THEME_MODE, 0)

    fun setAppThemeMode(context: Context, mode: Int) {
        prefs(context).edit().putInt(KEY_APP_THEME_MODE, mode).apply()
    }

    fun getWidgetBgMode(context: Context): Int =
        prefs(context).getInt(KEY_WIDGET_BG_MODE, 0).coerceIn(0, 4)

    fun setWidgetBgMode(context: Context, mode: Int) {
        prefs(context).edit().putInt(KEY_WIDGET_BG_MODE, mode.coerceIn(0, 4)).apply()
    }

    fun getAccentColor(context: Context): Int =
        prefs(context).getInt(KEY_ACCENT_COLOR, DEFAULT_ACCENT)

    fun setAccentColor(context: Context, colorInt: Int) {
        prefs(context).edit().putInt(KEY_ACCENT_COLOR, colorInt).apply()
    }

    fun isShowSeconds(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_SECONDS, false)

    fun setShowSeconds(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_SECONDS, show).apply()
    }

    fun isShowWeatherEffect(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_WEATHER_EFFECT, true)

    fun setShowWeatherEffect(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_WEATHER_EFFECT, show).apply()
    }

    fun isShowSevenDayForecast(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_SEVEN_DAY_FORECAST, true)

    fun setShowSevenDayForecast(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_SEVEN_DAY_FORECAST, show).apply()
    }

    fun getNextSevenDaysForecast(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_NEXT_SEVEN_DAYS_FORECAST, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    add(arr.optString(i))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun setNextSevenDaysForecast(context: Context, items: List<String>) {
        val arr = JSONArray()
        items.take(7).forEach { arr.put(it) }
        prefs(context).edit().putString(KEY_NEXT_SEVEN_DAYS_FORECAST, arr.toString()).apply()
    }

    fun isShowSignature(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_SIGNATURE, true)

    fun setShowSignature(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_SIGNATURE, show).apply()
    }

    fun getSignature(context: Context): String =
        prefs(context).getString(KEY_SIGNATURE, "Gió Độc") ?: "Gió Độc"

    fun setSignature(context: Context, sig: String) {
        prefs(context).edit().putString(KEY_SIGNATURE, sig).apply()
    }

    private fun readScale(context: Context, key: String): Int =
        prefs(context).getInt(key, 10).coerceIn(1, 20)

    private fun writeScale(context: Context, key: String, value: Int) {
        prefs(context).edit().putInt(key, value.coerceIn(1, 20)).apply()
    }

    fun getClockScale(context: Context): Int = readScale(context, KEY_CLOCK_SCALE)
    fun setClockScale(context: Context, value: Int) = writeScale(context, KEY_CLOCK_SCALE, value)

    fun getLunarScale(context: Context): Int = readScale(context, KEY_LUNAR_SCALE)
    fun setLunarScale(context: Context, value: Int) = writeScale(context, KEY_LUNAR_SCALE, value)

    fun getSolarScale(context: Context): Int = readScale(context, KEY_SOLAR_SCALE)
    fun setSolarScale(context: Context, value: Int) = writeScale(context, KEY_SOLAR_SCALE, value)

    fun getSigScale(context: Context): Int = readScale(context, KEY_SIG_SCALE)
    fun setSigScale(context: Context, value: Int) = writeScale(context, KEY_SIG_SCALE, value)

    // Quy đổi mức 1..20 sang cỡ chữ sp/dp thực tế trên Widget
    // Tăng mức cơ bản khi bật giây lên 23.5f để dễ nhìn mà vẫn bớt lẹm cột phải
    fun clockSp(scale: Int, showSeconds: Boolean = false): Float {
        val s = scale.coerceIn(1, 20)
        val base = if (showSeconds) 23.5f else 26f
        val step = if (showSeconds) 1.05f else 1.3f
        return base + (s - 1) * step
    }

    fun lunarSp(scale: Int): Float = 8.5f + (scale.coerceIn(1, 20) - 1) * 0.45f
    fun solarSp(scale: Int): Float = 9.5f + (scale.coerceIn(1, 20) - 1) * 0.5f
    fun sigSp(scale: Int): Float = 8f + (scale.coerceIn(1, 20) - 1) * 0.4f

    fun isAutoLocation(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_LOCATION, true)

    fun setAutoLocation(context: Context, auto: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_LOCATION, auto).apply()
    }

    fun getWeather(context: Context): WeatherCache {
        val p = prefs(context)
        return WeatherCache(
            locationName = p.getString(KEY_LOC_NAME, "Chưa có vị trí") ?: "Chưa có vị trí",
            latitude = java.lang.Double.longBitsToDouble(p.getLong(KEY_LAT, 0L)),
            longitude = java.lang.Double.longBitsToDouble(p.getLong(KEY_LON, 0L)),
            currentTemp = p.getString(KEY_TEMP, "--°C") ?: "--°C",
            weatherCode = p.getInt(KEY_CODE, -1),
            description = p.getString(KEY_DESC, "Chạm nhiệt độ để cập nhật") ?: "Chạm nhiệt độ để cập nhật",
            humidity = p.getString(KEY_HUMIDITY, "--%") ?: "--%",
            windSpeed = p.getString(KEY_WIND, "--km/h") ?: "--km/h",
            minTemp = p.getString(KEY_MIN_TEMP, "--°") ?: "--°",
            maxTemp = p.getString(KEY_MAX_TEMP, "--°") ?: "--°",
            lastUpdate = p.getLong(KEY_LAST_UPDATE, 0L)
        )
    }

    fun saveWeather(context: Context, w: WeatherCache) {
        prefs(context).edit()
            .putString(KEY_LOC_NAME, w.locationName)
            .putLong(KEY_LAT, java.lang.Double.doubleToRawLongBits(w.latitude))
            .putLong(KEY_LON, java.lang.Double.doubleToRawLongBits(w.longitude))
            .putString(KEY_TEMP, w.currentTemp)
            .putInt(KEY_CODE, w.weatherCode)
            .putString(KEY_DESC, w.description)
            .putString(KEY_HUMIDITY, w.humidity)
            .putString(KEY_WIND, w.windSpeed)
            .putString(KEY_MIN_TEMP, w.minTemp)
            .putString(KEY_MAX_TEMP, w.maxTemp)
            .putLong(KEY_LAST_UPDATE, w.lastUpdate)
            .apply()
    }

    fun getBirthdays(context: Context): List<BirthdayItem> {
        val raw = prefs(context).getString(KEY_BIRTHDAYS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        BirthdayItem(
                            id = obj.optString("id"),
                            name = obj.optString("name"),
                            day = obj.optInt("day"),
                            month = obj.optInt("month")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveBirthdays(context: Context, list: List<BirthdayItem>) {
        val arr = JSONArray()
        list.take(10).forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("day", item.day)
                    put("month", item.month)
                }
            )
        }
        prefs(context).edit().putString(KEY_BIRTHDAYS, arr.toString()).apply()
    }

    fun getLastBirthdayNotifiedDate(context: Context): String? =
        prefs(context).getString(KEY_LAST_BDAY_NOTIFIED_DATE, null)

    fun setLastBirthdayNotifiedDate(context: Context, dateKey: String) {
        prefs(context).edit().putString(KEY_LAST_BDAY_NOTIFIED_DATE, dateKey).apply()
    }

    fun getLastBirthdayPopupDate(context: Context): String? =
        prefs(context).getString(KEY_LAST_BDAY_POPUP_DATE, null)

    fun setLastBirthdayPopupDate(context: Context, dateKey: String) {
        prefs(context).edit().putString(KEY_LAST_BDAY_POPUP_DATE, dateKey).apply()
    }

    fun isHolidayReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HOLIDAY_REMINDER_ENABLED, true)

    fun setHolidayReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HOLIDAY_REMINDER_ENABLED, enabled).apply()
    }

    fun getEnabledHolidayIds(context: Context): Set<String>? {
        val raw = prefs(context).getString(KEY_ENABLED_HOLIDAY_IDS, null) ?: return null
        return try {
            val arr = JSONArray(raw)
            buildSet {
                for (i in 0 until arr.length()) {
                    val id = arr.optString(i)
                    if (id.isNotBlank()) add(id)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun setEnabledHolidayIds(context: Context, ids: Set<String>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        prefs(context).edit().putString(KEY_ENABLED_HOLIDAY_IDS, arr.toString()).apply()
    }

    fun getLastHolidayNotifiedDate(context: Context): String? =
        prefs(context).getString(KEY_LAST_HOLIDAY_NOTIFIED_DATE, null)

    fun setLastHolidayNotifiedDate(context: Context, dateKey: String) {
        prefs(context).edit().putString(KEY_LAST_HOLIDAY_NOTIFIED_DATE, dateKey).apply()
    }

    fun getLastHolidayAlertKey(context: Context): String? =
        prefs(context).getString(KEY_LAST_HOLIDAY_ALERT_KEY, null)

    fun setLastHolidayAlertKey(context: Context, alertKey: String) {
        prefs(context).edit().putString(KEY_LAST_HOLIDAY_ALERT_KEY, alertKey).apply()
    }

    fun getLunarHistory(context: Context): List<LunarLookupHistoryItem> {
        val raw = prefs(context).getString(KEY_LUNAR_HISTORY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        LunarLookupHistoryItem(
                            id = obj.optString("id"),
                            solarDay = obj.optInt("solarDay"),
                            solarMonth = obj.optInt("solarMonth"),
                            solarYear = obj.optInt("solarYear"),
                            lunarSummary = obj.optString("lunarSummary")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveLunarHistory(context: Context, list: List<LunarLookupHistoryItem>) {
        val arr = JSONArray()
        list.take(20).forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("solarDay", item.solarDay)
                    put("solarMonth", item.solarMonth)
                    put("solarYear", item.solarYear)
                    put("lunarSummary", item.lunarSummary)
                }
            )
        }
        prefs(context).edit().putString(KEY_LUNAR_HISTORY, arr.toString()).apply()
    }

    fun clearLunarHistory(context: Context) {
        prefs(context).edit().remove(KEY_LUNAR_HISTORY).apply()
    }

    fun getAgeHistory(context: Context): List<AgeHistoryItem> {
        val raw = prefs(context).getString(KEY_AGE_HISTORY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        AgeHistoryItem(
                            id = obj.optString("id"),
                            birthDay = obj.optInt("birthDay"),
                            birthMonth = obj.optInt("birthMonth"),
                            birthYear = obj.optInt("birthYear"),
                            ageSummary = obj.optString("ageSummary")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveAgeHistory(context: Context, list: List<AgeHistoryItem>) {
        val arr = JSONArray()
        list.take(20).forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("birthDay", item.birthDay)
                    put("birthMonth", item.birthMonth)
                    put("birthYear", item.birthYear)
                    put("ageSummary", item.ageSummary)
                }
            )
        }
        prefs(context).edit().putString(KEY_AGE_HISTORY, arr.toString()).apply()
    }

    fun clearAgeHistory(context: Context) {
        prefs(context).edit().remove(KEY_AGE_HISTORY).apply()
    }
}
