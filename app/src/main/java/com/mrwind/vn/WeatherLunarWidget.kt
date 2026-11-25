package com.mrwind.vn

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
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
        if (intent.action == ACTION_REFRESH_WEATHER) {
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
    }

    companion object {
        const val ACTION_REFRESH_WEATHER = "com.mrwind.vn.ACTION_REFRESH_WEATHER"

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

            val clockSp = AppPrefs.clockSp(AppPrefs.getClockScale(context), showSeconds)
            val lunarSp = AppPrefs.lunarSp(AppPrefs.getLunarScale(context))
            val solarSp = AppPrefs.solarSp(AppPrefs.getSolarScale(context))
            val sigSp = AppPrefs.sigSp(AppPrefs.getSigScale(context))

            val bgMode = AppPrefs.getWidgetBgMode(context)
            when (bgMode) {
                0 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_glass_dark)
                1 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_glass_light)
                2 -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_transparent)
                3 -> {
                    val dynamicBg = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 24f * context.resources.displayMetrics.density
                        setColor(Color.argb(150, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)))
                        setStroke(
                            (1f * context.resources.displayMetrics.density).toInt(),
                            Color.argb(90, 255, 255, 255)
                        )
                    }
                    views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_widget_theme)
                    if (dynamicBg.alpha >= 0) {
                        views.setTextColor(R.id.tv_solar_date, Color.WHITE)
                    }
                }
            }

            val isLightGlass = (bgMode == 1)
            val primaryTextColor = if (isLightGlass) Color.parseColor("#111827") else Color.WHITE
            val secondaryTextColor = if (isLightGlass) Color.parseColor("#374151") else Color.parseColor("#E5E7EB")

            views.setTextViewText(R.id.tv_solar_date, LunarCalendar.formatVietnameseSolarDate(cal))
            views.setTextColor(R.id.tv_solar_date, primaryTextColor)
            views.setTextViewTextSize(R.id.tv_solar_date, TypedValue.COMPLEX_UNIT_SP, solarSp)

            views.setTextViewText(R.id.tv_lunar_date, lunar.shortSummary())
            views.setTextColor(R.id.tv_lunar_date, accentColor)
            views.setTextViewTextSize(R.id.tv_lunar_date, TypedValue.COMPLEX_UNIT_SP, lunarSp)

            val clockFormat = if (showSeconds) "HH:mm:ss" else "HH:mm"
            views.setCharSequence(R.id.tv_clock, "setFormat12Hour", clockFormat)
            views.setCharSequence(R.id.tv_clock, "setFormat24Hour", clockFormat)
            views.setTextColor(R.id.tv_clock, primaryTextColor)
            views.setTextViewTextSize(R.id.tv_clock, TypedValue.COMPLEX_UNIT_SP, clockSp)

            views.setTextViewText(R.id.tv_min_max_temp, "▲ ${weather.maxTemp}  ▼ ${weather.minTemp}")
            views.setTextColor(R.id.tv_min_max_temp, secondaryTextColor)

            views.setTextViewText(R.id.tv_hour_1, weather.hour1)
            views.setTextViewText(R.id.tv_hour_2, weather.hour2)
            views.setTextViewText(R.id.tv_hour_3, weather.hour3)
            views.setTextViewText(R.id.tv_hour_4, weather.hour4)
            listOf(R.id.tv_hour_1, R.id.tv_hour_2, R.id.tv_hour_3, R.id.tv_hour_4).forEach { id ->
                views.setTextColor(id, secondaryTextColor)
            }

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
            views.setTextViewText(R.id.tv_signature, signature)
            views.setTextColor(R.id.tv_signature, primaryTextColor)
            views.setTextViewTextSize(R.id.tv_signature, TypedValue.COMPLEX_UNIT_SP, sigSp)

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
