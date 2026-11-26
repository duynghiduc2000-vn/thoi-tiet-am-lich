package com.mrwind.vn

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

private const val APP_VERSION_NAME = "31.12-HPBD"
private const val FEEDBACK_EMAIL = "duynghiduc12@gmail.com"

private val SMOOTH_EXPAND_SPEC = spring<androidx.compose.ui.unit.IntSize>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

private val DAILY_WARM_MESSAGES = listOf(
    "Chúc bạn hôm nay thật nhẹ nhàng, bình yên và gặp nhiều điều dễ thương",
    "Mỗi ngày mới là một cơ hội nhỏ để mỉm cười nhiều hơn một chút",
    "Mong rằng hôm nay mọi việc của bạn đều trôi chảy êm ái như ý",
    "Nhớ hít thở sâu, làm chậm lại một nhịp và thương bản thân thêm chút nhé",
    "Chúc bạn một ngày tràn đầy năng lượng lành và những tin vui nho nhỏ",
    "Dù bận rộn đến đâu cũng đừng quên dành cho mình một khoảng nghỉ dịu dàng",
    "Hy vọng hôm nay có ai đó hoặc điều gì đó khiến lòng bạn ấm lên",
    "Chúc bạn bước ra đường gặp gió mát, về nhà thấy lòng nhẹ tênh",
    "Những cố gắng thầm lặng của bạn rồi sẽ nở hoa vào đúng lúc đẹp nhất",
    "Hôm nay hãy cho phép bản thân vui vẻ mà không cần lý do gì cả",
    "Chúc bạn một ngày nhiều tiếng cười, ít âu lo và đủ đầy sự an yên",
    "Từng bước nhỏ vững vàng hôm nay chính là nền móng cho ngày mai rực rỡ",
    "Mong hôm nay của bạn ngọt ngào như một ly trà ấm giữa chiều se lạnh",
    "Chúc bạn giữ được sự lạc quan trong trẻo dù ngày hôm nay có ra sao",
    "Mỗi nụ cười của bạn đều có sức mạnh làm ngày hôm nay đẹp hơn",
    "Chúc bạn làm việc hiệu quả mà vẫn giữ được tinh thần thư thái",
    "Nếu hôm nay hơi mệt, hãy nghỉ ngơi một chút rồi đi tiếp thật nhẹ nhàng",
    "Chúc bạn luôn được bao quanh bởi những người tử tế và chân thành",
    "Ngày mới bắt đầu rồi, chúc bạn đón nhận mọi điều bằng tâm thế thật vui",
    "Mong rằng hôm nay bạn sẽ tìm thấy niềm vui từ những điều giản dị nhất",
    "Chúc bạn sức khỏe dồi dào, tâm hồn sáng trong và nhiều niềm tin",
    "Đừng khắt khe với chính mình quá nhé, hôm nay bạn đã cố gắng rất tốt rồi",
    "Chúc bạn có một ngày thật suôn sẻ, gặp đúng người và làm đúng việc",
    "Ánh nắng dịu dàng đang chờ bạn, chúc một ngày mới thật nhiều hứng khởi",
    "Chúc bạn mọi dự định hôm nay đều thuận buồm xuôi gió",
    "Một ngày bình thường cũng có thể trở nên đặc biệt nếu lòng mình đủ vui",
    "Chúc bạn luôn giữ được nụ cười rạng rỡ trên môi suốt cả ngày dài",
    "Mong hôm nay mang đến cho bạn thật nhiều bình an và sự nhẹ nhõm",
    "Chúc bạn đi đến đâu cũng gặp may mắn và những cái gật đầu thiện cảm",
    "Hãy tận hưởng từng khoảnh khắc nhỏ xinh của ngày hôm nay bạn nhé",
    "Chúc bạn có những phút giây thư giãn thật trọn vẹn sau giờ làm việc",
    "Mọi chuyện rồi sẽ ổn thôi, chúc bạn giữ vững niềm tin và sự bình tĩnh",
    "Chúc bạn hôm nay ăn ngon, ngủ sâu và có thật nhiều năng lượng tích cực",
    "Một lời chúc ấm áp gửi tới bạn cho một ngày mới tràn ngập yêu thương",
    "Chúc bạn biến những điều nhỏ bé hôm nay thành kỷ niệm đáng nhớ",
    "Chúc bạn luôn tự tin, vững vàng và tỏa sáng theo cách riêng của mình",
    "Ngày hôm nay là món quà đẹp, chúc bạn mở ra thật nhiều điều bất ngờ vui",
    "Chúc bạn gác lại muộn phiền và đón nhận ngày mới với trái tim rộng mở",
    "Mong rằng mọi nỗ lực của bạn hôm nay đều được đền đáp xứng đáng",
    "Chúc bạn một ngày thật êm đềm, nhẹ bước và nhiều niềm vui bất chợt",
    "Hãy tin rằng điều tốt đẹp luôn đang trên đường tìm đến với bạn",
    "Chúc bạn hôm nay có thật nhiều khoảnh khắc đáng yêu và ấm áp",
    "Dù hôm nay trôi qua thế nào, bạn vẫn xứng đáng được yêu thương và trân trọng",
    "Chúc bạn khởi đầu ngày mới thật tươi tắn và khép lại thật bình yên",
    "Mong hôm nay của bạn nhẹ như mây trôi và sáng như nắng sớm",
    "Chúc bạn luôn giữ được sự ấm áp trong tim để sưởi ấm cả ngày dài",
    "Một ngày mới với thật nhiều hy vọng tốt lành đang chờ bạn phía trước",
    "Chúc bạn hôm nay chạm tay vào đâu cũng thấy thuận lợi và vui vẻ",
    "Cảm ơn bạn vì đã bước qua ngày hôm nay thật kiên cường và dịu dàng",
    "Chúc bạn một ngày tràn ngập niềm vui, may mắn và những cái ôm ấm áp"
)

private val CHEERFUL_END_ICONS = listOf(
    "🌸", "☀️", "🌻", "✨", "🍀",
    "🌈", "🌼", "🌷", "☀️", "💛",
    "🦋", "🌿", "🎉", "🌟", "🍃"
)

private fun dailyWarmMessageForToday(calendar: Calendar): String {
    val year = calendar.get(Calendar.YEAR)
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val daySeed = year * 1000 + dayOfYear
    val msgRng = Random(daySeed * 31 + 7)
    val iconRng = Random(daySeed * 97 + 19)

    val message = DAILY_WARM_MESSAGES[msgRng.nextInt(DAILY_WARM_MESSAGES.size)]
    val icon = CHEERFUL_END_ICONS[iconRng.nextInt(CHEERFUL_END_ICONS.size)]
    return "$message $icon"
}

private enum class ExpandableSection {
    CLOCK_THEME_BG,
    ACCENT_COLOR,
    LOCATION_SIGNATURE,
    FONT_SCALE,
    TODO,
    LUNAR_LOOKUP,
    LUNAR_USEFUL_INFO,
    AGE_CALC,
    BIRTHDAY,
    HOLIDAY,
    BUG_REPORT
}

private enum class FeedbackCategory(val label: String) {
    BUG("Báo cáo lỗi"),
    SUGGESTION("Đề xuất")
}

private data class AdaptivePalette(
    val isDark: Boolean,
    val screenBg: Color,
    val screenBrush: Brush,
    val cardBg: Color,
    val liquidGlassBrush: Brush,
    val liquidGlassBorder: Brush,
    val popupBrush: Brush,
    val popupBorder: Brush,
    val popupBoxBg: Color,
    val popupTextPrimary: Color,
    val popupTextSecondary: Color,
    val subtleBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val onAccentText: Color
)

private fun buildAdaptivePalette(isDark: Boolean, accentColor: Color): AdaptivePalette {
    return if (isDark) {
        val screenBg = Color(0xFF09090B)
        AdaptivePalette(
            isDark = true,
            screenBg = screenBg,
            screenBrush = Brush.verticalGradient(
                listOf(
                    accentColor.copy(alpha = 0.09f),
                    Color(0xFF09090B),
                    Color(0xFF050507)
                )
            ),
            cardBg = Color(0xFF121215),
            liquidGlassBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF232B3A).copy(alpha = 0.78f),
                    Color(0xFF14171F).copy(alpha = 0.68f),
                    accentColor.copy(alpha = 0.14f),
                    Color(0xFF0D0F14).copy(alpha = 0.80f)
                )
            ),
            liquidGlassBorder = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.28f),
                    accentColor.copy(alpha = 0.26f),
                    Color.White.copy(alpha = 0.08f)
                )
            ),
            popupBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF1B1D24).copy(alpha = 0.97f),
                    Color(0xFF131419).copy(alpha = 0.98f),
                    accentColor.copy(alpha = 0.10f)
                )
            ),
            popupBorder = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.30f),
                    accentColor.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.12f)
                )
            ),
            popupBoxBg = Color(0xFF22242D).copy(alpha = 0.96f),
            popupTextPrimary = Color(0xFFFAFAFA),
            popupTextSecondary = Color(0xFFD4D4D8),
            subtleBg = Color(0xFF1B1B20),
            textPrimary = Color(0xFFFAFAFA),
            textSecondary = Color(0xFFA1A1AA),
            border = Color.White.copy(alpha = 0.10f),
            onAccentText = Color(0xFF09090B)
        )
    } else {
        val screenBg = Color(0xFFF4F5F7)
        AdaptivePalette(
            isDark = false,
            screenBg = screenBg,
            screenBrush = Brush.verticalGradient(
                listOf(
                    accentColor.copy(alpha = 0.08f),
                    Color(0xFFF4F5F7),
                    Color(0xFFECEEF2)
                )
            ),
            cardBg = Color(0xFFFFFFFF),
            liquidGlassBrush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.88f),
                    accentColor.copy(alpha = 0.10f),
                    Color(0xFFF3F6FA).copy(alpha = 0.76f)
                )
            ),
            liquidGlassBorder = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.90f),
                    accentColor.copy(alpha = 0.25f),
                    Color.Black.copy(alpha = 0.08f)
                )
            ),
            popupBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF).copy(alpha = 0.98f),
                    Color(0xFFF8F9FB).copy(alpha = 0.99f),
                    accentColor.copy(alpha = 0.07f)
                )
            ),
            popupBorder = Brush.linearGradient(
                listOf(
                    accentColor.copy(alpha = 0.45f),
                    Color.Black.copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0.95f)
                )
            ),
            popupBoxBg = Color(0xFFECEEF3).copy(alpha = 0.96f),
            popupTextPrimary = Color(0xFF09090B),
            popupTextSecondary = Color(0xFF3F3F46),
            subtleBg = Color(0xFFF1F3F6),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF52525B),
            border = Color.Black.copy(alpha = 0.08f),
            onAccentText = Color(0xFF09090B)
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WeatherService.scheduleTwoHourlySync(this)
        BirthdayHelper.notifyTodayBirthdaysOncePerDay(this)
        BirthdayHelper.notifyTodayHolidaysOncePerDay(this)
        BirthdayHelper.notifyLunarUsefulInfoOncePerDay(this)
        WeatherLunarWidget.updateAllWidgets(this)
        val initialTasks = TodoHelper.ensureCurrentDayOrReset(this)
        TodoHelper.scheduleAllTodayAlarms(this, initialTasks)
        intent?.getStringExtra(TodoHelper.EXTRA_TASK_ID)?.let { taskId ->
            TodoHelper.setPendingPopupTaskId(this, taskId)
        }

        setContent {
            ThoiTietAmLichApp()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(TodoHelper.EXTRA_TASK_ID)?.let { taskId ->
            TodoHelper.setPendingPopupTaskId(this, taskId)
        }
    }
}

private data class AccentOption(
    val name: String,
    val colorInt: Int
)

