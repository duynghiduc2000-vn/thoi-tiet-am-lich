package com.mrwind.vn

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import kotlin.math.floor
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WeatherService.scheduleTwoHourlySync(this)
        BirthdayHelper.notifyTodayBirthdaysOncePerDay(this)
        WeatherLunarWidget.updateAllWidgets(this)

        setContent {
            ThoiTietAmLichApp()
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
    // Xác thực ngày hợp lệ theo lịch dương
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
    if (lunarMonth >= 11 && diff < 4) {
        // Giữ năm âm lịch khớp với tháng 11/12 cuối năm
    }

    val leapText = if (lunarLeap) " (nhuận)" else ""
    val dCanChi = canChiDay(dayNumber)
    val mCanChi = canChiMonth(lunarMonth, lunarYear)
    val yCanChi = canChiYear(lunarYear)

    return "Âm lịch: Ngày $lunarDay/$lunarMonth/$lunarYear$leapText • Ngày $dCanChi • Tháng $mCanChi • Năm $yCanChi"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ThoiTietAmLichApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val nowCal = remember { Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")) }

    var appThemeMode by remember { mutableIntStateOf(AppPrefs.getAppThemeMode(context)) }
    var widgetBgMode by remember { mutableIntStateOf(AppPrefs.getWidgetBgMode(context)) }
    var accentColorInt by remember { mutableIntStateOf(AppPrefs.getAccentColor(context)) }
    var showSeconds by remember { mutableStateOf(AppPrefs.isShowSeconds(context)) }
    var showSecondsWarningPopup by remember { mutableStateOf(false) }
    var showWeatherEffect by remember { mutableStateOf(AppPrefs.isShowWeatherEffect(context)) }
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

    var birthdays by remember { mutableStateOf(AppPrefs.getBirthdays(context)) }
    var bdayNameInput by remember { mutableStateOf("") }
    var bdayDayInput by remember { mutableStateOf("1") }
    var bdayMonthInput by remember { mutableStateOf("1") }
    var bdayError by remember { mutableStateOf<String?>(null) }

    val lookupSectionBringIntoView = remember { BringIntoViewRequester() }
    val bdaySectionBringIntoView = remember { BringIntoViewRequester() }

    val todayBirthdays = remember(birthdays) { BirthdayHelper.getTodayBirthdays(context) }
    val showAdminBirthdayToday = remember { BirthdayHelper.isAdminBirthdayToday() }
    var showBirthdayPopup by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        scope.launch {
            isSyncing = true
            weather = WeatherService.syncWeather(context)
            isSyncing = false
        }
    }

    LaunchedEffect(Unit) {
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
            isSyncing = true
            weather = WeatherService.syncWeather(context)
            isSyncing = false
        }
    }

    val isDark = when (appThemeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val accentCompose = Color(accentColorInt)
    val colorScheme = if (isDark) {
        darkColorScheme(primary = accentCompose, secondary = accentCompose)
    } else {
        lightColorScheme(primary = accentCompose, secondary = accentCompose)
    }

    val lunarToday = remember { LunarCalendar.todayLunar() }
    val solarToday = remember { LunarCalendar.formatVietnameseSolarDate() }

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

    MaterialTheme(colorScheme = colorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Thời tiết và Âm lịch",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        isSyncing = true
                                        weather = WeatherService.syncWeather(context)
                                        isSyncing = false
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Đồng bộ thời tiết")
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Thẻ tổng quan Lịch Âm - Dương, Thời tiết & Ghim Widget
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    solarToday,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    lunarToday.fullSummary(),
                                    color = accentCompose,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "${weather.currentTemp} • ${weather.description}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${weather.locationName} • ▲ ${weather.maxTemp} ▼ ${weather.minTemp}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isSyncing = true
                                                weather = WeatherService.syncWeather(context)
                                                isSyncing = false
                                            }
                                        },
                                        enabled = !isSyncing
                                    ) {
                                        Text(if (isSyncing) "Đang tải..." else "Cập nhật")
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        val pinned = WeatherLunarWidget.requestPinWidget(context)
                                        if (!pinned) {
                                            Toast.makeText(
                                                context,
                                                "Launcher hiện tại không hỗ trợ ghim tự động. Bạn nhấn giữ màn hình chính -> Tiện ích để thêm nhé.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AddToHomeScreen, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ghim Widget ra màn hình chính")
                                }
                            }
                        }
                    }

                    // 2. Tra cứu lịch âm theo ngày dương lịch + lịch sử tìm kiếm
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(lookupSectionBringIntoView)
                        ) {
                            SectionCard(title = "Tra cứu lịch âm") {
                                Text(
                                    "Nhập ngày, tháng, năm dương lịch và bấm Tìm kiếm để xem thông báo lịch âm.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = lookupDayInput,
                                        onValueChange = {
                                            lookupDayInput = it.filter { ch -> ch.isDigit() }.take(2)
                                            lookupError = null
                                        },
                                        label = { Text("Ngày") },
                                        singleLine = true,
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
                                    OutlinedTextField(
                                        value = lookupMonthInput,
                                        onValueChange = {
                                            lookupMonthInput = it.filter { ch -> ch.isDigit() }.take(2)
                                            lookupError = null
                                        },
                                        label = { Text("Tháng") },
                                        singleLine = true,
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
                                    OutlinedTextField(
                                        value = lookupYearInput,
                                        onValueChange = {
                                            lookupYearInput = it.filter { ch -> ch.isDigit() }.take(4)
                                            lookupError = null
                                        },
                                        label = { Text("Năm") },
                                        singleLine = true,
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
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tìm kiếm")
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
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteSweep,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Xóa lịch sử", fontSize = 12.sp)
                                        }
                                    }
                                }

                                if (lunarHistory.isEmpty()) {
                                    Text(
                                        "Chưa có lịch sử tra cứu nào.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        lunarHistory.forEach { item ->
                                            val solarStr =
                                                "${item.solarDay.toString().padStart(2, '0')}/${item.solarMonth.toString().padStart(2, '0')}/${item.solarYear}"
                                            val freshSummary = remember(item) {
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
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        lookupDayInput = item.solarDay.toString()
                                                        lookupMonthInput = item.solarMonth.toString()
                                                        lookupYearInput = item.solarYear.toString()
                                                        lunarResultPopup = LunarResultPopupData(
                                                            solarLabel = "Dương lịch: $solarStr",
                                                            lunarSummary = freshSummary
                                                        )
                                                    }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            "📅 Dương: $solarStr",
                                                            fontWeight = FontWeight.SemiBold,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                        Text(
                                                            freshSummary,
                                                            color = accentCompose,
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

                    // 3. Đồng hồ, Hiệu ứng thời tiết & Nền Widget
                    item {
                        SectionCard(title = "Đồng hồ, Giao diện & Nền Widget") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Hiển thị giây (chạy liên tục)", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Định dạng HH:mm:ss trên đồng hồ Widget",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(
                                    checked = showSeconds,
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
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Hiệu ứng thời tiết trên Widget", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Bật/tắt lớp hiệu ứng nắng, mây, sương mù, mưa theo thời tiết",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(
                                    checked = showWeatherEffect,
                                    onCheckedChange = { checked ->
                                        showWeatherEffect = checked
                                        AppPrefs.setShowWeatherEffect(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                            }

                            Text("Chế độ giao diện ứng dụng", fontWeight = FontWeight.Medium)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                            ) {
                                val themeLabels = listOf("Hệ thống", "Sáng", "Tối")
                                themeLabels.forEachIndexed { idx, label ->
                                    FilterChip(
                                        selected = appThemeMode == idx,
                                        onClick = {
                                            appThemeMode = idx
                                            AppPrefs.setAppThemeMode(context, idx)
                                        },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            Text("Kiểu nền Widget trên màn hình chính", fontWeight = FontWeight.Medium)
                            val bgLabels = listOf("Kính lỏng tối", "Kính lỏng sáng", "Trong suốt", "Theo chủ đề app")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                items(bgLabels.size) { idx ->
                                    FilterChip(
                                        selected = widgetBgMode == idx,
                                        onClick = {
                                            widgetBgMode = idx
                                            AppPrefs.setWidgetBgMode(context, idx)
                                            WeatherLunarWidget.updateAllWidgets(context)
                                        },
                                        label = { Text(bgLabels[idx]) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Màu chủ đề
                    item {
                        SectionCard(title = "Màu chủ đề (Áp dụng App & Widget)") {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(ACCENT_OPTIONS) { opt ->
                                    val selected = opt.colorInt == accentColorInt
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            accentColorInt = opt.colorInt
                                            AppPrefs.setAccentColor(context, opt.colorInt)
                                            WeatherLunarWidget.updateAllWidgets(context)
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(opt.colorInt))
                                                .border(
                                                    width = if (selected) 3.dp else 1.dp,
                                                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                                    shape = CircleShape
                                                )
                                        )
                                        Text(
                                            opt.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Vị trí thời tiết (Tự động GPS hoặc Nhập tên tìm kiếm thủ công)
                    item {
                        SectionCard(title = "Vị trí cập nhật thời tiết") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tự động theo vị trí hiện tại (GPS)", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Tắt để nhập tên tìm địa điểm bất kỳ",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(
                                    checked = autoLocation,
                                    onCheckedChange = { checked ->
                                        autoLocation = checked
                                        AppPrefs.setAutoLocation(context, checked)
                                        scope.launch {
                                            isSyncing = true
                                            weather = WeatherService.syncWeather(context)
                                            isSyncing = false
                                        }
                                    }
                                )
                            }

                            if (!autoLocation) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = locationQuery,
                                        onValueChange = {
                                            locationQuery = it
                                            locationSearchMsg = null
                                        },
                                        label = { Text("Nhập tên địa điểm (VD: Nghi Đức, Đà Lạt...)") },
                                        singleLine = true,
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
                                        enabled = !isSearchingLocation
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isSearchingLocation) "..." else "Tìm")
                                    }
                                }

                                if (locationSearchMsg != null) {
                                    Text(
                                        locationSearchMsg!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }

                                searchResults.forEach { item ->
                                    val isCurrent = weather.locationName == item.displayName
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
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
                                                    isSyncing = true
                                                    weather = WeatherService.syncWeather(context)
                                                    isSyncing = false
                                                }
                                            }
                                            .padding(vertical = 8.dp, horizontal = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (isCurrent) accentCompose else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            item.displayName,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) accentCompose else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. Chữ ký cá nhân & bật/tắt
                    item {
                        SectionCard(title = "Chữ ký trên Widget") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = if (showSignature) 10.dp else 0.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Hiển thị chữ ký", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Tắt nếu không muốn hiện chữ ký dưới Widget",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(
                                    checked = showSignature,
                                    onCheckedChange = { checked ->
                                        showSignature = checked
                                        AppPrefs.setShowSignature(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    }
                                )
                            }

                            if (showSignature) {
                                OutlinedTextField(
                                    value = signature,
                                    onValueChange = { newText ->
                                        signature = newText
                                        AppPrefs.setSignature(context, newText)
                                        WeatherLunarWidget.updateAllWidgets(context)
                                    },
                                    label = { Text("Nhập chữ ký (có dấu tiếng Việt)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // 7. Thanh trượt kích cỡ chữ (1 đến 20)
                    item {
                        SectionCard(title = "Kích cỡ chữ trên Widget (Mức 1 – 20)") {
                            ScaleSliderRow(
                                label = "Đồng hồ",
                                value = clockScale,
                                onValueChange = { v ->
                                    clockScale = v
                                    AppPrefs.setClockScale(context, v)
                                    WeatherLunarWidget.updateAllWidgets(context)
                                }
                            )
                            ScaleSliderRow(
                                label = "Lịch âm",
                                value = lunarScale,
                                onValueChange = { v ->
                                    lunarScale = v
                                    AppPrefs.setLunarScale(context, v)
                                    WeatherLunarWidget.updateAllWidgets(context)
                                }
                            )
                            ScaleSliderRow(
                                label = "Dương lịch",
                                value = solarScale,
                                onValueChange = { v ->
                                    solarScale = v
                                    AppPrefs.setSolarScale(context, v)
                                    WeatherLunarWidget.updateAllWidgets(context)
                                }
                            )
                            ScaleSliderRow(
                                label = "Chữ ký",
                                value = sigScale,
                                onValueChange = { v ->
                                    sigScale = v
                                    AppPrefs.setSigScale(context, v)
                                    WeatherLunarWidget.updateAllWidgets(context)
                                }
                            )
                        }
                    }

                    // 8. Danh sách ngày sinh nhật (tối đa 10)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(bdaySectionBringIntoView)
                        ) {
                            SectionCard(title = "Nhắc ngày sinh nhật (${birthdays.size}/10)") {
                                Text(
                                    "Tự động hiện Pop-up khi mở app và gửi thông báo 1 lần/ngày vào đúng ngày sinh nhật.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = bdayNameInput,
                                    onValueChange = {
                                        bdayNameInput = it
                                        bdayError = null
                                    },
                                    label = { Text("Tên người sinh nhật") },
                                    singleLine = true,
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
                                    OutlinedTextField(
                                        value = bdayDayInput,
                                        onValueChange = {
                                            bdayDayInput = it.filter { ch -> ch.isDigit() }.take(2)
                                            bdayError = null
                                        },
                                        label = { Text("Ngày (1-31)") },
                                        singleLine = true,
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
                                    OutlinedTextField(
                                        value = bdayMonthInput,
                                        onValueChange = {
                                            bdayMonthInput = it.filter { ch -> ch.isDigit() }.take(2)
                                            bdayError = null
                                        },
                                        label = { Text("Tháng (1-12)") },
                                        singleLine = true,
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
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

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
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Thêm ngày sinh nhật")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                birthdays.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            "🎂 ${item.name} — ${item.day.toString().padStart(2, '0')}/${item.month.toString().padStart(2, '0')}",
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

                    // 9. Chân trang ở cuối ứng dụng
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 8.dp)
                        ) {
                            Text(
                                "Được tạo bởi Mr.WInd - Xiaomi Community Việt Nam\nỦng hộ anh ấy một cuốn triện gei qua MOMO: 0923032127",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Pop-up thông báo kết quả tra cứu lịch âm
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
                        .background(Color.Black.copy(alpha = 0.58f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { lunarResultPopup = null }
                        .padding(horizontal = 24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                    ) {
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
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                accentCompose.copy(alpha = 0.25f),
                                                accentCompose.copy(alpha = 0.12f)
                                            )
                                        )
                                    )
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
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (data != null) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accentCompose.copy(alpha = 0.16f)
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
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        data.lunarSummary,
                                        style = MaterialTheme.typography.titleMedium,
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
                                colors = ButtonDefaults.buttonColors(containerColor = accentCompose),
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

            // Pop-up hiển thị sau khi xóa lịch sử tìm kiếm
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
                        .background(Color.Black.copy(alpha = 0.58f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showHistoryClearedPopup = false }
                        .padding(horizontal = 24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(22.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                accentCompose.copy(alpha = 0.25f),
                                                accentCompose.copy(alpha = 0.12f)
                                            )
                                        )
                                    )
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
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Toàn bộ lịch sử tra cứu lịch âm đã được xóa thành công.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { showHistoryClearedPopup = false },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentCompose),
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

            // Pop-up cảnh báo hao pin khi bật hiển thị giây
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
                        .background(Color.Black.copy(alpha = 0.58f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showSecondsWarningPopup = false }
                        .padding(horizontal = 24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                    ) {
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
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                accentCompose.copy(alpha = 0.25f),
                                                accentCompose.copy(alpha = 0.12f)
                                            )
                                        )
                                    )
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
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                "Bật tính năng này sẽ làm cho thiết bị của bạn hao pin hơn.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    colors = ButtonDefaults.buttonColors(containerColor = accentCompose),
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

            // Pop-up nhắc sinh nhật hiện đại (hỗ trợ cả danh sách cá nhân lẫn thông điệp ẩn 31/12 cho Mr.Wind)
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
                        .background(Color.Black.copy(alpha = 0.58f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showBirthdayPopup = false }
                        .padding(horizontal = 24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                    ) {
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
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                accentCompose.copy(alpha = 0.25f),
                                                accentCompose.copy(alpha = 0.12f)
                                            )
                                        )
                                    )
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
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = accentCompose.copy(alpha = 0.16f)
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
                                    color = accentCompose.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = if (todayBirthdays.isNotEmpty()) 8.dp else 0.dp)
                                ) {
                                    Text(
                                        BirthdayHelper.ADMIN_BIRTHDAY_MESSAGE,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
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
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { showBirthdayPopup = false },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentCompose),
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

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun ScaleSliderRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label)
            Text("$value / 20", fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { raw ->
                val intVal = raw.roundToInt().coerceIn(1, 20)
                if (intVal != value) onValueChange(intVal)
            },
            valueRange = 1f..20f,
            steps = 18
        )
    }
}
