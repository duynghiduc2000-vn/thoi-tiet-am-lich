package com.mrwind.vn

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ============================================================
// LỊCH THÁNG (dạng lưới) + POP-UP TỜ LỊCH XÉ
// ------------------------------------------------------------
// - MonthCalendarSection : nội dung tab "Lịch" (đặt trong LazyColumn)
// - McTearOffSheet       : pop-up chi tiết ngày, kiểu tờ lịch xé + Liquid Glass
// - McEngine             : tính âm lịch, rằm/mùng 1, ngày lễ, ngày giỗ, hoàng đạo, tiết khí
// - Lọc chấm            : chạm từng mục ở dưới lưới để ẩn/hiện (lưu trong McPrefs)
// - Tìm ngày tốt        : chọn việc + ngày sinh => tô sáng ngày phù hợp (dùng BirthdayHelper.scoreDayForActivity)
// - Âm thanh xé lịch    : res/raw/calendar_tear.wav, phát khi xé tờ (bật/tắt bằng nút loa trên tờ lịch)
// ============================================================

/** Đổi thành false nếu muốn tuần bắt đầu từ Chủ nhật. */
private const val MC_WEEK_STARTS_MONDAY = true

private val MC_TZ: TimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")

private val MC_RED = Color(0xFFE53935)
private val MC_GOLD = Color(0xFFFFA000)
private val MC_PURPLE = Color(0xFFAB47BC)
private val MC_GREEN = Color(0xFF43A047)
private val MC_TEAL = Color(0xFF00ACC1)
private val MC_GOOD = Color(0xFF00C853)

/** Ngưỡng điểm trùng với kết luận trong BirthdayHelper.evaluateGoodBadDayForActivity. */
private const val MC_GOOD_MIN = 60
private const val MC_GREAT_MIN = 75

private val MC_FILTERS = listOf(
    McMarker.MOON to "Mùng 1 / Rằm",
    McMarker.HOLIDAY to "Ngày lễ",
    McMarker.MEMORIAL to "Giỗ / Sự kiện",
    McMarker.HOANG_DAO to "Hoàng đạo",
    McMarker.TIET_KHI to "Tiết khí"
)

// ---------------------------------------------------------------- Model

data class McDate(val year: Int, val month: Int, val day: Int) {
    private fun calendar(): Calendar = Calendar.getInstance(MC_TZ).apply {
        clear()
        set(year, month - 1, day)
    }

    fun plusDays(n: Int): McDate {
        val c = calendar()
        c.add(Calendar.DAY_OF_MONTH, n)
        return McDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    /** Calendar.SUNDAY (1) ... Calendar.SATURDAY (7) */
    fun weekday(): Int = calendar().get(Calendar.DAY_OF_WEEK)

    /** yyyy-MM-dd (cùng định dạng khóa ghi chú ngày Âm của app) */
    fun key(): String = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    fun isBefore(other: McDate): Boolean = key() < other.key()

    companion object {
        fun today(): McDate {
            val c = Calendar.getInstance(MC_TZ)
            return McDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        }
    }
}

enum class McMarker { MOON, HOLIDAY, MEMORIAL, HOANG_DAO, TIET_KHI }

/** Ngày sinh + giới tính dùng để chấm điểm ngày tốt (lưu trong McPrefs). */
data class McBirthProfile(val day: Int, val month: Int, val year: Int, val isMale: Boolean)

data class McEventLine(val text: String, val marker: McMarker)

data class McDayInfo(
    val date: McDate,
    val weekday: Int,
    val lunarDay: Int,
    val lunarMonth: Int,
    val lunarLeap: Boolean,
    val dayCanChi: String,
    val monthCanChi: String,
    val yearCanChi: String,
    val dayCan: Int,
    val dayChi: Int,
    val isMungMot: Boolean,
    val isRam: Boolean,
    val isHoangDao: Boolean,
    val dayStar: String,
    val solarTerm: String?,
    val events: List<McEventLine>,
    val markers: List<McMarker>
)

private data class McNav(val date: McDate, val dir: Int)

private fun mcDaysInMonth(year: Int, month: Int): Int {
    val c = Calendar.getInstance(MC_TZ)
    c.clear()
    c.set(year, month - 1, 1)
    return c.getActualMaximum(Calendar.DAY_OF_MONTH)
}

/** Danh sách ô của lưới tháng (bội số của 7, có cả ngày tháng trước/sau cho đủ hàng). */
private fun mcMonthGrid(year: Int, month: Int): List<McDate> {
    val first = McDate(year, month, 1)
    val dow = first.weekday()
    val lead = if (MC_WEEK_STARTS_MONDAY) (dow + 5) % 7 else dow - 1
    val total = lead + mcDaysInMonth(year, month)
    val cells = ((total + 6) / 7) * 7
    val start = first.plusDays(-lead)
    return List(cells) { start.plusDays(it) }
}

private fun mcJdFromDate(dd: Int, mm: Int, yy: Int): Int {
    val a = (14 - mm) / 12
    val y = yy + 4800 - a
    val m = mm + 12 * a - 3
    var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    if (jd < 2299161) {
        jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
    }
    return jd
}

// ---------------------------------------------------------------- Dữ liệu chữ

private val MC_CAN = arrayOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
private val MC_CHI = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

/** 12 "trực tinh": 6 sao Hoàng đạo (offset 0,1,4,5,7,10) và 6 sao Hắc đạo. */
private val MC_DAY_STARS = arrayOf(
    "Thanh Long", "Minh Đường", "Thiên Hình", "Chu Tước", "Kim Quỹ", "Bảo Quang",
    "Bạch Hổ", "Ngọc Đường", "Thiên Lao", "Huyền Vũ", "Tư Mệnh", "Câu Trần"
)
private val MC_HOANG_DAO_OFFSETS = setOf(0, 1, 4, 5, 7, 10)

private val MC_LUNAR_MONTH_WORD = arrayOf(
    "GIÊNG", "HAI", "BA", "TƯ", "NĂM", "SÁU", "BẢY", "TÁM", "CHÍN", "MƯỜI", "MƯỜI MỘT", "CHẠP"
)
private val MC_SOLAR_MONTH_WORD = arrayOf(
    "MỘT", "HAI", "BA", "TƯ", "NĂM", "SÁU", "BẢY", "TÁM", "CHÍN", "MƯỜI", "MƯỜI MỘT", "MƯỜI HAI"
)
private val MC_WEEKDAY_VN = arrayOf(
    "", "CHỦ NHẬT", "THỨ HAI", "THỨ BA", "THỨ TƯ", "THỨ NĂM", "THỨ SÁU", "THỨ BẢY"
)
private val MC_WEEKDAY_EN = arrayOf(
    "", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
)
private val MC_WEEKDAY_SHORT = arrayOf("", "CN", "T2", "T3", "T4", "T5", "T6", "T7")

private val MC_BLESSINGS = arrayOf(
    "VẠN SỰ NHƯ Ý", "PHÚ QUÝ CÁT TƯỜNG", "CÔNG THÀNH DANH TOẠI", "MÃ ĐÁO THÀNH CÔNG",
    "THUẬN BUỒM XUÔI GIÓ", "AN KHANG THỊNH VƯỢNG", "TÀI LỘC ĐẦY NHÀ", "BÌNH AN HẠNH PHÚC",
    "GẶP DỮ HÓA LÀNH", "SỨC KHỎE DỒI DÀO", "NIỀM VUI TRỌN VẸN", "TÂM AN VẠN SỰ LÀNH"
)

/** Màu số ngày trên tờ lịch theo thứ (giống các tờ lịch bloc nhiều màu). */
private fun mcWeekdayColor(dow: Int): Color = when (dow) {
    Calendar.SUNDAY -> Color(0xFFE53935)
    Calendar.MONDAY -> Color(0xFF00897B)
    Calendar.TUESDAY -> Color(0xFFFB8C00)
    Calendar.WEDNESDAY -> Color(0xFF8E24AA)
    Calendar.THURSDAY -> Color(0xFF1E88E5)
    Calendar.FRIDAY -> Color(0xFFD81B60)
    else -> Color(0xFF2E9E4F)
}

private fun mcMarkerColor(m: McMarker): Color = when (m) {
    McMarker.MOON -> MC_GOLD
    McMarker.HOLIDAY -> MC_RED
    McMarker.MEMORIAL -> MC_PURPLE
    McMarker.HOANG_DAO -> MC_GREEN
    McMarker.TIET_KHI -> MC_TEAL
}

// ---------------------------------------------------------------- Prefs / âm thanh / chấm điểm

private object McPrefs {
    private const val NAME = "month_calendar_prefs"
    private const val KEY_HIDDEN = "hidden_markers"
    private const val KEY_SOUND = "tear_sound_on"
    private const val KEY_BIRTH = "birth_profile"