private val ACCENT_OPTIONS = listOf(
    AccentOption("Xanh ngọc trời", 0xFF4FC3F7.toInt()),
    AccentOption("Xanh ngọc bích", 0xFF26A69A.toInt()),
    AccentOption("Xanh dương đậm", 0xFF29B6F6.toInt()),
    AccentOption("Xanh coban", 0xFF5C6BC0.toInt()),
    AccentOption("Xanh bạc hà", 0xFF4DB6AC.toInt()),
    AccentOption("Xanh lá rừng", 0xFF66BB6A.toInt()),
    AccentOption("Xanh chanh", 0xFF9CCC65.toInt()),
    AccentOption("Vàng nắng", 0xFFFFD54F.toInt()),
    AccentOption("Hổ phách ấm", 0xFFFFB74D.toInt()),
    AccentOption("Cam hoàng hôn", 0xFFFF8A65.toInt()),
    AccentOption("Đỏ san hô", 0xFFEF5350.toInt()),
    AccentOption("Hồng san hô", 0xFFF06292.toInt()),
    AccentOption("Hồng phấn", 0xFFF48FB1.toInt()),
    AccentOption("Tím oải hương", 0xFFBA68C8.toInt()),
    AccentOption("Tím hoa cà", 0xFF9575CD.toInt()),
    AccentOption("Trắng ngà", 0xFFF5F5F5.toInt())
)

private data class LunarResultPopupData(
    val solarLabel: String,
    val lunarSummary: String
)

private data class AgeResultPopupData(
    val birthLabel: String,
    val currentLabel: String,
    val ageSummary: String
)

private data class AgeCalculationResult(
    val years: Int,
    val months: Int,
    val currentLabel: String
) {
    val summary: String
        get() = "$years tuổi $months tháng"
}

private data class PreviewDayChip(
    val dayLabel: String,
    val icon: String
)

private fun forecastEmojiForCode(code: Int): String = when (code) {
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

private fun shortWeekdayVi(calendar: Calendar): String = when (calendar.get(Calendar.DAY_OF_WEEK)) {
    Calendar.MONDAY -> "T2"
    Calendar.TUESDAY -> "T3"
    Calendar.WEDNESDAY -> "T4"
    Calendar.THURSDAY -> "T5"
    Calendar.FRIDAY -> "T6"
    Calendar.SATURDAY -> "T7"
    Calendar.SUNDAY -> "CN"
    else -> ""
}

private fun resolvePreviewSevenDays(context: Context, fallbackCode: Int): List<PreviewDayChip> {
    val tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
    val defaultIcon = forecastEmojiForCode(fallbackCode)
    val fallback = (1..7).map { offset ->
        val c = Calendar.getInstance(tz).apply { add(Calendar.DAY_OF_MONTH, offset) }
        PreviewDayChip(dayLabel = shortWeekdayVi(c), icon = defaultIcon)
    }
    return try {
        val m = AppPrefs::class.java.getMethod("getNextSevenDaysForecast", Context::class.java)
        @Suppress("UNCHECKED_CAST")
        val raw = m.invoke(null, context) as? List<*>
        if (raw.isNullOrEmpty()) {
            fallback
        } else {
            (0..6).map { idx ->
                val item = raw.getOrNull(idx) ?: return@map fallback[idx]
                when (item) {
                    is String -> {
                        val parts = item.split("|")
                        if (parts.size >= 2) {
                            PreviewDayChip(
                                dayLabel = parts[0].trim().ifEmpty { fallback[idx].dayLabel },
                                icon = parts[1].trim().ifEmpty { fallback[idx].icon }
                            )
                        } else {
                            fallback[idx]
                        }
                    }
                    else -> fallback[idx]
                }
            }
        }
    } catch (_: Exception) {
        fallback
    }
}

private fun calculateAgeFromNow(birthDay: Int, birthMonth: Int, birthYear: Int): AgeCalculationResult {
    val tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
    val birthCal = Calendar.getInstance(tz).apply {
        isLenient = false
        set(Calendar.YEAR, birthYear)
        set(Calendar.MONTH, birthMonth - 1)
        set(Calendar.DAY_OF_MONTH, birthDay)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    birthCal.timeInMillis

    val nowCal = Calendar.getInstance(tz)
    val curYear = nowCal.get(Calendar.YEAR)
    val curMonth = nowCal.get(Calendar.MONTH) + 1
    val curDay = nowCal.get(Calendar.DAY_OF_MONTH)

    val birthTotalMonths = birthYear * 12 + birthMonth
    val curTotalMonths = curYear * 12 + curMonth
    if (curTotalMonths < birthTotalMonths || (curTotalMonths == birthTotalMonths && curDay < birthDay)) {
        throw IllegalArgumentException("Ngày sinh lớn hơn ngày hiện tại")
    }

    var totalMonths = curTotalMonths - birthTotalMonths
    if (curDay < birthDay) {
        totalMonths -= 1
    }
    val safeMonths = totalMonths.coerceAtLeast(0)
    val years = safeMonths / 12
    val months = safeMonths % 12
    val currentLabel =
        "${curDay.toString().padStart(2, '0')}/${curMonth.toString().padStart(2, '0')}/$curYear"

    return AgeCalculationResult(
        years = years,
        months = months,
        currentLabel = currentLabel
    )
}

private val CAN_NAMES = arrayOf(
    "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"
)

private val CHI_NAMES = arrayOf(
    "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
)

private fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
    val a = (14 - mm) / 12
    val y = yy + 4800 - a
    val m = mm + 12 * a - 3
    var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    if (jd < 2299161) {
        jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
    }
    return jd
}

private fun newMoonDayNumber(k: Int, timeZoneOffset: Double = 7.0): Int {
    val t = k / 1236.85
    val t2 = t * t
    val t3 = t2 * t
    val dr = Math.PI / 180.0
    var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
    jd1 += 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
    val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
    val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
    val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
    var c1 = (0.1734 - 0.000393 * t) * Math.sin(m * dr) + 0.0021 * Math.sin(2 * m * dr)
    c1 = c1 - 0.4068 * Math.sin(mpr * dr) + 0.0161 * Math.sin(2 * mpr * dr)
    c1 = c1 - 0.0004 * Math.sin(3 * mpr * dr)
    c1 = c1 + 0.0104 * Math.sin(2 * f * dr) - 0.0051 * Math.sin((m + mpr) * dr)
    c1 = c1 - 0.0074 * Math.sin((m - mpr) * dr) + 0.0004 * Math.sin((2 * f + m) * dr)
    c1 = c1 - 0.0004 * Math.sin((2 * f - m) * dr) - 0.0006 * Math.sin((2 * f + mpr) * dr)
    c1 = c1 + 0.0010 * Math.sin((2 * f - mpr) * dr) + 0.0005 * Math.sin((mpr + 2 * m) * dr)
    val deltat = if (t < -11) {
        0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t3 * t
    } else {
        -0.000278 + 0.000265 * t + 0.000262 * t2
    }
    val jdNew = jd1 + c1 - deltat
    return floor(jdNew + 0.5 + timeZoneOffset / 24.0).toInt()
}

private fun sunLongitude(jdn: Int, timeZoneOffset: Double = 7.0): Double {
    val t = (jdn - 2451545.0 - timeZoneOffset / 24.0) / 36525.0
    val t2 = t * t
    val dr = Math.PI / 180.0
    val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
    val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
    var dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m)
    dl += (0.019993 - 0.000101 * t) * Math.sin(dr * 2 * m) + 0.000290 * Math.sin(dr * 3 * m)
    var l = l0 + dl
    l *= dr
    l -= Math.PI * 2 * floor(l / (Math.PI * 2))
    return l
}

private fun sunMajorTerm(dayNumber: Int, timeZoneOffset: Double = 7.0): Int {
    return floor(sunLongitude(dayNumber - 0.5.toInt(), timeZoneOffset) / Math.PI * 6).toInt()
}

private fun lunarMonth11(yy: Int, timeZoneOffset: Double = 7.0): Int {
    val off = jdFromDate(31, 12, yy) - 2415021
    val k = floor(off / 29.530588853).toInt()
    var nm = newMoonDayNumber(k, timeZoneOffset)
    val sunLong = sunMajorTerm(nm, timeZoneOffset)
    if (sunLong >= 9) {
        nm = newMoonDayNumber(k - 1, timeZoneOffset)
    }
    return nm
}

private fun leapMonthOffset(a11: Int, timeZoneOffset: Double = 7.0): Int {
    val k = floor((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
    var last = 0
    var i = 1
    var arc = sunMajorTerm(newMoonDayNumber(k + i, timeZoneOffset), timeZoneOffset)
    do {
        last = arc
        i++
        arc = sunMajorTerm(newMoonDayNumber(k + i, timeZoneOffset), timeZoneOffset)
    } while (arc != last && i < 14)
    return i - 1
}

private fun canChiDay(jd: Int): String {
    val can = CAN_NAMES[(jd + 9) % 10]
    val chi = CHI_NAMES[(jd + 1) % 12]
    return "$can $chi"
}

private fun canChiMonth(lunarMonth: Int, lunarYear: Int): String {
    val monthCanIndex = (lunarYear * 12 + lunarMonth + 3) % 10
    val monthChiIndex = (lunarMonth + 1) % 12
    return "${CAN_NAMES[(monthCanIndex + 10) % 10]} ${CHI_NAMES[(monthChiIndex + 12) % 12]}"
}

private fun canChiYear(lunarYear: Int): String {
    val yearCanIndex = (lunarYear + 6) % 10
    val yearChiIndex = (lunarYear + 8) % 12
    return "${CAN_NAMES[(yearCanIndex + 10) % 10]} ${CHI_NAMES[(yearChiIndex + 12) % 12]}"
}

private fun convertSolarToLunarSummary(day: Int, month: Int, year: Int): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).apply {
        isLenient = false
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
    }
    cal.timeInMillis

    val timeZoneOffset = 7.0
    val dayNumber = jdFromDate(day, month, year)
    val k = floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
    var monthStart = newMoonDayNumber(k + 1, timeZoneOffset)
    if (monthStart > dayNumber) {
        monthStart = newMoonDayNumber(k, timeZoneOffset)
    }
    var a11 = lunarMonth11(year, timeZoneOffset)
    var b11 = a11
    val lunarYear: Int
    if (a11 >= monthStart) {
        lunarYear = year
        a11 = lunarMonth11(year - 1, timeZoneOffset)
    } else {
        lunarYear = year + 1
        b11 = lunarMonth11(year + 1, timeZoneOffset)
    }
    val lunarDay = dayNumber - monthStart + 1
    val diff = floor((monthStart - a11) / 29.0).toInt()
    var lunarLeap = false
    var lunarMonth = diff + 11
    if (b11 - a11 > 365) {
        val leapOff = leapMonthOffset(a11, timeZoneOffset)
        if (diff >= leapOff) {
            lunarMonth = diff + 10
            if (diff == leapOff) {
                lunarLeap = true
            }
        }
    }
    if (lunarMonth > 12) {
        lunarMonth -= 12
    }

    val leapText = if (lunarLeap) " (nhuận)" else ""
    val dCanChi = canChiDay(dayNumber)
    val mCanChi = canChiMonth(lunarMonth, lunarYear)
    val yCanChi = canChiYear(lunarYear)

    return "Âm lịch: Ngày $lunarDay/$lunarMonth/$lunarYear$leapText • Ngày $dCanChi • Tháng $mCanChi • Năm $yCanChi"
}

