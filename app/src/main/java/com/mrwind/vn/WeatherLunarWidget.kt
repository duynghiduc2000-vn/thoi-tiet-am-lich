package com.mrwind.vn

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.AlarmClock
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class WeatherLunarWidget : AppWidgetProvider() {

    private data class DayChipData(
        val dayLabel: String,
        val icon: String
    )

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
        BirthdayHelper.notifyTodayBirthdaysOncePerDay(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH_WEATHER -> {
                val pendingResult = goAsync()
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                scope.launch {
                    try {
                        WeatherService.syncWeather(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_OPEN_CLOCK -> {
                openSystemClockApp(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WEATHER = "com.mrwind.vn.ACTION_REFRESH_WEATHER"
        const val ACTION_OPEN_CLOCK = "com.mrwind.vn.ACTION_OPEN_CLOCK"
        private const val TARGET_CLOCK_PACKAGE = "com.android.deskclock"

        private fun formatShortLocationName(rawLocation: String): String {
            val trimmed = rawLocation.trim()
            if (trimmed.isEmpty()) return ""
            val beforeComma = trimmed.substringBefore(",").trim()
            return beforeComma.ifEmpty { trimmed }
        }

        private fun lockedSpToPx(context: Context, spValue: Float): Float {
            return spValue * context.resources.displayMetrics.density
        }

        private fun launchPackageIfAvailable(context: Context, packageName: String): Boolean {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return try {
                context.startActivity(launchIntent)
                true
            } catch (_: Exception) {
                false
            }
        }

        private fun openSystemClockApp(context: Context) {
            if (launchPackageIfAvailable(context, TARGET_CLOCK_PACKAGE)) return

            val candidates = listOf(
                Intent(AlarmClock.ACTION_SHOW_ALARMS),
                Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CLOCK")
            )
            for (candidate in candidates) {
                candidate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val resolved = candidate.resolveActivity(context.packageManager)
                if (resolved != null) {
                    try {
                        context.startActivity(candidate)
                        return
                    } catch (_: Exception) {
                    }
                }
            }

            val fallbackPackages = listOf(
                "com.google.android.deskclock",
                "com.miui.clock",
                "com.sec.android.app.clockpackage",
                "com.coloros.alarmclock",
                "com.oneplus.deskclock",
                "com.vivo.clock",
                "com.huawei.deskclock"
            )
            for (pkg in fallbackPackages) {
                if (launchPackageIfAvailable(context, pkg)) return
            }
        }

        fun requestPinWidget(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
            val manager = context.getSystemService(AppWidgetManager::class.java) ?: return false
            if (!manager.isRequestPinAppWidgetSupported) return false

            val component = ComponentName(context, WeatherLunarWidget::class.java)
            val callbackIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val successCallback = PendingIntent.getActivity(
                context,
                3030,
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return manager.requestPinAppWidget(component, null, successCallback)
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, WeatherLunarWidget::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                for (id in ids) {
                    updateSingleWidget(context, manager, id)
                }
            }
        }

        private fun weatherEmojiForCode(code: Int): String = when (code) {
            0 -> "☀️"
            1, 2 -> "⛅"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55, 56, 57 -> "🌦️"
            61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️"
            71, 73, 75, 77, 85, 86 -> "❄️"
            95, 96, 99 -> "⛈️"
            else -> "🌤️"
        }

        private fun shortWeekdayLabel(calendar: Calendar): String = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "T2"
            Calendar.TUESDAY -> "T3"
            Calendar.WEDNESDAY -> "T4"
            Calendar.THURSDAY -> "T5"
            Calendar.FRIDAY -> "T6"
            Calendar.SATURDAY -> "T7"
            Calendar.SUNDAY -> "CN"
            else -> ""
        }

        private fun buildFallbackSevenDayChips(baseCal: Calendar, fallbackCode: Int): List<DayChipData> {
            val icon = weatherEmojiForCode(fallbackCode)
            return (1..7).map { offset ->
                val c = (baseCal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, offset)
                }
                DayChipData(
                    dayLabel = shortWeekdayLabel(c),
                    icon = icon
                )
            }
        }

        private fun parseChipString(raw: String, fallback: DayChipData): DayChipData {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return fallback
            val parts = trimmed.split("|")
            if (parts.size >= 2) {
                return DayChipData(
                    dayLabel = parts[0].trim().ifEmpty { fallback.dayLabel },
                    icon = parts[1].trim().ifEmpty { fallback.icon }
                )
            }
            val tokens = trimmed.split(" ").filter { it.isNotBlank() }
            if (tokens.size >= 2) {
                return DayChipData(
                    dayLabel = tokens[0],
                    icon = tokens[1]
                )
            }
            return fallback
        }

        private fun resolveSevenDayForecastChips(
            context: Context,
            weather: WeatherCache,
            baseCal: Calendar
        ): List<DayChipData> {
            val fallback = buildFallbackSevenDayChips(baseCal, weather.weatherCode)
            val rawList = AppPrefs.getNextSevenDaysForecast(context)
            if (rawList.isEmpty()) return fallback
            return (0..6).map { idx ->
                val raw = rawList.getOrNull(idx) ?: return@map fallback[idx]
                parseChipString(raw, fallback[idx])
            }
        }

        private fun weatherEffectDrawableForCode(code: Int): Int = when (code) {
            0 -> R.drawable.fx_weather_clear
            1, 2 -> R.drawable.fx_weather_partly_cloudy
            3 -> R.drawable.fx_weather_cloudy
            45, 48 -> R.drawable.fx_weather_fog
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> R.drawable.fx_weather_rain
            71, 73, 75, 77, 85, 86 -> R.drawable.fx_weather_snow
            95, 96, 99 -> R.drawable.fx_weather_storm
            else -> R.drawable.fx_weather_partly_cloudy
        }

        fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather_lunar)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
            val lunar = LunarCalendar.todayLunar()
            val weather = AppPrefs.getWeather(context)
            val accentColor = AppPrefs.getAccentColor(context)
            val showSeconds = AppPrefs.isShowSeconds(context)
            val showWeatherEffect = AppPrefs.isShowWeatherEffect(context)
            val showLunarDate = AppPrefs.isShowLunarDate(context)
            val showWeatherInfo = AppPrefs.isShowWeatherInfo(context)
            val showSolarDate = AppPrefs.isShowSolarDate(context)
            val showLocationName = AppPrefs.isShowLocationName(context)
            val showSevenDayForecast = AppPrefs.isShowSevenDayForecast(context)
            val showSignature = AppPrefs.isShowSignature(context)

            val clockPx = lockedSpToPx(context, AppPrefs.clockSp(AppPrefs.getClockScale(context), showSeconds))
            val lunarPx = lockedSpToPx(context, AppPrefs.lunarSp(AppPrefs.getLunarScale(context)))
            val solarPx = lockedSpToPx(context, AppPrefs.solarSp(AppPrefs.getSolarScale(context)))
            val sigPx = lockedSpToPx(context, AppPrefs.sigSp(AppPrefs.getSigScale(context)))

            val bgMode = AppPrefs.getWidgetBgMode(context)
            when (bgMode) {
                0 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_glass_dark)
                1 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_glass_light)
                2 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_transparent)
                3 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_theme)
                4 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_liquid_glass)
                else -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_glass_dark)
            }

            views.setViewVisibility(
                R.id.iv_weather_effect,
                if (showWeatherEffect) View.VISIBLE else View.GONE
            )
            views.setImageViewResource(R.id.iv_weather_effect, weatherEffectDrawableForCode(weather.weatherCode))

            val isLightGlass = (bgMode == 1)
            val primaryTextColor = if (isLightGlass) Color.parseColor("#111827") else Color.WHITE
            val secondaryTextColor = if (isLightGlass) Color.parseColor("#374151") else Color.parseColor("#E5E7EB")

            views.setViewVisibility(R.id.tv_lunar_date, if (showLunarDate) View.VISIBLE else View.GONE)
            views.setTextViewText(R.id.tv_lunar_date, lunar.shortSummary())
            views.setTextColor(R.id.tv_lunar_date, accentColor)
            views.setTextViewTextSize(R.id.tv_lunar_date, TypedValue.COMPLEX_UNIT_PX, lunarPx)

            views.setViewVisibility(R.id.tv_solar_date, if (showSolarDate) View.VISIBLE else View.GONE)
            views.setTextViewText(R.id.tv_solar_date, LunarCalendar.formatVietnameseSolarDate(cal))
            views.setTextColor(R.id.tv_solar_date, primaryTextColor)
            views.setTextViewTextSize(R.id.tv_solar_date, TypedValue.COMPLEX_UNIT_PX, solarPx)

            val clockFormat = if (showSeconds) "HH:mm:ss" else "HH:mm"
            views.setCharSequence(R.id.tv_clock, "setFormat12Hour", clockFormat)
            views.setCharSequence(R.id.tv_clock, "setFormat24Hour", clockFormat)
            views.setTextColor(R.id.tv_clock, primaryTextColor)
            views.setTextViewTextSize(R.id.tv_clock, TypedValue.COMPLEX_UNIT_PX, clockPx)

            views.setTextViewText(R.id.tv_min_max_temp, "▲ ${weather.maxTemp}  ▼ ${weather.minTemp}")
            views.setTextColor(R.id.tv_min_max_temp, accentColor)

            val sevenDayVisibility = if (showSevenDayForecast) View.VISIBLE else View.GONE
            views.setViewVisibility(R.id.forecast_7days_row, sevenDayVisibility)

            val cardIds = listOf(
                R.id.card_forecast_day_1,
                R.id.card_forecast_day_2,
                R.id.card_forecast_day_3,
                R.id.card_forecast_day_4,
                R.id.card_forecast_day_5,
                R.id.card_forecast_day_6,
                R.id.card_forecast_day_7
            )
            val labelIds = listOf(
                R.id.tv_forecast_day_1_label,
                R.id.tv_forecast_day_2_label,
                R.id.tv_forecast_day_3_label,
                R.id.tv_forecast_day_4_label,
                R.id.tv_forecast_day_5_label,
                R.id.tv_forecast_day_6_label,
                R.id.tv_forecast_day_7_label
            )
            val iconIds = listOf(
                R.id.tv_forecast_day_1_icon,
                R.id.tv_forecast_day_2_icon,
                R.id.tv_forecast_day_3_icon,
                R.id.tv_forecast_day_4_icon,
                R.id.tv_forecast_day_5_icon,
                R.id.tv_forecast_day_6_icon,
                R.id.tv_forecast_day_7_icon
            )

            if (showSevenDayForecast) {
                val sevenDayChips = resolveSevenDayForecastChips(context, weather, cal)
                for (i in 0..6) {
                    val chip = sevenDayChips[i]
                    views.setViewVisibility(cardIds[i], View.VISIBLE)
                    views.setViewVisibility(labelIds[i], View.VISIBLE)
                    views.setViewVisibility(iconIds[i], View.VISIBLE)
                    views.setTextViewText(labelIds[i], chip.dayLabel)
                    views.setTextColor(labelIds[i], secondaryTextColor)
                    views.setTextViewText(iconIds[i], chip.icon)
                }
            } else {
                for (i in 0..6) {
                    views.setViewVisibility(cardIds[i], View.GONE)
                    views.setViewVisibility(labelIds[i], View.GONE)
                    views.setViewVisibility(iconIds[i], View.GONE)
                }
            }

            val weatherVisibility = if (showWeatherInfo) View.VISIBLE else View.GONE
            views.setViewVisibility(R.id.btn_refresh_weather, weatherVisibility)
            views.setViewVisibility(R.id.right_column, weatherVisibility)
            views.setTextViewText(R.id.tv_weather_emoji, weatherEmojiForCode(weather.weatherCode))
            views.setTextViewText(R.id.tv_current_temp, weather.currentTemp)
            views.setTextColor(R.id.tv_current_temp, accentColor)

            views.setViewVisibility(R.id.tv_location_name, if (showLocationName) View.VISIBLE else View.GONE)
            views.setTextViewText(R.id.tv_location_name, formatShortLocationName(weather.locationName))
            views.setTextColor(R.id.tv_location_name, primaryTextColor)
            views.setTextViewTextSize(R.id.tv_location_name, TypedValue.COMPLEX_UNIT_PX, solarPx)

            views.setTextViewText(R.id.tv_weather_desc, weather.description)
            views.setTextColor(R.id.tv_weather_desc, secondaryTextColor)

            views.setTextViewText(
                R.id.tv_humidity_wind,
                "💧 ${weather.humidity} • 💨 ${weather.windSpeed}"
            )
            views.setTextColor(R.id.tv_humidity_wind, secondaryTextColor)

            val signature = AppPrefs.getSignature(context).ifBlank { "Gió Độc" }
            views.setViewVisibility(R.id.tv_signature, if (showSignature) View.VISIBLE else View.GONE)
            views.setTextViewText(R.id.tv_signature, signature)
            views.setTextColor(R.id.tv_signature, accentColor)
            views.setTextViewTextSize(R.id.tv_signature, TypedValue.COMPLEX_UNIT_PX, sigPx)

            val clockIntent = Intent(context, WeatherLunarWidget::class.java).apply {
                action = ACTION_OPEN_CLOCK
            }
            val clockPending = PendingIntent.getBroadcast(
                context,
                appWidgetId + 50,
                clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_clock, clockPending)

            val refreshIntent = Intent(context, WeatherLunarWidget::class.java).apply {
                action = ACTION_REFRESH_WEATHER
            }
            val refreshPending = PendingIntent.getBroadcast(
                context,
                appWidgetId + 100,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_refresh_weather, refreshPending)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPending = PendingIntent.getActivity(
                context,
                appWidgetId + 200,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
