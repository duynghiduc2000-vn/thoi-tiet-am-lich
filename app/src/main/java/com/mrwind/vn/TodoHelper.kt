package com.mrwind.vn

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class TodoTaskItem(
    val id: String,
    val hour: Int,
    val minute: Int,
    val title: String,
    val isDone: Boolean = false,
    val triggeredToday: Boolean = false
) {
    val timeFormatted: String
        get() = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

object TodoHelper {
    private const val PREF_TODO = "mrwind_todo_prefs"
    private const val KEY_DATE = "todo_date_key"
    private const val KEY_TASKS = "todo_tasks_json"
    private const val KEY_PENDING_POPUP_ID = "todo_pending_popup_id"
    private const val CHANNEL_ID = "mrwind_todo_alarm_channel"
    private const val RESET_REQUEST_CODE = 8800

    const val MAX_TASKS_PER_DAY = 10
    const val ACTION_TRIGGER_TASK = "com.mrwind.vn.ACTION_TRIGGER_TASK"
    const val ACTION_DAILY_RESET = "com.mrwind.vn.ACTION_DAILY_RESET"
    const val EXTRA_TASK_ID = "extra_task_id"

    private fun tz(): TimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")

    fun todayKey(cal: Calendar = Calendar.getInstance(tz())): String {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_TODO, Context.MODE_PRIVATE)

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    fun ensureCurrentDayOrReset(context: Context): List<TodoTaskItem> {
        val currentKey = todayKey()
        val savedKey = prefs(context).getString(KEY_DATE, "") ?: ""
        if (savedKey != currentKey) {
            prefs(context).edit()
                .putString(KEY_DATE, currentKey)
                .putString(KEY_TASKS, "[]")
                .remove(KEY_PENDING_POPUP_ID)
                .apply()
            scheduleMidnightReset(context)
            return emptyList()
        }
        return loadRawTasks(context)
    }

    private fun loadRawTasks(context: Context): List<TodoTaskItem> {
        val raw = prefs(context).getString(KEY_TASKS, "[]") ?: "[]"
        val out = mutableListOf<TodoTaskItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                out.add(
                    TodoTaskItem(
                        id = obj.optString("id"),
                        hour = obj.optInt("hour", 8),
                        minute = obj.optInt("minute", 0),
                        title = obj.optString("title", ""),
                        isDone = obj.optBoolean("isDone", false),
                        triggeredToday = obj.optBoolean("triggeredToday", false)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun saveTasks(context: Context, tasks: List<TodoTaskItem>) {
        val arr = JSONArray()
        tasks.take(MAX_TASKS_PER_DAY).forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("hour", item.hour)
            obj.put("minute", item.minute)
            obj.put("title", item.title)
            obj.put("isDone", item.isDone)
            obj.put("triggeredToday", item.triggeredToday)
            arr.put(obj)
        }
        prefs(context).edit()
            .putString(KEY_DATE, todayKey())
            .putString(KEY_TASKS, arr.toString())
            .apply()
    }

    fun scheduleAllTodayAlarms(context: Context, tasks: List<TodoTaskItem>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nowMillis = System.currentTimeMillis()
        val cal = Calendar.getInstance(tz())

        tasks.forEach { task ->
            val intent = Intent(context, TodoAlarmReceiver::class.java).apply {
                action = ACTION_TRIGGER_TASK
                putExtra(EXTRA_TASK_ID, task.id)
            }
            val pi = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val target = (cal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, task.hour)
                set(Calendar.MINUTE, task.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (target.timeInMillis > nowMillis && !task.triggeredToday && !task.isDone) {
                setAlarmClockSafe(context, alarmManager, target.timeInMillis, pi)
            } else {
                alarmManager.cancel(pi)
            }
        }
        scheduleMidnightReset(context)
    }

    fun scheduleMidnightReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val midnight = Calendar.getInstance(tz()).apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val intent = Intent(context, TodoAlarmReceiver::class.java).apply {
            action = ACTION_DAILY_RESET
        }
        val pi = PendingIntent.getBroadcast(
            context,
            RESET_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactSafe(alarmManager, midnight.timeInMillis, pi)
    }

    private fun setAlarmClockSafe(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        operation: PendingIntent
    ) {
        val showAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showAppPi = PendingIntent.getActivity(
            context,
            "show_alarm_ui".hashCode(),
            showAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms(context)) {
                val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showAppPi)
                alarmManager.setAlarmClock(info, operation)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            }
        } catch (_: SecurityException) {
            setExactSafe(alarmManager, triggerAtMillis, operation)
        }
    }

    private fun setExactSafe(alarmManager: AlarmManager, triggerAtMillis: Long, operation: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    fun setPendingPopupTaskId(context: Context, taskId: String?) {
        prefs(context).edit().putString(KEY_PENDING_POPUP_ID, taskId).apply()
    }

    fun getPendingPopupTaskId(context: Context): String? =
        prefs(context).getString(KEY_PENDING_POPUP_ID, null)

    fun markTaskDone(context: Context, taskId: String): List<TodoTaskItem> {
        val updated = ensureCurrentDayOrReset(context).map {
            if (it.id == taskId) it.copy(isDone = true, triggeredToday = true) else it
        }
        saveTasks(context, updated)
        if (getPendingPopupTaskId(context) == taskId) {
            setPendingPopupTaskId(context, null)
        }
        return updated
    }

    fun markTaskSkipped(context: Context, taskId: String): List<TodoTaskItem> {
        val updated = ensureCurrentDayOrReset(context).map {
            if (it.id == taskId) it.copy(triggeredToday = true) else it
        }
        saveTasks(context, updated)
        if (getPendingPopupTaskId(context) == taskId) {
            setPendingPopupTaskId(context, null)
        }
        return updated
    }

    fun clearAllTasksForToday(context: Context) {
        prefs(context).edit()
            .putString(KEY_DATE, todayKey())
            .putString(KEY_TASKS, "[]")
            .remove(KEY_PENDING_POPUP_ID)
            .apply()
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc việc cần làm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo pop-up khi tới giờ việc cần làm"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun showSystemPopupNotification(context: Context, task: TodoTaskItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ensureChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val contentPi = PendingIntent.getActivity(
            context,
            task.id.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Việc cần làm (${task.timeFormatted})")
            .setContentText(task.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${task.timeFormatted} — ${task.title}"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPi)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            NotificationManagerCompat.from(context).notify(task.id.hashCode(), builder.build())
        } catch (_: SecurityException) {
        }
    }
}

class TodoAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TodoHelper.ACTION_DAILY_RESET -> {
                TodoHelper.clearAllTasksForToday(context)
                TodoHelper.scheduleMidnightReset(context)
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.intent.action.TIME_SET" -> {
                val tasks = TodoHelper.ensureCurrentDayOrReset(context)
                TodoHelper.scheduleAllTodayAlarms(context, tasks)
            }
            TodoHelper.ACTION_TRIGGER_TASK -> {
                val tasks = TodoHelper.ensureCurrentDayOrReset(context)
                val taskId = intent.getStringExtra(TodoHelper.EXTRA_TASK_ID) ?: return
                val matched = tasks.find { it.id == taskId } ?: return
                val triggeredTask = matched.copy(triggeredToday = true)
                val updated = tasks.map {
                    if (it.id == taskId) triggeredTask else it
                }
                TodoHelper.saveTasks(context, updated)
                TodoHelper.setPendingPopupTaskId(context, taskId)
                TodoHelper.showSystemPopupNotification(context, triggeredTask)
                TodoHelper.scheduleMidnightReset(context)
            }
        }
    }
}
