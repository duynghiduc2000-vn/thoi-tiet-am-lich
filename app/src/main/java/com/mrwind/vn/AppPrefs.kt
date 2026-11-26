package com.mrwind.vn

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

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
    val lunarSummary: String,
    val searchedAt: Long = System.currentTimeMillis()
)

data class AgeHistoryItem(
    val id: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val ageSummary: String,
    val calculatedAt: Long = System.currentTimeMillis()
)

data class WeatherCache(
    val locationName: String = "Chưa có vị trí",
    val latitude: Double = 11.15,
    val longitude: Double = 107.75,
    val currentTemp: String = "--°C",
    val maxTemp: String = "--°",
    val minTemp: String = "--°",
    val description: String = "Chạm nhiệt độ để cập nhật",
    val humidity: String = "--%",
    val windSpeed: String = "--km/h",
    val weatherCode: Int = 0,
    val hour1: String = "--:-- --°",
    val hour2: String = "--:-- --°",
    val hour3: String = "--:-- --°",
    val hour4: String = "--:-- --°",
    val lastUpdated: Long = 0L
)

object AppPrefs {
    private const val PREF_NAME = "thoi_tiet_am_lich_prefs"
    private const val KEY_NEXT_SEVEN_DAYS = "widget_next_seven_days_json"
    private const val KEY_SHOW_SEVEN_DAYS = "show_seven_day_forecast"

    // Giao diện App: 0 = Theo hệ thống, 1 = Sáng, 2 = Tối
    fun getAppThemeMode(context: Context): Int =
        prefs(context).getInt("app_theme_mode", 0)

    fun setAppThemeMode(context: Context, mode: Int) {
        prefs(context).edit().putInt("app_theme_mode", mode).apply()
    }

    // Nền Widget: 0 = Kính lỏng tối, 1 = Kính lỏng sáng, 2 = Trong suốt, 3 = Theo chủ đề app, 4 = Liquid Glass
    fun getWidgetBgMode(context: Context): Int =
        prefs(context).getInt("widget_bg_mode", 0).coerceIn(0, 4)

    fun setWidgetBgMode(context: Context, mode: Int) {
        prefs(context).edit().putInt("widget_bg_mode", mode.coerceIn(0, 4)).apply()
    }

    // Màu chủ đề App & Widget (lưu dạng Int ARGB)
    fun getAccentColor(context: Context): Int =
        prefs(context).getInt("accent_color", 0xFF4FC3F7.toInt())

    fun setAccentColor(context: Context, color: Int) {
        prefs(context).edit().putInt("accent_color", color).apply()
    }

    // Hiển thị giây chạy liên tục trên đồng hồ Widget
    fun isShowSeconds(context: Context): Boolean =
        prefs(context).getBoolean("show_seconds", false)

    fun setShowSeconds(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean("show_seconds", show).apply()
    }

    // Hiển thị lớp hiệu ứng thời tiết trên Widget
    fun isShowWeatherEffect(context: Context): Boolean =
        prefs(context).getBoolean("show_weather_effect", false)

    fun setShowWeatherEffect(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean("show_weather_effect", show).apply()
    }

    // Hiển thị dự báo 7 ngày trên Widget
    fun isShowSevenDayForecast(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_SEVEN_DAYS, true)

