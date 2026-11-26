package com.mrwind.vn

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
import kotlin.math.floor

enum class HolidayCalendarType {
    SOLAR,
    LUNAR
}

data class HolidayDefinition(
    val id: String,
    val title: String,
    val day: Int,
    val month: Int,
    val calendarType: HolidayCalendarType,
    val isLunarLeapMonth: Boolean = false
)

enum class HolidayReminderTiming {
    DAY_BEFORE,
    ON_DAY
}

data class ActiveHolidayAlert(
    val holiday: HolidayDefinition,
    val timing: HolidayReminderTiming,
    val targetDateKey: String
) {
    val message: String
        get() = when (timing) {
            HolidayReminderTiming.DAY_BEFORE -> "Ngày mai là ${holiday.title}."
            HolidayReminderTiming.ON_DAY -> "Hôm nay là ${holiday.title}."
        }
}

data class LunarUsefulInsight(
    val lunarDateKey: String,
    val lunarDateLabel: String,
    val dayCanChi: String,
    val auspiciousStatus: String,
    val auspiciousDetail: String,
    val auspiciousHours: String,
    val solarTermName: String,
    val daysUntilNextSolarTerm: Int,
    val nextSolarTermName: String,
    val moonMilestoneNote: String,
    val quickTip: String,
    val notificationSummary: String
)

object BirthdayHelper {
    private const val CHANNEL_ID = "birthday_reminder_channel"
    private const val HOLIDAY_CHANNEL_ID = "holiday_reminder_channel"
    private const val LUNAR_USEFUL_CHANNEL_ID = "lunar_useful_info_channel"
    private const val HOLIDAY_NOTIFICATION_ID = 1002
    private const val LUNAR_USEFUL_NOTIFICATION_ID = 1003
    private const val ADMIN_DAY = 31
    private const val ADMIN_MONTH = 12
    const val ADMIN_BIRTHDAY_MESSAGE =
        "Hôm nay là sinh nhật của admin Mr.Wind. Hãy cùng chúc mừng anh ấy nhé!"

