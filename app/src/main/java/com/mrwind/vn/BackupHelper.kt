package com.mrwind.vn

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Sao lưu & chuyển máy: xuất / nhập cài đặt, lịch sử, ghi chú ngày Âm lịch,
 * sinh nhật và ngày kỷ niệm Âm lịch ra một file JSON.
 *
 * - File chỉ chứa dữ liệu của người dùng, KHÔNG chứa mật khẩu hay khoá nào.
 * - Khi nhập, mọi giá trị đều được kiểm tra (kiểu, phạm vi, độ dài) và chỉ những khoá
 *   nằm trong danh sách cho phép mới được ghi, nên file lạ/hỏng không làm hỏng app.
 */
object BackupHelper {
    private const val APP_ID = "thoi_tiet_am_lich"
    private const val FORMAT_VERSION = 1
    private const val MAX_FILE_BYTES = 2 * 1024 * 1024

    private const val NOTE_PREFIX = "lunar_day_note_"
    private val NOTE_KEY_REGEX = Regex("[A-Za-z0-9_.:+\\-]{1,48}")
    private const val MAX_NOTE_LEN = 2000
    private const val MAX_NOTES = 1000

    private const val GBD_PREFS = "good_bad_day_history_prefs"
    private const val GBD_KEY = "entries"

    private const val MAX_BIRTHDAYS = 10
    private const val MAX_LUNAR_EVENTS = 40
    private const val MAX_HISTORY = 20

    private enum class Kind { BOOL, INT, FLOAT, STRING, STRING_ARRAY }

    /**
     * @param onlyWhenManualLocation chỉ xuất khi đang dùng vị trí thủ công (không dùng GPS).
     * @param keepIfAbsent khi file không có khoá này thì giữ nguyên giá trị hiện tại trên máy.
     */
    private class Spec(
        val key: String,
        val kind: Kind,
        val min: Double = Double.NEGATIVE_INFINITY,
        val max: Double = Double.POSITIVE_INFINITY,
        val maxLen: Int = 200,
        val keepIfAbsent: Boolean = false
    )

    // Danh sách cài đặt được phép sao lưu (khoá trong AppPrefs)
    private val SETTINGS = listOf(
        Spec("app_theme_mode", Kind.INT, 0.0, 2.0),
        Spec("card_bounce_enabled", Kind.BOOL),
        Spec("top_weather_bg_enabled", Kind.BOOL),
        Spec("bg_music_enabled", Kind.BOOL),
        Spec("bg_music_volume_v2", Kind.FLOAT, 0.0, 1.0),
        Spec("widget_bg_mode", Kind.INT, 0.0, 4.0),
        Spec("accent_color", Kind.INT),
        Spec("show_seconds", Kind.BOOL),
        Spec("show_weather_effect", Kind.BOOL),
        Spec("show_lunar_date", Kind.BOOL),
        Spec("show_solar_date", Kind.BOOL),
        Spec("show_location_name", Kind.BOOL),
        Spec("show_seven_day_forecast", Kind.BOOL),
        Spec("show_signature", Kind.BOOL),
        Spec("signature", Kind.STRING, maxLen = 100),
        Spec("scale_clock", Kind.INT, 1.0, 20.0),
        Spec("scale_lunar", Kind.INT, 1.0, 20.0),
        Spec("scale_solar", Kind.INT, 1.0, 20.0),
        Spec("scale_sig", Kind.INT, 1.0, 20.0),
        Spec("auto_location", Kind.BOOL),
        Spec("holiday_reminder_enabled", Kind.BOOL),
        Spec("enabled_holiday_ids_json", Kind.STRING_ARRAY),
        Spec("lunar_useful_notify_enabled", Kind.BOOL),
        Spec("ram_mung1_reminder_enabled", Kind.BOOL),
        Spec("weather_alert_enabled", Kind.BOOL),
        // Vị trí thủ công (chỉ có trong file khi không dùng GPS)
        Spec("w_loc", Kind.STRING, maxLen = 120, keepIfAbsent = true),
        Spec("w_lat", Kind.FLOAT, -90.0, 90.0, keepIfAbsent = true),
        Spec("w_lon", Kind.FLOAT, -180.0, 180.0, keepIfAbsent = true)
    )

    private val LOCATION_KEYS = setOf("w_loc", "w_lat", "w_lon")