    private fun sp(c: Context) = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun hiddenMarkers(c: Context): Set<McMarker> =
        sp(c).getStringSet(KEY_HIDDEN, emptySet()).orEmpty()
            .mapNotNull { n -> McMarker.entries.firstOrNull { it.name == n } }
            .toSet()

    fun setHiddenMarkers(c: Context, hidden: Set<McMarker>) {
        sp(c).edit().putStringSet(KEY_HIDDEN, hidden.map { it.name }.toSet()).apply()
    }

    fun soundOn(c: Context): Boolean = sp(c).getBoolean(KEY_SOUND, true)

    fun setSoundOn(c: Context, on: Boolean) {
        sp(c).edit().putBoolean(KEY_SOUND, on).apply()
    }

    fun birth(c: Context): McBirthProfile? {
        val parts = sp(c).getString(KEY_BIRTH, null)?.split("-") ?: return null
        if (parts.size != 4) return null
        val d = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val y = parts[2].toIntOrNull() ?: return null
        return McBirthProfile(d, m, y, parts[3] == "1")
    }

    fun clearBirth(c: Context) {
        sp(c).edit().remove(KEY_BIRTH).apply()
    }

    fun setBirth(c: Context, b: McBirthProfile) {
        sp(c).edit()
            .putString(KEY_BIRTH, "${b.day}-${b.month}-${b.year}-${if (b.isMale) 1 else 0}")
            .apply()
    }
}

private fun mcParseBirth(d: String, m: String, y: String, male: Boolean): McBirthProfile? {
    val dd = d.toIntOrNull() ?: return null
    val mm = m.toIntOrNull() ?: return null
    val yy = y.toIntOrNull() ?: return null
    if (yy !in 1900..McDate.today().year || mm !in 1..12) return null
    if (dd !in 1..mcDaysInMonth(yy, mm)) return null
    return McBirthProfile(dd, mm, yy, male)
}

/** Chấm điểm 1 ngày cho 1 việc, theo người sinh [birth]. Dùng lại BirthdayHelper.scoreDayForActivity. */
private class McScorer(
    private val engine: McEngine,
    private val birth: McBirthProfile,
    val activity: AuspiciousActivity
) {
    fun score(d: McDate): Int = engine.activityScore(d, birth, activity)
}

/**
 * Âm thanh xé lịch: res/raw/calendar_tear.wav (~0,5 giây), phát bằng SoundPool nên gần như không trễ.
 * Mỗi lần phát đổi tốc độ nhẹ để các lần xé nghe không giống hệt nhau.
 * Dùng luồng âm "sonification" => tắt khi điện thoại ở chế độ im lặng.
 */
private class McTearSound(context: Context) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val soundId: Int
    @Volatile private var loaded = false

    init {
        pool.setOnLoadCompleteListener { _, _, status -> loaded = (status == 0) }
        soundId = pool.load(context.applicationContext, R.raw.calendar_tear, 1)
    }

    fun play() {
        if (!loaded) return
        pool.play(soundId, 1f, 1f, 1, 0, 0.92f + Random.nextFloat() * 0.22f)
    }

    fun release() = pool.release()
}

// ---------------------------------------------------------------- Engine

/**
 * Tính thông tin từng ngày. Tạo 1 lần cho mỗi màn hình (đọc ngày lễ đã bật + ngày giỗ/sự kiện
 * Âm lịch của người dùng 1 lần), có cache nên cuộn/đổi tháng rất nhẹ.
 */
class McEngine(context: Context) {
    private val holidays: List<HolidayDefinition>
    private val memorials: List<LunarCustomEventItem>
    private val lunarCache = HashMap<McDate, LunarDateInfo>()
    private val infoCache = HashMap<McDate, McDayInfo>()

    init {
        val all = BirthdayHelper.DEFAULT_VIETNAM_AND_INTERNATIONAL_HOLIDAYS
        val enabled = AppPrefs.getEnabledHolidayIds(context)
        holidays = if (enabled == null) all else all.filter { it.id in enabled }
        memorials = AppPrefs.getLunarCustomEvents(context)
    }

    fun lunarOf(d: McDate): LunarDateInfo =
        lunarCache.getOrPut(d) { LunarCalendar.convertSolarToLunar(d.day, d.month, d.year) }

    fun dayInfo(d: McDate): McDayInfo = infoCache.getOrPut(d) { buildInfo(d) }

    private fun buildInfo(d: McDate): McDayInfo {
        val jd = mcJdFromDate(d.day, d.month, d.year)
        val lunar = lunarOf(d)
        val dayCan = ((jd + 9) % 10 + 10) % 10
        val dayChi = ((jd + 1) % 12 + 12) % 12

        // Hoàng đạo: 'sao khởi' của tháng Âm (Giêng/7 = Tý, 2/8 = Dần, 3/9 = Thìn, ...)
        val start = ((lunar.month - 1) % 6) * 2
        val offset = ((dayChi - start) % 12 + 12) % 12
        val isHoangDao = offset in MC_HOANG_DAO_OFFSETS
        val star = MC_DAY_STARS[offset]

        val leapText = if (lunar.isLeap) " nhuận" else ""
        val isMungMot = lunar.day == 1
        val isRam = lunar.day == 15

        val events = ArrayList<McEventLine>()
        if (isMungMot) {
            events.add(McEventLine("Mùng 1 tháng ${lunar.month}$leapText Âm lịch", McMarker.MOON))
        }
        if (isRam) {
            events.add(McEventLine("Rằm tháng ${lunar.month}$leapText Âm lịch", McMarker.MOON))
        }
        for (h in holidays) {
            val match = when (h.calendarType) {
                HolidayCalendarType.SOLAR -> h.day == d.day && h.month == d.month
                HolidayCalendarType.LUNAR ->
                    h.day == lunar.day && h.month == lunar.month && !lunar.isLeap
            }
            if (match) events.add(McEventLine(h.title, McMarker.HOLIDAY))
        }
        if (lunar.month == 12 && lunar.day >= 29) {
            val next = lunarOf(d.plusDays(1))
            if (next.month == 1 && next.day == 1) {
                events.add(McEventLine("Giao thừa (${lunar.day} tháng Chạp)", McMarker.HOLIDAY))
            }
        }
        for (ev in memorials) {
            if (ev.lunarDay == lunar.day && ev.lunarMonth == lunar.month && ev.isLeapMonth == lunar.isLeap) {
                events.add(McEventLine(ev.title, McMarker.MEMORIAL))
            }
        }

        // Tiết khí: ngày Mặt Trời đi qua mỗi 15° (24 ngày/năm)
        val solarTerm = BirthdayHelper.solarTermStartingOn(d.day, d.month, d.year)
        if (solarTerm != null) events.add(McEventLine("Tiết $solarTerm", McMarker.TIET_KHI))

        val markers = ArrayList<McMarker>()
        if (isMungMot || isRam) markers.add(McMarker.MOON)
        if (events.any { it.marker == McMarker.HOLIDAY }) markers.add(McMarker.HOLIDAY)
        if (events.any { it.marker == McMarker.MEMORIAL }) markers.add(McMarker.MEMORIAL)
        if (isHoangDao) markers.add(McMarker.HOANG_DAO)
        if (solarTerm != null) markers.add(McMarker.TIET_KHI)

        return McDayInfo(
            date = d,
            weekday = d.weekday(),
            lunarDay = lunar.day,
            lunarMonth = lunar.month,
            lunarLeap = lunar.isLeap,
            dayCanChi = lunar.dayCanChi,
            monthCanChi = lunar.monthCanChi,
            yearCanChi = lunar.yearCanChi,
            dayCan = dayCan,
            dayChi = dayChi,
            isMungMot = isMungMot,
            isRam = isRam,
            isHoangDao = isHoangDao,
            dayStar = star,
            solarTerm = solarTerm,
            events = events,
            markers = markers
        )
    }

