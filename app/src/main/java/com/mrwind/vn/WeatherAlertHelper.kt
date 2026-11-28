package com.mrwind.vn

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import java.util.Calendar
import java.util.TimeZone

enum class WeatherAlertType { RAIN, HEAT, UV }

data class WeatherAlertItem(
    val type: WeatherAlertType,
    val emoji: String,
    val title: String,
    val message: String
)

/** Một mốc dự báo theo giờ (giờ Việt Nam). */
data class HourlyPoint(
    val dateKey: String,   // yyyy-MM-dd
    val hour: Int,         // 0..23
    val temp: Double,
    val rainProb: Int,     // % khả năng mưa
    val precipMm: Double,  // lượng mưa (mm)
    val uv: Double,
    val weatherCode: Int
)

/**
 * Cảnh báo thời tiết thông minh: mưa theo giờ, nắng nóng, UV cao.
 * Dữ liệu theo giờ được WeatherService lưu vào AppPrefs mỗi lần đồng bộ.
 */
object WeatherAlertHelper {
    private const val CHANNEL_ID = "weather_alert_channel"
    private const val NOTIFICATION_ID = 1005
    private const val PENDING_INTENT_CODE = 2030

    // Ngưỡng cảnh báo (có thể chỉnh tại đây)
    private const val RAIN_PROB_THRESHOLD = 60      // % khả năng mưa
    private const val RAIN_MM_THRESHOLD = 0.5       // mm/giờ
    private const val HEAT_TEMP_THRESHOLD = 35.0    // °C
    private const val EXTREME_HEAT_THRESHOLD = 38.0 // °C
    private const val UV_THRESHOLD = 8.0            // UV "rất cao" trở lên
    private const val UV_EXTREME_THRESHOLD = 11.0

    // Khung giờ gửi thông báo buổi sáng (giờ Việt Nam): 05:00 - 11:59
    private const val MORNING_START_HOUR = 5
    private const val MORNING_END_HOUR = 12