private fun sendFeedbackViaMail(
    context: Context,
    category: FeedbackCategory,
    message: String,
    locationName: String,
    isDark: Boolean
) {
    val subject = "[Thời tiết & Âm lịch] ${category.label} (v$APP_VERSION_NAME)"
    val body = buildString {
        appendLine("Loại phản hồi: ${category.label}")
        appendLine("Phiên bản app: $APP_VERSION_NAME")
        appendLine("Địa điểm hiện tại: $locationName")
        appendLine("Chế độ nền: ${if (isDark) "Tối" else "Sáng"}")
        appendLine("Thiết bị: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
        appendLine("----------------------------------------")
        appendLine(message.trim())
    }

    val mailUri = Uri.parse(
        "mailto:${Uri.encode(FEEDBACK_EMAIL)}" +
            "?subject=${Uri.encode(subject)}" +
            "&body=${Uri.encode(body)}"
    )
    val intent = Intent(Intent.ACTION_SENDTO, mailUri)
    try {
        context.startActivity(Intent.createChooser(intent, "Gửi qua ứng dụng Email"))
    } catch (_: ActivityNotFoundException) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Phản hồi Thời tiết & Âm lịch", "To: $FEEDBACK_EMAIL\nSubject: $subject\n\n$body"))
        Toast.makeText(
            context,
            "Không tìm thấy app Mail. Đã sao chép nội dung tới $FEEDBACK_EMAIL vào bộ nhớ tạm.",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
private fun WidgetClockText(
    showSeconds: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color
) {
    var clockText by remember(showSeconds) {
        val fmt = SimpleDateFormat(if (showSeconds) "HH:mm:ss" else "HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }
        mutableStateOf(fmt.format(Date()))
    }

    LaunchedEffect(showSeconds) {
        val fmt = SimpleDateFormat(if (showSeconds) "HH:mm:ss" else "HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }
        while (true) {
            clockText = fmt.format(Date())
            delay(if (showSeconds) 1_000L else 15_000L)
        }
    }

    Text(
        clockText,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip
    )
}

@Composable
private fun WidgetPreviewBox(
    weather: WeatherCache,
    solarToday: String,
    lunarSummary: String,
    widgetBgMode: Int,
    accentColor: Color,
    showSeconds: Boolean,
    showWeatherEffect: Boolean,
    showLunarDate: Boolean,
    showWeatherInfo: Boolean,
    showSolarDate: Boolean,
    showLocationName: Boolean,
    showSevenDayForecast: Boolean,
    sevenDayChips: List<PreviewDayChip>,
    showSignature: Boolean,
    signature: String,
    clockScale: Int,
    lunarScale: Int,
    solarScale: Int,
    sigScale: Int,
    isDarkApp: Boolean,
    modifier: Modifier = Modifier
) {
    val effectiveDarkBg = when (widgetBgMode) {
        0 -> true
        1 -> false
        4 -> true
        else -> isDarkApp
    }

    val bgBrush = remember(widgetBgMode, isDarkApp, accentColor) {
        when (widgetBgMode) {
            0 -> Brush.linearGradient(
                listOf(
                    Color(0xFF18181C),
                    accentColor.copy(alpha = 0.14f),
                    Color(0xFF101013)
                )
            )
            1 -> Brush.linearGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    accentColor.copy(alpha = 0.08f),
                    Color(0xFFF1F3F6)
                )
            )
            2 -> Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
            4 -> Brush.linearGradient(
                listOf(
                    Color(0xFF263552).copy(alpha = 0.85f),
                    Color(0xFF1A2438).copy(alpha = 0.72f),
                    accentColor.copy(alpha = 0.18f),
                    Color(0xFF0D1320).copy(alpha = 0.78f)
                )
            )
            else -> if (isDarkApp) {
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1B1B20),
                        accentColor.copy(alpha = 0.12f),
                        Color(0xFF111115)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        accentColor.copy(alpha = 0.08f),
                        Color(0xFFF3F4F7)
                    )
                )
            }
        }
    }

    val primaryText = if (effectiveDarkBg) Color(0xFFFAFAFA) else Color(0xFF09090B)
    val secondaryText = if (effectiveDarkBg) Color(0xFFA1A1AA) else Color(0xFF52525B)
    val chipBg = if (effectiveDarkBg) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val borderColor = remember(widgetBgMode, effectiveDarkBg, accentColor) {
        when {
            widgetBgMode == 2 -> accentColor.copy(alpha = 0.55f)
            widgetBgMode == 4 -> Color(0xFF90CAF9).copy(alpha = 0.55f)
            effectiveDarkBg -> Color.White.copy(alpha = 0.16f)
            else -> Color.Black.copy(alpha = 0.12f)
        }
    }

    val clockSp = (26 + (clockScale - 10) * 0.8f).coerceIn(18f, 36f).sp
    val lunarSp = (11.5f + (lunarScale - 10) * 0.35f).coerceIn(9.5f, 16f).sp
    val solarSp = (13f + (solarScale - 10) * 0.35f).coerceIn(10.5f, 17.5f).sp
    val sigSp = (11f + (sigScale - 10) * 0.3f).coerceIn(9.5f, 15f).sp

    val effectBadge = remember(weather.weatherCode) {
        when (weather.weatherCode) {
            0 -> "☀️ Nắng"
            1, 2, 3 -> "☁️ Mây"
            45, 48 -> "🌫️ Sương mù"
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️ Mưa"
            95, 96, 99 -> "⛈️ Dông"
            else -> "🌤️ Thời tiết"
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bgBrush)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (showLunarDate) {
                        Text(
                            lunarSummary,
                            fontSize = lunarSp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            maxLines = 2
                        )
                    }
                    if (showWeatherEffect && showWeatherInfo) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                effectBadge,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (showWeatherInfo) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            forecastEmojiForCode(weather.weatherCode),
                            fontSize = 19.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            weather.currentTemp,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            maxLines = 1
                        )
                    }
                }
            }

            if (showSolarDate || showLocationName) {
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showSolarDate) {
                        Text(
                            solarToday,
                            fontSize = solarSp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (showLocationName) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            weather.locationName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.weight(1f)
                ) {
                    WidgetClockText(
                        showSeconds = showSeconds,
                        fontSize = clockSp,
                        color = primaryText
                    )
                }

                if (showWeatherInfo) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            weather.description,
                            fontSize = 11.sp,
                            color = secondaryText,
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                        Text(
                            "💧 ${weather.humidity} • 💨 ${weather.windSpeed}",
                            fontSize = 10.5.sp,
                            color = secondaryText,
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                        Text(
                            "▲ ${weather.maxTemp}  ▼ ${weather.minTemp}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showSevenDayForecast) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        sevenDayChips.take(7).forEach { chip ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(chipBg)
                                    .border(1.dp, borderColor, RoundedCornerShape(7.dp))
                                    .padding(1.dp)
                            ) {
                                Text(
                                    chip.dayLabel,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryText,
                                    maxLines = 1
                                )
                                Text(
                                    chip.icon,
                                    fontSize = 9.5.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (showSignature && signature.isNotBlank()) {
                    Text(
                        signature,
                        fontSize = sigSp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassPopupSurface(
    palette: AdaptivePalette,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val baseFill = if (palette.isDark) Color(0xFF14151B) else Color(0xFFFCFDFF)
    Surface(
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp,
        shadowElevation = 18.dp,
        color = baseFill,
        contentColor = palette.popupTextPrimary,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(baseFill)
            .background(palette.popupBrush)
            .border(1.3.dp, palette.popupBorder, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ThoiTietAmLichApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val baseDensity = LocalDensity.current
    val lockedFontScaleDensity = remember(baseDensity) {
        Density(density = baseDensity.density, fontScale = 1f)
    }

    val nowCal = remember { Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")) }
    val currentSolarYear = remember { nowCal.get(Calendar.YEAR) }
    val allDefaultHolidayIds = remember {
        BirthdayHelper.DEFAULT_VIETNAM_AND_INTERNATIONAL_HOLIDAYS.map { it.id }.toSet()
    }

    var appThemeMode by remember { mutableIntStateOf(AppPrefs.getAppThemeMode(context)) }
    var widgetBgMode by remember { mutableIntStateOf(AppPrefs.getWidgetBgMode(context)) }
    var accentColorInt by remember { mutableIntStateOf(AppPrefs.getAccentColor(context)) }
    var showSeconds by remember { mutableStateOf(AppPrefs.isShowSeconds(context)) }
    var showSecondsWarningPopup by remember { mutableStateOf(false) }
    var showWeatherEffect by remember { mutableStateOf(AppPrefs.isShowWeatherEffect(context)) }
    var showLunarDate by remember { mutableStateOf(AppPrefs.isShowLunarDate(context)) }
    var showWeatherInfo by remember { mutableStateOf(AppPrefs.isShowWeatherInfo(context)) }
    var showSolarDate by remember { mutableStateOf(AppPrefs.isShowSolarDate(context)) }
    var showLocationName by remember { mutableStateOf(AppPrefs.isShowLocationName(context)) }
    val showSevenDayForecast = true
    var showSignature by remember { mutableStateOf(AppPrefs.isShowSignature(context)) }
    var signature by remember { mutableStateOf(AppPrefs.getSignature(context)) }

    var clockScale by remember { mutableIntStateOf(AppPrefs.getClockScale(context)) }
    var lunarScale by remember { mutableIntStateOf(AppPrefs.getLunarScale(context)) }
    var solarScale by remember { mutableIntStateOf(AppPrefs.getSolarScale(context)) }
    var sigScale by remember { mutableIntStateOf(AppPrefs.getSigScale(context)) }

    var autoLocation by remember { mutableStateOf(AppPrefs.isAutoLocation(context)) }
    var weather by remember { mutableStateOf(AppPrefs.getWeather(context)) }
    var isSyncing by remember { mutableStateOf(false) }

    var locationQuery by remember { mutableStateOf("") }
    var isSearchingLocation by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<WeatherService.SearchLocationResult>>(emptyList()) }
    var locationSearchMsg by remember { mutableStateOf<String?>(null) }

    var lookupDayInput by remember { mutableStateOf(nowCal.get(Calendar.DAY_OF_MONTH).toString()) }
    var lookupMonthInput by remember { mutableStateOf((nowCal.get(Calendar.MONTH) + 1).toString()) }
    var lookupYearInput by remember { mutableStateOf(nowCal.get(Calendar.YEAR).toString()) }
    var lookupError by remember { mutableStateOf<String?>(null) }
    var lunarHistory by remember { mutableStateOf(AppPrefs.getLunarHistory(context)) }
    var lunarResultPopup by remember { mutableStateOf<LunarResultPopupData?>(null) }
    var showHistoryClearedPopup by remember { mutableStateOf(false) }

    val lunarInsight = remember(context) { BirthdayHelper.getLunarUsefulInsightForToday(context) }
    var lunarUsefulNotifyEnabled by remember { mutableStateOf(AppPrefs.isLunarUsefulNotifyEnabled(context)) }
    var lunarDayNoteInput by remember(lunarInsight.lunarDateKey) {
        mutableStateOf(AppPrefs.getLunarDayNote(context, lunarInsight.lunarDateKey))
    }
    var lunarNoteSavedNotice by remember { mutableStateOf(false) }

    var ageDayInput by remember { mutableStateOf("1") }
    var ageMonthInput by remember { mutableStateOf("1") }
    var ageYearInput by remember { mutableStateOf("2000") }
    var ageError by remember { mutableStateOf<String?>(null) }
    var ageHistory by remember { mutableStateOf(AppPrefs.getAgeHistory(context)) }
    var ageResultPopup by remember { mutableStateOf<AgeResultPopupData?>(null) }
    var showAgeHistoryClearedPopup by remember { mutableStateOf(false) }

    var todoTasks by remember { mutableStateOf(TodoHelper.ensureCurrentDayOrReset(context)) }
    var todoHourInput by remember { mutableStateOf(nowCal.get(Calendar.HOUR_OF_DAY).toString()) }
    var todoMinuteInput by remember {
        mutableStateOf(((nowCal.get(Calendar.MINUTE) + 1) % 60).toString())
    }
    var todoTitleInput by remember { mutableStateOf("") }
    var todoError by remember { mutableStateOf<String?>(null) }
    var activeTodoPopup by remember { mutableStateOf<TodoTaskItem?>(null) }

    var birthdays by remember { mutableStateOf(AppPrefs.getBirthdays(context)) }
    var bdayNameInput by remember { mutableStateOf("") }
    var bdayDayInput by remember { mutableStateOf("1") }
    var bdayMonthInput by remember { mutableStateOf("1") }
    var bdayError by remember { mutableStateOf<String?>(null) }

    var holidayReminderEnabled by remember { mutableStateOf(AppPrefs.isHolidayReminderEnabled(context)) }
    var enabledHolidayIds by remember {
        mutableStateOf(AppPrefs.getEnabledHolidayIds(context) ?: allDefaultHolidayIds)
    }
    var activeHolidayAlerts by remember {
        mutableStateOf(BirthdayHelper.getActiveHolidaysForToday(context))
    }
    var showHolidayPopup by remember { mutableStateOf(false) }

    var feedbackCategory by remember { mutableStateOf(FeedbackCategory.BUG) }
    var feedbackMessageInput by remember { mutableStateOf("") }
    var feedbackError by remember { mutableStateOf<String?>(null) }

    var expandedSection by remember { mutableStateOf<ExpandableSection?>(null) }
    fun toggleSection(section: ExpandableSection) {
        expandedSection = if (expandedSection == section) null else section
    }

    val lookupSectionBringIntoView = remember { BringIntoViewRequester() }
    val lunarUsefulBringIntoView = remember { BringIntoViewRequester() }
    val ageSectionBringIntoView = remember { BringIntoViewRequester() }
    val todoSectionBringIntoView = remember { BringIntoViewRequester() }
    val bdaySectionBringIntoView = remember { BringIntoViewRequester() }
    val feedbackSectionBringIntoView = remember { BringIntoViewRequester() }

    val todayBirthdays = remember(birthdays) { BirthdayHelper.getTodayBirthdays(context) }
    val showAdminBirthdayToday = remember { BirthdayHelper.isAdminBirthdayToday() }
    var showBirthdayPopup by remember { mutableStateOf(false) }

    val previewSevenDays = remember(weather) {
        resolvePreviewSevenDays(context, weather.weatherCode)
    }

    suspend fun syncWeatherData() {
        if (isSyncing) return
        isSyncing = true
        try {
            weather = WeatherService.syncWeather(context)
        } finally {
            isSyncing = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        scope.launch {
            syncWeatherData()
        }
    }

    fun refreshTodosAndCheckDueTasks() {
        val currentList = TodoHelper.ensureCurrentDayOrReset(context)
        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val curH = c.get(Calendar.HOUR_OF_DAY)
        val curM = c.get(Calendar.MINUTE)

        val pendingId = TodoHelper.getPendingPopupTaskId(context)
        val fromReceiver = pendingId?.let { id -> currentList.find { it.id == id } }
        val dueNow = currentList.firstOrNull { task ->
            !task.triggeredToday && !task.isDone &&
                (task.hour < curH || (task.hour == curH && task.minute <= curM))
        }

        val popupTarget = fromReceiver ?: dueNow
        if (popupTarget != null && activeTodoPopup?.id != popupTarget.id) {
            val marked = currentList.map {
                if (it.id == popupTarget.id) it.copy(triggeredToday = true) else it
            }
            TodoHelper.saveTasks(context, marked)
            TodoHelper.setPendingPopupTaskId(context, null)
            todoTasks = marked
            activeTodoPopup = popupTarget.copy(triggeredToday = true)
        } else if (currentList != todoTasks) {
            todoTasks = currentList
        }
    }

    LaunchedEffect(Unit) {
        TodoHelper.ensureChannel(context)
        refreshTodosAndCheckDueTasks()

        BirthdayHelper.notifyLunarUsefulInfoOncePerDay(context)

        val currentAlerts = BirthdayHelper.getActiveHolidaysForToday(context)
        activeHolidayAlerts = currentAlerts
        if (currentAlerts.isNotEmpty() &&
            AppPrefs.getLastHolidayAlertKey(context) != BirthdayHelper.todayDateKey()
        ) {
            showHolidayPopup = true
        }

        if (BirthdayHelper.shouldShowAppPopupToday(context)) {
            showBirthdayPopup = true
            BirthdayHelper.markAppPopupShownToday(context)
        }
        val neededPerms = buildList {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (neededPerms.isNotEmpty()) {
            permissionLauncher.launch(neededPerms.toTypedArray())
        } else {
            syncWeatherData()
        }

        while (true) {
            delay(30_000L)
            refreshTodosAndCheckDueTasks()
        }
    }

    val isDark = when (appThemeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val accentCompose = remember(accentColorInt) { Color(accentColorInt) }
    val palette = remember(isDark, accentCompose) {
        buildAdaptivePalette(isDark = isDark, accentColor = accentCompose)
    }
    val selectedAccentName = remember(accentColorInt) {
        ACCENT_OPTIONS.firstOrNull { it.colorInt == accentColorInt }?.name ?: "Tùy chỉnh"
    }

    val colorScheme = remember(palette, accentCompose) {
        if (palette.isDark) {
            darkColorScheme(
                primary = accentCompose,
                secondary = accentCompose,
                background = palette.screenBg,
                surface = palette.cardBg,
                surfaceVariant = palette.subtleBg,
                onBackground = palette.textPrimary,
                onSurface = palette.textPrimary,
                onSurfaceVariant = palette.textSecondary
            )
        } else {
            lightColorScheme(
                primary = accentCompose,
                secondary = accentCompose,
                background = palette.screenBg,
                surface = palette.cardBg,
                surfaceVariant = palette.subtleBg,
                onBackground = palette.textPrimary,
                onSurface = palette.textPrimary,
                onSurfaceVariant = palette.textSecondary
            )
        }
    }

    val lunarToday = remember { LunarCalendar.todayLunar() }
    val lunarTodaySummary = remember(lunarToday) { lunarToday.shortSummary() }
    val solarToday = remember { LunarCalendar.formatVietnameseSolarDate() }
    val dailyWarmMessage = remember(nowCal) { dailyWarmMessageForToday(nowCal) }

    fun performLunarLookup(day: Int, month: Int, year: Int) {
        val summary = convertSolarToLunarSummary(day, month, year)
        val solarFormatted = "${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/$year"
        val newItem = LunarLookupHistoryItem(
            id = UUID.randomUUID().toString(),
            solarDay = day,
            solarMonth = month,
            solarYear = year,
            lunarSummary = summary
        )
        val updatedHistory = (listOf(newItem) + lunarHistory.filterNot {
            it.solarDay == day && it.solarMonth == month && it.solarYear == year
        }).take(20)
        lunarHistory = updatedHistory
        AppPrefs.saveLunarHistory(context, updatedHistory)
        lunarResultPopup = LunarResultPopupData(
            solarLabel = "Dương lịch: $solarFormatted",
            lunarSummary = summary
        )
    }

    fun performAgeCalculation(day: Int, month: Int, year: Int) {
        val calc = calculateAgeFromNow(day, month, year)
        val birthFormatted = "${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/$year"
        val newItem = AgeHistoryItem(
            id = UUID.randomUUID().toString(),
            birthDay = day,
            birthMonth = month,
            birthYear = year,
            ageSummary = calc.summary
        )
        val updatedHistory = (listOf(newItem) + ageHistory.filterNot {
            it.birthDay == day && it.birthMonth == month && it.birthYear == year
        }).take(20)
        ageHistory = updatedHistory
        AppPrefs.saveAgeHistory(context, updatedHistory)
        ageResultPopup = AgeResultPopupData(
            birthLabel = "Ngày sinh: $birthFormatted",
            currentLabel = "Tính đến ngày: ${calc.currentLabel}",
            ageSummary = calc.summary
        )
    }

    CompositionLocalProvider(LocalDensity provides lockedFontScaleDensity) {
        MaterialTheme(colorScheme = colorScheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.screenBrush)
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = palette.textPrimary,
                                actionIconContentColor = palette.textPrimary
                            ),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(accentCompose)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Thời tiết & Âm lịch",
                                        color = palette.textPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.3).sp
                                    )
                                }
                            },
                            actions = {
                                Surface(
                                    shape = CircleShape,
                                    color = palette.cardBg,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .border(1.dp, palette.border, CircleShape)
                                ) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                syncWeatherData()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Đồng bộ thời tiết",
                                            tint = palette.textPrimary
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "top_bento_editorial") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 2.dp)) {
                                    Text(
                                        solarToday.uppercase(Locale.getDefault()),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = accentCompose
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        dailyWarmMessage,
                                        fontSize = 19.sp,
                                        lineHeight = 25.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = palette.textPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = palette.cardBg,
                                    contentColor = palette.textPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, palette.border, RoundedCornerShape(24.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 10.dp)
                                        ) {
                                            Text(
                                                "XEM TRƯỚC WIDGET",
                                                fontSize = 11.sp,
                                                letterSpacing = 0.8.sp,
                                                color = palette.textSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                weather.locationName,
                                                fontSize = 11.sp,
                                                color = accentCompose,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }

                                        WidgetPreviewBox(
                                            weather = weather,
                                            solarToday = solarToday,
                                            lunarSummary = lunarTodaySummary,
                                            widgetBgMode = widgetBgMode,
                                            accentColor = accentCompose,
                                            showSeconds = showSeconds,
                                            showWeatherEffect = showWeatherEffect,
                                            showLunarDate = showLunarDate,
                                            showWeatherInfo = showWeatherInfo,
                                            showSolarDate = showSolarDate,
                                            showLocationName = showLocationName,
                                            showSevenDayForecast = showSevenDayForecast,
                                            sevenDayChips = previewSevenDays,
                                            showSignature = showSignature,
                                            signature = signature,
                                            clockScale = clockScale,
                                            lunarScale = lunarScale,
                                            solarScale = solarScale,
                                            sigScale = sigScale,
                                            isDarkApp = palette.isDark
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = palette.cardBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
                                            .clickable(enabled = !isSyncing) {
                                                scope.launch {
                                                    syncWeatherData()
                                                }
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = null,
                                                tint = accentCompose,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                if (isSyncing) "Đang tải..." else "Cập nhật",
                                                color = palette.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                "Đồng bộ thời tiết",
                                                color = palette.textSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = palette.cardBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
                                            .clickable {
                                                val pinned = WeatherLunarWidget.requestPinWidget(context)
                                                if (!pinned) {
                                                    Toast.makeText(
                                                        context,
                                                        "Launcher hiện tại không hỗ trợ ghim tự động. Bạn nhấn giữ màn hình chính -> Tiện ích để thêm nhé.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Icon(
                                                Icons.Default.AddToHomeScreen,
                                                contentDescription = null,
                                                tint = accentCompose,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Ghim Widget",
                                                color = palette.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                "Ra màn hình chính",
                                                color = palette.textSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "app_theme_outside_bar") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Nền ứng dụng",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val themeLabels = listOf("Hệ thống", "Sáng", "Tối")
                                    themeLabels.forEachIndexed { idx, label ->
                                        val selected = appThemeMode == idx
                                        FilterChip(
                                            selected = selected,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = palette.cardBg,
                                                labelColor = palette.textPrimary,
                                                selectedContainerColor = accentCompose,
                                                selectedLabelColor = palette.onAccentText
                                            ),
                                            onClick = {
                                                appThemeMode = idx
                                                AppPrefs.setAppThemeMode(context, idx)
                                            },
                                            label = {
                                                Text(
                                                    label,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Đồng hồ, giao diện, nền widget
                        item(key = "clock_theme_bg_section") {
                            CollapsibleSectionCard(
                                title = "Đồng hồ, Giao diện & Nền Widget",
                                badgeText = if (showSeconds) "HH:mm:ss" else "HH:mm",
                                accentColor = accentCompose,
                                palette = palette,
                                expanded = expandedSection == ExpandableSection.CLOCK_THEME_BG,
                                onToggle = { toggleSection(ExpandableSection.CLOCK_THEME_BG) }
                            ) {
                                FlatSwitchRow(
                                    title = "Hiển thị giây (chạy liên tục)",
                                    subtitle = "Định dạng HH:mm:ss trên đồng hồ Widget",
                                    checked = showSeconds,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            showSecondsWarningPopup = true
                                        } else {
                                            showSeconds = false
                                            AppPrefs.setShowSeconds(context, false)
                                            WeatherLunarWidget.updateAllWidgets(context)
                                        }
                                    }
                                )
                                SectionHairline(palette)

                                FlatSwitchRow(
                                    title = "Hiệu ứng thời tiết trên Widget",
                                    subtitle = "Bật/tắt lớp hiệu ứng nắng, mây, sương mù, mưa theo thời tiết",
                                    checked = showWeatherEffect,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        showWeatherEffect = checked
                                        AppPrefs.setShowWeatherEffect(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                                SectionHairline(palette)

                                FlatSwitchRow(
                                    title = "Hiển thị lịch âm",
                                    subtitle = "Bật/tắt dòng thông tin Âm lịch trên Widget",
                                    checked = showLunarDate,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        showLunarDate = checked
                                        AppPrefs.setShowLunarDate(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                                SectionHairline(palette)

                                FlatSwitchRow(
                                    title = "Hiển thị nhiệt độ (kèm thời tiết)",
                                    subtitle = "Bật/tắt nhiệt độ, trạng thái, độ ẩm, gió & biên độ nhiệt",
                                    checked = showWeatherInfo,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        showWeatherInfo = checked
                                        AppPrefs.setShowWeatherInfo(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                                SectionHairline(palette)

                                FlatSwitchRow(
                                    title = "Hiển thị dương lịch",
                                    subtitle = "Bật/tắt dòng thứ và ngày tháng Dương lịch trên Widget",
                                    checked = showSolarDate,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        showSolarDate = checked
                                        AppPrefs.setShowSolarDate(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                                SectionHairline(palette)

                                FlatSwitchRow(
                                    title = "Hiển thị địa điểm",
                                    subtitle = "Bật/tắt tên địa danh cập nhật thời tiết trên Widget",
                                    checked = showLocationName,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        showLocationName = checked
                                        AppPrefs.setShowLocationName(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    "Kiểu nền Widget trên màn hình chính",
                                    color = accentCompose,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                val bgLabels = listOf(
                                    "Kính lỏng tối",
                                    "Kính lỏng sáng",
                                    "Trong suốt",
                                    "Theo chủ đề app",
                                    "Liquid Glass"
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    items(bgLabels.size) { idx ->
                                        val selected = widgetBgMode == idx
                                        FilterChip(
                                            selected = selected,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = palette.subtleBg,
                                                labelColor = palette.textPrimary,
                                                selectedContainerColor = accentCompose.copy(alpha = 0.22f),
                                                selectedLabelColor = palette.textPrimary
                                            ),
                                            onClick = {
                                                widgetBgMode = idx
                                                AppPrefs.setWidgetBgMode(context, idx)
                                                WeatherLunarWidget.updateAllWidgets(context)
                                            },
                                            label = { Text(bgLabels[idx], fontWeight = FontWeight.SemiBold) }
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Màu chủ đề
                        item(key = "accent_color_section") {
                            CollapsibleSectionCard(
                                title = "Màu chủ đề (Áp dụng App & Widget)",
                                badgeText = selectedAccentName,
                                accentColor = accentCompose,
                                palette = palette,
                                expanded = expandedSection == ExpandableSection.ACCENT_COLOR,
                                onToggle = { toggleSection(ExpandableSection.ACCENT_COLOR) }
                            ) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(ACCENT_OPTIONS, key = { it.colorInt }) { opt ->
                                        val selected = opt.colorInt == accentColorInt
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    accentColorInt = opt.colorInt
                                                    AppPrefs.setAccentColor(context, opt.colorInt)
                                                    WeatherLunarWidget.updateAllWidgets(context)
                                                }
                                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(opt.colorInt))
                                                    .border(
                                                        width = if (selected) 3.dp else 1.5.dp,
                                                        color = if (selected) palette.textPrimary else Color.White.copy(alpha = 0.45f),
                                                        shape = CircleShape
                                                    )
                                            )
                                            Text(
                                                opt.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) Color(opt.colorInt) else palette.textPrimary,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                modifier = Modifier.padding(top = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Vị trí cập nhật thời tiết & Chữ ký trên widget
                        item(key = "location_and_signature_section") {
                            CollapsibleSectionCard(
                                title = "Vị trí cập nhật thời tiết & Chữ ký trên Widget",
                                badgeText = if (autoLocation) "GPS" else "Tùy chọn",
                                accentColor = accentCompose,
                                palette = palette,
                                expanded = expandedSection == ExpandableSection.LOCATION_SIGNATURE,
                                onToggle = { toggleSection(ExpandableSection.LOCATION_SIGNATURE) }
                            ) {
                                FlatSwitchRow(
                                    title = "Tự động theo vị trí hiện tại (GPS)",
                                    subtitle = "Tắt để nhập tên tìm địa điểm bất kỳ",
                                    checked = autoLocation,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        autoLocation = checked
                                        AppPrefs.setAutoLocation(context, checked)
                                        scope.launch {
                                            syncWeatherData()
                                        }
                                    }
                                )

                                if (!autoLocation) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        ModernTextField(
                                            value = locationQuery,
                                            onValueChange = {
                                                locationQuery = it
                                                locationSearchMsg = null
                                            },
                                            label = "Nhập tên địa điểm (VD: Nghi Đức, Đà Lạt...)",
                                            accentColor = accentCompose,
                                            palette = palette,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isSearchingLocation = true
                                                    locationSearchMsg = null
                                                    val found = WeatherService.searchLocationsByName(locationQuery)
                                                    searchResults = found
                                                    if (found.isEmpty()) {
                                                        locationSearchMsg = "Không tìm thấy địa điểm phù hợp."
                                                    }
                                                    isSearchingLocation = false
                                                }
                                            },
                                            enabled = !isSearchingLocation,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = accentCompose,
                                                contentColor = palette.onAccentText
                                            )
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isSearchingLocation) "..." else "Tìm", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (locationSearchMsg != null) {
                                        Text(
                                            locationSearchMsg!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.textSecondary,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }

                                    searchResults.forEach { item ->
                                        val isCurrent = weather.locationName == item.displayName
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    val updatedCache = weather.copy(
                                                        locationName = item.displayName,
                                                        latitude = item.lat,
                                                        longitude = item.lon
                                                    )
                                                    AppPrefs.saveWeather(context, updatedCache)
                                                    weather = updatedCache
                                                    WeatherLunarWidget.updateAllWidgets(context)
                                                    scope.launch {
                                                        syncWeatherData()
                                                    }
                                                }
                                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) accentCompose else palette.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    item.displayName,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isCurrent) accentCompose else palette.textPrimary
                                                )
                                            }
                                            SectionHairline(palette, topPadding = 8.dp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                SectionHairline(palette)
                                Spacer(modifier = Modifier.height(6.dp))

                                FlatSwitchRow(
                                    title = "Hiển thị chữ ký",
                                    subtitle = "Tắt nếu không muốn hiện chữ ký dưới Widget",
                                    checked = showSignature,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onCheckedChange = { checked ->
                                        showSignature = checked
                                        AppPrefs.setShowSignature(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )

                                if (showSignature) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    ModernTextField(
                                        value = signature,
                                        onValueChange = { newText ->
                                            signature = newText
                                            AppPrefs.setSignature(context, newText)
                                            WeatherLunarWidget.updateAllWidgets(context)
                                        },
                                        label = "Nhập chữ ký (có dấu tiếng Việt)",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // 4b. Điều chỉnh kích cỡ chữ trên Widget
                        item(key = "font_scale_section") {
                            CollapsibleSectionCard(
                                title = "Kích cỡ chữ trên Widget",
                                badgeText = "1 – 20",
                                accentColor = accentCompose,
                                palette = palette,
                                expanded = expandedSection == ExpandableSection.FONT_SCALE,
                                onToggle = { toggleSection(ExpandableSection.FONT_SCALE) }
                            ) {
                                Text(
                                    "Điều chỉnh mức cỡ chữ từ 1 đến 20 cho từng thành phần trên Widget.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ScaleSliderRow(
                                    label = "Đồng hồ",
                                    value = clockScale,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onValueChange = { v ->
                                        clockScale = v
                                        AppPrefs.setClockScale(context, v)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                                SectionHairline(palette, topPadding = 4.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                ScaleSliderRow(
                                    label = "Lịch âm",
                                    value = lunarScale,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onValueChange = { v ->
                                        lunarScale = v
                                        AppPrefs.setLunarScale(context, v)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                                SectionHairline(palette, topPadding = 4.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                ScaleSliderRow(
                                    label = "Dương lịch",
                                    value = solarScale,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onValueChange = { v ->
                                        solarScale = v
                                        AppPrefs.setSolarScale(context, v)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                                SectionHairline(palette, topPadding = 4.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                ScaleSliderRow(
                                    label = "Chữ ký",
                                    value = sigScale,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    onValueChange = { v ->
                                        sigScale = v
                                        AppPrefs.setSigScale(context, v)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                            }
                        }

                        // 5. Việc cần làm
                        item(key = "todo_section") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(todoSectionBringIntoView)
                            ) {
                                CollapsibleSectionCard(
                                    title = "Việc cần làm hôm nay",
                                    badgeText = "${todoTasks.size}/${TodoHelper.MAX_TASKS_PER_DAY}",
                                    accentColor = accentCompose,
                                    palette = palette,
                                    expanded = expandedSection == ExpandableSection.TODO,
                                    onToggle = { toggleSection(ExpandableSection.TODO) }
                                ) {
                                    Text(
                                        "Tối đa 10 việc/ngày. Đúng giờ đặt sẽ hiện pop-up; tự động xóa hết lúc 00:00 ngày hôm sau.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        ModernTextField(
                                            value = todoHourInput,
                                            onValueChange = {
                                                todoHourInput = it.filter { ch -> ch.isDigit() }.take(2)
                                                todoError = null
                                            },
                                            label = "Giờ (0-23)",
                                            accentColor = accentCompose,
                                            palette = palette,
                                            modifier = Modifier
                                                .weight(1f)
                                                .onFocusChanged { focusState ->
                                                    if (focusState.isFocused) {
                                                        scope.launch {
                                                            delay(180)
                                                            todoSectionBringIntoView.bringIntoView()
                                                        }
                                                    }
                                                }
                                        )
                                        ModernTextField(
                                            value = todoMinuteInput,
                                            onValueChange = {
                                                todoMinuteInput = it.filter { ch -> ch.isDigit() }.take(2)
                                                todoError = null
                                            },
                                            label = "Phút (0-59)",
                                            accentColor = accentCompose,
                                            palette = palette,
                                            modifier = Modifier
                                                .weight(1f)
                                                .onFocusChanged { focusState ->
                                                    if (focusState.isFocused) {
                                                        scope.launch {
                                                            delay(180)
                                                            todoSectionBringIntoView.bringIntoView()
                                                        }
                                                    }
                                                }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ModernTextField(
                                        value = todoTitleInput,
                                        onValueChange = {
                                            todoTitleInput = it
                                            todoError = null
                                        },
                                        label = "Nội dung việc cần làm",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    scope.launch {
                                                        delay(180)
                                                        todoSectionBringIntoView.bringIntoView()
                                                    }
                                                }
                                            }
                                    )

                                    if (todoError != null) {
                                        Text(
                                            todoError!!,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            val currentList = TodoHelper.ensureCurrentDayOrReset(context)
                                            if (currentList.size >= TodoHelper.MAX_TASKS_PER_DAY) {
                                                todoError = "Mỗi ngày chỉ được thêm tối đa ${TodoHelper.MAX_TASKS_PER_DAY} việc cần làm."
                                                return@Button
                                            }
                                            val h = todoHourInput.toIntOrNull() ?: -1
                                            val m = todoMinuteInput.toIntOrNull() ?: -1
                                            val titleTrim = todoTitleInput.trim()
                                            if (h !in 0..23 || m !in 0..59) {
                                                todoError = "Vui lòng nhập giờ từ 0–23 và phút từ 0–59."
                                                return@Button
                                            }
                                            if (titleTrim.isEmpty()) {
                                                todoError = "Vui lòng nhập nội dung việc cần làm."
                                                return@Button
                                            }
                                            val newTask = TodoTaskItem(
                                                id = UUID.randomUUID().toString(),
                                                hour = h,
                                                minute = m,
                                                title = titleTrim
                                            )
                                            val updated = (currentList + newTask).sortedWith(
                                                compareBy({ it.hour }, { it.minute })
                                            )
                                            TodoHelper.saveTasks(context, updated)
                                            TodoHelper.scheduleAllTodayAlarms(context, updated)
                                            todoTasks = updated
                                            todoTitleInput = ""
                                            todoError = null
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accentCompose,
                                            contentColor = palette.onAccentText
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                    ) {
                                        Icon(Icons.Default.AddTask, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Thêm việc cần làm", fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (todoTasks.isEmpty()) {
                                        Text(
                                            "Hôm nay chưa có việc cần làm nào.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.textSecondary
                                        )
                                    } else {
                                        Column {
                                            todoTasks.forEachIndexed { idx, task ->
                                                if (idx > 0) SectionHairline(palette)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            task.timeFormatted,
                                                            color = accentCompose,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            style = MaterialTheme.typography.labelLarge
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            task.title,
                                                            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                            color = if (task.isDone) palette.textSecondary else palette.textPrimary,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            val updated = todoTasks.filterNot { it.id == task.id }
                                                            TodoHelper.saveTasks(context, updated)
                                                            TodoHelper.scheduleAllTodayAlarms(context, updated)
                                                            todoTasks = updated
                                                        }
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = "Xóa việc cần làm",
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6 & 7: Gộp "Tra cứu lịch âm" và "Tính tuổi" lên cùng 1 hàng
                        item(key = "lunar_and_age_row_section") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = SMOOTH_EXPAND_SPEC)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CompactDualCard(
                                        title = "Tra cứu lịch âm",
                                        badgeText = "${lunarHistory.size} lưu",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        expanded = expandedSection == ExpandableSection.LUNAR_LOOKUP,
                                        onToggle = { toggleSection(ExpandableSection.LUNAR_LOOKUP) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    CompactDualCard(
                                        title = "Tính tuổi",
                                        badgeText = "${ageHistory.size} lưu",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        expanded = expandedSection == ExpandableSection.AGE_CALC,
                                        onToggle = { toggleSection(ExpandableSection.AGE_CALC) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (expandedSection == ExpandableSection.LUNAR_LOOKUP) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.Transparent,
                                        contentColor = palette.textPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .bringIntoViewRequester(lookupSectionBringIntoView)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(palette.liquidGlassBrush)
                                            .border(1.3.dp, accentCompose.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                "Nhập ngày, tháng, năm dương lịch và bấm Tìm kiếm để xem thông báo lịch âm.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.textSecondary
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                ModernTextField(
                                                    value = lookupDayInput,
                                                    onValueChange = {
                                                        lookupDayInput = it.filter { ch -> ch.isDigit() }.take(2)
                                                        lookupError = null
                                                    },
                                                    label = "Ngày",
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                scope.launch {
                                                                    delay(180)
                                                                    lookupSectionBringIntoView.bringIntoView()
                                                                }
                                                            }
                                                        }
                                                )
                                                ModernTextField(
                                                    value = lookupMonthInput,
                                                    onValueChange = {
                                                        lookupMonthInput = it.filter { ch -> ch.isDigit() }.take(2)
                                                        lookupError = null
                                                    },
                                                    label = "Tháng",
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                scope.launch {
                                                                    delay(180)
                                                                    lookupSectionBringIntoView.bringIntoView()
                                                                }
                                                            }
                                                        }
                                                )
                                                ModernTextField(
                                                    value = lookupYearInput,
                                                    onValueChange = {
                                                        lookupYearInput = it.filter { ch -> ch.isDigit() }.take(4)
                                                        lookupError = null
                                                    },
                                                    label = "Năm",
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    modifier = Modifier
                                                        .weight(1.35f)
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                scope.launch {
                                                                    delay(180)
                                                                    lookupSectionBringIntoView.bringIntoView()
                                                                }
                                                            }
                                                        }
                                                )
                                            }

                                            if (lookupError != null) {
                                                Text(
                                                    lookupError!!,
                                                    color = MaterialTheme.colorScheme.error,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(top = 6.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = {
                                                    val d = lookupDayInput.toIntOrNull() ?: 0
                                                    val m = lookupMonthInput.toIntOrNull() ?: 0
                                                    val y = lookupYearInput.toIntOrNull() ?: 0
                                                    if (y !in 1900..2100 || m !in 1..12 || d !in 1..31) {
                                                        lookupError = "Vui lòng nhập ngày (1–31), tháng (1–12), năm (1900–2100) hợp lệ."
                                                        return@Button
                                                    }
                                                    try {
                                                        performLunarLookup(d, m, y)
                                                        lookupError = null
                                                    } catch (_: Exception) {
                                                        lookupError = "Ngày dương lịch không tồn tại (ví dụ 30/02 hoặc 31/04)."
                                                    }
                                                },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = accentCompose,
                                                    contentColor = palette.onAccentText
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(46.dp)
                                            ) {
                                                Icon(Icons.Default.Search, contentDescription = null)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Tìm kiếm", fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.History,
                                                        contentDescription = null,
                                                        tint = accentCompose,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "Lịch sử tìm kiếm (${lunarHistory.size})",
                                                        color = palette.textPrimary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                if (lunarHistory.isNotEmpty()) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            AppPrefs.clearLunarHistory(context)
                                                            lunarHistory = emptyList()
                                                            showHistoryClearedPopup = true
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.DeleteSweep,
                                                            contentDescription = null,
                                                            tint = palette.textPrimary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Xóa lịch sử", color = palette.textPrimary, fontSize = 12.sp)
                                                    }
                                                }
                                            }

                                            if (lunarHistory.isEmpty()) {
                                                Text(
                                                    "Chưa có lịch sử tra cứu nào.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = palette.textSecondary,
                                                    modifier = Modifier.padding(top = 6.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Column {
                                                    lunarHistory.forEachIndexed { idx, item ->
                                                        if (idx > 0) SectionHairline(palette)
                                                        val solarStr = remember(item.id) {
                                                            "${item.solarDay.toString().padStart(2, '0')}/${item.solarMonth.toString().padStart(2, '0')}/${item.solarYear}"
                                                        }
                                                        val freshSummary = remember(item.id) {
                                                            try {
                                                                convertSolarToLunarSummary(
                                                                    item.solarDay,
                                                                    item.solarMonth,
                                                                    item.solarYear
                                                                )
                                                            } catch (_: Exception) {
                                                                item.lunarSummary
                                                            }
                                                        }
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    lookupDayInput = item.solarDay.toString()
                                                                    lookupMonthInput = item.solarMonth.toString()
                                                                    lookupYearInput = item.solarYear.toString()
                                                                    lunarResultPopup = LunarResultPopupData(
                                                                        solarLabel = "Dương lịch: $solarStr",
                                                                        lunarSummary = freshSummary
                                                                    )
                                                                }
                                                                .padding(vertical = 10.dp)
                                                        ) {
                                                            Text(
                                                                "📅 Dương: $solarStr",
                                                                color = accentCompose,
                                                                fontWeight = FontWeight.Bold,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                            Text(
                                                                freshSummary,
                                                                color = palette.textPrimary,
                                                                fontWeight = FontWeight.Medium,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (expandedSection == ExpandableSection.AGE_CALC) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.Transparent,
                                        contentColor = palette.textPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .bringIntoViewRequester(ageSectionBringIntoView)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(palette.liquidGlassBrush)
                                            .border(1.3.dp, accentCompose.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                "Nhập ngày, tháng, năm sinh để tính số tuổi kèm số tháng tính đến hiện tại.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.textSecondary
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                ModernTextField(
                                                    value = ageDayInput,
                                                    onValueChange = {
                                                        ageDayInput = it.filter { ch -> ch.isDigit() }.take(2)
                                                        ageError = null
                                                    },
                                                    label = "Ngày sinh",
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                scope.launch {
                                                                    delay(180)
                                                                    ageSectionBringIntoView.bringIntoView()
                                                                }
                                                            }
                                                        }
                                                )
                                                ModernTextField(
                                                    value = ageMonthInput,
                                                    onValueChange = {
                                                        ageMonthInput = it.filter { ch -> ch.isDigit() }.take(2)
                                                        ageError = null
                                                    },
                                                    label = "Tháng sinh",
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                scope.launch {
                                                                    delay(180)
                                                                    ageSectionBringIntoView.bringIntoView()
                                                                }
                                                            }
                                                        }
                                                )
                                                ModernTextField(
                                                    value = ageYearInput,
                                                    onValueChange = {
                                                        ageYearInput = it.filter { ch -> ch.isDigit() }.take(4)
                                                        ageError = null
                                                    },
                                                    label = "Năm sinh",
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    modifier = Modifier
                                                        .weight(1.35f)
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                scope.launch {
                                                                    delay(180)
                                                                    ageSectionBringIntoView.bringIntoView()
                                                                }
                                                            }
                                                        }
                                                )
                                            }

                                            if (ageError != null) {
                                                Text(
                                                    ageError!!,
                                                    color = MaterialTheme.colorScheme.error,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(top = 6.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = {
                                                    val d = ageDayInput.toIntOrNull() ?: 0
                                                    val m = ageMonthInput.toIntOrNull() ?: 0
                                                    val y = ageYearInput.toIntOrNull() ?: 0
                                                    val currentYear = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
                                                        .get(Calendar.YEAR)
                                                    if (y !in 1900..currentYear || m !in 1..12 || d !in 1..31) {
                                                        ageError = "Vui lòng nhập ngày (1–31), tháng (1–12), năm sinh (1900–$currentYear) hợp lệ."
                                                        return@Button
                                                    }
                                                    try {
                                                        performAgeCalculation(d, m, y)
                                                        ageError = null
                                                    } catch (_: IllegalArgumentException) {
                                                        ageError = "Ngày sinh không được lớn hơn ngày hiện tại."
                                                    } catch (_: Exception) {
                                                        ageError = "Ngày sinh không tồn tại trên lịch dương."
                                                    }
                                                },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = accentCompose,
                                                    contentColor = palette.onAccentText
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(46.dp)
                                            ) {
                                                Icon(Icons.Default.PersonSearch, contentDescription = null)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Tính tuổi", fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.History,
                                                        contentDescription = null,
                                                        tint = accentCompose,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "Lịch sử tính tuổi (${ageHistory.size})",
                                                        color = palette.textPrimary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                if (ageHistory.isNotEmpty()) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            AppPrefs.clearAgeHistory(context)
                                                            ageHistory = emptyList()
                                                            showAgeHistoryClearedPopup = true
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.DeleteSweep,
                                                            contentDescription = null,
                                                            tint = palette.textPrimary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Xóa lịch sử", color = palette.textPrimary, fontSize = 12.sp)
                                                    }
                                                }
                                            }

                                            if (ageHistory.isEmpty()) {
                                                Text(
                                                    "Chưa có lịch sử tính tuổi nào.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = palette.textSecondary,
                                                    modifier = Modifier.padding(top = 6.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Column {
                                                    ageHistory.forEachIndexed { idx, item ->
                                                        if (idx > 0) SectionHairline(palette)
                                                        val birthStr = remember(item.id) {
                                                            "${item.birthDay.toString().padStart(2, '0')}/${item.birthMonth.toString().padStart(2, '0')}/${item.birthYear}"
                                                        }
                                                        val liveCalc = remember(item.id) {
                                                            try {
                                                                calculateAgeFromNow(item.birthDay, item.birthMonth, item.birthYear)
                                                            } catch (_: Exception) {
                                                                null
                                                            }
                                                        }
                                                        val displayAge = liveCalc?.summary ?: item.ageSummary
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    ageDayInput = item.birthDay.toString()
                                                                    ageMonthInput = item.birthMonth.toString()
                                                                    ageYearInput = item.birthYear.toString()
                                                                    val curStr = liveCalc?.currentLabel ?: run {
                                                                        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
                                                                        "${c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}/${(c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}/${c.get(Calendar.YEAR)}"
                                                                    }
                                                                    ageResultPopup = AgeResultPopupData(
                                                                        birthLabel = "Ngày sinh: $birthStr",
                                                                        currentLabel = "Tính đến ngày: $curStr",
                                                                        ageSummary = displayAge
                                                                    )
                                                                }
                                                                .padding(vertical = 10.dp)
                                                        ) {
                                                            Text(
                                                                "🎂 Sinh: $birthStr",
                                                                color = palette.textPrimary,
                                                                fontWeight = FontWeight.SemiBold,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                            Text(
                                                                displayAge,
                                                                color = accentCompose,
                                                                fontWeight = FontWeight.Bold,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6b. Thông tin hữu ích theo ngày Âm lịch
                        item(key = "lunar_useful_info_section") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(lunarUsefulBringIntoView)
                            ) {
                                CollapsibleSectionCard(
                                    title = "Thông tin hữu ích theo ngày Âm lịch",
                                    badgeText = lunarInsight.auspiciousStatus,
                                    accentColor = accentCompose,
                                    palette = palette,
                                    expanded = expandedSection == ExpandableSection.LUNAR_USEFUL_INFO,
                                    onToggle = { toggleSection(ExpandableSection.LUNAR_USEFUL_INFO) }
                                ) {
                                    FlatSwitchRow(
                                        title = "Thông báo 1 lần/ngày",
                                        subtitle = "Nhắc ngày Hoàng Đạo, tiết khí & giờ tốt theo ngày Âm lịch",
                                        checked = lunarUsefulNotifyEnabled,
                                        accentColor = accentCompose,
                                        palette = palette,
                                        onCheckedChange = { checked ->
                                            lunarUsefulNotifyEnabled = checked
                                            AppPrefs.setLunarUsefulNotifyEnabled(context, checked)
                                            if (checked) {
                                                BirthdayHelper.notifyLunarUsefulInfoOncePerDay(context)
                                            }
                                        }
                                    )
                                    SectionHairline(palette)

                                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                                        Text(
                                            "${lunarInsight.lunarDateLabel} • Ngày ${lunarInsight.dayCanChi}",
                                            color = accentCompose,
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "${lunarInsight.auspiciousStatus}: ${lunarInsight.auspiciousDetail}",
                                            color = palette.textPrimary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    SectionHairline(palette)

                                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                                        Text(
                                            "Tiết khí: ${lunarInsight.solarTermName}",
                                            color = palette.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Còn ${lunarInsight.daysUntilNextSolarTerm} ngày tới tiết khí ${lunarInsight.nextSolarTermName}.",
                                            color = palette.textSecondary,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    SectionHairline(palette)

                                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                                        Text(
                                            "Giờ Hoàng Đạo trong ngày",
                                            color = palette.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            lunarInsight.auspiciousHours,
                                            color = accentCompose,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    SectionHairline(palette)

                                    Text(
                                        lunarInsight.moonMilestoneNote,
                                        color = palette.textPrimary,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                    SectionHairline(palette)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    ModernTextField(
                                        value = lunarDayNoteInput,
                                        onValueChange = {
                                            lunarDayNoteInput = it
                                            lunarNoteSavedNotice = false
                                        },
                                        label = "Ghi chú nhanh cho ngày Âm lịch hôm nay",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    scope.launch {
                                                        delay(180)
                                                        lunarUsefulBringIntoView.bringIntoView()
                                                    }
                                                }
                                            }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedButton(
                                        onClick = {
                                            AppPrefs.setLunarDayNote(
                                                context,
                                                lunarInsight.lunarDateKey,
                                                lunarDayNoteInput.trim()
                                            )
                                            lunarNoteSavedNotice = true
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            if (lunarNoteSavedNotice) "Đã lưu ghi chú ngày Âm lịch" else "Lưu ghi chú ngày Âm lịch",
                                            color = palette.textPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // 8 & 9: Gộp "Nhắc ngày sinh nhật" và "Nhắc ngày lễ & kỷ niệm" lên cùng 1 hàng
                        item(key = "birthday_and_holiday_row_section") {
                            val activeHolidayCount = if (holidayReminderEnabled) enabledHolidayIds.size else 0
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = SMOOTH_EXPAND_SPEC)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CompactDualCard(
                                        title = "Nhắc ngày sinh nhật",
                                        badgeText = "${birthdays.size}/10",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        expanded = expandedSection == ExpandableSection.BIRTHDAY,
                                        onToggle = { toggleSection(ExpandableSection.BIRTHDAY) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    CompactDualCard(
                                        title = "Nhắc ngày lễ & kỷ niệm",
                                        badgeText = if (holidayReminderEnabled) "$activeHolidayCount bật" else "Tắt",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        expanded = expandedSection == ExpandableSection.HOLIDAY,
                                        onToggle = { toggleSection(ExpandableSection.HOLIDAY) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (expandedSection == ExpandableSection.BIRTHDAY) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.Transparent,
                                        contentColor = palette.textPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .bringIntoViewRequester(bdaySectionBringIntoView)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(palette.liquidGlassBrush)
                                            .border(1.3.dp, accentCompose.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                "Tự động hiện Pop-up khi mở app và gửi thông báo 1 lần/ngày vào đúng ngày sinh nhật.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.textSecondary
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            ModernTextField(
                                                value = bdayNameInput,
                                                onValueChange = {
                                                    bdayNameInput = it
                                                    bdayError = null
                                                },
                                                label = "Tên người sinh nhật",
                                                accentColor = accentCompose,
                                                palette = palette,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .onFocusChanged { focusState ->
                                                        if (focusState.isFocused) {
                                                            scope.launch {
                                                                delay(180)
                                                                bdaySectionBringIntoView.bringIntoView()
                                                            }
                                                        }
                                                    }
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                ModernTextField(
                                                    value = bdayDayInput,
                                                    onValueChange = {
                                                        bdayDayInput = it.filter { ch -> ch.isDigit() }.take(2)
                                                        bdayError = null
                                                    },
                                                    label = "Ngày (1-31)",
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                scope.launch {
                                                                    delay(180)
                                                                    bdaySectionBringIntoView.bringIntoView()
                                                                }
                                                            }
                                                        }
                                                )
                                                ModernTextField(
                                                    value = bdayMonthInput,
                                                    onValueChange = {
                                                        bdayMonthInput = it.filter { ch -> ch.isDigit() }.take(2)
                                                        bdayError = null
                                                    },
                                                    label = "Tháng (1-12)",
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                scope.launch {
                                                                    delay(180)
                                                                    bdaySectionBringIntoView.bringIntoView()
                                                                }
                                                            }
                                                        }
                                                )
                                            }

                                            if (bdayError != null) {
                                                Text(
                                                    bdayError!!,
                                                    color = MaterialTheme.colorScheme.error,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(top = 6.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedButton(
                                                onClick = {
                                                    if (birthdays.size >= 10) {
                                                        bdayError = "Đã đạt giới hạn tối đa 10 ngày sinh nhật."
                                                        return@OutlinedButton
                                                    }
                                                    val nameTrim = bdayNameInput.trim()
                                                    val d = bdayDayInput.toIntOrNull() ?: 0
                                                    val m = bdayMonthInput.toIntOrNull() ?: 0
                                                    if (nameTrim.isEmpty()) {
                                                        bdayError = "Vui lòng nhập tên."
                                                        return@OutlinedButton
                                                    }
                                                    if (d !in 1..31 || m !in 1..12) {
                                                        bdayError = "Ngày phải từ 1–31 và tháng từ 1–12."
                                                        return@OutlinedButton
                                                    }
                                                    val updated = birthdays + BirthdayItem(
                                                        id = UUID.randomUUID().toString(),
                                                        name = nameTrim,
                                                        day = d,
                                                        month = m
                                                    )
                                                    birthdays = updated
                                                    AppPrefs.saveBirthdays(context, updated)
                                                    bdayNameInput = ""
                                                    bdayError = null
                                                    BirthdayHelper.notifyTodayBirthdaysOncePerDay(context)
                                                },
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(46.dp)
                                            ) {
                                                Text("Thêm ngày sinh nhật", color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                                            }

                                            if (birthdays.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Column {
                                                    birthdays.forEachIndexed { idx, item ->
                                                        if (idx > 0) SectionHairline(palette)
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 4.dp)
                                                        ) {
                                                            Text(
                                                                "🎂 ${item.name} — ${item.day.toString().padStart(2, '0')}/${item.month.toString().padStart(2, '0')}",
                                                                color = palette.textPrimary,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                            IconButton(
                                                                onClick = {
                                                                    val updated = birthdays.filterNot { it.id == item.id }
                                                                    birthdays = updated
                                                                    AppPrefs.saveBirthdays(context, updated)
                                                                }
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Delete,
                                                                    contentDescription = "Xóa sinh nhật",
                                                                    tint = MaterialTheme.colorScheme.error
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (expandedSection == ExpandableSection.HOLIDAY) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.Transparent,
                                        contentColor = palette.textPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(palette.liquidGlassBrush)
                                            .border(1.3.dp, accentCompose.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            FlatSwitchRow(
                                                title = "Bật nhắc tất cả ngày lễ / kỷ niệm",
                                                subtitle = if (holidayReminderEnabled)
                                                    "Đang bật nhắc toàn bộ ${enabledHolidayIds.size} ngày lễ đã lưu (trước 1 ngày & đúng ngày)"
                                                else
                                                    "Gạt bật để bật nhắc hết tất cả các ngày lễ đã lưu",
                                                checked = holidayReminderEnabled,
                                                accentColor = accentCompose,
                                                palette = palette,
                                                onCheckedChange = { checked ->
                                                    holidayReminderEnabled = checked
                                                    AppPrefs.setHolidayReminderEnabled(context, checked)
                                                    if (checked) {
                                                        val allIds = BirthdayHelper.DEFAULT_VIETNAM_AND_INTERNATIONAL_HOLIDAYS
                                                            .map { it.id }
                                                            .toSet()
                                                        enabledHolidayIds = allIds
                                                        AppPrefs.setEnabledHolidayIds(context, allIds)
                                                        BirthdayHelper.notifyTodayHolidaysOncePerDay(context)
                                                    }
                                                    activeHolidayAlerts = BirthdayHelper.getActiveHolidaysForToday(context)
                                                }
                                            )

                                            SectionHairline(palette)
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                "Danh sách ngày lễ sẽ được nhắc (tính theo năm $currentSolarYear):",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.textSecondary
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Column {
                                                BirthdayHelper.DEFAULT_VIETNAM_AND_INTERNATIONAL_HOLIDAYS.forEachIndexed { idx, holiday ->
                                                    if (idx > 0) SectionHairline(palette)
                                                    val isReminded = holidayReminderEnabled && (holiday.id in enabledHolidayIds)
                                                    val resolvedSolar = remember(holiday.id, currentSolarYear) {
                                                        BirthdayHelper.resolveSolarDateForHoliday(holiday, currentSolarYear)
                                                    }
                                                    val typeTag = if (holiday.calendarType == HolidayCalendarType.LUNAR) {
                                                        "Âm ${holiday.day}/${holiday.month}"
                                                    } else {
                                                        "Dương ${holiday.day.toString().padStart(2, '0')}/${holiday.month.toString().padStart(2, '0')}"
                                                    }
                                                    val solarNote = resolvedSolar?.let { (sd, sm, sy) ->
                                                        "Dương: ${sd.toString().padStart(2, '0')}/${sm.toString().padStart(2, '0')}/$sy"
                                                    } ?: ""

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 8.dp)
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                holiday.title,
                                                                color = if (isReminded) palette.textPrimary else palette.textSecondary,
                                                                fontWeight = FontWeight.SemiBold,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                            Text(
                                                                if (solarNote.isNotEmpty()) "$typeTag • $solarNote" else typeTag,
                                                                color = if (isReminded) accentCompose else palette.textSecondary,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            if (isReminded) "Đang nhắc" else "Tắt",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = if (isReminded) accentCompose else palette.textSecondary,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 10. Báo cáo lỗi và đề xuất
                        item(key = "bug_report_section") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(feedbackSectionBringIntoView)
                            ) {
                                CollapsibleSectionCard(
                                    title = "Báo cáo lỗi và đề xuất",
                                    badgeText = "Email",
                                    accentColor = accentCompose,
                                    palette = palette,
                                    expanded = expandedSection == ExpandableSection.BUG_REPORT,
                                    onToggle = { toggleSection(ExpandableSection.BUG_REPORT) }
                                ) {
                                    Text(
                                        "Chọn loại phản hồi và nhập nội dung để mở trình gửi mail tới $FEEDBACK_EMAIL (kèm thông tin phiên bản & thiết bị).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FeedbackCategory.entries.forEach { category ->
                                            val selected = feedbackCategory == category
                                            FilterChip(
                                                selected = selected,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = palette.subtleBg,
                                                    labelColor = palette.textPrimary,
                                                    selectedContainerColor = accentCompose,
                                                    selectedLabelColor = palette.onAccentText
                                                ),
                                                onClick = {
                                                    feedbackCategory = category
                                                    feedbackError = null
                                                },
                                                label = {
                                                    Text(
                                                        category.label,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = feedbackMessageInput,
                                        onValueChange = {
                                            feedbackMessageInput = it
                                            feedbackError = null
                                        },
                                        label = {
                                            Text(
                                                if (feedbackCategory == FeedbackCategory.BUG)
                                                    "Mô tả lỗi bạn gặp phải..."
                                                else
                                                    "Chia sẻ ý tưởng hoặc tính năng bạn mong muốn...",
                                                color = palette.textSecondary
                                            )
                                        },
                                        minLines = 3,
                                        maxLines = 6,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = palette.textPrimary,
                                            unfocusedTextColor = palette.textPrimary,
                                            focusedBorderColor = accentCompose,
                                            unfocusedBorderColor = palette.border,
                                            focusedLabelColor = accentCompose,
                                            unfocusedLabelColor = palette.textSecondary,
                                            unfocusedContainerColor = palette.subtleBg,
                                            focusedContainerColor = palette.subtleBg
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    scope.launch {
                                                        delay(180)
                                                        feedbackSectionBringIntoView.bringIntoView()
                                                    }
                                                }
                                            }
                                    )

                                    if (feedbackError != null) {
                                        Text(
                                            feedbackError!!,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            val trimmed = feedbackMessageInput.trim()
                                            if (trimmed.isEmpty()) {
                                                feedbackError = "Vui lòng nhập nội dung trước khi gửi."
                                                return@Button
                                            }
                                            feedbackError = null
                                            sendFeedbackViaMail(
                                                context = context,
                                                category = feedbackCategory,
                                                message = trimmed,
                                                locationName = weather.locationName,
                                                isDark = palette.isDark
                                            )
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accentCompose,
                                            contentColor = palette.onAccentText
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Gửi qua Email", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Chân trang
                        item(key = "footer_section") {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 8.dp)
                            ) {
                                Text(
                                    "Phiên bản $APP_VERSION_NAME\nĐược tạo bởi Mr.WInd - Xiaomi Community Việt Nam\nỦng hộ anh ấy một cốc cà hê qua MOMO: 0923032127",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = palette.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Pop-up nhắc ngày lễ (Liquid Glass)
                AnimatedVisibility(
                    visible = showHolidayPopup && activeHolidayAlerts.isNotEmpty(),
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                BirthdayHelper.markAppPopupShownToday(context)
                                showHolidayPopup = false
                            }
                            .padding(horizontal = 24.dp)
                    ) {
                        LiquidGlassPopupSurface(palette = palette) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = {
                                            BirthdayHelper.markAppPopupShownToday(context)
                                            showHolidayPopup = false
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Đóng",
                                            tint = palette.popupTextSecondary
                                        )
                                    }
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    "Nhắc ngày lễ & kỷ niệm",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accentCompose.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        solarToday,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = accentCompose,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    activeHolidayAlerts.forEach { alert ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = palette.popupBoxBg,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                alert.message,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = palette.popupTextPrimary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        BirthdayHelper.markAppPopupShownToday(context)
                                        showHolidayPopup = false
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentCompose,
                                        contentColor = palette.onAccentText
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        "Đã rõ!",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Pop-up nhắc việc cần làm ngay khi tới giờ (Liquid Glass)
                AnimatedVisibility(
                    visible = activeTodoPopup != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val todoData = activeTodoPopup
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                            .padding(horizontal = 24.dp)
                    ) {
                        LiquidGlassPopupSurface(palette = palette) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    "Đến giờ làm việc!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                if (todoData != null) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = accentCompose.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            "Giờ đặt: ${todoData.timeFormatted}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = accentCompose,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = palette.popupBoxBg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            todoData.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = palette.popupTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                todoTasks = TodoHelper.markTaskSkipped(context, todoData.id)
                                                activeTodoPopup = null
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Text(
                                                "Tạm bỏ qua",
                                                color = palette.popupTextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                todoTasks = TodoHelper.markTaskDone(context, todoData.id)
                                                activeTodoPopup = null
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = accentCompose,
                                                contentColor = palette.onAccentText
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Ok, làm ngay đây",
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Pop-up hiển thị kết quả tính tuổi (Liquid Glass)
                AnimatedVisibility(
                    visible = ageResultPopup != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val ageData = ageResultPopup
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { ageResultPopup = null }
                            .padding(horizontal = 24.dp)
                    ) {
                        LiquidGlassPopupSurface(palette = palette) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { ageResultPopup = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Đóng",
                                            tint = palette.popupTextSecondary
                                        )
                                    }
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.PersonSearch,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    "Kết quả tính tuổi",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (ageData != null) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = accentCompose.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            ageData.birthLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = accentCompose,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        ageData.currentLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.popupTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = palette.popupBoxBg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            ageData.ageSummary,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = accentCompose,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { ageResultPopup = null },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentCompose,
                                        contentColor = palette.onAccentText
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        "Đóng",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Pop-up hiển thị sau khi xóa lịch sử tính tuổi (Liquid Glass)
                AnimatedVisibility(
                    visible = showAgeHistoryClearedPopup,
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showAgeHistoryClearedPopup = false }
                            .padding(horizontal = 24.dp)
                    ) {
                        LiquidGlassPopupSurface(palette = palette) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    "Đã xóa lịch sử tính tuổi",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Toàn bộ lịch sử tính tuổi đã được xóa thành công.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = palette.popupTextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { showAgeHistoryClearedPopup = false },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentCompose,
                                        contentColor = palette.onAccentText
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        "Ok",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Pop-up thông báo kết quả tra cứu lịch âm (Liquid Glass)
                AnimatedVisibility(
                    visible = lunarResultPopup != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val data = lunarResultPopup
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { lunarResultPopup = null }
                            .padding(horizontal = 24.dp)
                    ) {
                        LiquidGlassPopupSurface(palette = palette) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { lunarResultPopup = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Đóng",
                                            tint = palette.popupTextSecondary
                                        )
                                    }
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    "Thông báo lịch âm",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (data != null) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = accentCompose.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            data.solarLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = accentCompose,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = palette.popupBoxBg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            data.lunarSummary,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = palette.popupTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { lunarResultPopup = null },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentCompose,
                                        contentColor = palette.onAccentText
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        "Đóng",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Pop-up hiển thị sau khi xóa lịch sử tìm kiếm (Liquid Glass)
                AnimatedVisibility(
                    visible = showHistoryClearedPopup,
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showHistoryClearedPopup = false }
                            .padding(horizontal = 24.dp)
                    ) {
                        LiquidGlassPopupSurface(palette = palette) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    "Đã xóa lịch sử",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Toàn bộ lịch sử tra cứu lịch âm đã được xóa thành công.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = palette.popupTextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { showHistoryClearedPopup = false },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentCompose,
                                        contentColor = palette.onAccentText
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        "Ok",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Pop-up cảnh báo hao pin khi bật hiển thị giây (Liquid Glass)
                AnimatedVisibility(
                    visible = showSecondsWarningPopup,
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showSecondsWarningPopup = false }
                            .padding(horizontal = 24.dp)
                    ) {
                        LiquidGlassPopupSurface(palette = palette) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { showSecondsWarningPopup = false },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Đóng",
                                            tint = palette.popupTextSecondary
                                        )
                                    }
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.BatteryAlert,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    "Lưu ý mức tiêu thụ pin",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    "Bật tính năng này sẽ làm cho thiết bị của bạn hao pin hơn.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = palette.popupTextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { showSecondsWarningPopup = false },
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            "Hủy",
                                            color = palette.popupTextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            showSecondsWarningPopup = false
                                            showSeconds = true
                                            AppPrefs.setShowSeconds(context, true)
                                            WeatherLunarWidget.updateAllWidgets(context)
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accentCompose,
                                            contentColor = palette.onAccentText
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            "Ok",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Pop-up nhắc sinh nhật hiện đại (Liquid Glass)
                AnimatedVisibility(
                    visible = showBirthdayPopup && (todayBirthdays.isNotEmpty() || showAdminBirthdayToday),
                    enter = fadeIn() + scaleIn(initialScale = 0.92f),
                    exit = fadeOut() + scaleOut(targetScale = 0.92f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showBirthdayPopup = false }
                            .padding(horizontal = 24.dp)
                    ) {
                        LiquidGlassPopupSurface(palette = palette) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { showBirthdayPopup = false },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Đóng",
                                            tint = palette.popupTextSecondary
                                        )
                                    }
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Cake,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    if (showAdminBirthdayToday && todayBirthdays.isEmpty())
                                        "Chúc mừng sinh nhật!"
                                    else
                                        "Sinh nhật hôm nay",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accentCompose.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        solarToday,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = accentCompose,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (showAdminBirthdayToday) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = palette.popupBoxBg,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = if (todayBirthdays.isNotEmpty()) 8.dp else 0.dp)
                                    ) {
                                        Text(
                                            BirthdayHelper.ADMIN_BIRTHDAY_MESSAGE,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = palette.popupTextPrimary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                        )
                                    }
                                }

                                if (todayBirthdays.isNotEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        todayBirthdays.forEach { person ->
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = palette.popupBoxBg,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                                ) {
                                                    Text(
                                                        "🎉 ${person.name}",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = palette.popupTextPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "${person.day.toString().padStart(2, '0')}/${person.month.toString().padStart(2, '0')}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = accentCompose,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    "Đừng quên gửi lời chúc mừng sinh nhật thật ấm áp nhé!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.popupTextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { showBirthdayPopup = false },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentCompose,
                                        contentColor = palette.onAccentText
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        "Tuyệt vời, đã nhớ!",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactDualCard(
    title: String,
    badgeText: String,
    accentColor: Color,
    palette: AdaptivePalette,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "dual_card_arrow_rotation"
    )
    val borderBrush = remember(expanded, accentColor, palette) {
        if (expanded) {
            Brush.linearGradient(
                listOf(
                    accentColor.copy(alpha = 0.85f),
                    Color.White.copy(alpha = if (palette.isDark) 0.25f else 0.75f),
                    accentColor.copy(alpha = 0.25f)
                )
            )
        } else {
            palette.liquidGlassBorder
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        contentColor = palette.textPrimary,
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(palette.liquidGlassBrush)
            .border(
                width = if (expanded) 1.4.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onToggle)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = true)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .height(16.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            repeatDelayMillis = 900,
                            velocity = 28.dp
                        )
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = badgeText,
                fontSize = 11.sp,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Thu gọn" else "Mở rộng",
                tint = palette.textPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(arrowRotation)
            )
        }
    }
}

@Composable
private fun SectionHairline(
    palette: AdaptivePalette,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding),
        thickness = 0.8.dp,
        color = palette.border
    )
}

@Composable
private fun FlatSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    palette: AdaptivePalette,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = palette.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            colors = SwitchDefaults.colors(checkedTrackColor = accentColor),
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollapsibleSectionCard(
    title: String,
    badgeText: String? = null,
    accentColor: Color,
    palette: AdaptivePalette,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "section_arrow_rotation"
    )

    val activeBorderBrush = remember(expanded, accentColor, palette) {
        if (expanded) {
            Brush.linearGradient(
                listOf(
                    accentColor.copy(alpha = 0.80f),
                    Color.White.copy(alpha = if (palette.isDark) 0.25f else 0.75f),
                    accentColor.copy(alpha = 0.20f)
                )
            )
        } else {
            palette.liquidGlassBorder
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        contentColor = palette.textPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.liquidGlassBrush)
            .border(
                width = if (expanded) 1.4.dp else 1.dp,
                brush = activeBorderBrush,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = SMOOTH_EXPAND_SPEC)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = true)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                repeatDelayMillis = 900,
                                velocity = 30.dp
                            )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (badgeText != null) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Thu gọn" else "Mở rộng",
                    tint = palette.textPrimary,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(arrowRotation)
                )
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    SectionHairline(palette)
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    accentColor: Color,
    palette: AdaptivePalette,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = palette.textSecondary, maxLines = 1) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = palette.textPrimary,
            unfocusedTextColor = palette.textPrimary,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = palette.border,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = palette.textSecondary,
            unfocusedContainerColor = palette.subtleBg,
            focusedContainerColor = palette.subtleBg
        ),
        modifier = modifier
    )
}

@Composable
private fun ScaleSliderRow(
    label: String,
    value: Int,
    accentColor: Color,
    palette: AdaptivePalette,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, color = palette.textPrimary, fontWeight = FontWeight.Medium)
            Text(
                "$value / 20",
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { raw ->
                val intVal = raw.roundToInt().coerceIn(1, 20)
                if (intVal != value) onValueChange(intVal)
            },
            valueRange = 1f..20f,
            steps = 18,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = palette.subtleBg
            )
        )
    }
}
