package com.mrwind.vn

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BirthdayHelper {
    private const val CHANNEL_ID = "birthday_reminders_channel"

    fun todayKey(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }
        return fmt.format(Date())
    }

    fun getTodayBirthdays(context: Context): List<BirthdayItem> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        return AppPrefs.getBirthdays(context).filter { it.day == day && it.month == month }
    }

    fun shouldShowAppPopupToday(context: Context): Boolean {
        val todays = getTodayBirthdays(context)
        if (todays.isEmpty()) return false
        return AppPrefs.getLastPopupDate(context) != todayKey()
    }

    fun markAppPopupShownToday(context: Context) {
        AppPrefs.setLastPopupDate(context, todayKey())
    }

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc sinh nhật",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc ngày sinh nhật trong danh sách"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun notifyTodayBirthdaysOncePerDay(context: Context) {
        val todays = getTodayBirthdays(context)
        if (todays.isEmpty()) return

        val key = todayKey()
        if (AppPrefs.getLastNotifyDate(context) == key) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureNotificationChannel(context)

        val names = todays.joinToString(", ") { it.name }
        val title = "Hôm nay là sinh nhật của $names!"
        val content = "Nhớ gửi lời chúc mừng sinh nhật tới ${names} nhé."

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2026,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
            AppPrefs.setLastNotifyDate(context, key)
        } catch (_: SecurityException) {
        }
    }
}

class BirthdayBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            WeatherService.scheduleTwoHourlySync(context)
            BirthdayHelper.notifyTodayBirthdaysOncePerDay(context)
            WeatherLunarWidget.updateAllWidgets(context)
        }
    }
}
