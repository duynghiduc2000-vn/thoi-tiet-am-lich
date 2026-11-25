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
import java.util.Calendar
import java.util.TimeZone

object BirthdayHelper {
    private const val CHANNEL_ID = "birthday_reminder_channel"
    private const val ADMIN_DAY = 31
    private const val ADMIN_MONTH = 12
    const val ADMIN_BIRTHDAY_MESSAGE =
        "Hôm nay là sinh nhật của admin Mr.Wind. Hãy cùng chúc mừng anh ấy nhé!"

    fun todayDateKey(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val y = cal.get(Calendar.YEAR)
        return "$y-$m-$d"
    }

    fun isAdminBirthdayToday(): Boolean {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        return day == ADMIN_DAY && month == ADMIN_MONTH
    }

    fun getTodayBirthdays(context: Context): List<BirthdayItem> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        return AppPrefs.getBirthdays(context).filter { it.day == day && it.month == month }
    }

    fun shouldShowAppPopupToday(context: Context): Boolean {
        val hasUserBirthdaysToday = getTodayBirthdays(context).isNotEmpty()
        val hasAdminPopupToday = isAdminBirthdayToday() &&
            AppPrefs.getLastAdminPopupDate(context) != todayDateKey()
        return hasUserBirthdaysToday || hasAdminPopupToday
    }

    fun markAppPopupShownToday(context: Context) {
        val key = todayDateKey()
        if (getTodayBirthdays(context).isNotEmpty()) {
            AppPrefs.setLastPopupDate(context, key)
        }
        if (isAdminBirthdayToday()) {
            AppPrefs.setLastAdminPopupDate(context, key)
        }
    }

    fun notifyTodayBirthdaysOncePerDay(context: Context) {
        val todayBirthdays = getTodayBirthdays(context)
        val isAdminToday = isAdminBirthdayToday()
        if (todayBirthdays.isEmpty() && !isAdminToday) return

        val key = todayDateKey()
        if (AppPrefs.getLastNotifyDate(context) == key) return

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
            2026,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            if (isAdminToday) {
                append(ADMIN_BIRTHDAY_MESSAGE)
            }
            if (todayBirthdays.isNotEmpty()) {
                if (isNotEmpty()) append(" • ")
                val names = todayBirthdays.joinToString(", ") { it.name }
                append("Sinh nhật hôm nay: $names.")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(if (isAdminToday && todayBirthdays.isEmpty()) "Chúc mừng sinh nhật Mr.Wind!" else "Nhắc ngày sinh nhật hôm nay")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
            AppPrefs.setLastNotifyDate(context, key)
        } catch (_: SecurityException) {
        }
    }

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc sinh nhật",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo nhắc ngày sinh nhật trong ngày"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
