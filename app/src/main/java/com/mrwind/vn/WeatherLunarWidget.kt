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
            val showSignature = AppPrefs.isShowSignature(context)

            val clockSp = AppPrefs.clockSp(AppPrefs.getClockScale(context), showSeconds)
            val lunarSp = AppPrefs.lunarSp(AppPrefs.getLunarScale(context))
            val solarSp = AppPrefs.solarSp(AppPrefs.getSolarScale(context))
            val sigSp = AppPrefs.sigSp(AppPrefs.getSigScale(context))

            val bgMode = AppPrefs.getWidgetBgMode(context)
            when (bgMode) {
                0 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_glass_dark)
                1 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_glass_light)
                2 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_transparent)
                3 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_theme)
            }

            views.setViewVisibility(
                R.id.iv_weather_effect,
                if (showWeatherEffect) View.VISIBLE else View.GONE
            )
            views.setImageViewResource(R.id.iv_weather_effect, weatherEffectDrawableForCode(weather.weatherCode))

            val isLightGlass = (bgMode == 1)
            val primaryTextColor = if (isLightGlass) Color.parseColor("#111827") else Color.WHITE
            val secondaryTextColor = if (isLightGlass) Color.parseColor("#374151") else Color.parseColor("#E5E7EB")

            views.setTextViewText(R.id.tv_lunar_date, lunar.shortSummary())
            views.setTextColor(R.id.tv_lunar_date, accentColor)
            views.setTextViewTextSize(R.id.tv_lunar_date, TypedValue.COMPLEX_UNIT_SP, lunarSp)

            views.setTextViewText(R.id.tv_solar_date, LunarCalendar.formatVietnameseSolarDate(cal))
            views.setTextColor(R.id.tv_solar_date, primaryTextColor)
            views.setTextViewTextSize(R.id.tv_solar_date, TypedValue.COMPLEX_UNIT_SP, solarSp)

            val clockFormat = if (showSeconds) "HH:mm:ss" else "HH:mm"
            views.setCharSequence(R.id.tv_clock, "setFormat12Hour", clockFormat)
            views.setCharSequence(R.id.tv_clock, "setFormat24Hour", clockFormat)
            views.setTextColor(R.id.tv_clock, primaryTextColor)
            views.setTextViewTextSize(R.id.tv_clock, TypedValue.COMPLEX_UNIT_SP, clockSp)

            views.setTextViewText(R.id.tv_min_max_temp, "▲ ${weather.maxTemp}  ▼ ${weather.minTemp}")
            views.setTextColor(R.id.tv_min_max_temp, accentColor)

            views.setTextViewText(R.id.tv_hour_1, weather.hour1)
            views.setTextViewText(R.id.tv_hour_2, weather.hour2)
            views.setTextViewText(R.id.tv_hour_3, weather.hour3)
            views.setTextViewText(R.id.tv_hour_4, weather.hour4)
            listOf(R.id.tv_hour_1, R.id.tv_hour_2, R.id.tv_hour_3, R.id.tv_hour_4).forEach { id ->
                views.setTextColor(id, secondaryTextColor)
            }

            views.setTextViewText(R.id.tv_weather_emoji, weatherEmojiForCode(weather.weatherCode))
            views.setTextViewText(R.id.tv_current_temp, weather.currentTemp)
            views.setTextColor(R.id.tv_current_temp, accentColor)

            views.setTextViewText(R.id.tv_location_name, weather.locationName)
            views.setTextColor(R.id.tv_location_name, primaryTextColor)

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
            views.setTextViewTextSize(R.id.tv_signature, TypedValue.COMPLEX_UNIT_SP, sigSp)

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
