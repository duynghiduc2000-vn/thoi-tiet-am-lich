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
    private const val KEY_TET_FLOWER = "tet_flower"

    private fun sp(c: Context) = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun hiddenMarkers(c: Context): Set<McMarker> =
        sp(c).getStringSet(KEY_HIDDEN, emptySet()).orEmpty()
            .mapNotNull { n -> McMarker.entries.firstOrNull { it.name == n } }
            .toSet()

    fun setHiddenMarkers(c: Context, hidden: Set<McMarker>) {
        sp(c).edit().putStringSet(KEY_HIDDEN, hidden.map { it.name }.toSet()).apply()
    }

    /** Hoa Tết: 0 = tự động (ngẫu nhiên mỗi lần mở), 1 = hoa đào, 2 = hoa mai vàng. */
    fun tetFlower(c: Context): Int = sp(c).getInt(KEY_TET_FLOWER, 0).coerceIn(0, 2)

    fun setTetFlower(c: Context, v: Int) {
        sp(c).edit().putInt(KEY_TET_FLOWER, v).apply()
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
    // Hoa Tết: 0 = tự động, 1 = đào, 2 = mai (nhớ lựa chọn cho lần sau)
    var tetFlower by remember { mutableStateOf(McPrefs.tetFlower(context)) }
    val cycleTetFlower = {
        tetFlower = (tetFlower + 1) % 3
        McPrefs.setTetFlower(context, tetFlower)
    }
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
                tetFlower = tetFlower,
                onCycleTetFlower = cycleTetFlower,
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
            tetFlower = tetFlower,
            onCycleTetFlower = cycleTetFlower,
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
    tetFlower: Int,
    onCycleTetFlower: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val jd = remember(date) { mcJdFromDate(date.day, date.month, date.year) }
    val info = remember(date) { engine.dayInfo(date) }
    val theme = remember(date, salt, tetFlower) { mcOccasionTheme(info, tetFlower, salt) ?: mcThemeFor(date, salt) }
    val isTet = theme == McPaperTheme.TET_DAO || theme == McPaperTheme.TET_MAI
    val variantSeed = remember(date, salt) { mcMix(salt xor 0x5bd1e995, jd) }
    val particles = remember(variantSeed) { mcParticles(variantSeed xor 0x1234567, 34) }
    val fx = rememberInfiniteTransition(label = "paper_fx")
    val phase by fx.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 16000, easing = LinearEasing)),
        label = "paper_phase"
    )
    val lunarTag = remember(date) { engine.lunarMonthTag(date) }
    val hours = remember(date) { engine.goodHours(info.dayChi) }
    val headline = remember(date, theme) { mcOccasionHeadline(theme, info) ?: engine.blessing(info) }
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
                if (isTet) {
                    // Chạm để đổi: 🎲 tự động → 🌸 hoa đào → 🌼 hoa mai
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .clickable(onClick = onCycleTetFlower)
                    ) {
                        Text(
                            when (tetFlower) { 1 -> "🌸"; 2 -> "🌼"; else -> "🎲" },
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
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

private enum class McPaperTheme(val tint: Color, val occasion: Boolean = false) {
    CLASSIC(Color.Transparent),      // kính trong, chỉ có vệt sáng chéo
    SAKURA(Color(0xFFF48FB1)),       // cánh hoa anh đào rơi
    STARS(Color(0xFF5C6BC0)),        // sao nhấp nháy
    BOKEH(Color(0xFF26C6DA)),        // bong bóng sáng bay lên
    WAVES(Color(0xFF29B6F6)),        // sóng nước nhẹ dưới chân tờ lịch
    SUNBURST(Color(0xFFFFB300)),     // tia nắng đung đưa
    LATTICE(Color(0xFFE53935)),      // hoa văn ô trám + ánh kim quét ngang

    // ---- Tờ theo dịp: chỉ xuất hiện đúng ngày, KHÔNG nằm trong vòng quay ngẫu nhiên ----
    TET_DAO(Color(0xFFF06292), true),     // cành đào + cánh hoa đào rơi + kim tuyến vàng
    TET_MAI(Color(0xFFFFC107), true),     // cành mai vàng + cánh mai rơi + kim tuyến (miền Nam)
    TRUNG_THU(Color(0xFFFFA000), true),   // trăng rằm + đèn lồng bay lên
    GIANG_SINH(Color(0xFF64B5F6), true),  // tuyết rơi + nền tuyết
    QUOC_KHANH(Color(0xFFDA251D), true),  // lá cờ đỏ sao vàng bay phấp phới (2/9, 30/4)
    VALENTINE(Color(0xFFE91E63), true),   // trái tim đập rung động + tim bay lên
    PHAO_HOA(Color(0xFF7E57C2), true),    // pháo hoa (31/12, 1/1)
    HOA_TUOI(Color(0xFFEC407A), true),    // hoa hồng/tulip/cúc rơi (8/3, 20/10)
    THIEU_NHI(Color(0xFF26C6DA), true),   // bóng bay + hoa giấy (1/6)
    NHA_GIAO(Color(0xFF5C6BC0), true),    // mũ cử nhân rơi + sao (20/11)
    HALLOWEEN(Color(0xFFFF6F00), true),   // bí ngô phát sáng + dơi bay (31/10)
    VU_LAN(Color(0xFF4DB6AC), true)       // hoa sen nổi trên ao + đom đóm (rằm tháng 7)
}

/** Chỉ các theme thường mới được bốc ngẫu nhiên (thứ tự giữ nguyên như trước). */
private val MC_RANDOM_THEMES = McPaperTheme.entries.filterNot { it.occasion }

// Khoảng ngày áp dụng tờ theo dịp (chỉnh số ở đây nếu muốn dài/ngắn hơn)
private const val MC_TET_FROM_DAY_MONTH_12 = 23   // từ 23 tháng Chạp (ông Táo)
private const val MC_TET_TO_DAY_MONTH_1 = 10      // đến mùng 10 tháng Giêng
private const val MC_TRUNG_THU_FROM_DAY = 10      // Trung thu: 10..16 tháng Tám Âm lịch
private const val MC_TRUNG_THU_TO_DAY = 16
private const val MC_NOEL_FROM_DAY = 20           // Giáng sinh: 20..26 tháng 12 Dương lịch
private const val MC_NOEL_TO_DAY = 26

private const val MC_VU_LAN_FROM_DAY = 14        // Vu Lan: 14..16 tháng Bảy Âm lịch
private const val MC_VU_LAN_TO_DAY = 16

private data class McSolarOccasion(val month: Int, val fromDay: Int, val toDay: Int, val theme: McPaperTheme)

/**
 * Các dịp theo ngày Dương lịch. Xếp từ trên xuống: ngày nào trùng nhau thì dòng ở trên thắng
 * (và các dịp này thắng cả Tết/Trung thu Âm lịch). Muốn thêm dịp mới: thêm 1 dòng + 1 theme.
 */
private val MC_SOLAR_OCCASIONS = listOf(
    McSolarOccasion(1, 1, 1, McPaperTheme.PHAO_HOA),                 // Tết Dương lịch
    McSolarOccasion(2, 14, 14, McPaperTheme.VALENTINE),
    McSolarOccasion(3, 8, 8, McPaperTheme.HOA_TUOI),                 // Quốc tế Phụ nữ
    McSolarOccasion(4, 30, 30, McPaperTheme.QUOC_KHANH),             // Giải phóng miền Nam
    McSolarOccasion(6, 1, 1, McPaperTheme.THIEU_NHI),
    McSolarOccasion(9, 1, 3, McPaperTheme.QUOC_KHANH),               // Quốc khánh 2/9 (+ngày liền kề)
    McSolarOccasion(10, 20, 20, McPaperTheme.HOA_TUOI),              // Phụ nữ Việt Nam
    McSolarOccasion(10, 31, 31, McPaperTheme.HALLOWEEN),
    McSolarOccasion(11, 20, 20, McPaperTheme.NHA_GIAO),
    McSolarOccasion(12, MC_NOEL_FROM_DAY, MC_NOEL_TO_DAY, McPaperTheme.GIANG_SINH),
    McSolarOccasion(12, 31, 31, McPaperTheme.PHAO_HOA)
)

/** Trả về theme của dịp lễ nếu [info] rơi vào khoảng ngày trên, ngược lại null (dùng nền ngẫu nhiên). */
private fun mcOccasionTheme(info: McDayInfo, tetFlower: Int, salt: Int): McPaperTheme? {
    val d = info.date
    for (o in MC_SOLAR_OCCASIONS) {
        if (d.month == o.month && d.day in o.fromDay..o.toDay) return o.theme
    }
    if (!info.lunarLeap) {
        if ((info.lunarMonth == 12 && info.lunarDay >= MC_TET_FROM_DAY_MONTH_12) ||
            (info.lunarMonth == 1 && info.lunarDay <= MC_TET_TO_DAY_MONTH_1)
        ) {
            return when (tetFlower) {
                1 -> McPaperTheme.TET_DAO
                2 -> McPaperTheme.TET_MAI
                else -> if (Math.floorMod(mcMix(salt, 0x7e7), 2) == 0) McPaperTheme.TET_DAO else McPaperTheme.TET_MAI
            }
        }
        if (info.lunarMonth == 8 && info.lunarDay in MC_TRUNG_THU_FROM_DAY..MC_TRUNG_THU_TO_DAY) {
            return McPaperTheme.TRUNG_THU
        }
        if (info.lunarMonth == 7 && info.lunarDay in MC_VU_LAN_FROM_DAY..MC_VU_LAN_TO_DAY) {
            return McPaperTheme.VU_LAN
        }
    }
    return null
}

/** Dòng chúc thay cho câu chúc ngẫu nhiên ở dải đỏ đầu tờ, chỉ dùng cho tờ theo dịp. */
private fun mcOccasionHeadline(theme: McPaperTheme, info: McDayInfo): String? = when (theme) {
    McPaperTheme.TET_DAO, McPaperTheme.TET_MAI -> "TẾT ĐẾN XUÂN VỀ"
    McPaperTheme.TRUNG_THU -> "VUI TẾT TRUNG THU"
    McPaperTheme.GIANG_SINH -> "GIÁNG SINH AN LÀNH"
    McPaperTheme.QUOC_KHANH ->
        if (info.date.month == 4) "MỪNG ĐẠI THẮNG 30/4" else "MỪNG QUỐC KHÁNH 2/9"
    McPaperTheme.VALENTINE -> "VALENTINE NGỌT NGÀO"
    McPaperTheme.PHAO_HOA -> "CHÀO NĂM MỚI"
    McPaperTheme.HOA_TUOI ->
        if (info.date.month == 3) "MỪNG NGÀY QUỐC TẾ PHỤ NỮ" else "MỪNG NGÀY PHỤ NỮ VIỆT NAM"
    McPaperTheme.THIEU_NHI -> "NGÀY QUỐC TẾ THIẾU NHI"
    McPaperTheme.NHA_GIAO -> "TRI ÂN NHÀ GIÁO VIỆT NAM"
    McPaperTheme.HALLOWEEN -> "HALLOWEEN VUI VẺ"
    McPaperTheme.VU_LAN -> "MÙA VU LAN BÁO HIẾU"
    else -> null
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
    val n = MC_RANDOM_THEMES.size
    fun raw(x: McDate) = Math.floorMod(mcMix(salt, mcJdFromDate(x.day, x.month, x.year)), n)
    val t = raw(d)
    return MC_RANDOM_THEMES[if (t == raw(d.plusDays(-1))) (t + 1) % n else t]
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

        McPaperTheme.TET_DAO -> {
            val wood = Color(0xFF6D4C41).copy(alpha = if (dark) 0.70f else 0.80f)
            val sw = 3.2f * dp1
            // Cành đào mọc từ góc dưới trái + hai nhánh con
            val main = Path().apply {
                moveTo(-w * 0.02f, h * 0.985f)
                cubicTo(w * 0.14f, h * 0.93f, w * 0.27f, h * 0.86f, w * 0.42f, h * 0.71f)
            }
            val twigA = Path().apply {
                moveTo(w * 0.15f, h * 0.925f)
                cubicTo(w * 0.22f, h * 0.93f, w * 0.30f, h * 0.91f, w * 0.38f, h * 0.88f)
            }
            val twigB = Path().apply {
                moveTo(w * 0.29f, h * 0.835f)
                cubicTo(w * 0.31f, h * 0.78f, w * 0.30f, h * 0.74f, w * 0.27f, h * 0.69f)
            }
            drawPath(main, wood, style = Stroke(sw, cap = StrokeCap.Round))
            drawPath(twigA, wood, style = Stroke(sw * 0.7f, cap = StrokeCap.Round))
            drawPath(twigB, wood, style = Stroke(sw * 0.7f, cap = StrokeCap.Round))

            // Nụ đào
            val buds = listOf(Offset(0.45f, 0.77f), Offset(0.33f, 0.73f), Offset(0.07f, 0.965f), Offset(0.40f, 0.92f))
            for (b in buds) drawCircle(Color(0xFFEC407A).copy(alpha = 0.85f), 3.2f * dp1, Offset(w * b.x, h * b.y))

            // Hoa đào nở: (x, y, bán kính dp) - hơi "thở" theo phase (lặp liền mạch)
            val blossoms = listOf(
                Triple(0.42f, 0.71f, 11f), Triple(0.36f, 0.78f, 9f), Triple(0.27f, 0.69f, 10f),
                Triple(0.38f, 0.88f, 8f), Triple(0.22f, 0.89f, 9f), Triple(0.12f, 0.945f, 8f),
                Triple(0.31f, 0.83f, 7f)
            )
            blossoms.forEachIndexed { i, (fx, fy, rdp) ->
                val pulse = 1f + 0.06f * sin(tau * (phase + i * 0.17f))
                val col = if (i % 2 == 0) Color(0xFFF06292) else Color(0xFFF8BBD0)
                mcDrawFlower(1, Offset(w * fx, h * fy), rdp * dp1 * pulse, col)
            }

            // Cánh đào rơi
            for (p in ps.take(22)) {
                val prog = (p.y + phase * p.k) % 1f
                val x = (p.x + 0.05f * sin(tau * (prog * 2f + p.r))) * w
                val y = prog * h
                val len = (5f + 6f * p.s) * dp1
                val col = if (p.hue == 0) Color(0xFFF8BBD0) else Color(0xFFF06292)
                rotate(360f * ((p.r + phase * p.k) % 1f), Offset(x, y)) {
                    mcPetal(x, y, len, len * 0.55f, col.copy(alpha = 0.65f))
                }
            }
            // Kim tuyến vàng lấp lánh (không khí Tết)
            for (p in ps.drop(22).take(10)) {
                val tw = 0.3f + 0.7f * (0.5f + 0.5f * sin(tau * (phase * (1 + p.k) + p.r)))
                drawCircle(
                    Color(0xFFFFC107).copy(alpha = tw * 0.8f),
                    (1.4f + 1.8f * p.s) * dp1,
                    Offset(p.x * w, p.y * h * 0.9f)
                )
            }
        }

        McPaperTheme.QUOC_KHANH -> {
            // Cờ đỏ sao vàng treo ở mép phải, gió thổi làm lá cờ lượn sóng
            mcWavingFlag(w * 0.96f, h * 0.13f, w * 0.56f, w * 0.37f, phase, dark)
            // Sao vàng nhỏ lấp lánh
            for (p in ps.take(14)) {
                val tw = 0.3f + 0.7f * (0.5f + 0.5f * sin(tau * (phase * (1 + p.k) + p.r)))
                mcStar5(Offset(p.x * w, p.y * h * 0.95f), (2.2f + 3f * p.s) * dp1, Color(0xFFFFC107).copy(alpha = tw * 0.75f))
            }
        }

        McPaperTheme.VALENTINE -> {
            // Nhịp tim "thịch-thịch": 2 nhịp rồi nghỉ, lặp 10 lần mỗi vòng phase
            fun beat(off: Float): Float {
                val b = (phase * 10f + off) % 1f
                val x = maxOf(0f, sin(tau * b * 2f))
                return x * x
            }
            // Vài trái tim lớn đập rung động
            val bigs = listOf(Triple(0.84f, 0.60f, 0.10f), Triple(0.14f, 0.80f, 0.065f), Triple(0.78f, 0.89f, 0.045f))
            bigs.forEachIndexed { i, (fx, fy, fr) ->
                val c = Offset(w * fx, h * fy)
                val s = w * fr * (1f + 0.20f * beat(i * 0.23f))
                rotate(4f * sin(tau * (phase * 10f + i * 0.3f)), c) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFE91E63).copy(alpha = 0.25f), Color.Transparent),
                            center = c, radius = s * 2.2f
                        ),
                        s * 2.2f, c
                    )
                    mcHeart(c, s, Color(0xFFE91E63).copy(alpha = 0.38f))
                    mcHeart(Offset(c.x - s * 0.35f, c.y - s * 0.35f), s * 0.18f, Color.White.copy(alpha = 0.35f))
                }
            }
            // Tim nhỏ bay lên, cũng đập nhẹ
            val cols = listOf(Color(0xFFF48FB1), Color(0xFFF06292), Color(0xFFE53935))
            for (p in ps.take(14)) {
                val prog = (p.y + phase * p.k) % 1f
                val fade = minOf(prog / 0.12f, (1f - prog) / 0.15f, 1f).coerceIn(0f, 1f)
                val cx = (0.06f + 0.88f * p.x + 0.03f * sin(tau * (prog * 2f + p.r))) * w
                val cy = (1.1f - 1.2f * prog) * h
                val s = (4f + 6f * p.s) * dp1 * (1f + 0.12f * beat(p.r))
                mcHeart(Offset(cx, cy), s, cols[p.hue].copy(alpha = 0.55f * fade))
            }
        }

        McPaperTheme.PHAO_HOA -> {
            val cols = listOf(
                Color(0xFFFFC107), Color(0xFFE53935), Color(0xFF26C6DA), Color(0xFFEC407A), Color(0xFF7E57C2)
            )
            for (i in 0 until 5) {
                val p = ps[i]
                val t = (phase * 2f + i * 0.2f) % 1f
                val cx = (0.15f + 0.70f * p.x) * w
                val cy = (0.18f + 0.45f * p.y) * h
                val col = cols[i % cols.size]
                if (t < 0.15f) {
                    // quả pháo bay lên
                    val rise = t / 0.15f
                    drawCircle(col.copy(alpha = 0.85f), 2f * dp1, Offset(cx, cy + (1f - rise) * h * 0.25f))
                } else {
                    val e = (t - 0.15f) / 0.85f
                    val ease = 1f - (1f - e) * (1f - e)
                    val radius = (28f + 22f * p.s) * dp1 * ease
                    val a = 1f - e
                    for (k in 0 until 16) {
                        val ang = tau * k / 16f + p.r * tau
                        val dx = cos(ang) * radius
                        val dy = sin(ang) * radius + 10f * dp1 * e * e // rơi nhẹ do trọng lực
                        drawLine(
                            col.copy(alpha = a * 0.35f),
                            Offset(cx + cos(ang) * radius * 0.82f, cy + sin(ang) * radius * 0.82f),
                            Offset(cx + dx, cy + dy),
                            strokeWidth = dp1
                        )
                        drawCircle(col.copy(alpha = a * 0.85f), (1.8f - 0.8f * e) * dp1, Offset(cx + dx, cy + dy))
                    }
                }
            }
            for (p in ps.drop(5).take(14)) {
                val tw = 0.3f + 0.7f * (0.5f + 0.5f * sin(tau * (phase * (1 + p.k) + p.r)))
                drawCircle(Color(0xFFFFC107).copy(alpha = tw * 0.7f), (1.2f + 1.6f * p.s) * dp1, Offset(p.x * w, p.y * h * 0.9f))
            }
        }

        McPaperTheme.HOA_TUOI -> {
            val cols = listOf(
                Color(0xFFF48FB1), Color(0xFFEC407A), Color(0xFFBA68C8), Color(0xFFFF8A65), Color(0xFFFFB74D)
            )
            for (p in ps.take(16)) {
                val prog = (p.y + phase * p.k) % 1f
                val x = (p.x + 0.05f * sin(tau * (prog * 2f + p.r))) * w
                val y = prog * h
                val r = (5f + 5f * p.s) * dp1
                val style = (p.s * 6f).toInt().coerceIn(0, 5)
                rotate(360f * ((p.r + phase * p.k) % 1f), Offset(x, y)) {
                    mcDrawFlower(style, Offset(x, y), r, cols[(p.hue + p.k) % cols.size])
                }
            }
            for (p in ps.drop(16).take(10)) {
                val tw = 0.3f + 0.7f * (0.5f + 0.5f * sin(tau * (phase * (1 + p.k) + p.r)))
                drawCircle(Color(0xFFFFC107).copy(alpha = tw * 0.7f), (1.2f + 1.6f * p.s) * dp1, Offset(p.x * w, p.y * h * 0.9f))
            }
        }

        McPaperTheme.THIEU_NHI -> {
            val cols = listOf(
                Color(0xFFE53935), Color(0xFF29B6F6), Color(0xFFFFCA28),
                Color(0xFF66BB6A), Color(0xFFEC407A), Color(0xFFAB47BC)
            )
            // Hoa giấy rơi
            for (p in ps.drop(10).take(16)) {
                val prog = (p.y + phase * p.k) % 1f
                val x = (p.x + 0.03f * sin(tau * (prog * 3f + p.r))) * w
                val y = prog * h
                val sz = (2.5f + 2.5f * p.s) * dp1
                rotate(360f * ((p.r + phase * p.k * 2f) % 1f), Offset(x, y)) {
                    drawRect(cols[(p.hue * 2 + p.k) % cols.size].copy(alpha = 0.7f), Offset(x - sz, y - sz * 0.5f), Size(sz * 2f, sz))
                }
            }
            // Bóng bay bay lên
            for (p in ps.take(10)) {
                val prog = (p.y + phase * p.k) % 1f
                val fade = minOf(prog / 0.10f, (1f - prog) / 0.20f, 1f).coerceIn(0f, 1f)
                val cx = (0.08f + 0.84f * p.x + 0.02f * sin(tau * (prog * 2f + p.r))) * w
                val cy = (1.15f - 1.3f * prog) * h
                val sway = sin(tau * (phase * 3f + p.r))
                mcBalloon(Offset(cx, cy), (8f + 7f * p.s) * dp1, cols[(p.hue * 2 + p.k) % cols.size], fade, sway)
            }
        }

        McPaperTheme.NHA_GIAO -> {
            for (p in ps.drop(9).take(10)) {
                val tw = 0.3f + 0.7f * (0.5f + 0.5f * sin(tau * (phase * (1 + p.k) + p.r)))
                mcStar5(Offset(p.x * w, p.y * h * 0.95f), (2f + 2.6f * p.s) * dp1, Color(0xFFFFC107).copy(alpha = tw * 0.7f))
            }
            // Mũ cử nhân rơi chậm, nghiêng qua lại
            for (p in ps.take(9)) {
                val prog = (p.y + phase * p.k) % 1f
                val fade = minOf(prog / 0.10f, (1f - prog) / 0.10f, 1f).coerceIn(0f, 1f)
                val c = Offset((p.x + 0.04f * sin(tau * (prog * 2f + p.r))) * w, prog * h)
                rotate(25f * sin(tau * (phase * 2f + p.r)), c) {
                    mcGradCap(c, (7f + 6f * p.s) * dp1, 0.70f * fade)
                }
            }
        }

        McPaperTheme.HALLOWEEN -> {
            // Trăng tròn mờ
            val moonC = Offset(w * 0.84f, h * 0.20f)
            val moonR = w * 0.09f
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFFFF59D).copy(alpha = if (dark) 0.35f else 0.40f), Color.Transparent),
                    center = moonC, radius = moonR * 3f
                ),
                moonR * 3f, moonC
            )
            drawCircle(Color(0xFFFFF59D).copy(alpha = if (dark) 0.50f else 0.55f), moonR, moonC)
            // Bí ngô phát sáng bay lên
            for (p in ps.drop(6).take(5)) {
                val prog = (p.y + phase * p.k) % 1f
                val fade = minOf(prog / 0.12f, (1f - prog) / 0.15f, 1f).coerceIn(0f, 1f)
                val cx = (0.10f + 0.80f * p.x + 0.02f * sin(tau * (prog * 2f + p.r))) * w
                val cy = (1.1f - 1.25f * prog) * h
                mcPumpkin(Offset(cx, cy), (9f + 6f * p.s) * dp1, fade)
            }
            // Dơi bay ngang, vỗ cánh
            val bat = if (dark) Color(0xFFB39DDB).copy(alpha = 0.70f) else Color(0xFF4A148C).copy(alpha = 0.60f)
            for (i in 0 until 6) {
                val p = ps[i]
                val u = (p.x * 1.2f + phase * 1.2f * p.k) % 1.2f
                val x = (u - 0.1f) * w
                val y = (0.12f + 0.50f * p.y) * h + 14f * dp1 * sin(tau * (phase * 3f + p.r))
                val flap = 0.35f + 0.65f * (0.5f + 0.5f * sin(tau * (phase * 12f + p.r)))
                mcBat(Offset(x, y), (9f + 7f * p.s) * dp1, flap, bat)
            }
        }

        McPaperTheme.VU_LAN -> {
            // Mặt nước gợn sóng
            for (i in 0 until 3) {
                val base = 0.76f + 0.06f * i
                val path = Path()
                path.moveTo(0f, h)
                val step = w / 48f
                var x = 0f
                while (x <= w + 0.1f) {
                    val y = h * base + h * 0.012f * sin(tau * (x / w * (1.4f + i * 0.5f) + phase * (if (i % 2 == 0) 1 else -1)))
                    path.lineTo(x, y)
                    x += step
                }
                path.lineTo(w, h)
                path.close()
                drawPath(path, Color(0xFF4DB6AC).copy(alpha = 0.12f + 0.04f * i))
            }
            // Lá sen + hoa sen nhấp nhô theo sóng
            val lotus = listOf(Triple(0.18f, 0.80f, 11f), Triple(0.46f, 0.86f, 13f), Triple(0.74f, 0.82f, 10f), Triple(0.92f, 0.90f, 9f))
            lotus.forEachIndexed { i, (fx, fy, rdp) ->
                val r = rdp * dp1
                val bob = 2f * dp1 * sin(tau * (phase + i * 0.25f))
                val c = Offset(w * fx, h * fy + bob)
                drawOval(Color(0xFF66BB6A).copy(alpha = 0.45f), Offset(c.x - r * 1.5f, c.y + r * 0.25f), Size(r * 3f, r * 1.0f))
                mcDrawFlower(2, c, r, Color(0xFFF48FB1))
            }
            // Đom đóm / ánh đèn hoa đăng lấp lánh bay lên
            for (p in ps.take(14)) {
                val prog = (p.y + phase * p.k) % 1f
                val fade = minOf(prog / 0.12f, (1f - prog) / 0.15f, 1f).coerceIn(0f, 1f)
                val tw = 0.5f + 0.5f * sin(tau * (phase * (2 + p.k) + p.r))
                val c = Offset((p.x + 0.03f * sin(tau * (prog * 3f + p.r))) * w, (1f - prog) * h)
                val r = (1.6f + 1.8f * p.s) * dp1
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFEE58).copy(alpha = 0.45f * fade * tw), Color.Transparent),
                        center = c, radius = r * 4f
                    ),
                    r * 4f, c
                )
                drawCircle(Color(0xFFFFF59D).copy(alpha = 0.9f * fade * tw), r * 0.7f, c)
            }
        }

        McPaperTheme.TET_MAI -> {
            val wood = Color(0xFF5D4037).copy(alpha = if (dark) 0.75f else 0.85f)
            val sw = 3.6f * dp1
            // Cành mai gân guốc, uốn khúc, mọc từ góc dưới trái
            val main = Path().apply {
                moveTo(-w * 0.02f, h * 0.985f)
                cubicTo(w * 0.10f, h * 0.90f, w * 0.20f, h * 0.92f, w * 0.27f, h * 0.82f)
                cubicTo(w * 0.33f, h * 0.74f, w * 0.40f, h * 0.76f, w * 0.46f, h * 0.68f)
            }
            val twigA = Path().apply {
                moveTo(w * 0.22f, h * 0.89f)
                cubicTo(w * 0.28f, h * 0.90f, w * 0.34f, h * 0.88f, w * 0.40f, h * 0.84f)
            }
            val twigB = Path().apply {
                moveTo(w * 0.30f, h * 0.775f)
                cubicTo(w * 0.28f, h * 0.72f, w * 0.30f, h * 0.68f, w * 0.34f, h * 0.64f)
            }
            drawPath(main, wood, style = Stroke(sw, cap = StrokeCap.Round))
            drawPath(twigA, wood, style = Stroke(sw * 0.65f, cap = StrokeCap.Round))
            drawPath(twigB, wood, style = Stroke(sw * 0.65f, cap = StrokeCap.Round))

            // Nụ mai (xanh vàng)
            val buds = listOf(Offset(0.50f, 0.72f), Offset(0.38f, 0.60f), Offset(0.07f, 0.95f), Offset(0.44f, 0.88f))
            for (b in buds) drawCircle(Color(0xFFC0CA33).copy(alpha = 0.85f), 3.2f * dp1, Offset(w * b.x, h * b.y))

            // Hoa mai nở: (x, y, bán kính dp)
            val blossoms = listOf(
                Triple(0.46f, 0.68f, 12f), Triple(0.40f, 0.84f, 10f), Triple(0.34f, 0.64f, 10f),
                Triple(0.27f, 0.82f, 11f), Triple(0.17f, 0.90f, 9f), Triple(0.09f, 0.95f, 8f),
                Triple(0.36f, 0.75f, 8f)
            )
            blossoms.forEachIndexed { i, (fx, fy, rdp) ->
                val pulse = 1f + 0.06f * sin(tau * (phase + i * 0.17f))
                mcMaiFlower(Offset(w * fx, h * fy), rdp * dp1 * pulse, i % 2 == 1)
            }

            // Cánh mai rơi
            for (p in ps.take(20)) {
                val prog = (p.y + phase * p.k) % 1f
                val x = (p.x + 0.05f * sin(tau * (prog * 2f + p.r))) * w
                val y = prog * h
                val len = (4.5f + 5f * p.s) * dp1
                val col = if (p.hue == 0) Color(0xFFFFD54F) else Color(0xFFFFC107)
                rotate(360f * ((p.r + phase * p.k) % 1f), Offset(x, y)) {
                    mcPetal(x, y, len, len * 0.85f, col.copy(alpha = 0.70f))
                }
            }
            // Kim tuyến đỏ - vàng lấp lánh
            for (p in ps.drop(20).take(12)) {
                val tw = 0.3f + 0.7f * (0.5f + 0.5f * sin(tau * (phase * (1 + p.k) + p.r)))
                val c = if (p.hue == 0) Color(0xFFE53935) else Color(0xFFFFB300)
                drawCircle(c.copy(alpha = tw * 0.75f), (1.4f + 1.8f * p.s) * dp1, Offset(p.x * w, p.y * h * 0.9f))
            }
        }

        McPaperTheme.TRUNG_THU -> {
            // Trăng rằm + quầng sáng
            val moonC = Offset(w * 0.84f, h * 0.22f)
            val moonR = w * 0.13f
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFFFE082).copy(alpha = if (dark) 0.40f else 0.45f), Color.Transparent),
                    center = moonC, radius = moonR * 3f
                ),
                moonR * 3f, moonC
            )
            drawCircle(Color(0xFFFFF59D).copy(alpha = if (dark) 0.55f else 0.60f), moonR, moonC)

            // Sao nhỏ
            for (p in ps.drop(22).take(10)) {
                val tw = 0.3f + 0.7f * (0.5f + 0.5f * sin(tau * (phase * (1 + p.k) + p.r)))
                drawCircle(
                    Color(0xFFFFCA28).copy(alpha = tw * 0.7f),
                    (1.2f + 1.6f * p.s) * dp1,
                    Offset(p.x * w, p.y * h * 0.6f)
                )
            }

            // Đèn lồng bay lên, đung đưa nhẹ, mờ dần ở hai đầu để lặp không bị "giật"
            val lanternCols = listOf(Color(0xFFE53935), Color(0xFFFF9800), Color(0xFFFFC107))
            for (p in ps.take(9)) {
                val prog = (p.y + phase * p.k) % 1f
                val fade = minOf(prog / 0.12f, (1f - prog) / 0.15f, 1f).coerceIn(0f, 1f)
                val cx = (0.08f + 0.84f * p.x + 0.025f * sin(tau * (prog * 2f + p.r))) * w
                val cy = (1.1f - 1.25f * prog) * h
                mcLantern(Offset(cx, cy), (9f + 9f * p.s) * dp1, lanternCols[p.hue], fade)
            }
        }

        McPaperTheme.GIANG_SINH -> {
            // Màu bông tuyết: trắng trên nền tối, xanh băng trên nền sáng (trắng sẽ chìm vào giấy)
            val flake = if (dark) Color.White else Color(0xFF64B5F6)
            for (p in ps) {
                val prog = (p.y + phase * p.k) % 1f
                val x = (p.x + 0.04f * sin(tau * (prog * 3f + p.r))) * w
                val y = prog * h
                val r = (1.6f + 3.4f * p.s) * dp1
                val a = 0.35f + 0.5f * p.s // bông to = gần = rõ hơn
                if (p.s > 0.62f) {
                    // Bông tuyết 6 cánh, xoay chậm
                    val spin = 360f * ((p.r + phase * p.k) % 1f)
                    for (i in 0 until 3) {
                        val ang = Math.toRadians((spin + i * 60f).toDouble())
                        val dx = cos(ang).toFloat() * r * 1.6f
                        val dy = sin(ang).toFloat() * r * 1.6f
                        drawLine(
                            flake.copy(alpha = a), Offset(x - dx, y - dy), Offset(x + dx, y + dy),
                            strokeWidth = 1.2f * dp1, cap = StrokeCap.Round
                        )
                    }
                } else {
                    drawCircle(flake.copy(alpha = a), r * 0.6f, Offset(x, y))
                }
            }
            mcSnowGround(dark)
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

private fun Path.addMcHeart(cx: Float, cy: Float, s: Float) {
    moveTo(cx, cy + s * 0.9f)
    cubicTo(cx - s * 1.3f, cy + s * 0.1f, cx - s * 0.9f, cy - s * 0.9f, cx, cy - s * 0.35f)
    cubicTo(cx + s * 0.9f, cy - s * 0.9f, cx + s * 1.3f, cy + s * 0.1f, cx, cy + s * 0.9f)
    close()
}

/** Trái tim tâm [c], "bán kính" [s]. */
private fun DrawScope.mcHeart(c: Offset, s: Float, color: Color) {
    drawPath(Path().apply { addMcHeart(c.x, c.y, s) }, color)
}

/** Ngôi sao 5 cánh. */
private fun DrawScope.mcStar5(c: Offset, r: Float, color: Color) {
    val path = Path()
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) r else r * 0.382f
        val a = (-90.0 + i * 36.0) * PI / 180.0
        val x = c.x + cos(a).toFloat() * rad
        val y = c.y + sin(a).toFloat() * rad
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

/**
 * Lá cờ đỏ sao vàng lượn sóng: cột cờ ở [poleX], lá cờ bay sang trái, rộng [fw] cao [fh].
 * Chia thành các dải dọc, mỗi dải lệch lên/xuống theo sóng (càng xa cột càng dao động mạnh)
 * và đổi độ sáng theo độ dốc để có cảm giác nếp vải. phase chạy 0..1 => lặp liền mạch.
 */
private fun DrawScope.mcWavingFlag(poleX: Float, top: Float, fw: Float, fh: Float, phase: Float, dark: Boolean) {
    val tau = (2 * PI).toFloat()
    val d = density
    val amp = fh * 0.10f
    fun dy(t: Float): Float = amp * t * sin(tau * (t * 1.5f - phase))

    // cột cờ
    drawLine(
        Color(0xFF8D6E63).copy(alpha = 0.75f),
        Offset(poleX, top - fh * 0.12f), Offset(poleX, top + fh * 2.2f),
        strokeWidth = 3f * d, cap = StrokeCap.Round
    )
    drawCircle(Color(0xFFFFC107).copy(alpha = 0.9f), 3.5f * d, Offset(poleX, top - fh * 0.12f))

    val n = 28
    val red = Color(0xFFDA251D)
    for (i in 0 until n) {
        val t0 = i / n.toFloat()
        val t1 = (i + 1) / n.toFloat()
        val x0 = poleX - t0 * fw
        val x1 = poleX - t1 * fw - 0.6f
        val d0 = dy(t0)
        val d1 = dy(t1)
        val slope = (d1 - d0) / (fw / n)
        val shade = (slope * 1.6f).coerceIn(-0.35f, 0.35f)
        val col = if (shade >= 0f) lerp(red, Color.White, shade) else lerp(red, Color.Black, -shade)
        val strip = Path().apply {
            moveTo(x0, top + d0)
            lineTo(x1, top + d1)
            lineTo(x1, top + fh + d1)
            lineTo(x0, top + fh + d0)
            close()
        }
        drawPath(strip, col.copy(alpha = if (dark) 0.50f else 0.42f))
    }
    mcStar5(
        Offset(poleX - 0.5f * fw, top + fh / 2f + dy(0.5f)),
        fh * 0.30f,
        Color(0xFFFFEB3B).copy(alpha = 0.85f)
    )
}

/** Bóng bay có dây, nút thắt và vệt sáng. [sway] -1..1 làm dây đung đưa. */
private fun DrawScope.mcBalloon(c: Offset, r: Float, color: Color, fade: Float, sway: Float) {
    if (fade <= 0.01f) return
    val d = density
    val bw = r * 1.7f
    val bh = r * 2f
    val str = Path().apply {
        moveTo(c.x, c.y + bh * 0.5f + r * 0.2f)
        cubicTo(c.x + sway * r * 0.6f, c.y + r * 1.6f, c.x - sway * r * 0.6f, c.y + r * 2.4f, c.x + sway * r * 0.3f, c.y + r * 3.4f)
    }
    drawPath(str, Color(0xFF90A4AE).copy(alpha = 0.55f * fade), style = Stroke(d))
    drawOval(color.copy(alpha = 0.75f * fade), Offset(c.x - bw / 2f, c.y - bh / 2f), Size(bw, bh))
    val knot = Path().apply {
        moveTo(c.x, c.y + bh * 0.5f - r * 0.02f)
        lineTo(c.x - r * 0.16f, c.y + bh * 0.5f + r * 0.2f)
        lineTo(c.x + r * 0.16f, c.y + bh * 0.5f + r * 0.2f)
        close()
    }
    drawPath(knot, color.copy(alpha = 0.85f * fade))
    drawOval(
        Color.White.copy(alpha = 0.45f * fade),
        Offset(c.x - bw * 0.28f, c.y - bh * 0.32f), Size(bw * 0.18f, bh * 0.26f)
    )
}

/** Mũ cử nhân (tâm [c], cỡ [s]) có tua rua vàng. */
private fun DrawScope.mcGradCap(c: Offset, s: Float, alpha: Float) {
    if (alpha <= 0.01f) return
    val navy = Color(0xFF283593)
    val base = Path().apply {
        moveTo(c.x - s * 0.62f, c.y + s * 0.05f)
        lineTo(c.x - s * 0.62f, c.y + s * 0.55f)
        cubicTo(c.x - s * 0.3f, c.y + s * 0.8f, c.x + s * 0.3f, c.y + s * 0.8f, c.x + s * 0.62f, c.y + s * 0.55f)
        lineTo(c.x + s * 0.62f, c.y + s * 0.05f)
        lineTo(c.x, c.y + s * 0.3f)
        close()
    }
    drawPath(base, Color(0xFF3949AB).copy(alpha = alpha))
    val board = Path().apply {
        moveTo(c.x, c.y - s * 0.55f)
        lineTo(c.x + s * 1.2f, c.y - s * 0.1f)
        lineTo(c.x, c.y + s * 0.35f)
        lineTo(c.x - s * 1.2f, c.y - s * 0.1f)
        close()
    }
    drawPath(board, navy.copy(alpha = alpha))
    val gold = Color(0xFFFFC107).copy(alpha = alpha)
    drawLine(gold, Offset(c.x, c.y - s * 0.1f), Offset(c.x + s * 1.0f, c.y - s * 0.02f), strokeWidth = density)
    drawLine(gold, Offset(c.x + s * 1.0f, c.y - s * 0.02f), Offset(c.x + s * 1.0f, c.y + s * 0.5f), strokeWidth = 1.4f * density)
    drawCircle(gold, s * 0.08f, Offset(c.x + s * 1.0f, c.y + s * 0.55f))
}

/** Bí ngô Halloween phát sáng, mặt cười nham hiểm. */
private fun DrawScope.mcPumpkin(c: Offset, r: Float, fade: Float) {
    if (fade <= 0.01f) return
    val orange = Color(0xFFFF7A00)
    drawCircle(
        Brush.radialGradient(listOf(orange.copy(alpha = 0.30f * fade), Color.Transparent), center = c, radius = r * 2.2f),
        r * 2.2f, c
    )
    drawOval(orange.copy(alpha = 0.75f * fade), Offset(c.x - r * 1.05f, c.y - r * 0.75f), Size(r * 1.3f, r * 1.5f))
    drawOval(orange.copy(alpha = 0.75f * fade), Offset(c.x - r * 0.25f, c.y - r * 0.75f), Size(r * 1.3f, r * 1.5f))
    drawOval(
        lerp(orange, Color(0xFFFFB300), 0.3f).copy(alpha = 0.85f * fade),
        Offset(c.x - r * 0.55f, c.y - r * 0.85f), Size(r * 1.1f, r * 1.7f)
    )
    drawRect(Color(0xFF2E7D32).copy(alpha = 0.85f * fade), Offset(c.x - r * 0.1f, c.y - r * 1.1f), Size(r * 0.2f, r * 0.3f))
    val glow = Color(0xFFFFF59D).copy(alpha = 0.9f * fade)
    for (sign in listOf(-1f, 1f)) {
        val eye = Path().apply {
            moveTo(c.x + sign * r * 0.42f, c.y - r * 0.45f)
            lineTo(c.x + sign * r * 0.15f, c.y - r * 0.05f)
            lineTo(c.x + sign * r * 0.69f, c.y - r * 0.05f)
            close()
        }
        drawPath(eye, glow)
    }
    val mouth = Path().apply {
        moveTo(c.x - r * 0.6f, c.y + r * 0.2f)
        lineTo(c.x + r * 0.6f, c.y + r * 0.2f)
        lineTo(c.x + r * 0.35f, c.y + r * 0.62f)
        lineTo(c.x + r * 0.12f, c.y + r * 0.4f)
        lineTo(c.x - r * 0.12f, c.y + r * 0.62f)
        lineTo(c.x - r * 0.35f, c.y + r * 0.4f)
        close()
    }
    drawPath(mouth, glow)
}

/** Con dơi: [flap] 0..1 là độ vỗ cánh (1 = cánh giương cao). */
private fun DrawScope.mcBat(c: Offset, s: Float, flap: Float, color: Color) {
    val path = Path().apply {
        moveTo(c.x, c.y)
        cubicTo(c.x - s * 0.4f, c.y - s * 0.9f * flap, c.x - s * 0.9f, c.y - s * 0.7f * flap, c.x - s, c.y - s * 0.15f * flap)
        cubicTo(c.x - s * 0.75f, c.y + s * 0.05f, c.x - s * 0.6f, c.y + s * 0.35f, c.x - s * 0.45f, c.y + s * 0.2f)
        cubicTo(c.x - s * 0.3f, c.y + s * 0.4f, c.x - s * 0.15f, c.y + s * 0.3f, c.x, c.y + s * 0.35f)
        cubicTo(c.x + s * 0.15f, c.y + s * 0.3f, c.x + s * 0.3f, c.y + s * 0.4f, c.x + s * 0.45f, c.y + s * 0.2f)
        cubicTo(c.x + s * 0.6f, c.y + s * 0.35f, c.x + s * 0.75f, c.y + s * 0.05f, c.x + s, c.y - s * 0.15f * flap)
        cubicTo(c.x + s * 0.9f, c.y - s * 0.7f * flap, c.x + s * 0.4f, c.y - s * 0.9f * flap, c.x, c.y)
        close()
    }
    drawPath(path, color)
    drawCircle(color, s * 0.14f, Offset(c.x, c.y + s * 0.05f))
}

/** Hoa mai vàng: 5 cánh tròn bầu + nhụy cam. [light] = tông vàng nhạt hơn để hoa xen kẽ. */
private fun DrawScope.mcMaiFlower(c: Offset, r: Float, light: Boolean) {
    val petal = if (light) Color(0xFFFFE57F) else Color(0xFFFFD600)
    for (i in 0 until 5) {
        val a = (i * 72.0 - 90.0) * PI / 180.0
        val pc = Offset(c.x + cos(a).toFloat() * r * 0.55f, c.y + sin(a).toFloat() * r * 0.55f)
        drawCircle(petal.copy(alpha = 0.95f), r * 0.52f, pc)
        drawCircle(Color(0xFFFFA000).copy(alpha = 0.35f), r * 0.52f, pc, style = Stroke(r * 0.05f))
    }
    drawCircle(Color(0xFFFF8F00), r * 0.2f, c)
    for (i in 0 until 6) {
        val a = (i * 60.0) * PI / 180.0
        drawCircle(
            Color(0xFFFFE082), r * 0.045f,
            Offset(c.x + cos(a).toFloat() * r * 0.32f, c.y + sin(a).toFloat() * r * 0.32f)
        )
    }
}

/** Đèn lồng tròn: quầng sáng, thân có gân, nắp vàng, tua rua. [fade] 0..1 để mờ dần. */
private fun DrawScope.mcLantern(c: Offset, r: Float, color: Color, fade: Float) {
    if (fade <= 0.01f) return
    val gold = Color(0xFFFFD54F)
    val d = density
    drawCircle(
        Brush.radialGradient(
            listOf(color.copy(alpha = 0.32f * fade), Color.Transparent),
            center = c, radius = r * 2.4f
        ),
        r * 2.4f, c
    )
    // dây treo
    drawLine(gold.copy(alpha = 0.6f * fade), Offset(c.x, c.y - r * 1.35f), Offset(c.x, c.y - r * 0.95f), strokeWidth = 1.2f * d)
    // thân
    drawOval(color.copy(alpha = 0.78f * fade), Offset(c.x - r, c.y - r * 0.8f), Size(2f * r, 1.6f * r))
    val rib = Color.White.copy(alpha = 0.30f * fade)
    drawOval(rib, Offset(c.x - r * 0.5f, c.y - r * 0.8f), Size(r, 1.6f * r), style = Stroke(1.1f * d))
    drawLine(rib, Offset(c.x, c.y - r * 0.8f), Offset(c.x, c.y + r * 0.8f), strokeWidth = 1.1f * d)
    // nắp trên/dưới
    val capW = r * 0.9f
    drawRoundRect(gold.copy(alpha = 0.85f * fade), Offset(c.x - capW / 2f, c.y - r * 0.98f), Size(capW, r * 0.22f), CornerRadius(r * 0.06f, r * 0.06f))
    drawRoundRect(gold.copy(alpha = 0.85f * fade), Offset(c.x - capW / 2f, c.y + r * 0.76f), Size(capW, r * 0.22f), CornerRadius(r * 0.06f, r * 0.06f))
    // tua rua
    drawLine(gold.copy(alpha = 0.8f * fade), Offset(c.x, c.y + r * 0.98f), Offset(c.x, c.y + r * 1.6f), strokeWidth = 1.6f * d, cap = StrokeCap.Round)
}

/** Lớp tuyết phủ dưới chân tờ lịch (đường viền gợn sóng, tĩnh). */
private fun DrawScope.mcSnowGround(dark: Boolean) {
    val w = size.width
    val h = size.height
    val tau = (2 * PI).toFloat()
    val d = density
    val step = w / 40f
    fun yAt(x: Float): Float =
        h * 0.955f - h * 0.012f * (sin(tau * (x / w * 2.2f)) + 0.5f * sin(tau * (x / w * 5f + 0.3f)))

    val top = Path()
    top.moveTo(0f, yAt(0f))
    var x = step
    while (x <= w + 0.1f) {
        top.lineTo(x, yAt(x))
        x += step
    }
    val fill = Path().apply {
        addPath(top)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(fill, if (dark) Color(0xFFE3F2FD).copy(alpha = 0.50f) else Color.White.copy(alpha = 0.90f))
    drawPath(top, Color(0xFF90CAF9).copy(alpha = if (dark) 0.35f else 0.60f), style = Stroke(1.2f * d))
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