    /** Điểm 0–100 của ngày [d] cho việc [activity] với người sinh [birth] (>=60 nên làm, >=75 rất nên). */
    fun activityScore(d: McDate, birth: McBirthProfile, activity: AuspiciousActivity): Int {
        val info = dayInfo(d)
        return BirthdayHelper.scoreDayForActivity(
            jd = mcJdFromDate(d.day, d.month, d.year),
            lunarDay = info.lunarDay,
            lunarMonth = info.lunarMonth,
            birthDay = birth.day,
            birthMonth = birth.month,
            birthYear = birth.year,
            gender = if (birth.isMale) AuspiciousGender.MALE else AuspiciousGender.FEMALE,
            activity = activity
        )
    }

    /** 6 giờ Hoàng đạo của ngày (theo chi của ngày). */
    fun goodHours(dayChi: Int): List<String> {
        val start = (dayChi % 6) * 2
        return intArrayOf(0, 1, 3, 6, 8, 9)
            .map { (start + it) % 12 }
            .sorted()
            .map { chi ->
                val from = (23 + 2 * chi) % 24
                val to = (from + 2) % 24
                "${MC_CHI[chi]} (${from}h-${to}h)"
            }
    }

    /** "Đ" (đủ 30 ngày) hoặc "T" (thiếu 29 ngày) của tháng Âm chứa ngày [d]. */
    fun lunarMonthTag(d: McDate): String {
        val lunar = lunarOf(d)
        val probe = lunarOf(d.plusDays(29 - lunar.day + 1))
        return if (probe.day == 1) "T" else "Đ"
    }

    /** Câu chúc in trên đầu tờ lịch. */
    fun blessing(info: McDayInfo): String {
        if (info.events.any { it.text.contains("Tết Nguyên Đán") }) return "CUNG CHÚC TÂN XUÂN"
        val jd = mcJdFromDate(info.date.day, info.date.month, info.date.year)
        return MC_BLESSINGS[jd % MC_BLESSINGS.size]
    }

    /** Can chi của giờ Tý đầu ngày (ví dụ "Giáp Tý"). */
    fun firstHourCanChi(info: McDayInfo): String = "${MC_CAN[(info.dayCan * 2) % 10]} Tý"
}

// ============================================================
//  TAB LỊCH THÁNG
// ============================================================

