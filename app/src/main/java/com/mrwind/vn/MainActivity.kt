package com.mrwind.vn

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    AccentOption("Hổ phách ấm", 0xFFFFB74D.toInt()),
    AccentOption("Hồng san hô", 0xFFF06292.toInt()),
    AccentOption("Tím oải hương", 0xFFBA68C8.toInt()),
    AccentOption("Xanh lá rừng", 0xFF66BB6A.toInt())
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThoiTietAmLichApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var appThemeMode by remember { mutableIntStateOf(AppPrefs.getAppThemeMode(context)) }
    var widgetBgMode by remember { mutableIntStateOf(AppPrefs.getWidgetBgMode(context)) }
    var accentColorInt by remember { mutableIntStateOf(AppPrefs.getAccentColor(context)) }
    var signature by remember { mutableStateOf(AppPrefs.getSignature(context)) }

    var clockScale by remember { mutableIntStateOf(AppPrefs.getClockScale(context)) }
    var lunarScale by remember { mutableIntStateOf(AppPrefs.getLunarScale(context)) }
    var solarScale by remember { mutableIntStateOf(AppPrefs.getSolarScale(context)) }
    var sigScale by remember { mutableIntStateOf(AppPrefs.getSigScale(context)) }

    var autoLocation by remember { mutableStateOf(AppPrefs.isAutoLocation(context)) }
    var weather by remember { mutableStateOf(AppPrefs.getWeather(context)) }
    var isSyncing by remember { mutableStateOf(false) }

    var birthdays by remember { mutableStateOf(AppPrefs.getBirthdays(context)) }
    var bdayNameInput by remember { mutableStateOf("") }
    var bdayDayInput by remember { mutableStateOf("1") }
    var bdayMonthInput by remember { mutableStateOf("1") }
    var bdayError by remember { mutableStateOf<String?>(null) }

    val todayBirthdays = remember(birthdays) { BirthdayHelper.getTodayBirthdays(context) }
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

    if (showBirthdayPopup && todayBirthdays.isNotEmpty()) {
        val namesText = todayBirthdays.joinToString(", ") { it.name }
        AlertDialog(
            onDismissRequest = { showBirthdayPopup = false },
            icon = { Icon(Icons.Default.Cake, contentDescription = null, tint = accentCompose) },
            title = { Text("Nhắc sinh nhật hôm nay") },
            text = {
                Text("Hôm nay là ngày sinh nhật của $namesText. Đừng quên gửi lời chúc tốt đẹp nhất nhé!")
            },
            confirmButton = {
                Button(onClick = { showBirthdayPopup = false }) {
                    Text("Đã rõ")
                }
            }
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
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
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Thẻ tổng quan Lịch Âm - Dương & Thời tiết
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
                                Column {
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
                        }
                    }
                }

                // 2. Giao diện App & Nền Widget
                item {
                    SectionCard(title = "Giao diện ứng dụng & Nền Widget") {
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

                // 4. Vị trí thời tiết (Tự động GPS hoặc Thủ công)
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
                                    "Tắt để chọn sẵn một địa điểm thủ công",
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
                            Text("Chọn địa điểm thủ công:", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            WeatherService.PRESET_LOCATIONS.forEach { preset ->
                                val isCurrent = weather.locationName == preset.name
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            val updatedCache = weather.copy(
                                                locationName = preset.name,
                                                latitude = preset.lat,
                                                longitude = preset.lon
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
                                        preset.name,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) accentCompose else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Chữ ký cá nhân
                item {
                    SectionCard(title = "Chữ ký trên Widget") {
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
                            modifier = Modifier.fillMaxWidth()
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
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = bdayMonthInput,
                                onValueChange = {
                                    bdayMonthInput = it.filter { ch -> ch.isDigit() }.take(2)
                                    bdayError = null
                                },
                                label = { Text("Tháng (1-12)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
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