    val DEFAULT_VIETNAM_AND_INTERNATIONAL_HOLIDAYS: List<HolidayDefinition> = listOf(
        // Dương lịch (Việt Nam & Quốc tế phổ biến tại Việt Nam)
        HolidayDefinition("solar_01_01", "Tết Dương lịch", 1, 1, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_14_02", "Ngày Lễ Tình nhân (Valentine)", 14, 2, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_27_02", "Ngày Thầy thuốc Việt Nam", 27, 2, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_08_03", "Ngày Quốc tế Phụ nữ", 8, 3, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_26_03", "Ngày Thành lập Đoàn TNCS Hồ Chí Minh", 26, 3, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_30_04", "Ngày Giải phóng miền Nam, thống nhất đất nước", 30, 4, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_01_05", "Ngày Quốc tế Lao động", 1, 5, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_01_06", "Ngày Quốc tế Thiếu nhi", 1, 6, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_28_06", "Ngày Gia đình Việt Nam", 28, 6, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_27_07", "Ngày Thương binh - Liệt sĩ", 27, 7, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_19_08", "Ngày Cách mạng Tháng Tám thành công", 19, 8, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_02_09", "Ngày Quốc khánh nước CHXHCN Việt Nam", 2, 9, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_10_10", "Ngày Giải phóng Thủ đô / Ngày Chuyển đổi số quốc gia", 10, 10, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_20_10", "Ngày Phụ nữ Việt Nam", 20, 10, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_31_10", "Lễ Halloween", 31, 10, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_20_11", "Ngày Nhà giáo Việt Nam", 20, 11, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_22_12", "Ngày Thành lập Quân đội Nhân dân Việt Nam", 22, 12, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_24_12", "Đêm Giáng sinh (Noel)", 24, 12, HolidayCalendarType.SOLAR),
        HolidayDefinition("solar_25_12", "Lễ Giáng sinh (Noel)", 25, 12, HolidayCalendarType.SOLAR),

        // Âm lịch Việt Nam (tính toán theo từng năm dương lịch)
        HolidayDefinition("lunar_01_01", "Mùng 1 Tết Nguyên Đán", 1, 1, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_02_01", "Mùng 2 Tết Nguyên Đán", 2, 1, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_03_01", "Mùng 3 Tết Nguyên Đán", 3, 1, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_15_01", "Rằm tháng Giêng (Tết Nguyên tiêu)", 15, 1, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_03_03", "Tết Hàn thực (mùng 3 tháng 3 Âm lịch)", 3, 3, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_10_03", "Giỗ Tổ Hùng Vương (10/3 Âm lịch)", 10, 3, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_15_04", "Lễ Phật Đản (Rằm tháng Tư Âm lịch)", 15, 4, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_05_05", "Tết Đoan Ngọ (mùng 5 tháng 5 Âm lịch)", 5, 5, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_15_07", "Lễ Vu Lan / Rằm tháng Bảy Âm lịch", 15, 7, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_15_08", "Tết Trung Thu (Rằm tháng Tám Âm lịch)", 15, 8, HolidayCalendarType.LUNAR),
        HolidayDefinition("lunar_23_12", "Lễ Ông Công Ông Táo (23 tháng Chạp)", 23, 12, HolidayCalendarType.LUNAR)
    )

    private val TWENTY_FOUR_SOLAR_TERMS = arrayOf(
        "Xuân phân", "Thanh minh", "Cốc vũ", "Lập hạ",
        "Tiểu mãn", "Mang chủng", "Hạ chí", "Tiểu thử",
        "Đại thử", "Lập thu", "Xử thử", "Bạch lộ",
        "Thu phân", "Hàn lộ", "Sương giáng", "Lập đông",
        "Tiểu tuyết", "Đại tuyết", "Đông chí", "Tiểu hàn",
        "Đại hàn", "Lập xuân", "Vũ thủy", "Kinh trập"
    )

    private val CHI_HOUR_RANGES = arrayOf(
        "Tý (23h-1h)", "Sửu (1h-3h)", "Dần (3h-5h)", "Mão (5h-7h)",
        "Thìn (7h-9h)", "Tỵ (9h-11h)", "Ngọ (11h-13h)", "Mùi (13h-15h)",
        "Thân (15h-17h)", "Dậu (17h-19h)", "Tuất (19h-21h)", "Hợi (21h-23h)"
    )

    // Bảng Chi ngày Hoàng Đạo theo Chi tháng Âm lịch (index 0..11 tương ứng Tý..Hợi)
    private val HOANG_DAO_DAY_CHI_BY_MONTH_CHI = mapOf(
        2 to setOf(0, 1, 4, 5, 7, 10),   // Tháng Dần
        3 to setOf(2, 3, 6, 7, 9, 0),     // Tháng Mão
        4 to setOf(4, 5, 8, 9, 11, 2),    // Tháng Thìn
        5 to setOf(6, 7, 10, 11, 1, 4),    // Tháng Tỵ
        6 to setOf(8, 9, 0, 1, 3, 6),      // Tháng Ngọ
        7 to setOf(10, 11, 2, 3, 5, 8),    // Tháng Mùi
        8 to setOf(0, 1, 4, 5, 7, 10),   // Tháng Thân
        9 to setOf(2, 3, 6, 7, 9, 0),     // Tháng Dậu
        10 to setOf(4, 5, 8, 9, 11, 2),   // Tháng Tuất
        11 to setOf(6, 7, 10, 11, 1, 4),   // Tháng Hợi
        0 to setOf(8, 9, 0, 1, 3, 6),     // Tháng Tý
        1 to setOf(10, 11, 2, 3, 5, 8)    // Tháng Sửu
    )

    // Bảng giờ Hoàng Đạo theo Chi ngày (index 0..11 tương ứng Tý..Hợi)
    private val HOANG_DAO_HOURS_BY_DAY_CHI = mapOf(
        0 to listOf(0, 1, 3, 6, 8, 9),
        1 to listOf(2, 3, 5, 8, 10, 11),
        2 to listOf(0, 1, 4, 5, 7, 10),
        3 to listOf(2, 3, 6, 7, 9, 0),
        4 to listOf(4, 5, 8, 9, 11, 2),
        5 to listOf(6, 7, 10, 11, 1, 4),
        6 to listOf(0, 1, 3, 6, 8, 9),
        7 to listOf(2, 3, 5, 8, 10, 11),
        8 to listOf(0, 1, 4, 5, 7, 10),
        9 to listOf(2, 3, 6, 7, 9, 0),
        10 to listOf(4, 5, 8, 9, 11, 2),
        11 to listOf(6, 7, 10, 11, 1, 4)
    )

    private data class SolarYmd(val day: Int, val month: Int, val year: Int)

    private val CAN_NAMES = arrayOf(
        "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"
    )
    private val CHI_NAMES = arrayOf(
        "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    )

    private fun vietnamCalendarAtOffset(dayOffsetFromToday: Int): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).apply {
            if (dayOffsetFromToday != 0) {
                add(Calendar.DAY_OF_MONTH, dayOffsetFromToday)
            }
        }
    }

    private fun dateKeyForCalendar(cal: Calendar): String {
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val y = cal.get(Calendar.YEAR)
        return "$y-$m-$d"
    }

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

    private fun jdToDate(jd: Int): SolarYmd {
        val a: Int
        val b: Int
        val c: Int
        if (jd > 2299160) {
            a = jd + 32044
            b = (4 * a + 3) / 146097
            c = a - (b * 146097) / 4
        } else {
            a = jd + 32082
            b = (4 * a + 3) / 1461
            c = a - (b * 1461) / 4
        }
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = b * 100 + d - 4800 + (m / 10)
        return SolarYmd(day, month, year)
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

    private fun sunLongitudeDegrees(jdn: Int, timeZoneOffset: Double = 7.0): Double {
        val deg = sunLongitude(jdn, timeZoneOffset) * 180.0 / Math.PI
        return ((deg % 360.0) + 360.0) % 360.0
    }

    private fun solarTermIndexFromDegrees(deg: Double): Int {
        // Bắt đầu từ Xuân phân (0°): mỗi tiết khí cách nhau 15°
        return (floor(((deg + 7.5) % 360.0) / 15.0).toInt()) % 24
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

    private fun lunarToSolar(
        lunarDay: Int,
        lunarMonth: Int,
        lunarYear: Int,
        lunarLeap: Boolean = false,
        timeZoneOffset: Double = 7.0
    ): SolarYmd? {
        val a11: Int
        val b11: Int
        if (lunarMonth < 11) {
            a11 = lunarMonth11(lunarYear - 1, timeZoneOffset)
            b11 = lunarMonth11(lunarYear, timeZoneOffset)
        } else {
            a11 = lunarMonth11(lunarYear, timeZoneOffset)
            b11 = lunarMonth11(lunarYear + 1, timeZoneOffset)
        }
        val k = floor(0.5 + (a11 - 2415021.076998695) / 29.530588853).toInt()
        val off = lunarMonth - 11
        val monthOffset = if (off < 0) off + 12 else off
        val leapOff = leapMonthOffset(a11, timeZoneOffset)
        var leapMonth = if (leapOff > 0) leapOff - 2 else 0
        if (leapMonth < 0) leapMonth += 12

        var targetOffset = monthOffset
        if (b11 - a11 > 365 && leapOff > 0) {
            val isLeapMonthExisting = (leapMonth == lunarMonth)
            if (isLeapMonthExisting && lunarLeap) {
                targetOffset = monthOffset + 1
            } else if (monthOffset >= leapOff || (leapOff - monthOffset == 1 && isLeapMonthExisting)) {
                targetOffset = monthOffset + 1
            }
        }
        val monthStart = newMoonDayNumber(k + targetOffset, timeZoneOffset)
        val jd = monthStart + lunarDay - 1
        return jdToDate(jd)
    }

    fun resolveSolarDateForHoliday(holiday: HolidayDefinition, solarYear: Int): Triple<Int, Int, Int>? {
        return when (holiday.calendarType) {
            HolidayCalendarType.SOLAR -> Triple(holiday.day, holiday.month, solarYear)
            HolidayCalendarType.LUNAR -> {
                val candidates = listOf(solarYear - 1, solarYear, solarYear + 1)
                for (ly in candidates) {
                    val converted = lunarToSolar(
                        lunarDay = holiday.day,
                        lunarMonth = holiday.month,
                        lunarYear = ly,
                        lunarLeap = holiday.isLunarLeapMonth
                    )
                    if (converted != null && converted.year == solarYear) {
                        return Triple(converted.day, converted.month, converted.year)
                    }

                    if (!holiday.isLunarLeapMonth) {
                        val leapConverted = lunarToSolar(
                            lunarDay = holiday.day,
                            lunarMonth = holiday.month,
                            lunarYear = ly,
                            lunarLeap = true
                        )
                        if (leapConverted != null && leapConverted.year == solarYear && leapConverted != converted) {
                            return Triple(leapConverted.day, leapConverted.month, leapConverted.year)
                        }
                    }
                }
                null
            }
        }
    }

    fun lunarYearCanChi(lunarYear: Int): String {
        val yearCanIndex = (lunarYear + 6) % 10
        val yearChiIndex = (lunarYear + 8) % 12
        return "${CAN_NAMES[(yearCanIndex + 10) % 10]} ${CHI_NAMES[(yearChiIndex + 12) % 12]}"
    }

    fun getLunarUsefulInsightForToday(context: Context): LunarUsefulInsight {
        val tzOffset = 7.0
        val todayCal = vietnamCalendarAtOffset(0)
        val day = todayCal.get(Calendar.DAY_OF_MONTH)
        val month = todayCal.get(Calendar.MONTH) + 1
        val year = todayCal.get(Calendar.YEAR)
        val jd = jdFromDate(day, month, year)

        val lunar = LunarCalendar.convertSolarToLunar(day, month, year, tzOffset)
        val lunarDay = lunar.lunarDay
        val lunarMonth = lunar.lunarMonth
        val lunarYear = lunar.lunarYear
        val leapSuffix = if (lunar.isLeapMonth) " (nhuận)" else ""
        val lunarDateKey = "$lunarYear-${lunarMonth.toString().padStart(2, '0')}-${lunarDay.toString().padStart(2, '0')}-${if (lunar.isLeapMonth) 1 else 0}"
        val lunarDateLabel = "Ngày $lunarDay/$lunarMonth/$lunarYear$leapSuffix"

        val dayCanChi = LunarCalendar.canChiOfDay(jd)
        val dayChiIdx = ((jd + 1) % 12 + 12) % 12
        val monthChiIdx = ((lunarMonth + 1) % 12 + 12) % 12
        val isHoangDaoDay = HOANG_DAO_DAY_CHI_BY_MONTH_CHI[monthChiIdx]?.contains(dayChiIdx) == true
        val auspiciousStatus = if (isHoangDaoDay) "Ngày Hoàng Đạo" else "Ngày Hắc Đạo"
        val auspiciousDetail = if (isHoangDaoDay) {
            "Thuận lợi cho khởi sự, xuất hành, giao dịch và sắp xếp việc quan trọng."
        } else {
            "Nên thận trọng khi khởi sự lớn; ưu tiên hoàn thiện việc tồn đọng và nghỉ ngơi."
        }

        val goodHourIndices = HOANG_DAO_HOURS_BY_DAY_CHI[dayChiIdx] ?: listOf(0, 1, 4, 5, 7, 10)
        val auspiciousHours = goodHourIndices.joinToString(" • ") { CHI_HOUR_RANGES[it] }

        val curDeg = sunLongitudeDegrees(jd, tzOffset)
        val curTermIdx = solarTermIndexFromDegrees(curDeg)
        val solarTermName = TWENTY_FOUR_SOLAR_TERMS[curTermIdx]
        val nextTermIdx = (curTermIdx + 1) % 24
        val nextSolarTermName = TWENTY_FOUR_SOLAR_TERMS[nextTermIdx]

        var daysUntilNextTerm = 1
        for (offset in 1..20) {
            val probeIdx = solarTermIndexFromDegrees(sunLongitudeDegrees(jd + offset, tzOffset))
            if (probeIdx != curTermIdx) {
                daysUntilNextTerm = offset
                break
            }
        }

        val moonMilestoneNote = when {
            lunarDay == 1 -> "Hôm nay là Mùng 1 Âm lịch — ngày đầu tháng, thuận tiện thắp hương/cúng gia tiên và đặt mục tiêu mới."
            lunarDay == 15 -> "Hôm nay là ngày Rằm Âm lịch — trăng tròn giữa tháng, thuận tiện cúng Rằm/thắp hương."
            lunarDay < 15 -> {
                val rem = 15 - lunarDay
                "Còn $rem ngày nữa tới ngày Rằm (15/$lunarMonth Âm lịch)."
            }
            else -> {
                val estDaysInMonth = if (lunarDay <= 29) 30 else 30
                val remToNewMonth = (estDaysInMonth - lunarDay + 1).coerceAtLeast(1)
                "Sắp bước sang Mùng 1 tháng Âm lịch kế tiếp (ước tính khoảng $remToNewMonth ngày)."
            }
        }

        val customNote = AppPrefs.getLunarDayNote(context, lunarDateKey).trim()
        val quickTip = if (customNote.isNotEmpty()) {
            "Ghi chú ngày Âm: $customNote"
        } else {
            "Tiết khí $solarTermName • $auspiciousStatus ($dayCanChi)."
        }

        val notificationSummary = buildString {
            append("$lunarDateLabel ($dayCanChi) • $auspiciousStatus. ")
            append("Tiết khí: $solarTermName (còn $daysUntilNextTerm ngày tới $nextSolarTermName). ")
            append("Giờ Hoàng đạo: $auspiciousHours.")
            if (customNote.isNotEmpty()) {
                append(" Ghi chú: $customNote")
            }
        }

        return LunarUsefulInsight(
            lunarDateKey = lunarDateKey,
            lunarDateLabel = lunarDateLabel,
            dayCanChi = dayCanChi,
            auspiciousStatus = auspiciousStatus,
            auspiciousDetail = auspiciousDetail,
            auspiciousHours = auspiciousHours,
            solarTermName = solarTermName,
            daysUntilNextSolarTerm = daysUntilNextTerm,
            nextSolarTermName = nextSolarTermName,
            moonMilestoneNote = moonMilestoneNote,
            quickTip = quickTip,
            notificationSummary = notificationSummary
        )
    }

    fun notifyLunarUsefulInfoOncePerDay(context: Context) {
        if (!AppPrefs.isLunarUsefulNotifyEnabled(context)) return

        val key = todayDateKey()
        if (AppPrefs.getLastLunarUsefulNotifyDate(context) == key) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        createLunarUsefulChannelIfNeeded(context)

        val insight = getLunarUsefulInsightForToday(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2028,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "${insight.lunarDateLabel} • ${insight.auspiciousStatus}"
        val text = insight.notificationSummary

        val notification = NotificationCompat.Builder(context, LUNAR_USEFUL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(LUNAR_USEFUL_NOTIFICATION_ID, notification)
            AppPrefs.setLastLunarUsefulNotifyDate(context, key)
        } catch (_: SecurityException) {
        }
    }

    private fun isHolidayReminderEnabled(context: Context): Boolean {
        return try {
            val method = AppPrefs::class.java.getMethod("isHolidayReminderEnabled", Context::class.java)
            method.invoke(null, context) as? Boolean ?: true
        } catch (_: Exception) {
            true
        }
    }

    private fun getEnabledHolidayIds(context: Context): Set<String>? {
        return try {
            val method = AppPrefs::class.java.getMethod("getEnabledHolidayIds", Context::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(null, context) as? Set<String>
        } catch (_: Exception) {
            null
        }
    }

    private fun getLastHolidayAlertKey(context: Context): String? {
        return try {
            val method = AppPrefs::class.java.getMethod("getLastHolidayAlertKey", Context::class.java)
            method.invoke(null, context) as? String
        } catch (_: Exception) {
            null
        }
    }

    private fun setLastHolidayAlertKey(context: Context, value: String) {
        try {
            val method = AppPrefs::class.java.getMethod(
                "setLastHolidayAlertKey",
                Context::class.java,
                String::class.java
            )
            method.invoke(null, context, value)
        } catch (_: Exception) {
        }
    }

    private fun buildHolidayAlertToken(alerts: List<ActiveHolidayAlert>): String {
        val dayKey = todayDateKey()
        val sig = alerts
            .map { "${it.holiday.id}:${it.timing.name}:${it.targetDateKey}" }
            .sorted()
            .joinToString(",")
        return "$dayKey|$sig"
    }

    fun getActiveHolidaysForToday(context: Context): List<ActiveHolidayAlert> {
        if (!isHolidayReminderEnabled(context)) return emptyList()
        val enabledIds = getEnabledHolidayIds(context)
        val holidays = if (enabledIds == null) {
            DEFAULT_VIETNAM_AND_INTERNATIONAL_HOLIDAYS
        } else {
            DEFAULT_VIETNAM_AND_INTERNATIONAL_HOLIDAYS.filter { it.id in enabledIds }
        }

        val todayCal = vietnamCalendarAtOffset(0)
        val tomorrowCal = vietnamCalendarAtOffset(1)
        val todayKey = dateKeyForCalendar(todayCal)
        val tomorrowKey = dateKeyForCalendar(tomorrowCal)

        val tDay = todayCal.get(Calendar.DAY_OF_MONTH)
        val tMonth = todayCal.get(Calendar.MONTH) + 1
        val tYear = todayCal.get(Calendar.YEAR)

        val tmDay = tomorrowCal.get(Calendar.DAY_OF_MONTH)
        val tmMonth = tomorrowCal.get(Calendar.MONTH) + 1
        val tmYear = tomorrowCal.get(Calendar.YEAR)

        val result = mutableListOf<ActiveHolidayAlert>()
        for (h in holidays) {
            val todaySolar = resolveSolarDateForHoliday(h, tYear)
            if (todaySolar != null && todaySolar.first == tDay && todaySolar.second == tMonth) {
                result.add(ActiveHolidayAlert(h, HolidayReminderTiming.ON_DAY, todayKey))
                continue
            }

            val tomorrowSolar = resolveSolarDateForHoliday(h, tmYear)
            if (tomorrowSolar != null && tomorrowSolar.first == tmDay && tomorrowSolar.second == tmMonth) {
                result.add(ActiveHolidayAlert(h, HolidayReminderTiming.DAY_BEFORE, tomorrowKey))
            }
        }
        return result
    }

    fun todayDateKey(): String {
        return dateKeyForCalendar(vietnamCalendarAtOffset(0))
    }

    fun isAdminBirthdayToday(): Boolean {
        val cal = vietnamCalendarAtOffset(0)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        return day == ADMIN_DAY && month == ADMIN_MONTH
    }

    fun getTodayBirthdays(context: Context): List<BirthdayItem> {
        val cal = vietnamCalendarAtOffset(0)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        return AppPrefs.getBirthdays(context).filter { it.day == day && it.month == month }
    }

    fun shouldShowAppPopupToday(context: Context): Boolean {
        val hasUserBirthdaysToday = getTodayBirthdays(context).isNotEmpty()
        val hasAdminPopupToday = isAdminBirthdayToday() &&
            AppPrefs.getLastAdminPopupDate(context) != todayDateKey()
        val activeHolidays = getActiveHolidaysForToday(context)
        val hasHolidayPopupToday = activeHolidays.isNotEmpty() &&
            getLastHolidayAlertKey(context) != buildHolidayAlertToken(activeHolidays)
        return hasUserBirthdaysToday || hasAdminPopupToday || hasHolidayPopupToday
    }

    fun markAppPopupShownToday(context: Context) {
        val key = todayDateKey()
        if (getTodayBirthdays(context).isNotEmpty()) {
            AppPrefs.setLastPopupDate(context, key)
        }
        if (isAdminBirthdayToday()) {
            AppPrefs.setLastAdminPopupDate(context, key)
        }
        val activeHolidays = getActiveHolidaysForToday(context)
        if (activeHolidays.isNotEmpty()) {
            setLastHolidayAlertKey(context, buildHolidayAlertToken(activeHolidays))
        }
    }

    fun notifyTodayHolidaysOncePerDay(context: Context) {
        val alerts = getActiveHolidaysForToday(context)
        if (alerts.isEmpty()) return

        val token = buildHolidayAlertToken(alerts)
        if (getLastHolidayAlertKey(context) == token) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        createHolidayChannelIfNeeded(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2027,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = alerts.joinToString(" • ") { it.message }
        val hasOnDay = alerts.any { it.timing == HolidayReminderTiming.ON_DAY }
        val title = if (hasOnDay) "Nhắc ngày lễ hôm nay" else "Nhắc ngày lễ sắp tới"

        val notification = NotificationCompat.Builder(context, HOLIDAY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(HOLIDAY_NOTIFICATION_ID, notification)
            setLastHolidayAlertKey(context, token)
        } catch (_: SecurityException) {
        }
    }

    fun notifyTodayBirthdaysOncePerDay(context: Context) {
        notifyTodayHolidaysOncePerDay(context)
        notifyLunarUsefulInfoOncePerDay(context)

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

    private fun createLunarUsefulChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                LUNAR_USEFUL_CHANNEL_ID,
                "Thông tin hữu ích Âm lịch",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo 1 lần/ngày về ngày Hoàng Đạo, tiết khí và giờ tốt theo ngày Âm lịch"
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun createHolidayChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                HOLIDAY_CHANNEL_ID,
                "Nhắc ngày lễ & kỷ niệm",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo nhắc trước 1 ngày và đúng ngày lễ/kỷ niệm"
            }
            manager.createNotificationChannel(channel)
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
