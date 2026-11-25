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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import java.util.UUID
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ThoiTietAmLichApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var appThemeMode by remember { mutableIntStateOf(AppPrefs.getAppThemeMode(context)) }
    var widgetBgMode by remember { mutableIntStateOf(AppPrefs.getWidgetBgMode(context)) }
    var accentColorInt by remember { mutableIntStateOf(AppPrefs.getAccentColor(context)) }
    var showSeconds by remember { mutableStateOf(AppPrefs.isShowSeconds(context)) }
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

    var birthdays by remember { mutableStateOf(AppPrefs.getBirthdays(context)) }
    var bdayNameInput by remember { mutableStateOf("") }
    var bdayDayInput by remember { mutableStateOf("1") }
    var bdayMonthInput by remember { mutableStateOf("1") }
    var bdayError by remember { mutableStateOf<String?>(null) }

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

                    // 2. Đồng hồ & Nền Widget
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
                                        showSeconds = checked
                                        AppPrefs.setShowSeconds(context, checked)
                                        WeatherLunarWidget.updateAllWidgets(context)
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
                                        "Lớp hiệu ứng nắng, mây, sương mù, mưa theo thời tiết hiện tại",
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

                    // 3. Màu chủ đề
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

                    // 4. Vị trí thời tiết (Tự động GPS hoặc Nhập tên tìm kiếm thủ công)
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

                    // 5. Chữ ký cá nhân & bật/tắt
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

                    // 6. Thanh trượt kích cỡ chữ (1 đến 20)
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

                    // 7. Danh sách ngày sinh nhật (tối đa 10)
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

                    // 8. Chân trang ở cuối ứng dụng
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