@Composable
internal fun MonthCalendarSection(
    palette: AdaptivePalette,
    accent: Color,
    onDayClick: (McDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val engine = remember { McEngine(context) }
    val today = remember { McDate.today() }
    var viewYear by rememberSaveable { mutableStateOf(today.year) }
    var viewMonth by rememberSaveable { mutableStateOf(today.month) }

    // Lọc chấm (nhớ lại giữa các lần mở app)
    var hidden by remember { mutableStateOf(McPrefs.hiddenMarkers(context)) }

    // Tìm ngày tốt
    var finderActivity by rememberSaveable { mutableStateOf<String?>(null) }
    var birth by remember { mutableStateOf(McPrefs.birth(context)) }
    val activity = AuspiciousActivity.entries.firstOrNull { it.name == finderActivity }
    val scorer = remember(activity, birth) {
        val b = birth
        if (activity != null && b != null) McScorer(engine, b, activity) else null
    }
    val goodDays = remember(viewYear, viewMonth, scorer) {
        if (scorer == null) {
            emptyList<Pair<McDate, Int>>()
        } else {
            (1..mcDaysInMonth(viewYear, viewMonth))
                .map { McDate(viewYear, viewMonth, it) }
                .filter { !it.isBefore(today) }
                .map { it to scorer.score(it) }
                .filter { it.second >= MC_GOOD_MIN }
        }
    }

    fun shiftMonth(delta: Int) {
        val total = viewYear * 12 + (viewMonth - 1) + delta
        val newYear = total / 12
        if (newYear !in 1901..2099) return
        viewYear = newYear
        viewMonth = total % 12 + 1
    }

    val lunarRangeLabel = remember(viewYear, viewMonth) {
        val first = engine.lunarOf(McDate(viewYear, viewMonth, 1))
        val mid = engine.lunarOf(McDate(viewYear, viewMonth, 15))
        val last = engine.lunarOf(McDate(viewYear, viewMonth, mcDaysInMonth(viewYear, viewMonth)))
        val monthPart = if (first.month == last.month) {
            "tháng ${first.month}"
        } else {
            "tháng ${first.month}–${last.month}"
        }
        "Năm ${mid.yearCanChi} • Âm lịch $monthPart"
    }

    val monthEvents = remember(viewYear, viewMonth) {
        (1..mcDaysInMonth(viewYear, viewMonth))
            .map { engine.dayInfo(McDate(viewYear, viewMonth, it)) }
            .filter { it.events.isNotEmpty() }
    }

    val cardShape = RoundedCornerShape(24.dp)
    val headers = if (MC_WEEK_STARTS_MONDAY) {
        listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    } else {
        listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // ---------- Thẻ lưới tháng ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(palette.cardBg)
                .background(palette.liquidGlassBrush)
                .border(1.dp, palette.liquidGlassBorder, cardShape)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "LỊCH THÁNG",
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = palette.textSecondary,
                    fontWeight = FontWeight.Bold
                )
                McPill(
                    text = "Hôm nay",
                    color = accent,
                    filled = false,
                    textColor = accent,
                    onClick = {
                        viewYear = today.year
                        viewMonth = today.month
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                McArrowButton("‹", palette, accent) { shiftMonth(-1) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Tháng $viewMonth / $viewYear",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        lunarRangeLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                McArrowButton("›", palette, accent) { shiftMonth(1) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                headers.forEach { h ->
                    Text(
                        h,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (h == "CN") MC_RED else palette.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Vuốt trái/phải để đổi tháng
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        var acc = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { acc = 0f },
                            onDragEnd = {
                                if (acc > 90f) shiftMonth(-1) else if (acc < -90f) shiftMonth(1)
                            },
                            onDragCancel = { acc = 0f }
                        ) { _, dx -> acc += dx }
                    }
            ) {
                Crossfade(
                    targetState = viewYear * 12 + (viewMonth - 1),
                    animationSpec = tween(160),
                    label = "month_grid_fade"
                ) { key ->
                    McMonthGrid(
                        year = key / 12,
                        month = key % 12 + 1,
                        engine = engine,
                        today = today,
                        palette = palette,
                        accent = accent,
                        hidden = hidden,
                        scorer = scorer,
                        onDayClick = onDayClick
                    )
                }
            }
        }

        // ---------- Lọc chấm (chạm để ẩn / hiện) ----------
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                MC_FILTERS.forEach { (marker, label) ->
                    McFilterChip(
                        color = mcMarkerColor(marker),
                        label = label,
                        on = marker !in hidden,
                        palette = palette
                    ) {
                        hidden = if (marker in hidden) hidden - marker else hidden + marker
                        McPrefs.setHiddenMarkers(context, hidden)
                    }
                }
            }
            Text(
                "Chạm vào từng mục để ẩn / hiện chấm trên lưới.",
                fontSize = 10.5.sp,
                color = palette.textSecondary
            )
        }

        // ---------- Tìm ngày tốt theo việc ----------
        McFinderCard(
            palette = palette,
            accent = accent,
            selected = activity,
            onSelect = { finderActivity = it?.name },
            birth = birth,
            onBirthSaved = {
                birth = it
                McPrefs.setBirth(context, it)
            },
            goodDays = goodDays,
            onClear = {
                finderActivity = null
                birth = null
                McPrefs.clearBirth(context)
            },
            onDayClick = onDayClick
        )

        // ---------- Sự kiện trong tháng ----------
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(palette.cardBg)
                .background(palette.liquidGlassBrush)
                .border(1.dp, palette.liquidGlassBorder, cardShape)
                .padding(14.dp)
        ) {
            Text(
                "SỰ KIỆN TRONG THÁNG",
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = palette.textSecondary,
                fontWeight = FontWeight.Bold
            )
            if (monthEvents.isEmpty()) {
                Text(
                    "Tháng này chưa có ngày lễ, tiết khí, ngày giỗ hay ngày Rằm / Mùng 1 nào.",
                    fontSize = 12.5.sp,
                    color = palette.textSecondary
                )
            } else {
                monthEvents.forEach { info ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDayClick(info.date) }
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accent.copy(alpha = 0.14f))
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                "${info.date.day}/${info.date.month}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.textPrimary,
                                maxLines = 1
                            )
                            Text(
                                MC_WEEKDAY_SHORT[info.weekday],
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (info.weekday == Calendar.SUNDAY) MC_RED else palette.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            info.events.forEach { ev ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 5.dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(mcMarkerColor(ev.marker))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        ev.text,
                                        fontSize = 12.5.sp,
                                        color = palette.textPrimary
                                    )
                                }
                            }
                            Text(
                                "Âm lịch ${info.lunarDay}/${info.lunarMonth}" +
                                    (if (info.lunarLeap) " nhuận" else ""),
                                fontSize = 10.5.sp,
                                color = palette.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun McMonthGrid(
    year: Int,
    month: Int,
    engine: McEngine,
    today: McDate,
    palette: AdaptivePalette,
    accent: Color,
    hidden: Set<McMarker>,
    scorer: McScorer?,
    onDayClick: (McDate) -> Unit
) {
    val cells = remember(year, month) { mcMonthGrid(year, month) }
    val infos = remember(year, month) { cells.map { engine.dayInfo(it) } }
    val scores = remember(year, month, scorer) {
        if (scorer == null) null else cells.associateWith { scorer.score(it) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (r in 0 until cells.size / 7) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (c in 0 until 7) {
                    val info = infos[r * 7 + c]
                    McDayCell(
                        info = info,
                        inMonth = info.date.month == month,
                        isToday = info.date == today,
                        isPast = info.date.isBefore(today),
                        hidden = hidden,
                        score = scores?.get(info.date),
                        palette = palette,
                        accent = accent,
                        onClick = { onDayClick(info.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun McDayCell(
    info: McDayInfo,
    inMonth: Boolean,
    isToday: Boolean,
    isPast: Boolean,
    hidden: Set<McMarker>,
    score: Int?,
    palette: AdaptivePalette,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    // Tô sáng ngày phù hợp (chỉ ngày trong tháng, từ hôm nay trở đi): 2 = rất tốt, 1 = tốt
    val finderOn = score != null
    val level = when {
        score == null || !inMonth || isPast -> 0
        score >= MC_GREAT_MIN -> 2
        score >= MC_GOOD_MIN -> 1
        else -> 0
    }
    val shownMarkers = info.markers.filter { it !in hidden }
    val isSunday = info.weekday == Calendar.SUNDAY
    val solarColor = when {
        isToday -> palette.onAccentText
        isSunday -> MC_RED
        else -> palette.textPrimary
    }
    val lunarColor = when {
        isToday -> palette.onAccentText.copy(alpha = 0.85f)
        info.isMungMot || info.isRam -> Color(0xFFE08A00)
        else -> palette.textSecondary
    }
    // Mùng 1 ghi cả tháng (vd 1/10), các ngày khác chỉ ghi ngày Âm
    val lunarText = if (info.isMungMot) {
        "${info.lunarDay}/${info.lunarMonth}" + (if (info.lunarLeap) "n" else "")
    } else {
        info.lunarDay.toString()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(60.dp)
            .graphicsLayer {
                alpha = when {
                    !inMonth -> 0.35f
                    finderOn && level == 0 && !isToday -> 0.55f
                    else -> 1f
                }
            }
            .clip(shape)
            .background(
                when {
                    isToday -> accent
                    level == 2 -> MC_GOOD.copy(alpha = 0.30f)
                    level == 1 -> MC_GOOD.copy(alpha = 0.14f)
                    else -> palette.subtleBg.copy(alpha = 0.55f)
                }
            )
            .then(
                if (level > 0) {
                    Modifier.border(if (level == 2) 1.8.dp else 1.dp, MC_GOOD.copy(alpha = if (level == 2) 1f else 0.6f), shape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp)
    ) {
        Text(
            info.date.day.toString(),
            fontSize = 16.sp,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = solarColor,
            maxLines = 1
        )
        Text(
            lunarText,
            fontSize = 10.5.sp,
            fontWeight = if (info.isMungMot || info.isRam) FontWeight.Bold else FontWeight.Normal,
            color = lunarColor,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(5.dp)
        ) {
            shownMarkers.forEach { m ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(mcMarkerColor(m))
                )
            }
        }
    }
}

/** Chip bật/tắt một loại chấm trên lưới. */
@Composable
private fun McFilterChip(
    color: Color,
    label: String,
    on: Boolean,
    palette: AdaptivePalette,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(shape)
            .background(if (on) color.copy(alpha = 0.14f) else palette.subtleBg.copy(alpha = 0.5f))
            .border(1.dp, if (on) color.copy(alpha = 0.55f) else palette.textSecondary.copy(alpha = 0.3f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .then(
                    if (on) Modifier.background(color)
                    else Modifier.border(1.2.dp, palette.textSecondary.copy(alpha = 0.6f), CircleShape)
                )
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
            color = if (on) palette.textPrimary else palette.textSecondary.copy(alpha = 0.8f),
            textDecoration = if (on) TextDecoration.None else TextDecoration.LineThrough,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun McNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    maxLen: Int,
    palette: AdaptivePalette,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    BasicTextField(
        value = value,
        onValueChange = { v -> if (v.length <= maxLen && v.all { it.isDigit() }) onValueChange(v) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(
            color = palette.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(accent),
        decorationBox = { inner ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(palette.subtleBg)
                    .border(1.dp, accent.copy(alpha = 0.45f), shape)
                    .padding(horizontal = 6.dp, vertical = 9.dp)
            ) {
                if (value.isEmpty()) {
                    Text(hint, fontSize = 13.sp, color = palette.textSecondary.copy(alpha = 0.7f))
                }
                inner()
            }
        },
        modifier = modifier
    )
}

/** Thẻ "Tìm ngày tốt": chọn việc, nhập ngày sinh một lần, lưới tháng sẽ tô sáng ngày phù hợp. */
@Composable
private fun McFinderCard(
    palette: AdaptivePalette,
    accent: Color,
    selected: AuspiciousActivity?,
    onSelect: (AuspiciousActivity?) -> Unit,
    birth: McBirthProfile?,
    onBirthSaved: (McBirthProfile) -> Unit,
    goodDays: List<Pair<McDate, Int>>,
    onClear: () -> Unit,
    onDayClick: (McDate) -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)
    var editing by remember { mutableStateOf(false) }
    var dText by remember { mutableStateOf(birth?.day?.toString() ?: "") }
    var mText by remember { mutableStateOf(birth?.month?.toString() ?: "") }
    var yText by remember { mutableStateOf(birth?.year?.toString() ?: "") }
    var male by remember { mutableStateOf(birth?.isMale ?: true) }
    var error by remember { mutableStateOf<String?>(null) }
    val showForm = birth == null || editing
    // Xóa kết quả đã tìm: bỏ chọn việc (hết tô sáng) + xóa ngày sinh đã lưu + xóa ô nhập
    val clearAll = {
        dText = ""
        mText = ""
        yText = ""
        male = true
        error = null
        editing = false
        onClear()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(palette.cardBg)
            .background(palette.liquidGlassBrush)
            .border(1.dp, palette.liquidGlassBorder, cardShape)
            .padding(14.dp)
    ) {
        Text(
            "TÌM NGÀY TỐT THEO VIỆC",
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            color = palette.textSecondary,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            AuspiciousActivity.entries.forEach { a ->
                val on = a == selected
                McPill(
                    text = a.label.substringBefore(" /"),
                    color = accent,
                    filled = on,
                    textColor = if (on) palette.onAccentText else palette.textPrimary,
                    onClick = { onSelect(if (on) null else a) }
                )
            }
        }

        if (selected == null) {
            Text(
                "Chọn một việc để tô sáng các ngày phù hợp trên lưới lịch.",
                fontSize = 12.sp,
                color = palette.textSecondary
            )
            if (birth != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Đã lưu: sinh ${birth.day}/${birth.month}/${birth.year} • ${if (birth.isMale) "Nam" else "Nữ"}",
                        fontSize = 12.sp,
                        color = palette.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    McPill("Xóa", MC_RED, false, MC_RED, clearAll)
                }
            }
        } else if (showForm) {
            Text(
                "Nhập ngày sinh để chấm điểm theo tuổi",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                McNumberField(dText, { dText = it }, "Ngày", 2, palette, accent, Modifier.weight(1f))
                McNumberField(mText, { mText = it }, "Tháng", 2, palette, accent, Modifier.weight(1f))
                McNumberField(yText, { yText = it }, "Năm", 4, palette, accent, Modifier.weight(1.4f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                McPill("Nam", accent, male, if (male) palette.onAccentText else palette.textPrimary,
                    { male = true }, Modifier.weight(1f))
                McPill("Nữ", accent, !male, if (!male) palette.onAccentText else palette.textPrimary,
                    { male = false }, Modifier.weight(1f))
            }
            error?.let { Text(it, fontSize = 11.5.sp, color = MC_RED) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            McPill(
                text = "Lưu",
                color = accent,
                filled = true,
                textColor = palette.onAccentText,
                onClick = {
                    val parsed = mcParseBirth(dText, mText, yText, male)
                    if (parsed == null) {
                        error = "Ngày sinh không hợp lệ (năm 1900–${McDate.today().year})."
                    } else {
                        error = null
                        editing = false
                        onBirthSaved(parsed)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            McPill(
                text = "Xóa",
                color = MC_RED,
                filled = false,
                textColor = MC_RED,
                onClick = clearAll,
                modifier = Modifier.weight(1f)
            )
            }
        } else if (birth != null) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Sinh ${birth.day}/${birth.month}/${birth.year} • ${if (birth.isMale) "Nam" else "Nữ"}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    McPill("Sửa", accent, false, accent, { editing = true })
                    McPill("Xóa", MC_RED, false, MC_RED, clearAll)
                }
            }

            val great = goodDays.count { it.second >= MC_GREAT_MIN }
            if (goodDays.isEmpty()) {
                Text(
                    "Từ hôm nay đến hết tháng này chưa có ngày nào phù hợp cho " +
                        "${selected.label.substringBefore(" /").lowercase()}. Thử vuốt sang tháng sau.",
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
            } else {
                Text(
                    "${goodDays.size} ngày phù hợp trong tháng ($great ngày rất tốt). Gợi ý nhất:",
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    goodDays.sortedByDescending { it.second }.take(3).forEach { (d, sc) ->
                        McPill(
                            text = "${d.day}/${d.month} • $sc điểm",
                            color = MC_GOOD,
                            filled = false,
                            textColor = palette.textPrimary,
                            onClick = { onDayClick(d) }
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MC_GOOD.copy(alpha = 0.30f))
                            .border(1.5.dp, MC_GOOD, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rất tốt (≥$MC_GREAT_MIN)", fontSize = 10.5.sp, color = palette.textSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MC_GOOD.copy(alpha = 0.14f))
                            .border(1.dp, MC_GOOD.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tốt ($MC_GOOD_MIN–${MC_GREAT_MIN - 1})", fontSize = 10.5.sp, color = palette.textSecondary)
                }
            }
            Text(
                "Điểm mang tính tham khảo, tính theo ngày Hoàng/Hắc đạo, ngày Âm và tuổi.",
                fontSize = 10.sp,
                color = palette.textSecondary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun McArrowButton(
    symbol: String,
    palette: AdaptivePalette,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(palette.subtleBg)
            .border(1.dp, accent.copy(alpha = 0.45f), CircleShape)
            .clickable(onClick = onClick)
    ) {
        Text(
            symbol,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
    }
}

@Composable
private fun McPill(
    text: String,
    color: Color,
    filled: Boolean,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .background(if (filled) color else color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = if (filled) 0f else 0.55f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

// ============================================================
//  POP-UP TỜ LỊCH XÉ (Liquid Glass)
// ============================================================

/**
 * Pop-up chi tiết ngày: một tờ lịch bloc có nền kính (Liquid Glass).
 * - Vuốt LÊN (hoặc bấm "Xé tờ") để xé sang ngày sau, vuốt XUỐNG (hoặc "Ngày trước") để xem ngày trước.
 * - Đặt trong AnimatedPopupOverlay.
 */
@Composable
internal fun McTearOffSheet(
    startDate: McDate,
    palette: AdaptivePalette,
    accent: Color,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val engine = remember { McEngine(context) }
    var nav by remember(startDate) { mutableStateOf(McNav(startDate, 0)) }
    val tear = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var soundOn by remember { mutableStateOf(McPrefs.soundOn(context)) }
    // Mỗi lần mở pop-up đổi 'hạt giống' => nền, hiệu ứng và bó hoa mỗi tờ mỗi khác
    val salt = remember { Random.nextInt() }
    val tearSound = remember { McTearSound(context) }
    DisposableEffect(Unit) { onDispose { tearSound.release() } }
    val toggleSound = {
        soundOn = !soundOn
        McPrefs.setSoundOn(context, soundOn)
    }
    var sheetHeightPx by remember { mutableStateOf(1500f) }
    val thresholdPx = with(LocalDensity.current) { 70.dp.toPx() }
    val sheetH = minOf(560, (LocalConfiguration.current.screenHeightDp * 0.86f).toInt()).dp

    // dir = +1: xé tờ hiện tại bay lên, lộ ngày sau.  dir = -1: tờ ngày trước rơi xuống phủ lên.
    fun go(dir: Int) {
        if (nav.dir != 0) return
        if (dir == 1 && soundOn) tearSound.play() // chỉ phát khi XÉ (sang ngày sau)
        scope.launch {
            tear.snapTo(0f)
            nav = McNav(nav.date, dir)
            tear.animateTo(1f, tween(durationMillis = 360, easing = FastOutSlowInEasing))
            nav = McNav(nav.date.plusDays(dir), 0)
            tear.snapTo(0f)
        }
    }

    val cur = nav
    val topDate = if (cur.dir == -1) cur.date.plusDays(-1) else cur.date
    val underDate: McDate? = when (cur.dir) {
        1 -> cur.date.plusDays(1)
        -1 -> cur.date
        else -> null
    }
    val torn = when (cur.dir) {
        1 -> tear.value
        -1 -> 1f - tear.value
        else -> 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 360.dp)
            .height(sheetH + 16.dp)
            .drawBehind {
                // Hai lớp giấy xếp chồng phía sau => cảm giác cuốn bloc lịch dày
                val r = 22.dp.toPx()
                val layerColor = if (palette.isDark) Color(0xFF2B2D36) else Color(0xFFE4E0D6)
                drawRoundRect(
                    color = layerColor.copy(alpha = 0.55f),
                    topLeft = Offset(16.dp.toPx(), 16.dp.toPx()),
                    size = Size(size.width - 32.dp.toPx(), size.height - 16.dp.toPx()),
                    cornerRadius = CornerRadius(r, r)
                )
                drawRoundRect(
                    color = layerColor.copy(alpha = 0.85f),
                    topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                    size = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
                    cornerRadius = CornerRadius(r, r)
                )
            }
            .pointerInput(Unit) {
                var acc = 0f
                detectVerticalDragGestures(
                    onDragStart = { acc = 0f },
                    onDragEnd = {
                        if (acc < -thresholdPx) go(1) else if (acc > thresholdPx) go(-1)
                    },
                    onDragCancel = { acc = 0f }
                ) { _, dy -> acc += dy }
            }
    ) {
        if (underDate != null) {
            McPaper(
                date = underDate,
                engine = engine,
                palette = palette,
                accent = accent,
                onClose = onClose,
                onPrev = {},
                onNext = {},
                soundOn = soundOn,
                onToggleSound = toggleSound,
                salt = salt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetH)
            )
        }
        McPaper(
            date = topDate,
            engine = engine,
            palette = palette,
            accent = accent,
            onClose = onClose,
            onPrev = { go(-1) },
            onNext = { go(1) },
            soundOn = soundOn,
            onToggleSound = toggleSound,
            salt = salt,
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetH)
                .onSizeChanged { sheetHeightPx = it.height.toFloat() }
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.2f, 0f)
                    translationY = -torn * sheetHeightPx * 1.25f
                    rotationZ = -9f * torn
                    alpha = 1f - ((torn - 0.6f) / 0.4f).coerceIn(0f, 1f)
                }
        )
    }
}

@Composable
private fun McPaper(
    date: McDate,
    engine: McEngine,
    palette: AdaptivePalette,
    accent: Color,
    onClose: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    soundOn: Boolean,
    onToggleSound: () -> Unit,
    salt: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val jd = remember(date) { mcJdFromDate(date.day, date.month, date.year) }
    val theme = remember(date, salt) { mcThemeFor(date, salt) }
    val variantSeed = remember(date, salt) { mcMix(salt xor 0x5bd1e995, jd) }
    val particles = remember(variantSeed) { mcParticles(variantSeed xor 0x1234567, 34) }
    val fx = rememberInfiniteTransition(label = "paper_fx")
    val phase by fx.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 16000, easing = LinearEasing)),
        label = "paper_phase"
    )
    val info = remember(date) { engine.dayInfo(date) }
    val lunarTag = remember(date) { engine.lunarMonthTag(date) }
    val hours = remember(date) { engine.goodHours(info.dayChi) }
    val headline = remember(date) { engine.blessing(info) }
    val firstHour = remember(date) { engine.firstHourCanChi(info) }
    val note = remember(date) { AppPrefs.getLunarDayNote(context, date.key()).trim() }

    val numColor = mcWeekdayColor(info.weekday)
    val shape = RoundedCornerShape(22.dp)
    val paperBase = if (palette.isDark) Color(0xFF15161B) else Color(0xFFFFFCF6)
    val numBrush = Brush.verticalGradient(
        listOf(
            lerp(numColor, Color.White, 0.35f),
            numColor,
            lerp(numColor, Color.Black, 0.20f)
        )
    )
    val hdColor = if (info.isHoangDao) MC_GREEN else Color(0xFF6B7280)
    val lunarMonthWord = MC_LUNAR_MONTH_WORD[(info.lunarMonth - 1).coerceIn(0, 11)]

    Box(
        modifier = modifier
            .shadow(14.dp, shape)
            .clip(shape)
            .background(paperBase)
            .background(palette.liquidGlassBrush)
            .border(1.4.dp, palette.liquidGlassBorder, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
    ) {
        // Nền + hiệu ứng riêng của từng tờ (nằm dưới chữ)
        if (theme != McPaperTheme.CLASSIC) {
            Canvas(modifier = Modifier.matchParentSize()) {
                mcDrawPaperTheme(theme, phase, particles, palette.isDark)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ---------- Dải đỏ đầu tờ lịch ----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFE53935), Color(0xFFB71C1C)))
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.26f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        date.year.toString(),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Text(
                        "THÁNG ${MC_SOLAR_MONTH_WORD[(date.month - 1).coerceIn(0, 11)]}",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Text(
                    headline,
                    color = Color(0xFFFFE082),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                        .clickable(onClick = onToggleSound)
                ) {
                    Text(if (soundOn) "🔊" else "🔇", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                        .clickable(onClick = onClose)
                ) {
                    Text("✕", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ---------- Thân tờ lịch ----------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 10.dp)
            ) {
                // Số ngày to + thứ + ngày Âm
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(142.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            date.day.toString(),
                            maxLines = 1,
                            softWrap = false,
                            style = TextStyle(
                                brush = numBrush,
                                fontSize = 104.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 108.sp,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.28f),
                                    offset = Offset(3f, 6f),
                                    blurRadius = 8f
                                )
                            )
                        )
                        // Lỗ khuyên của bloc lịch
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 14.dp, end = 4.dp)
                                .size(13.dp)
                                .border(3.dp, Color(0xFFFFC107), CircleShape)
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            MC_WEEKDAY_VN[info.weekday],
                            color = numColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            MC_WEEKDAY_EN[info.weekday],
                            color = palette.popupTextSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            info.lunarDay.toString(),
                            maxLines = 1,
                            style = TextStyle(
                                brush = numBrush,
                                fontSize = 50.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 54.sp,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.22f),
                                    offset = Offset(2f, 4f),
                                    blurRadius = 6f
                                )
                            )
                        )
                        Text(
                            "THÁNG $lunarMonthWord" +
                                (if (info.lunarLeap) " NHUẬN" else "") +
                                " ($lunarTag)",
                            color = numColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Can chi + hoa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        McCanChiLine("Năm", info.yearCanChi, numColor, palette)
                        McCanChiLine("Tháng", info.monthCanChi, numColor, palette)
                        McCanChiLine("Ngày", info.dayCanChi, numColor, palette)
                        McCanChiLine("Giờ", firstHour, numColor, palette)
                    }
                    McBouquet(
                        color = numColor,
                        seed = variantSeed,
                        modifier = Modifier.size(width = 96.dp, height = 74.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Hoàng đạo / Hắc đạo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(hdColor.copy(alpha = 0.16f))
                        .border(1.dp, hdColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(hdColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        (if (info.isHoangDao) "Ngày Hoàng Đạo" else "Ngày Hắc Đạo") + " • " + info.dayStar,
                        color = palette.popupTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Giờ hoàng đạo
                Text(
                    "Giờ Hoàng Đạo",
                    color = palette.popupTextSecondary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                for (row in 0 until 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (row == 0) 4.dp else 0.dp)
                    ) {
                        for (col in 0 until 3) {
                            val h = hours.getOrNull(row * 3 + col) ?: ""
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MC_GREEN.copy(alpha = 0.13f))
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    h,
                                    color = palette.popupTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Sự kiện / ngày lễ / ngày giỗ / ghi chú (vùng này tự cuộn nếu dài)
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (info.events.isEmpty() && note.isEmpty()) {
                        Text(
                            "Không có ngày lễ hay sự kiện nào trong ngày.",
                            color = palette.popupTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    info.events.forEach { ev ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(mcMarkerColor(ev.marker))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                ev.text,
                                color = palette.popupTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (note.isNotEmpty()) {
                        Text(
                            "✎ Ghi chú: $note",
                            color = palette.popupTextSecondary,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    McPill(
                        text = "‹ Ngày trước",
                        color = accent,
                        filled = false,
                        textColor = palette.popupTextPrimary,
                        onClick = onPrev,
                        modifier = Modifier.weight(1f)
                    )
                    McPill(
                        text = "Xé tờ ↑",
                        color = accent,
                        filled = true,
                        textColor = palette.onAccentText,
                        onClick = onNext,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Vệt sáng chéo kiểu kính
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val streak = Path().apply {
                moveTo(w * 0.52f, 0f)
                lineTo(w * 0.78f, 0f)
                lineTo(w * 0.30f, h)
                lineTo(w * 0.04f, h)
                close()
            }
            drawPath(
                path = streak,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = if (palette.isDark) 0.07f else 0.22f),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, 0f)
                )
            )
        }
    }
}

@Composable
private fun McCanChiLine(label: String, value: String, color: Color, palette: AdaptivePalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .graphicsLayer { rotationZ = 45f }
                .background(color)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = palette.popupTextSecondary,
            maxLines = 1,
            modifier = Modifier.width(46.dp)
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = palette.popupTextPrimary,
            maxLines = 1
        )
    }
}

// ============================================================
//  NỀN + HIỆU ỨNG NGẪU NHIÊN CHO TỜ LỊCH
// ============================================================

private enum class McPaperTheme(val tint: Color) {
    CLASSIC(Color.Transparent),      // kính trong, chỉ có vệt sáng chéo
    SAKURA(Color(0xFFF48FB1)),       // cánh hoa anh đào rơi
    STARS(Color(0xFF5C6BC0)),        // sao nhấp nháy
    BOKEH(Color(0xFF26C6DA)),        // bong bóng sáng bay lên
    WAVES(Color(0xFF29B6F6)),        // sóng nước nhẹ dưới chân tờ lịch
    SUNBURST(Color(0xFFFFB300)),     // tia nắng đung đưa
    LATTICE(Color(0xFFE53935))       // hoa văn ô trám + ánh kim quét ngang
}

private data class McParticle(
    val x: Float,
    val y: Float,
    val s: Float,
    val r: Float,
    val k: Int,    // 1 hoặc 2: số vòng lặp nguyên => hiệu ứng lặp liền mạch
    val hue: Int
)

private fun mcMix(a: Int, b: Int): Int {
    var h = a * -1640531535 + b
    h = h xor (h ushr 15)
    h *= 0x2c1b3c6d
    h = h xor (h ushr 12)
    return h
}

/** Chọn kiểu nền cho ngày [d]; ngày liền kề luôn khác kiểu để mỗi lần xé thấy đổi hẳn. */
private fun mcThemeFor(d: McDate, salt: Int): McPaperTheme {
    val n = McPaperTheme.entries.size
    fun raw(x: McDate) = Math.floorMod(mcMix(salt, mcJdFromDate(x.day, x.month, x.year)), n)
    val t = raw(d)
    return McPaperTheme.entries[if (t == raw(d.plusDays(-1))) (t + 1) % n else t]
}

private fun mcParticles(seed: Int, n: Int): List<McParticle> {
    val rng = Random(seed)
    return List(n) {
        McParticle(
            x = rng.nextFloat(), y = rng.nextFloat(), s = rng.nextFloat(), r = rng.nextFloat(),
            k = 1 + rng.nextInt(2), hue = rng.nextInt(3)
        )
    }
}

/** Cánh hoa/lá hình giọt nước, mũi chỉ lên trên, gốc tại (cx, cy). */
private fun DrawScope.mcPetal(cx: Float, cy: Float, len: Float, wid: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy)
        cubicTo(cx - wid, cy - len * 0.45f, cx - wid * 0.6f, cy - len, cx, cy - len)
        cubicTo(cx + wid * 0.6f, cy - len, cx + wid, cy - len * 0.45f, cx, cy)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.mcDrawPaperTheme(
    theme: McPaperTheme,
    phase: Float,
    ps: List<McParticle>,
    dark: Boolean
) {
    val w = size.width
    val h = size.height
    val tau = (2 * PI).toFloat()
    val dp1 = density

    // Lớp màu nền chuyển sắc nhẹ theo chủ đề
    drawRect(
        Brush.verticalGradient(
            listOf(
                theme.tint.copy(alpha = if (dark) 0.26f else 0.20f),
                Color.Transparent,
                theme.tint.copy(alpha = if (dark) 0.14f else 0.10f)
            )
        )
    )

    when (theme) {
        McPaperTheme.CLASSIC -> Unit

        McPaperTheme.SAKURA -> for (p in ps.take(16)) {
            val prog = (p.y + phase * p.k) % 1f
            val x = (p.x + 0.05f * sin(tau * (prog * 2f + p.r))) * w
            val y = prog * h
            val len = (6f + 7f * p.s) * dp1
            rotate(360f * ((p.r + phase * p.k) % 1f), Offset(x, y)) {
                mcPetal(x, y, len, len * 0.55f, Color(0xFFF48FB1).copy(alpha = 0.60f))
            }
        }

        McPaperTheme.STARS -> {
            val starColor = if (dark) Color(0xFFFFF3C4) else Color(0xFFFFB300)
            for (p in ps) {
                val tw = 0.35f + 0.65f * (0.5f + 0.5f * sin(tau * (phase * (1 + p.k) + p.r)))
                val cx = p.x * w
                val cy = p.y * h * 0.92f
                val r = (1.2f + 2.4f * p.s) * dp1
                val c = starColor.copy(alpha = tw)
                drawCircle(c, r * 0.55f, Offset(cx, cy))
                if (p.s > 0.45f) {
                    drawLine(c, Offset(cx - r * 1.8f, cy), Offset(cx + r * 1.8f, cy), strokeWidth = dp1)
                    drawLine(c, Offset(cx, cy - r * 1.8f), Offset(cx, cy + r * 1.8f), strokeWidth = dp1)
                }
            }
        }

        McPaperTheme.BOKEH -> {
            val cols = listOf(Color(0xFFFF80AB), Color(0xFF4DD0E1), Color(0xFFFFD54F))
            for (p in ps.take(12)) {
                val prog = (p.y + phase * p.k) % 1f
                val c = Offset((p.x + 0.03f * sin(tau * (prog * 3f + p.r))) * w, (1f - prog) * h)
                val r = (8f + 20f * p.s) * dp1
                val col = cols[p.hue]
                drawCircle(col.copy(alpha = 0.13f), r, c)
                drawCircle(col.copy(alpha = 0.30f), r, c, style = Stroke(1.2f * dp1))
            }
        }

        McPaperTheme.WAVES -> {
            val layers = listOf(Triple(0.74f, 0.030f, 1), Triple(0.80f, 0.026f, -1), Triple(0.86f, 0.022f, 2))
            layers.forEachIndexed { i, (base, amp, k) ->
                val path = Path()
                path.moveTo(0f, h)
                val step = w / 48f
                var x = 0f
                while (x <= w + 0.1f) {
                    val y = h * base + h * amp * sin(tau * (x / w * (1.3f + i * 0.4f) + phase * k))
                    path.lineTo(x, y)
                    x += step
                }
                path.lineTo(w, h)
                path.close()
                drawPath(path, Color(0xFF29B6F6).copy(alpha = 0.10f + 0.04f * i))
            }
        }

        McPaperTheme.SUNBURST -> {
            val origin = Offset(w * 0.8f, -h * 0.05f)
            val sway = 6.0 * sin(tau * phase)
            val rays = 14
            val radius = h * 1.6f
            for (i in 0 until rays step 2) {
                val a0 = Math.toRadians(i * 360.0 / rays + sway)
                val a1 = Math.toRadians((i + 1) * 360.0 / rays + sway)
                val path = Path().apply {
                    moveTo(origin.x, origin.y)
                    lineTo(origin.x + radius * cos(a0).toFloat(), origin.y + radius * sin(a0).toFloat())
                    lineTo(origin.x + radius * cos(a1).toFloat(), origin.y + radius * sin(a1).toFloat())
                    close()
                }
                drawPath(path, Color(0xFFFFC107).copy(alpha = if (dark) 0.10f else 0.14f))
            }
        }

        McPaperTheme.LATTICE -> {
            val gap = 30f * dp1
            val lc = Color(0xFFE53935).copy(alpha = if (dark) 0.12f else 0.10f)
            var d = -h
            while (d < w + h) {
                drawLine(lc, Offset(d, 0f), Offset(d + h, h), strokeWidth = dp1)
                drawLine(lc, Offset(d + h, 0f), Offset(d, h), strokeWidth = dp1)
                d += gap
            }
            // Dải ánh kim quét ngang qua tờ lịch
            val bandX = phase * (w + h) - h * 0.5f
            drawRect(
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.16f), Color.Transparent),
                    start = Offset(bandX - 120f * dp1, 0f),
                    end = Offset(bandX + 120f * dp1, h * 0.4f)
                )
            )
        }
    }
}

// ============================================================
//  BÓ HOA NGẪU NHIÊN (6 loài hoa, bố cục + lá khác nhau mỗi tờ)
// ============================================================

private fun DrawScope.mcDrawFlower(style: Int, c: Offset, r: Float, col: Color) {
    val yellow = Color(0xFFFFE082)
    when (style) {
        0 -> { // Cúc: 6 cánh tròn
            for (i in 0 until 6) {
                val a = (i * 60.0 - 90.0) * PI / 180.0
                drawCircle(
                    col.copy(alpha = 0.92f), r * 0.5f,
                    Offset(c.x + cos(a).toFloat() * r * 0.55f, c.y + sin(a).toFloat() * r * 0.55f)
                )
            }
            drawCircle(yellow, r * 0.32f, c)
        }
        1 -> { // Anh đào: 5 cánh bầu + nhụy
            for (i in 0 until 5) {
                rotate(i * 72f, c) { mcPetal(c.x, c.y, r, r * 0.62f, col.copy(alpha = 0.95f)) }
            }
            drawCircle(yellow, r * 0.18f, c)
            for (i in 0 until 5) {
                val a = (i * 72.0 - 90.0) * PI / 180.0
                drawCircle(
                    Color(0xFFFFB300), r * 0.05f,
                    Offset(c.x + cos(a).toFloat() * r * 0.3f, c.y + sin(a).toFloat() * r * 0.3f)
                )
            }
        }
        2 -> { // Sen: 2 lớp cánh nhọn
            for (i in 0 until 8) {
                rotate(i * 45f, c) { mcPetal(c.x, c.y, r * 1.05f, r * 0.38f, col.copy(alpha = 0.85f)) }
            }
            for (i in 0 until 8) {
                rotate(i * 45f + 22.5f, c) { mcPetal(c.x, c.y, r * 0.72f, r * 0.30f, lerp(col, Color.White, 0.35f)) }
            }
            drawCircle(yellow, r * 0.2f, c)
        }
        3 -> { // Hướng dương: nhiều cánh mảnh + nhụy nâu
            for (i in 0 until 14) {
                rotate(i * (360f / 14f), c) { mcPetal(c.x, c.y, r * 1.05f, r * 0.2f, col.copy(alpha = 0.95f)) }
            }
            drawCircle(Color(0xFF5D4037), r * 0.38f, c)
            drawCircle(Color(0xFF8D6E63), r * 0.38f, c, style = Stroke(r * 0.07f))
        }
        4 -> { // Hồng: các vòng cánh cuộn vào trong
            drawCircle(col, r * 0.9f, c)
            for (i in 1..4) {
                drawCircle(
                    lerp(col, Color.Black, 0.10f * i), r * (0.9f - 0.18f * i),
                    Offset(c.x + r * 0.03f * i, c.y - r * 0.02f * i), style = Stroke(r * 0.07f)
                )
            }
        }
        else -> { // Tulip: 3 cánh úp lại
            val b = Offset(c.x, c.y + r * 0.55f)
            rotate(-24f, b) { mcPetal(b.x, b.y, r * 1.25f, r * 0.5f, lerp(col, Color.Black, 0.10f)) }
            rotate(24f, b) { mcPetal(b.x, b.y, r * 1.25f, r * 0.5f, lerp(col, Color.Black, 0.10f)) }
            mcPetal(b.x, b.y, r * 1.35f, r * 0.55f, col)
        }
    }
}

/**
 * Bó 3 bông vẽ bằng Canvas. [seed] quyết định: loài hoa (6 loại, hoặc mỗi bông một loại),
 * vị trí hơi lệch, số lá. Màu chủ đạo vẫn theo thứ trong tuần.
 */
@Composable
private fun McBouquet(color: Color, seed: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val rng = Random(seed)
        val w = size.width
        val h = size.height
        val stem = Color(0xFF2E7D32)
        val stemW = 2.5.dp.toPx()

        val style = rng.nextInt(7) // 0..5: cả bó một loài; 6: mỗi bông một loài
        fun jit() = (rng.nextFloat() - 0.5f) * 0.08f

        val base = Offset(w * 0.5f, h)
        val heads = listOf(
            Offset(w * (0.50f + jit()), h * (0.30f + jit())),
            Offset(w * (0.22f + jit()), h * (0.58f + jit())),
            Offset(w * (0.78f + jit()), h * (0.52f + jit()))
        )
        val radii = listOf(0.26f * w, 0.20f * w, 0.20f * w)
        val tones = listOf(color, lerp(color, Color.White, 0.30f), lerp(color, Color.Black, 0.12f))

        heads.forEach { drawLine(stem, base, it, strokeWidth = stemW, cap = StrokeCap.Round) }

        // Lá: 1–2 chiếc mọc từ cuống hai bông bên
        val leafCount = 1 + rng.nextInt(2)
        for (j in 0 until leafCount) {
            val head = heads[1 + j]
            val t = 0.40f + rng.nextFloat() * 0.25f
            val anchor = Offset(base.x + (head.x - base.x) * t, base.y + (head.y - base.y) * t)
            val angle = if (head.x < base.x) -55f else 55f
            rotate(angle, anchor) {
                mcPetal(anchor.x, anchor.y, h * (0.26f + rng.nextFloat() * 0.08f), h * 0.11f, Color(0xFF388E3C))
            }
        }

        // Vẽ hai bông bên trước, bông giữa sau cùng
        for (i in intArrayOf(1, 2, 0)) {
            val st = if (style == 6) rng.nextInt(6) else style
            mcDrawFlower(st, heads[i], radii[i], tones[i])
        }
    }
}