    /** Tóm tắt nội dung file sao lưu để hiện cho người dùng xác nhận. */
    data class Summary(
        val settings: Int,
        val birthdays: Int,
        val lunarEvents: Int,
        val lunarHistory: Int,
        val ageHistory: Int,
        val goodBadHistory: Int,
        val notes: Int
    ) {
        val historyTotal: Int get() = lunarHistory + ageHistory + goodBadHistory
    }

    /** Dữ liệu đã kiểm tra hợp lệ, sẵn sàng ghi vào máy. */
    class ParsedBackup internal constructor(
        val exportedAt: Long,
        val summary: Summary,
        internal val settingsPut: Map<String, Any>,
        internal val settingsRemove: List<String>,
        internal val birthdays: List<BirthdayItem>,
        internal val lunarEvents: List<LunarCustomEventItem>,
        internal val lunarHistory: List<LunarLookupHistoryItem>,
        internal val ageHistory: List<AgeHistoryItem>,
        internal val goodBadHistory: JSONArray,
        internal val notes: Map<String, String>
    )

    fun suggestedFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "ThoiTietAmLich_SaoLuu_$stamp.json"
    }

    // ------------------------------------------------------------------
    //  XUẤT
    // ------------------------------------------------------------------

    fun buildBackupJson(context: Context): String {
        val prefs = AppPrefs.backupPrefs(context)
        val all = prefs.all
        val autoLocation = (all["auto_location"] as? Boolean) ?: true

        val root = JSONObject()
        root.put("app", APP_ID)
        root.put("format", FORMAT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            root.put("appVersion", info.versionName ?: "")
        } catch (_: Exception) {
        }

        // Cài đặt
        val settings = JSONObject()
        for (spec in SETTINGS) {
            if (spec.key in LOCATION_KEYS && autoLocation) continue
            val v = all[spec.key] ?: continue
            when (spec.kind) {
                Kind.BOOL -> (v as? Boolean)?.let { settings.put(spec.key, it) }
                Kind.INT -> (v as? Int)?.let { settings.put(spec.key, it) }
                Kind.FLOAT -> (v as? Float)?.toDouble()?.takeIf { it.isFinite() }?.let { settings.put(spec.key, it) }
                Kind.STRING -> (v as? String)?.let { settings.put(spec.key, it) }
                Kind.STRING_ARRAY -> (v as? String)?.let {
                    try {
                        settings.put(spec.key, JSONArray(it))
                    } catch (_: Exception) {
                    }
                }
            }
        }
        root.put("settings", settings)

        // Sinh nhật
        val birthdays = JSONArray()
        AppPrefs.getBirthdays(context).forEach {
            birthdays.put(
                JSONObject().put("id", it.id).put("name", it.name).put("day", it.day).put("month", it.month)
            )
        }
        root.put("birthdays", birthdays)

        // Ngày kỷ niệm Âm lịch đã thêm
        val events = JSONArray()
        AppPrefs.getLunarCustomEvents(context).forEach {
            events.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("lunarDay", it.lunarDay)
                    .put("lunarMonth", it.lunarMonth)
                    .put("isLeapMonth", it.isLeapMonth)
            )
        }
        root.put("lunarEvents", events)

        // Lịch sử
        val history = JSONObject()
        val lunarHistory = JSONArray()
        AppPrefs.getLunarHistory(context).forEach {
            lunarHistory.put(
                JSONObject()
                    .put("id", it.id)
                    .put("solarDay", it.solarDay)
                    .put("solarMonth", it.solarMonth)
                    .put("solarYear", it.solarYear)
                    .put("lunarSummary", it.lunarSummary)
                    .put("searchedAt", it.searchedAt)
            )
        }
        history.put("lunarLookup", lunarHistory)
        val ageHistory = JSONArray()
        AppPrefs.getAgeHistory(context).forEach {
            ageHistory.put(
                JSONObject()
                    .put("id", it.id)
                    .put("birthDay", it.birthDay)
                    .put("birthMonth", it.birthMonth)
                    .put("birthYear", it.birthYear)
                    .put("ageSummary", it.ageSummary)
                    .put("calculatedAt", it.calculatedAt)
            )
        }
        history.put("age", ageHistory)
        history.put("goodBadDay", readGoodBadDayRaw(context))
        root.put("history", history)

        // Ghi chú theo ngày Âm lịch
        val notes = JSONObject()
        for ((k, v) in all) {
            if (!k.startsWith(NOTE_PREFIX)) continue
            val text = (v as? String)?.trim().orEmpty()
            if (text.isEmpty()) continue
            val noteKey = k.removePrefix(NOTE_PREFIX)
            if (NOTE_KEY_REGEX.matches(noteKey)) notes.put(noteKey, text.take(MAX_NOTE_LEN))
        }
        root.put("notes", notes)

        return root.toString(2)
    }

    private fun readGoodBadDayRaw(context: Context): JSONArray {
        return try {
            val raw = context.getSharedPreferences(GBD_PREFS, Context.MODE_PRIVATE).getString(GBD_KEY, null)
            if (raw.isNullOrBlank()) JSONArray() else JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }
    }

    /** Ghi file sao lưu vào [uri] (do người dùng chọn qua hộp thoại lưu file). */
    suspend fun exportToUri(context: Context, uri: android.net.Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val bytes = buildBackupJson(context).toByteArray(Charsets.UTF_8)
            val resolver = context.contentResolver
            var stream: java.io.OutputStream? = null
            try {
                stream = resolver.openOutputStream(uri, "wt")
            } catch (_: Exception) {
            }
            if (stream == null) stream = resolver.openOutputStream(uri)
            val out = stream ?: return@withContext false
            out.use { it.write(bytes) }
            true
        } catch (_: Exception) {
            false
        }
    }

    // ------------------------------------------------------------------
    //  NHẬP
    // ------------------------------------------------------------------

    /** Đọc + kiểm tra file. Ném [IllegalArgumentException] với thông báo tiếng Việt nếu file không hợp lệ. */
    suspend fun readAndParse(context: Context, uri: android.net.Uri): ParsedBackup = withContext(Dispatchers.IO) {
        val text = readText(context, uri)
        parse(text)
    }

    private fun readText(context: Context, uri: android.net.Uri): String {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Không mở được file đã chọn")
        input.use {
            val out = ByteArrayOutputStream()
            val buf = ByteArray(8192)
            var total = 0
            while (true) {
                val n = it.read(buf)
                if (n < 0) break
                total += n
                if (total > MAX_FILE_BYTES) throw IllegalArgumentException("File quá lớn, không phải file sao lưu của ứng dụng")
                out.write(buf, 0, n)
            }
            return out.toString("UTF-8").trimStart('\uFEFF')
        }
    }

    fun parse(text: String): ParsedBackup {
        val root = try {
            JSONObject(text)
        } catch (_: Exception) {
            throw IllegalArgumentException("File không đúng định dạng sao lưu")
        }
        if (root.optString("app") != APP_ID) {
            throw IllegalArgumentException("Đây không phải file sao lưu của ứng dụng Thời tiết & Âm lịch")
        }
        if (root.optInt("format", 0) !in 1..FORMAT_VERSION) {
            throw IllegalArgumentException("File sao lưu tạo bởi phiên bản mới hơn, hãy cập nhật ứng dụng rồi thử lại")
        }

        // ----- Cài đặt -----
        val settingsJson = root.optJSONObject("settings") ?: JSONObject()
        val put = LinkedHashMap<String, Any>()
        val remove = ArrayList<String>()
        for (spec in SETTINGS) {
            val value = if (settingsJson.has(spec.key)) settingsJson.opt(spec.key) else null
            val parsed = if (value == null) null else parseSetting(spec, value)
            when {
                parsed != null -> put[spec.key] = parsed
                spec.keepIfAbsent -> Unit
                else -> remove.add(spec.key)
            }
        }
        // Chỉ ghi vị trí khi đủ cả 3 trường; thiếu thì bỏ hết để tránh lệch dữ liệu
        if (LOCATION_KEYS.any { it !in put }) LOCATION_KEYS.forEach { put.remove(it) }

        // ----- Sinh nhật -----
        val birthdays = ArrayList<BirthdayItem>()
        val usedIds = HashSet<String>()
        root.optJSONArray("birthdays")?.let { arr ->
            for (i in 0 until arr.length()) {
                if (birthdays.size >= MAX_BIRTHDAYS) break
                val o = arr.optJSONObject(i) ?: continue
                val name = str(o, "name", 80).trim()
                val day = intIn(o, "day", 1, 31) ?: continue
                val month = intIn(o, "month", 1, 12) ?: continue
                if (name.isEmpty()) continue
                birthdays.add(BirthdayItem(uniqueId(o, usedIds), name, day, month))
            }
        }

        // ----- Ngày kỷ niệm Âm lịch -----
        val events = ArrayList<LunarCustomEventItem>()
        usedIds.clear()
        root.optJSONArray("lunarEvents")?.let { arr ->
            for (i in 0 until arr.length()) {
                if (events.size >= MAX_LUNAR_EVENTS) break
                val o = arr.optJSONObject(i) ?: continue
                val title = str(o, "title", 120).trim()
                val day = intIn(o, "lunarDay", 1, 30) ?: continue
                val month = intIn(o, "lunarMonth", 1, 12) ?: continue
                if (title.isEmpty()) continue
                events.add(
                    LunarCustomEventItem(
                        id = uniqueId(o, usedIds),
                        title = title,
                        lunarDay = day,
                        lunarMonth = month,
                        isLeapMonth = o.optBoolean("isLeapMonth", false)
                    )
                )
            }
        }

        // ----- Lịch sử -----
        val historyJson = root.optJSONObject("history") ?: JSONObject()

        val lunarHistory = ArrayList<LunarLookupHistoryItem>()
        usedIds.clear()
        historyJson.optJSONArray("lunarLookup")?.let { arr ->
            for (i in 0 until arr.length()) {
                if (lunarHistory.size >= MAX_HISTORY) break
                val o = arr.optJSONObject(i) ?: continue
                val d = intIn(o, "solarDay", 1, 31) ?: continue
                val m = intIn(o, "solarMonth", 1, 12) ?: continue
                val y = intIn(o, "solarYear", 1, 9999) ?: continue
                lunarHistory.add(
                    LunarLookupHistoryItem(
                        id = uniqueId(o, usedIds),
                        solarDay = d,
                        solarMonth = m,
                        solarYear = y,
                        lunarSummary = str(o, "lunarSummary", 300),
                        searchedAt = longOr(o, "searchedAt")
                    )
                )
            }
        }

        val ageHistory = ArrayList<AgeHistoryItem>()
        usedIds.clear()
        historyJson.optJSONArray("age")?.let { arr ->
            for (i in 0 until arr.length()) {
                if (ageHistory.size >= MAX_HISTORY) break
                val o = arr.optJSONObject(i) ?: continue
                val d = intIn(o, "birthDay", 1, 31) ?: continue
                val m = intIn(o, "birthMonth", 1, 12) ?: continue
                val y = intIn(o, "birthYear", 1, 9999) ?: continue
                ageHistory.add(
                    AgeHistoryItem(
                        id = uniqueId(o, usedIds),
                        birthDay = d,
                        birthMonth = m,
                        birthYear = y,
                        ageSummary = str(o, "ageSummary", 300),
                        calculatedAt = longOr(o, "calculatedAt")
                    )
                )
            }
        }

        val goodBad = JSONArray()
        historyJson.optJSONArray("goodBadDay")?.let { arr ->
            for (i in 0 until arr.length()) {
                if (goodBad.length() >= MAX_HISTORY) break
                val o = arr.optJSONObject(i) ?: continue
                val d = intIn(o, "d", 1, 31) ?: continue
                val m = intIn(o, "m", 1, 12) ?: continue
                val y = intIn(o, "y", 1, 9999) ?: continue
                goodBad.put(
                    JSONObject()
                        .put("d", d)
                        .put("m", m)
                        .put("y", y)
                        .put("male", o.optBoolean("male", true))
                        .put("a", str(o, "a", 120))
                        .put("t", longOr(o, "t"))
                )
            }
        }

        // ----- Ghi chú -----
        val notes = LinkedHashMap<String, String>()
        root.optJSONObject("notes")?.let { obj ->
            val keys = obj.keys()
            while (keys.hasNext() && notes.size < MAX_NOTES) {
                val k = keys.next()
                if (!NOTE_KEY_REGEX.matches(k)) continue
                if (obj.isNull(k)) continue
                val text = obj.optString(k, "").trim().take(MAX_NOTE_LEN)
                if (text.isNotEmpty()) notes[k] = text
            }
        }

        val settingsCount = put.keys.count { it !in LOCATION_KEYS } + if (LOCATION_KEYS.all { it in put }) 1 else 0
        val summary = Summary(
            settings = settingsCount,
            birthdays = birthdays.size,
            lunarEvents = events.size,
            lunarHistory = lunarHistory.size,
            ageHistory = ageHistory.size,
            goodBadHistory = goodBad.length(),
            notes = notes.size
        )
        return ParsedBackup(
            exportedAt = root.optLong("exportedAt", 0L),
            summary = summary,
            settingsPut = put,
            settingsRemove = remove,
            birthdays = birthdays,
            lunarEvents = events,
            lunarHistory = lunarHistory,
            ageHistory = ageHistory,
            goodBadHistory = goodBad,
            notes = notes
        )
    }

    private fun parseSetting(spec: Spec, v: Any): Any? {
        return when (spec.kind) {
            Kind.BOOL -> v as? Boolean
            Kind.INT -> {
                val n = v as? Number ?: return null
                val d = n.toDouble()
                if (!d.isFinite() || d != Math.floor(d)) return null
                d.coerceIn(spec.min.coerceAtLeast(Int.MIN_VALUE.toDouble()), spec.max.coerceAtMost(Int.MAX_VALUE.toDouble())).toInt()
            }
            Kind.FLOAT -> {
                val n = v as? Number ?: return null
                val d = n.toDouble()
                if (!d.isFinite()) return null
                d.coerceIn(spec.min, spec.max).toFloat()
            }
            Kind.STRING -> {
                val s = v as? String ?: return null
                s.take(spec.maxLen)
            }
            Kind.STRING_ARRAY -> {
                val arr = v as? JSONArray ?: return null
                val out = JSONArray()
                for (i in 0 until minOf(arr.length(), 500)) {
                    val s = arr.optString(i, "").trim()
                    if (s.isNotEmpty() && s.length <= 80) out.put(s)
                }
                out.toString()
            }
        }
    }

    private fun str(o: JSONObject, key: String, max: Int): String =
        if (o.isNull(key)) "" else o.optString(key, "").take(max)

    private fun intIn(o: JSONObject, key: String, min: Int, max: Int): Int? {
        val n = o.opt(key) as? Number ?: return null
        val d = n.toDouble()
        if (!d.isFinite() || d != Math.floor(d)) return null
        val i = d.toInt()
        return if (i in min..max) i else null
    }

    private fun longOr(o: JSONObject, key: String): Long = (o.opt(key) as? Number)?.toLong() ?: 0L

    private fun uniqueId(o: JSONObject, used: MutableSet<String>): String {
        var id = str(o, "id", 64).trim()
        if (id.isEmpty() || !used.add(id)) {
            id = UUID.randomUUID().toString()
            used.add(id)
        }
        return id
    }

    /**
     * Ghi dữ liệu đã kiểm tra vào máy. Các mục có trong file sẽ THAY THẾ dữ liệu hiện tại
     * (sinh nhật, ngày kỷ niệm, lịch sử, ghi chú, cài đặt).
     */
    suspend fun restore(context: Context, data: ParsedBackup) = withContext(Dispatchers.IO) {
        val prefs = AppPrefs.backupPrefs(context)
        val editor = prefs.edit()

        for ((key, value) in data.settingsPut) {
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
            }
        }
        data.settingsRemove.forEach { editor.remove(it) }

        // Ghi chú: thay toàn bộ ghi chú cũ bằng ghi chú trong file
        prefs.all.keys.filter { it.startsWith(NOTE_PREFIX) }.forEach { editor.remove(it) }
        data.notes.forEach { (k, v) -> editor.putString(NOTE_PREFIX + k, v) }
        editor.commit()

        AppPrefs.saveBirthdays(context, data.birthdays)
        AppPrefs.saveLunarCustomEvents(context, data.lunarEvents)
        AppPrefs.saveLunarHistory(context, data.lunarHistory)
        AppPrefs.saveAgeHistory(context, data.ageHistory)
        context.getSharedPreferences(GBD_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(GBD_KEY, data.goodBadHistory.toString())
            .commit()

        // Đảm bảo các thao tác apply() ở trên đã được ghi xuống đĩa
        prefs.edit().commit()
    }
}