    private fun vnCalendar(): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))

    fun parseHourly(context: Context): List<HourlyPoint> {
        val raw = AppPrefs.getHourlyForecastJson(context)
        val out = mutableListOf<HourlyPoint>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val t = o.optString("t", "")
                if (t.length < 13) continue
                val hour = t.substring(11, 13).toIntOrNull() ?: continue
                out.add(
                    HourlyPoint(
                        dateKey = t.substring(0, 10),
                        hour = hour,
                        temp = o.optDouble("temp", Double.NaN),
                        rainProb = o.optInt("pop", 0),
                        precipMm = o.optDouble("pr", 0.0).let { if (it.isNaN()) 0.0 else it },
                        uv = o.optDouble("uv", 0.0).let { if (it.isNaN()) 0.0 else it },
                        weatherCode = o.optInt("code", 0)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return out
    }

    /** "3 giờ chiều", "9 giờ sáng"... */
    private fun friendlyHour(h: Int): String = when (h) {
        in 0..10 -> "$h giờ sáng"
        in 11..12 -> "$h giờ trưa"
        in 13..17 -> "${h - 12} giờ chiều"
        in 18..21 -> "${h - 12} giờ tối"
        else -> "${h - 12} giờ đêm"
    }

    private fun isRainyHour(p: HourlyPoint): Boolean =
        p.rainProb >= RAIN_PROB_THRESHOLD ||
            p.precipMm >= RAIN_MM_THRESHOLD ||
            p.weatherCode in 95..99

    /** Danh sách cảnh báo cho phần còn lại của hôm nay (không kiểm tra công tắc bật/tắt). */
    fun getAlertsForToday(context: Context): List<WeatherAlertItem> {
        val today = BirthdayHelper.todayDateKey()
        val nowHour = vnCalendar().get(Calendar.HOUR_OF_DAY)
        val points = parseHourly(context).filter { it.dateKey == today }
        if (points.isEmpty()) return emptyList()

        val result = mutableListOf<WeatherAlertItem>()

        // 1) Mưa: xét từ giờ hiện tại (tối thiểu 5h sáng) đến 22h
        val rainFrom = maxOf(nowHour, 5)
        val rainy = points.filter { it.hour in rainFrom..22 && isRainyHour(it) }
        if (rainy.isNotEmpty()) {
            val first = rainy.first()
            val maxProb = rainy.maxOf { it.rainProb }
            val thunder = rainy.any { it.weatherCode in 95..99 }
            val probText = if (maxProb > 0) " (khả năng khoảng $maxProb%)" else ""
            val thunderText = if (thunder) " Có thể kèm dông, hạn chế đứng dưới gốc cây." else ""
            val msg = if (first.hour <= nowHour) {
                "Trời đang có khả năng mưa$probText, nhớ mang áo mưa hoặc ô nhé.$thunderText"
            } else {
                "Hôm nay có mưa sau ${friendlyHour(first.hour)}$probText, nhớ mang áo mưa nhé.$thunderText"
            }
            result.add(WeatherAlertItem(WeatherAlertType.RAIN, "☔", "Cảnh báo mưa", msg))
        }

        // 2) Nắng nóng: nhiệt độ cao nhất trong phần còn lại của khung 10h - 17h
        val heatFrom = maxOf(nowHour, 10)
        val heatPoints = points.filter { it.hour in heatFrom..17 && !it.temp.isNaN() }
        val hottest = heatPoints.maxByOrNull { it.temp }
        if (hottest != null && hottest.temp >= HEAT_TEMP_THRESHOLD) {
            val level = if (hottest.temp >= EXTREME_HEAT_THRESHOLD) "Nắng nóng gay gắt" else "Nắng nóng"
            val msg = "$level: nhiệt độ có thể lên tới ${Math.round(hottest.temp)}° vào khoảng " +
                "${friendlyHour(hottest.hour)}. Hạn chế ra nắng, nhớ uống đủ nước."
            result.add(WeatherAlertItem(WeatherAlertType.HEAT, "🌡️", "Cảnh báo nắng nóng", msg))
        }

        // 3) UV cao: xét khung 8h - 17h
        val uvFrom = maxOf(nowHour, 8)
        val uvHigh = points.filter { it.hour in uvFrom..17 && it.uv >= UV_THRESHOLD }
        if (uvHigh.isNotEmpty()) {
            val peak = uvHigh.maxOf { it.uv }
            val label = if (peak >= UV_EXTREME_THRESHOLD) "cực cao" else "rất cao"
            val from = uvHigh.first().hour
            val to = uvHigh.last().hour + 1
            val msg = "Chỉ số UV $label (${"%.0f".format(peak)}) khoảng ${from}h - ${to}h. " +
                "Nên đội nón, che chắn và bôi kem chống nắng."
            result.add(WeatherAlertItem(WeatherAlertType.UV, "🕶️", "Cảnh báo tia UV", msg))
        }

        return result
    }

    /** Danh sách cảnh báo hiệu lực: rỗng nếu người dùng đã tắt tính năng. */
    fun getActiveAlerts(context: Context): List<WeatherAlertItem> =
        if (AppPrefs.isWeatherAlertEnabled(context)) getAlertsForToday(context) else emptyList()

    private fun buildToken(alerts: List<WeatherAlertItem>): String {
        val types = alerts.map { it.type.name }.sorted().joinToString(",")
        return "${BirthdayHelper.todayDateKey()}|$types"
    }

    // ---------- Pop-up trong app: 1 lần/ngày cho mỗi bộ cảnh báo ----------

    fun shouldShowPopup(context: Context, alerts: List<WeatherAlertItem>): Boolean {
        if (!AppPrefs.isWeatherAlertEnabled(context) || alerts.isEmpty()) return false
        return AppPrefs.getLastWeatherAlertPopupKey(context) != buildToken(alerts)
    }

    fun markPopupShown(context: Context, alerts: List<WeatherAlertItem>) {
        if (alerts.isNotEmpty()) {
            AppPrefs.setLastWeatherAlertPopupKey(context, buildToken(alerts))
        }
    }

    // ---------- Thông báo hệ thống ----------

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cảnh báo thời tiết",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo buổi sáng khi hôm nay có mưa, nắng nóng hoặc UV cao"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun cancelNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        } catch (_: Exception) {
        }
    }

    /**
     * Gửi thông báo cảnh báo thời tiết tối đa 1 lần/ngày cho mỗi bộ cảnh báo.
     * @param ignoreMorningWindow true = bỏ qua khung giờ sáng (dùng khi người dùng vừa bật tính năng).
     */
    fun notifyAlertsOncePerDay(context: Context, ignoreMorningWindow: Boolean = false) {
        if (!AppPrefs.isWeatherAlertEnabled(context)) return

        if (!ignoreMorningWindow) {
            val hour = vnCalendar().get(Calendar.HOUR_OF_DAY)
            if (hour < MORNING_START_HOUR || hour >= MORNING_END_HOUR) return
        }

        val alerts = getAlertsForToday(context)
        if (alerts.isEmpty()) return

        val token = buildToken(alerts)
        if (AppPrefs.getLastWeatherAlertNotifyKey(context) == token) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        createChannelIfNeeded(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            PENDING_INTENT_CODE,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (alerts.size == 1) {
            "${alerts[0].emoji} ${alerts[0].title}"
        } else {
            "⚠️ Cảnh báo thời tiết hôm nay"
        }
        val bigText = alerts.joinToString("\n") { "${it.emoji} ${it.message}" }
        val shortText = alerts.joinToString(" • ") { it.message }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            AppPrefs.setLastWeatherAlertNotifyKey(context, token)
        } catch (_: SecurityException) {
        }
    }
}