    fun setShowSevenDayForecast(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_SEVEN_DAYS, show).apply()
    }

    // Hiển thị chữ ký trên Widget
    fun isShowSignature(context: Context): Boolean =
        prefs(context).getBoolean("show_signature", false)

    fun setShowSignature(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean("show_signature", show).apply()
    }

    // Chữ ký cá nhân
    fun getSignature(context: Context): String =
        prefs(context).getString("signature", "Gió Độc") ?: "Gió Độc"

    fun setSignature(context: Context, sig: String) {
        prefs(context).edit().putString("signature", sig).apply()
    }

    // Thanh trượt cỡ chữ từ 1 đến 20
    fun getClockScale(context: Context): Int = prefs(context).getInt("scale_clock", 10)
    fun setClockScale(context: Context, v: Int) = prefs(context).edit().putInt("scale_clock", v.coerceIn(1, 20)).apply()

    fun getLunarScale(context: Context): Int = prefs(context).getInt("scale_lunar", 10)
    fun setLunarScale(context: Context, v: Int) = prefs(context).edit().putInt("scale_lunar", v.coerceIn(1, 20)).apply()

    fun getSolarScale(context: Context): Int = prefs(context).getInt("scale_solar", 10)
    fun setSolarScale(context: Context, v: Int) = prefs(context).edit().putInt("scale_solar", v.coerceIn(1, 20)).apply()

    fun getSigScale(context: Context): Int = prefs(context).getInt("scale_sig", 10)
    fun setSigScale(context: Context, v: Int) = prefs(context).edit().putInt("scale_sig", v.coerceIn(1, 20)).apply()

    // Quy đổi mức 1..20 sang cỡ chữ sp thực tế trên Widget
    fun clockSp(scale: Int, showSeconds: Boolean = false): Float {
        val base = if (showSeconds) 23.5f else 26f
        val step = if (showSeconds) 1.05f else 1.3f
        return base + (scale - 1) * step
    }

    fun lunarSp(scale: Int): Float = 8.5f + (scale - 1) * 0.35f
    fun solarSp(scale: Int): Float = 11f + (scale - 1) * 0.5f
    fun sigSp(scale: Int): Float = 10f + (scale - 1) * 0.45f

    // Chế độ vị trí: true = tự động GPS, false = chọn địa điểm thủ công
    fun isAutoLocation(context: Context): Boolean =
        prefs(context).getBoolean("auto_location", true)

    fun setAutoLocation(context: Context, auto: Boolean) {
        prefs(context).edit().putBoolean("auto_location", auto).apply()
    }

    fun saveNextSevenDaysForecast(context: Context, items: List<String>) {
        val arr = JSONArray()
        items.take(7).forEach { arr.put(it) }
        prefs(context).edit().putString(KEY_NEXT_SEVEN_DAYS, arr.toString()).apply()
    }

    fun getNextSevenDaysForecast(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_NEXT_SEVEN_DAYS, "[]") ?: "[]"
        val out = mutableListOf<String>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val s = arr.optString(i, "").trim()
                if (s.isNotEmpty()) out.add(s)
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun saveWeather(context: Context, w: WeatherCache) {
        prefs(context).edit()
            .putString("w_loc", w.locationName)
            .putFloat("w_lat", w.latitude.toFloat())
            .putFloat("w_lon", w.longitude.toFloat())
            .putString("w_temp", w.currentTemp)
            .putString("w_max", w.maxTemp)
            .putString("w_min", w.minTemp)
            .putString("w_desc", w.description)
            .putString("w_hum", w.humidity)
            .putString("w_wind", w.windSpeed)
            .putInt("w_code", w.weatherCode)
            .putString("w_h1", w.hour1)
            .putString("w_h2", w.hour2)
            .putString("w_h3", w.hour3)
            .putString("w_h4", w.hour4)
            .putLong("w_updated", w.lastUpdated)
            .apply()
    }

    fun getWeather(context: Context): WeatherCache {
        val p = prefs(context)
        return WeatherCache(
            locationName = p.getString("w_loc", "Chưa có vị trí") ?: "Chưa có vị trí",
            latitude = p.getFloat("w_lat", 11.15f).toDouble(),
            longitude = p.getFloat("w_lon", 107.75f).toDouble(),
            currentTemp = p.getString("w_temp", "--°C") ?: "--°C",
            maxTemp = p.getString("w_max", "--°") ?: "--°",
            minTemp = p.getString("w_min", "--°") ?: "--°",
            description = p.getString("w_desc", "Chạm nhiệt độ để cập nhật") ?: "",
            humidity = p.getString("w_hum", "--%") ?: "--%",
            windSpeed = p.getString("w_wind", "--km/h") ?: "--km/h",
            weatherCode = p.getInt("w_code", 0),
            hour1 = p.getString("w_h1", "--:-- --°") ?: "--:-- --°",
            hour2 = p.getString("w_h2", "--:-- --°") ?: "--:-- --°",
            hour3 = p.getString("w_h3", "--:-- --°") ?: "--:-- --°",
            hour4 = p.getString("w_h4", "--:-- --°") ?: "--:-- --°",
            lastUpdated = p.getLong("w_updated", 0L)
        )
    }

    fun getLunarHistory(context: Context): List<LunarLookupHistoryItem> {
        val raw = prefs(context).getString("lunar_lookup_history_json", "[]") ?: "[]"
        val list = mutableListOf<LunarLookupHistoryItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    LunarLookupHistoryItem(
                        id = obj.optString("id"),
                        solarDay = obj.optInt("solarDay", 1),
                        solarMonth = obj.optInt("solarMonth", 1),
                        solarYear = obj.optInt("solarYear", 2026),
                        lunarSummary = obj.optString("lunarSummary", ""),
                        searchedAt = obj.optLong("searchedAt", 0L)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveLunarHistory(context: Context, list: List<LunarLookupHistoryItem>) {
        val arr = JSONArray()
        list.take(20).forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("solarDay", item.solarDay)
            obj.put("solarMonth", item.solarMonth)
            obj.put("solarYear", item.solarYear)
            obj.put("lunarSummary", item.lunarSummary)
            obj.put("searchedAt", item.searchedAt)
            arr.put(obj)
        }
        prefs(context).edit().putString("lunar_lookup_history_json", arr.toString()).apply()
    }

    fun clearLunarHistory(context: Context) {
        prefs(context).edit().remove("lunar_lookup_history_json").apply()
    }

    fun getAgeHistory(context: Context): List<AgeHistoryItem> {
        val raw = prefs(context).getString("age_history_json", "[]") ?: "[]"
        val list = mutableListOf<AgeHistoryItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AgeHistoryItem(
                        id = obj.optString("id"),
                        birthDay = obj.optInt("birthDay", 1),
                        birthMonth = obj.optInt("birthMonth", 1),
                        birthYear = obj.optInt("birthYear", 2000),
                        ageSummary = obj.optString("ageSummary", ""),
                        calculatedAt = obj.optLong("calculatedAt", 0L)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveAgeHistory(context: Context, list: List<AgeHistoryItem>) {
        val arr = JSONArray()
        list.take(20).forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("birthDay", item.birthDay)
            obj.put("birthMonth", item.birthMonth)
            obj.put("birthYear", item.birthYear)
            obj.put("ageSummary", item.ageSummary)
            obj.put("calculatedAt", item.calculatedAt)
            arr.put(obj)
        }
        prefs(context).edit().putString("age_history_json", arr.toString()).apply()
    }

    fun clearAgeHistory(context: Context) {
        prefs(context).edit().remove("age_history_json").apply()
    }

    fun getBirthdays(context: Context): List<BirthdayItem> {
        val raw = prefs(context).getString("birthdays_json", "[]") ?: "[]"
        val list = mutableListOf<BirthdayItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    BirthdayItem(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        day = obj.optInt("day", 1),
                        month = obj.optInt("month", 1)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveBirthdays(context: Context, list: List<BirthdayItem>) {
        val arr = JSONArray()
        list.take(10).forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("day", item.day)
            obj.put("month", item.month)
            arr.put(obj)
        }
        prefs(context).edit().putString("birthdays_json", arr.toString()).apply()
    }

    fun getLastPopupDate(context: Context): String =
        prefs(context).getString("last_popup_date", "") ?: ""

    fun setLastPopupDate(context: Context, dateStr: String) {
        prefs(context).edit().putString("last_popup_date", dateStr).apply()
    }

    fun getLastAdminPopupDate(context: Context): String =
        prefs(context).getString("last_admin_popup_date", "") ?: ""

    fun setLastAdminPopupDate(context: Context, dateStr: String) {
        prefs(context).edit().putString("last_admin_popup_date", dateStr).apply()
    }

    fun getLastNotifyDate(context: Context): String =
        prefs(context).getString("last_notify_date", "") ?: ""

    fun setLastNotifyDate(context: Context, dateStr: String) {
        prefs(context).edit().putString("last_notify_date", dateStr).apply()
    }

    // Nhắc ngày lễ / kỷ niệm Việt Nam & quốc tế tại Việt Nam
    fun isHolidayReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean("holiday_reminder_enabled", true)

    fun setHolidayReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("holiday_reminder_enabled", enabled).apply()
    }

    fun getEnabledHolidayIds(context: Context): Set<String>? {
        if (!prefs(context).contains("enabled_holiday_ids_json")) return null
        val raw = prefs(context).getString("enabled_holiday_ids_json", "[]") ?: "[]"
        val set = mutableSetOf<String>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val id = arr.optString(i, "")
                if (id.isNotBlank()) set.add(id)
            }
        } catch (_: Exception) {
            return null
        }
        return set
    }

    fun setEnabledHolidayIds(context: Context, ids: Set<String>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        prefs(context).edit().putString("enabled_holiday_ids_json", arr.toString()).apply()
    }

    fun getLastHolidayAlertKey(context: Context): String =
        prefs(context).getString("last_holiday_alert_key", "") ?: ""

    fun setLastHolidayAlertKey(context: Context, key: String) {
        prefs(context).edit().putString("last_holiday_alert_key", key).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
