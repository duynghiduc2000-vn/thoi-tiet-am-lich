package com.mrwind.vn

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.floor

data class LunarDateInfo(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeap: Boolean,
    val dayCanChi: String,
    val monthCanChi: String,
    val yearCanChi: String
) {
    fun shortSummary(): String {
        val leapText = if (isLeap) " nhuận" else ""
        return "$day/${month}$leapText ÂL • Năm $yearCanChi"
    }

    fun fullSummary(): String {
        val leapText = if (isLeap) " (Nhuận)" else ""
        return "Âm lịch: Ngày $day tháng $month$leapText • Ngày $dayCanChi • Tháng $monthCanChi • Năm $yearCanChi"
    }
}

object LunarCalendar {
    private val CANS = arrayOf(
        "Giáp", "Ất", "Bính", "Đinh", "Mậu",
        "Kỷ", "Canh", " Tân", "Nhâm", "Quý"
    ).map { it.trim() }.toTypedArray()

    private val CHIS = arrayOf(
        "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ",
        "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    )

    // Tính Julian Day Number từ ngày Dương lịch
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

    // Góc kinh độ Mặt Trời tại thời điểm tính Sóc (múi giờ +7)
    private fun getSunLongitude(dayNumber: Int, timeZone: Double = 7.0): Int {
        val t = (dayNumber - 0.5 - timeZone / 24.0 - 2451545.0) / 36525.0
        val t2 = t * t
        val dr = Math.PI / 180.0
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        var dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * Math.sin(dr * 2 * m)
        dl += 0.000290 * Math.sin(dr * 3 * m)
        var l = l0 + dl
        l *= dr
        l -= Math.PI * 2 * floor(l / (Math.PI * 2))
        return floor(l / Math.PI * 6).toInt()
    }

    // Ngày Sóc thứ k tính theo chu kỳ Mặt Trăng
    private fun getNewMoonDay(k: Int, timeZone: Double = 7.0): Int {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = Math.PI / 180.0
        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mPr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * Math.sin(m * dr) + 0.0021 * Math.sin(2 * dr * m)
        c1 -= 0.4068 * Math.sin(mPr * dr) + 0.0161 * Math.sin(dr * 2 * mPr)
        c1 += 0.0104 * Math.sin(dr * 2 * f) - 0.0051 * Math.sin(dr * (m + mPr))
        c1 -= 0.0074 * Math.sin(dr * (m - mPr)) + 0.0004 * Math.sin(dr * (2 * f + m))
        c1 -= 0.0004 * Math.sin(dr * (2 * f - m)) - 0.0006 * Math.sin(dr * (2 * f + mPr))
        c1 += 0.0010 * Math.sin(dr * (2 * f - mPr)) + 0.0005 * Math.sin(dr * (mPr + 2 * m))
        val deltat = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t3 * t
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        val jdNew = jd1 + c1 - deltat
        return floor(jdNew + 0.5 + timeZone / 24.0).toInt()
    }

    private fun getLunarMonth11(yy: Int, timeZone: Double = 7.0): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = floor(off / 29.530588853).toInt()
        var nm = getNewMoonDay(k, timeZone)
        val sunLong = getSunLongitude(nm, timeZone)
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone)
        }
        return nm
    }

    private fun getLeapMonthOffset(a11: Int, timeZone: Double = 7.0): Int {
        val k = floor((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
        var last = 0
        var i = 1
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        do {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        } while (arc != last && i < 14)
        return i - 1
    }

    fun convertSolarToLunar(dd: Int, mm: Int, yy: Int, timeZone: Double = 7.0): LunarDateInfo {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay(k + 1, timeZone)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone)
        }
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        var lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = getLunarMonth11(yy - 1, timeZone)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = floor((monthStart - a11) / 29.0).toInt()
        var lunarLeap = false
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) {
                    lunarLeap = true
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth -= 12
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1
        }

        val dayCanChi = canChiOfDay(dayNumber)
        val monthCanChi = canChiOfMonth(lunarYear, lunarMonth)
        val yearCanChi = canChiOfYear(lunarYear)

        return LunarDateInfo(
            day = lunarDay,
            month = lunarMonth,
            year = lunarYear,
            isLeap = lunarLeap,
            dayCanChi = dayCanChi,
            monthCanChi = monthCanChi,
            yearCanChi = yearCanChi
        )
    }

    fun canChiOfDay(jd: Int): String {
        val can = CANS[(jd + 9) % 10]
        val chi = CHIS[(jd + 1) % 12]
        return "$can $chi"
    }

    fun canChiOfMonth(lunarYear: Int, lunarMonth: Int): String {
        val yearCanIndex = (lunarYear + 6) % 10
        val monthCanIndex = (yearCanIndex * 2 + lunarMonth + 1) % 10
        val monthChiIndex = (lunarMonth + 1) % 12
        return "${CANS[monthCanIndex]} ${CHIS[monthChiIndex]}"
    }

    fun canChiOfYear(lunarYear: Int): String {
        val can = CANS[(lunarYear + 6) % 10]
        val chi = CHIS[(lunarYear + 8) % 12]
        return "$can $chi"
    }

    fun todayLunar(): LunarDateInfo {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        return convertSolarToLunar(
            dd = cal.get(Calendar.DAY_OF_MONTH),
            mm = cal.get(Calendar.MONTH) + 1,
            yy = cal.get(Calendar.YEAR),
            timeZone = 7.0
        )
    }

    fun formatVietnameseSolarDate(cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))): String {
        val thu = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Chủ Nhật"
            Calendar.MONDAY -> "Thứ Hai"
            Calendar.TUESDAY -> "Thứ Ba"
            Calendar.WEDNESDAY -> "Thứ Tư"
            Calendar.THURSDAY -> "Thứ Năm"
            Calendar.FRIDAY -> "Thứ Sáu"
            else -> "Thứ Bảy"
        }
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val y = cal.get(Calendar.YEAR)
        return "$thu • $d/$m/$y"
    }
}
