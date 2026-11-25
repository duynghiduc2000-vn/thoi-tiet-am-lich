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

    // Giao diện App: 0 = Theo hệ thống, 1 = Sáng, 2 = Tối
    fun getAppThemeMode(context: Context): Int =
        prefs(context).getInt("app_theme_mode", 0)

    fun setAppThemeMode(context: Context, mode: Int) {
        prefs(context).edit().putInt("app_theme_mode", mode).apply()
    }

    // Nền Widget: 0 = Kính lỏng tối, 1 = Kính lỏng sáng, 2 = Trong suốt, 3 = Theo chủ đề app
    fun getWidgetBgMode(context: Context): Int =
        prefs(context).getInt("widget_bg_mode", 0)

    fun setWidgetBgMode(context: Context, mode: Int) {
        prefs(context).edit().putInt("widget_bg_mode", mode).apply()
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

    // Hiển thị chữ ký trên Widget
    fun isShowSignature(context: Context): Boolean =
        prefs(context).getBoolean("show_signature", true)

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
        val base = if (showSeconds) 20f else 26f
        val step = if (showSeconds) 0.9f else 1.3f
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

    fun getLastNotifyDate(context: Context): String =
        prefs(context).getString("last_notify_date", "") ?: ""

    fun setLastNotifyDate(context: Context, dateStr: String) {
        prefs(context).edit().putString("last_notify_date", dateStr).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
