package com.mrwind.vn

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.net.Uri
import android.media.ExifInterface
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Thông số ngẫu nhiên của một thẻ ảnh: nền + câu chúc + vị trí hoa văn. */
data class ShareCardSpec(
    val themeIndex: Int,
    val greeting: String,
    val decorSeed: Int,
    val styleIndex: Int = 0
)

/**
 * Tạo thẻ ảnh chia sẻ (thời tiết + ngày âm + câu chúc) bằng Canvas, không cần thư viện ngoài.
 * Kích thước 1080x1350 (tỉ lệ 4:5) - hiển thị đẹp trên Zalo / Messenger.
 */
object ShareCardHelper {
    private const val W = 1080
    private const val H = 1350

    private class Theme(val top: Int, val bottom: Int, val glow: Int)

    // Mọi nền đều đủ tối/đậm để chữ trắng luôn dễ đọc
    private val THEMES = listOf(
        Theme(0xFF1E3C72.toInt(), 0xFF2A5298.toInt(), 0xFF7FB3FF.toInt()), // xanh biển đêm
        Theme(0xFF0F2027.toInt(), 0xFF2C7A8C.toInt(), 0xFF6FE3E1.toInt()), // xanh ngọc sâu
        Theme(0xFF3A1C71.toInt(), 0xFFD76D77.toInt(), 0xFFFFC3A0.toInt()), // tím - hồng hoàng hôn
        Theme(0xFF134E5E.toInt(), 0xFF2E8B57.toInt(), 0xFFA8F0B5.toInt()), // xanh rừng
        Theme(0xFFB24592.toInt(), 0xFFF15F79.toInt(), 0xFFFFD1DC.toInt()), // hồng cánh sen
        Theme(0xFF283E51.toInt(), 0xFF4B79A1.toInt(), 0xFFBFD7FF.toInt()), // xám xanh mưa
        Theme(0xFFCC5333.toInt(), 0xFFE8913A.toInt(), 0xFFFFE29A.toInt()), // cam nắng ấm
        Theme(0xFF41295A.toInt(), 0xFF2F0743.toInt(), 0xFFC9A7FF.toInt()), // tím đêm
        Theme(0xFF0B486B.toInt(), 0xFF3B8686.toInt(), 0xFFCFF09E.toInt()), // xanh lam - bạc hà
        Theme(0xFF8E2DE2.toInt(), 0xFF4A00E0.toInt(), 0xFFE0C3FC.toInt()), // tím điện
        Theme(0xFF232526.toInt(), 0xFF414345.toInt(), 0xFFB0BEC5.toInt()), // than chì
        Theme(0xFF0F0C29.toInt(), 0xFF302B63.toInt(), 0xFF8E9EFF.toInt()), // chàm nửa đêm
        Theme(0xFF1D4350.toInt(), 0xFFA43931.toInt(), 0xFFFFB199.toInt()), // ngọc - đỏ gạch
        Theme(0xFF16222A.toInt(), 0xFF3A6073.toInt(), 0xFF9FD3E8.toInt()), // xám bão
        Theme(0xFF642B73.toInt(), 0xFFC6426E.toInt(), 0xFFFFC0D9.toInt()), // mận hồng
        Theme(0xFF004E92.toInt(), 0xFF000428.toInt(), 0xFF5DA9FF.toInt()), // đại dương sâu
        Theme(0xFF0B6E4F.toInt(), 0xFF093028.toInt(), 0xFF8DF5C8.toInt()), // lục bảo
        Theme(0xFFB3541E.toInt(), 0xFF5B1F0F.toInt(), 0xFFFFC38A.toInt()), // than hồng
        Theme(0xFF4A1942.toInt(), 0xFFC2185B.toInt(), 0xFFFFA7C7.toInt()), // quả mọng
        Theme(0xFF8A2387.toInt(), 0xFFE94057.toInt(), 0xFFFFD0A6.toInt()), // tím - đỏ rực
        Theme(0xFF0A2342.toInt(), 0xFF2CA58D.toInt(), 0xFFB8F2E6.toInt()), // đêm - bạc hà
        Theme(0xFF3D348B.toInt(), 0xFF7678ED.toInt(), 0xFFD6D3FF.toInt())  // tím phiên
    )

    /** Số kiểu hiệu ứng nền (xem [drawDecor]). */
    private const val STYLE_COUNT = 15

    private val GREETINGS_MORNING = listOf(
        "Chào buổi sáng! Chúc bạn một ngày mới thật nhiều năng lượng và niềm vui.",
        "Sáng nay nắng đẹp, mong mọi điều tốt lành sẽ đến với bạn.",
        "Một ngày mới bắt đầu rồi, cười thật tươi và cùng cố gắng nhé!",
        "Chúc bạn buổi sáng bình yên, làm việc hiệu quả và gặp nhiều may mắn.",
        "Thức dậy với tâm trạng tốt, hôm nay sẽ là một ngày tuyệt vời!",
        "Chúc bạn khởi đầu ngày mới thật nhẹ nhàng và đầy hứng khởi."
    )
    private val GREETINGS_NOON = listOf(
        "Trưa rồi, nhớ ăn uống đầy đủ và nghỉ ngơi một chút nhé!",
        "Chúc bạn buổi trưa vui vẻ, nạp đầy năng lượng cho buổi chiều.",
        "Dù bận rộn đến đâu cũng nhớ dành thời gian chăm sóc bản thân nhé.",
        "Buổi trưa an lành, chúc bạn bữa cơm ngon miệng và giấc ngủ ngắn thật ngon."
    )
    private val GREETINGS_AFTERNOON = listOf(
        "Chúc bạn buổi chiều thật dễ chịu và mọi việc đều suôn sẻ.",
        "Chiều rồi, cố thêm chút nữa là hoàn thành một ngày tuyệt vời!",
        "Chúc bạn một buổi chiều thảnh thơi, nhẹ nhàng như làn gió.",
        "Mệt thì nghỉ một chút, uống ly nước rồi lại tiếp tục nhé!"
    )
    private val GREETINGS_EVENING = listOf(
        "Chúc bạn buổi tối ấm áp bên gia đình và những người thân yêu.",
        "Một ngày dài đã qua, chúc bạn buổi tối thật thư giãn.",
        "Tối rồi, gác lại lo toan và tận hưởng chút bình yên nhé!",
        "Chúc bạn buổi tối vui vẻ, bình an và nhiều niềm vui."
    )
    private val GREETINGS_NIGHT = listOf(
        "Khuya rồi, ngủ sớm để mai thức dậy thật tươi nhé. Ngủ ngon!",
        "Chúc bạn đêm an lành và những giấc mơ thật đẹp.",
        "Bỏ lại mọi lo âu, đi ngủ thôi. Hẹn gặp lại bạn vào sáng mai!",
        "Đêm yên tĩnh, chúc bạn một giấc ngủ thật sâu và bình yên."
    )
    private val GREETINGS_RAIN = listOf(
        "Trời có mưa, nhớ mang áo mưa và giữ ấm nhé!",
        "Mưa rơi nhẹ nhàng, chúc bạn một ngày bình yên dù ngoài trời ẩm ướt.",
        "Ngày mưa thật hợp để uống ly trà nóng. Đi đường cẩn thận nhé!"
    )
    private val GREETINGS_HOT = listOf(
        "Trời nóng lắm, nhớ uống đủ nước và tránh nắng nhé!",
        "Nắng gắt thế này, bạn nhớ che chắn kỹ khi ra đường nha."
    )

    private fun isRainCode(code: Int) = code in 51..67 || code in 80..82 || code in 95..99

    private fun parseTemp(text: String): Int? =
        Regex("-?\\d+").find(text)?.value?.toIntOrNull()

    private fun vnHour(): Int =
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).get(Calendar.HOUR_OF_DAY)

    private fun greetingPool(weather: WeatherCache): List<String> {
        val minutes = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).let {
            it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
        }
        val timePool = when {
            minutes in (6 * 60) until (11 * 60) -> GREETINGS_MORNING
            minutes in (11 * 60) until (14 * 60) -> GREETINGS_NOON
            minutes in (14 * 60) until (18 * 60) -> GREETINGS_AFTERNOON
            minutes in (18 * 60) until (21 * 60 + 30) -> GREETINGS_EVENING
            else -> GREETINGS_NIGHT
        }
        val pool = timePool.toMutableList()
        if (isRainCode(weather.weatherCode)) pool += GREETINGS_RAIN
        val temp = parseTemp(weather.currentTemp)
        if (temp != null && temp >= 35) pool += GREETINGS_HOT
        return pool
    }

    private fun pickGreeting(weather: WeatherCache, previousGreeting: String?): String {
        val pool = greetingPool(weather)
        var greeting: String
        var tries = 0
        do {
            greeting = pool.random()
            tries++
        } while (previousGreeting != null && greeting == previousGreeting && pool.size > 1 && tries < 10)
        return greeting
    }

    /** Chọn ngẫu nhiên nền (màu + hiệu ứng) + câu chúc. [previous] dùng để tránh trùng với lần trước. */
    fun randomSpec(weather: WeatherCache, previous: ShareCardSpec? = null): ShareCardSpec {
        var theme: Int
        do {
            theme = Random.nextInt(THEMES.size)
        } while (previous != null && theme == previous.themeIndex && THEMES.size > 1)

        var style: Int
        do {
            style = Random.nextInt(STYLE_COUNT)
        } while (previous != null && style == previous.styleIndex && STYLE_COUNT > 1)

        return ShareCardSpec(theme, pickGreeting(weather, previous?.greeting), Random.nextInt(), style)
    }

    /** Chỉ đổi câu chúc, GIỮ NGUYÊN nền (màu, hiệu ứng, vị trí hoa văn). */
    fun refreshGreeting(weather: WeatherCache, previous: ShareCardSpec?): ShareCardSpec =
        if (previous == null) randomSpec(weather, null)
        else previous.copy(greeting = pickGreeting(weather, previous.greeting))

    private fun weatherEmoji(code: Int): String {
        val hour = vnHour()
        val isNight = hour < 6 || hour >= 18
        return when (code) {
            0 -> if (isNight) "🌙" else "☀️"
            1, 2 -> if (isNight) "☁️" else "⛅"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55, 56, 57 -> "🌦️"
            61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️"
            71, 73, 75, 77, 85, 86 -> "❄️"
            95, 96, 99 -> "⛈️"
            else -> "🌤️"
        }
    }

    private fun paint(size: Float, bold: Boolean = false, alpha: Int = 255): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.alpha = alpha
            textSize = size
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 2f, 0x80000000.toInt())
        }

    private fun drawCentered(canvas: Canvas, text: String, cy: Float, p: TextPaint, maxWidth: Float = 940f) {
        val fitted = TextUtils.ellipsize(text, p, maxWidth, TextUtils.TruncateAt.END).toString()
        canvas.drawText(fitted, W / 2f, cy, p)
    }

    // ---------------------------------------------------------------------
    //  NỀN NGẪU NHIÊN: 15 kiểu hiệu ứng, tất cả đều nhẹ để chữ trắng vẫn dễ đọc
    // ---------------------------------------------------------------------

    private fun withAlpha(c: Int, a: Int): Int = (c and 0x00FFFFFF) or ((a.coerceIn(0, 255)) shl 24)

    private fun mix(a: Int, b: Int, t: Float): Int {
        val r = (Color.red(a) + (Color.red(b) - Color.red(a)) * t).toInt()
        val g = (Color.green(a) + (Color.green(b) - Color.green(a)) * t).toInt()
        val bl = (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t).toInt()
        return Color.rgb(r.coerceIn(0, 255), g.coerceIn(0, 255), bl.coerceIn(0, 255))
    }

    private fun fillPaint(c: Int, a: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = c
        alpha = a.coerceIn(0, 255)
        style = Paint.Style.FILL
    }

    private fun strokePaint(c: Int, a: Int, width: Float): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = c
        alpha = a.coerceIn(0, 255)
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private fun sparkle(canvas: Canvas, cx: Float, cy: Float, s: Float, paint: Paint) {
        val path = Path()
        path.moveTo(cx, cy - s)
        path.quadTo(cx, cy, cx + s, cy)
        path.quadTo(cx, cy, cx, cy + s)
        path.quadTo(cx, cy, cx - s, cy)
        path.quadTo(cx, cy, cx, cy - s)
        path.close()
        canvas.drawPath(path, paint)
    }

    /** Nền gradient (4 kiểu hướng chuyển màu khác nhau). */
    private fun drawBackgroundGradient(canvas: Canvas, theme: Theme, rnd: Random) {
        val w = W.toFloat()
        val h = H.toFloat()
        val shader: Shader = when (rnd.nextInt(4)) {
            0 -> LinearGradient(0f, 0f, w, h, theme.top, theme.bottom, Shader.TileMode.CLAMP)
            1 -> LinearGradient(0f, 0f, 0f, h, theme.top, theme.bottom, Shader.TileMode.CLAMP)
            2 -> LinearGradient(w, 0f, 0f, h, theme.top, theme.bottom, Shader.TileMode.CLAMP)
            else -> RadialGradient(
                w * (0.25f + 0.5f * rnd.nextFloat()),
                h * (0.2f + 0.3f * rnd.nextFloat()),
                h * 0.95f,
                mix(theme.bottom, theme.top, 0.35f),
                theme.top,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader })
    }

    private fun drawDecor(canvas: Canvas, style: Int, rnd: Random, theme: Theme) {
        when (style) {
            0 -> decorBokeh(canvas, rnd, theme)
            1 -> decorWaves(canvas, rnd, theme)
            2 -> decorStars(canvas, rnd, theme)
            3 -> decorStripes(canvas, rnd, theme)
            4 -> decorRays(canvas, rnd, theme)
            5 -> decorRipples(canvas, rnd, theme)
            6 -> decorRain(canvas, rnd, theme)
            7 -> decorDots(canvas, rnd, theme)
            8 -> decorAurora(canvas, rnd, theme)
            9 -> decorMountains(canvas, rnd, theme)
            10 -> decorLowPoly(canvas, rnd, theme)
            11 -> decorBubbles(canvas, rnd, theme)
            12 -> decorFireflies(canvas, rnd, theme)
            13 -> decorClouds(canvas, rnd, theme)
            else -> decorContours(canvas, rnd, theme)
        }
        // Lớp tối nhẹ về phía dưới giúp chữ luôn dễ đọc
        val vignette = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, H.toFloat(), 0x00000000, 0x33000000, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), vignette)
    }

    /** 0. Vòng sáng mờ (bokeh) */
    private fun decorBokeh(canvas: Canvas, rnd: Random, theme: Theme) {
        val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(9) {
            val r = 90f + rnd.nextFloat() * 260f
            glow.color = theme.glow
            glow.alpha = 18 + rnd.nextInt(30)
            canvas.drawCircle(rnd.nextFloat() * W, rnd.nextFloat() * H, r, glow)
        }
    }

    /** 1. Sóng nhiều lớp */
    private fun decorWaves(canvas: Canvas, rnd: Random, theme: Theme) {
        val twoPi = (2.0 * PI).toFloat()
        for (i in 0 until 5) {
            val baseY = H * 0.42f + i * H * 0.12f + rnd.nextFloat() * 40f
            val amp = 36f + rnd.nextFloat() * 60f
            val len = 360f + rnd.nextFloat() * 440f
            val phase = rnd.nextFloat() * twoPi
            val path = Path()
            path.moveTo(0f, H.toFloat())
            var x = 0f
            while (x <= W) {
                path.lineTo(x, baseY + sin(x / len * twoPi + phase) * amp)
                x += 8f
            }
            path.lineTo(W.toFloat(), H.toFloat())
            path.close()
            canvas.drawPath(path, fillPaint(theme.glow, 14 + i * 8))
        }
    }

    /** 2. Bầu trời sao + lấp lánh */
    private fun decorStars(canvas: Canvas, rnd: Random, theme: Theme) {
        repeat(80) {
            val r = 1.5f + rnd.nextFloat() * 4f
            canvas.drawCircle(rnd.nextFloat() * W, rnd.nextFloat() * H, r, fillPaint(Color.WHITE, 50 + rnd.nextInt(100)))
        }
        repeat(8) {
            val s = 18f + rnd.nextFloat() * 30f
            sparkle(canvas, rnd.nextFloat() * W, rnd.nextFloat() * H, s, fillPaint(theme.glow, 110 + rnd.nextInt(40)))
        }
        // Vầng trăng/mặt trời mờ
        val cx = if (rnd.nextBoolean()) 150f + rnd.nextFloat() * 120f else W - 270f + rnd.nextFloat() * 120f
        canvas.drawCircle(cx, 330f, 150f, fillPaint(theme.glow, 22))
        canvas.drawCircle(cx, 330f, 84f, fillPaint(theme.glow, 50))
    }

    /** 3. Sọc chéo */
    private fun decorStripes(canvas: Canvas, rnd: Random, theme: Theme) {
        canvas.save()
        canvas.rotate(-25f + rnd.nextFloat() * 10f, W / 2f, H / 2f)
        var x = -W.toFloat()
        while (x < W * 2f) {
            val w = 40f + rnd.nextFloat() * 120f
            canvas.drawRect(x, -H.toFloat(), x + w, H * 2f, fillPaint(theme.glow, 10 + rnd.nextInt(22)))
            x += w + 30f + rnd.nextFloat() * 140f
        }
        canvas.restore()
    }

    /** 4. Tia sáng toả từ trên xuống */
    private fun decorRays(canvas: Canvas, rnd: Random, theme: Theme) {
        val cx = W * (0.2f + 0.6f * rnd.nextFloat())
        val cy = -80f - rnd.nextFloat() * 120f
        val n = 12
        val start = Math.toRadians(30.0)
        val step = Math.toRadians(120.0) / n
        val radius = 2300f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx, cy, cx, cy + 1700f,
                withAlpha(theme.glow, 95), withAlpha(theme.glow, 0), Shader.TileMode.CLAMP
            )
        }
        var i = 0
        while (i < n) {
            val a0 = start + i * step
            val a1 = a0 + step * (0.5 + 0.4 * rnd.nextDouble())
            val path = Path()
            path.moveTo(cx, cy)
            path.lineTo(cx + (radius * cos(a0)).toFloat(), cy + (radius * sin(a0)).toFloat())
            path.lineTo(cx + (radius * cos(a1)).toFloat(), cy + (radius * sin(a1)).toFloat())
            path.close()
            canvas.drawPath(path, paint)
            i += 2
        }
        canvas.drawCircle(cx, 0f, 170f, fillPaint(theme.glow, 40))
    }

    /** 5. Gợn sóng lan toả (như giọt mưa rơi xuống nước) */
    private fun decorRipples(canvas: Canvas, rnd: Random, theme: Theme) {
        repeat(2) { k ->
            val cx = W * (0.15f + 0.7f * rnd.nextFloat())
            val cy = H * (0.15f + 0.7f * rnd.nextFloat())
            val gap = 70f + rnd.nextFloat() * 40f
            val rings = if (k == 0) 9 else 6
            for (i in 1..rings) {
                canvas.drawCircle(cx, cy, i * gap, strokePaint(theme.glow, (70 - i * 6).coerceAtLeast(10), 3.5f))
            }
        }
    }

    /** 6. Mưa rơi */
    private fun decorRain(canvas: Canvas, rnd: Random, theme: Theme) {
        repeat(110) {
            val x = -100f + rnd.nextFloat() * (W + 200f)
            val y = rnd.nextFloat() * H
            val len = 50f + rnd.nextFloat() * 90f
            val width = 2.5f + rnd.nextFloat() * 2f
            canvas.drawLine(x, y, x - len * 0.35f, y + len, strokePaint(theme.glow, 25 + rnd.nextInt(45), width))
        }
    }

    /** 7. Lưới chấm to dần theo đường chéo */
    private fun decorDots(canvas: Canvas, rnd: Random, theme: Theme) {
        val flip = rnd.nextBoolean()
        val gap = 54f
        var y = 30f
        while (y < H) {
            var x = 30f
            while (x < W) {
                var t = (x / W + y / H) / 2f
                if (flip) t = 1f - t
                canvas.drawCircle(x, y, 1.5f + 5f * t, fillPaint(theme.glow, (25 + 60 * t).toInt()))
                x += gap
            }
            y += gap
        }
    }

    /** 8. Cực quang (dải sáng mềm) */
    private fun decorAurora(canvas: Canvas, rnd: Random, theme: Theme) {
        val second = mix(theme.glow, Color.WHITE, 0.4f)
        for (i in 0 until 4) {
            val y0 = H * (0.1f + 0.2f * i) + rnd.nextFloat() * 80f
            val path = Path()
            path.moveTo(-50f, y0)
            path.cubicTo(
                W * 0.3f, y0 - 200f - rnd.nextFloat() * 200f,
                W * 0.6f, y0 + 200f + rnd.nextFloat() * 200f,
                W + 50f, y0 + rnd.nextFloat() * 100f - 50f
            )
            val paint = strokePaint(if (i % 2 == 0) theme.glow else second, 46, 120f + rnd.nextFloat() * 90f).apply {
                maskFilter = BlurMaskFilter(60f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawPath(path, paint)
        }
    }

    /** 9. Núi nhiều lớp + mặt trăng/mặt trời */
    private fun decorMountains(canvas: Canvas, rnd: Random, theme: Theme) {
        val cx = if (rnd.nextBoolean()) 150f + rnd.nextFloat() * 110f else W - 260f + rnd.nextFloat() * 110f
        canvas.drawCircle(cx, 340f, 130f, fillPaint(theme.glow, 28))
        canvas.drawCircle(cx, 340f, 78f, fillPaint(theme.glow, 70))
        for (layer in 0 until 4) {
            val baseY = H * 0.64f + layer * H * 0.08f
            val path = Path()
            path.moveTo(0f, H.toFloat())
            path.lineTo(0f, baseY)
            var x = 0f
            while (x < W) {
                x += 90f + rnd.nextFloat() * 140f
                path.lineTo(x.coerceAtMost(W.toFloat()), baseY - rnd.nextFloat() * (170f - layer * 25f))
            }
            path.lineTo(W.toFloat(), H.toFloat())
            path.close()
            canvas.drawPath(path, fillPaint(mix(theme.bottom, Color.BLACK, 0.2f + layer * 0.14f), 110 + layer * 25))
        }
    }

    /** 10. Mảnh tam giác (low-poly) */
    private fun decorLowPoly(canvas: Canvas, rnd: Random, theme: Theme) {
        val cols = 6
        val rows = 8
        val cw = W / cols.toFloat()
        val ch = H / rows.toFloat()
        val pts = Array(rows + 1) { r ->
            Array(cols + 1) { c ->
                val jx = if (c == 0 || c == cols) 0f else (rnd.nextFloat() - 0.5f) * cw * 0.7f
                val jy = if (r == 0 || r == rows) 0f else (rnd.nextFloat() - 0.5f) * ch * 0.7f
                floatArrayOf(c * cw + jx, r * ch + jy)
            }
        }
        fun tri(a: FloatArray, b: FloatArray, c: FloatArray) {
            val path = Path()
            path.moveTo(a[0], a[1])
            path.lineTo(b[0], b[1])
            path.lineTo(c[0], c[1])
            path.close()
            val light = rnd.nextBoolean()
            canvas.drawPath(
                path,
                fillPaint(if (light) theme.glow else Color.BLACK, if (light) 4 + rnd.nextInt(24) else 6 + rnd.nextInt(30))
            )
        }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                tri(pts[r][c], pts[r][c + 1], pts[r + 1][c])
                tri(pts[r][c + 1], pts[r + 1][c + 1], pts[r + 1][c])
            }
        }
    }

    /** 11. Bong bóng */
    private fun decorBubbles(canvas: Canvas, rnd: Random, theme: Theme) {
        repeat(16) {
            val k = rnd.nextFloat()
            val r = 20f + k * k * 110f
            val cx = rnd.nextFloat() * W
            val cy = rnd.nextFloat() * H
            canvas.drawCircle(cx, cy, r, fillPaint(theme.glow, 14))
            canvas.drawCircle(cx, cy, r, strokePaint(Color.WHITE, 70, 3f))
            canvas.drawCircle(cx - r * 0.35f, cy - r * 0.35f, r * 0.16f, fillPaint(Color.WHITE, 90))
        }
    }

    /** 12. Đom đóm / hạt sáng mờ */
    private fun decorFireflies(canvas: Canvas, rnd: Random, theme: Theme) {
        repeat(26) {
            val r = 6f + rnd.nextFloat() * 22f
            val paint = fillPaint(if (rnd.nextBoolean()) theme.glow else Color.WHITE, 90 + rnd.nextInt(90)).apply {
                maskFilter = BlurMaskFilter(r * 0.9f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(rnd.nextFloat() * W, rnd.nextFloat() * H, r, paint)
        }
    }

    /** 13. Mây trôi */
    private fun decorClouds(canvas: Canvas, rnd: Random, theme: Theme) {
        repeat(5) {
            val s = 60f + rnd.nextFloat() * 70f
            val x = rnd.nextFloat() * W
            val y = H * (0.08f + 0.8f * rnd.nextFloat())
            val paint = fillPaint(Color.WHITE, 16 + rnd.nextInt(14))
            canvas.drawCircle(x, y, s, paint)
            canvas.drawCircle(x + s * 0.9f, y + s * 0.2f, s * 0.75f, paint)
            canvas.drawCircle(x - s * 0.9f, y + s * 0.25f, s * 0.7f, paint)
            canvas.drawCircle(x + s * 0.3f, y - s * 0.4f, s * 0.7f, paint)
            canvas.drawRoundRect(RectF(x - s * 1.6f, y + s * 0.1f, x + s * 1.7f, y + s * 0.95f), s * 0.4f, s * 0.4f, paint)
        }
    }

    /** 14. Đường đồng mức uốn lượn */
    private fun decorContours(canvas: Canvas, rnd: Random, theme: Theme) {
        val twoPi = (2.0 * PI).toFloat()
        val len1 = 300f + rnd.nextFloat() * 300f
        val len2 = 140f + rnd.nextFloat() * 120f
        val amp1 = 40f + rnd.nextFloat() * 50f
        val amp2 = 12f + rnd.nextFloat() * 18f
        val ph1 = rnd.nextFloat() * twoPi
        val ph2 = rnd.nextFloat() * twoPi
        val paint = strokePaint(theme.glow, 34, 2.5f)
        for (i in 0 until 26) {
            val y0 = i * H / 25f - 100f
            val path = Path()
            var x = -10f
            path.moveTo(x, y0 + sin(x / len1 * twoPi + ph1 + i * 0.3f) * amp1)
            while (x <= W + 10f) {
                x += 10f
                path.lineTo(x, y0 + sin(x / len1 * twoPi + ph1 + i * 0.3f) * amp1 + sin(x / len2 * twoPi + ph2) * amp2)
            }
            canvas.drawPath(path, paint)
        }
    }

    /** Ảnh người dùng: phủ kín (center-crop) + lớp tối nhẹ để chữ trắng luôn dễ đọc. */
    private fun drawPhotoBackground(canvas: Canvas, photo: Bitmap) {
        val scale = maxOf(W.toFloat() / photo.width, H.toFloat() / photo.height)
        val cropW = (W / scale).toInt().coerceAtMost(photo.width)
        val cropH = (H / scale).toInt().coerceAtMost(photo.height)
        val left = (photo.width - cropW) / 2
        val top = (photo.height - cropH) / 2
        val src = Rect(left, top, left + cropW, top + cropH)
        val dst = Rect(0, 0, W, H)
        canvas.drawBitmap(photo, src, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        val overlay = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, H.toFloat(), 0x59000000, 0xB3000000.toInt(), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), overlay)
    }

    /** Đọc ảnh từ thư viện, thu nhỏ nếu quá lớn và xoay đúng chiều theo EXIF. */
    fun loadPhoto(context: Context, uri: Uri): Bitmap? {
        return try {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            var sample = 1
            while (bounds.outWidth / sample > 2400 || bounds.outHeight / sample > 2400) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val raw = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: return null
            val orientation = try {
                resolver.openInputStream(uri)?.use {
                    ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }
            val m = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
                else -> return raw
            }
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadPhotoAsync(context: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) { loadPhoto(context, uri) }

    /** Vẽ thẻ ảnh. Nên gọi ở luồng nền (Dispatchers.Default). */
    fun render(
        context: Context,
        weather: WeatherCache,
        solarLabel: String,
        lunarLabel: String,
        spec: ShareCardSpec,
        photo: Bitmap? = null
    ): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val theme = THEMES[spec.themeIndex.coerceIn(0, THEMES.size - 1)]
        val rnd = Random(spec.decorSeed)

        // Nền gradient (hướng ngẫu nhiên) + hiệu ứng ngẫu nhiên, hoặc ảnh người dùng
        drawBackgroundGradient(canvas, theme, rnd)
        if (photo != null) {
            drawPhotoBackground(canvas, photo)
        } else {
            drawDecor(canvas, spec.styleIndex.coerceIn(0, STYLE_COUNT - 1), rnd, theme)
        }

        // Vị trí + ngày dương lịch
        drawCentered(canvas, "📍 ${weather.locationName}", 130f, paint(46f, bold = true))
        drawCentered(canvas, solarLabel, 200f, paint(38f, alpha = 225))

        // Biểu tượng thời tiết + nhiệt độ
        val emojiPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 250f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(weatherEmoji(weather.weatherCode), W / 2f, 500f, emojiPaint)
        drawCentered(canvas, weather.currentTemp, 720f, paint(190f, bold = true))
        drawCentered(canvas, weather.description, 800f, paint(54f, bold = true, alpha = 240))
        drawCentered(
            canvas,
            "Cao ${weather.maxTemp}  •  Thấp ${weather.minTemp}",
            875f,
            paint(42f, alpha = 230)
        )
        drawCentered(
            canvas,
            "Độ ẩm ${weather.humidity}  •  Gió ${weather.windSpeed}",
            935f,
            paint(38f, alpha = 210)
        )

        // Thẻ kính mờ: ngày âm + câu chúc
        val card = RectF(70f, 990f, W - 70f, 1250f)
        val cardFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 40
        }
        val cardStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 90
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(card, 56f, 56f, cardFill)
        canvas.drawRoundRect(card, 56f, 56f, cardStroke)

        val lunarLine = if (lunarLabel.contains("âm", ignoreCase = true)) lunarLabel else "Âm lịch: $lunarLabel"
        drawCentered(canvas, lunarLine, 1052f, paint(36f, bold = true), maxWidth = 880f)

        // Câu chúc tự co cỡ chữ để vừa khung
        val textWidth = (card.width() - 90f).toInt()
        val maxTextHeight = 150
        var size = 44f
        var layout: StaticLayout
        while (true) {
            val tp = paint(size, bold = true).apply { textAlign = Paint.Align.LEFT }
            layout = StaticLayout.Builder
                .obtain(spec.greeting, 0, spec.greeting.length, tp, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.12f)
                .build()
            if (layout.height <= maxTextHeight || size <= 28f) break
            size -= 2f
        }
        val top = 1082f + (maxTextHeight - layout.height).coerceAtLeast(0) / 2f
        canvas.save()
        canvas.translate(card.left + 45f, top)
        layout.draw(canvas)
        canvas.restore()

        // Chân trang
        val appName = try {
            context.getString(R.string.app_name)
        } catch (_: Exception) {
            "Thời tiết và Âm lịch"
        }
        drawCentered(canvas, appName, 1305f, paint(30f, alpha = 170))

        return bmp
    }

    /** Lưu ảnh vào Pictures/ThoiTietAmLich. Trả về true nếu thành công. */
    fun saveToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "TheThoiTiet_$stamp.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ThoiTietAmLich")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
                try {
                    val stream = resolver.openOutputStream(uri) ?: throw IOException("Không mở được luồng ghi")
                    stream.use { out ->
                        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                            throw IOException("Không ghi được ảnh")
                        }
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    return false
                }
            } else {
                // Android 9 trở xuống: ghi thẳng vào Pictures (cần quyền WRITE_EXTERNAL_STORAGE)
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "ThoiTietAmLich"
                )
                if (!dir.exists() && !dir.mkdirs()) return false
                val file = File(dir, fileName)
                FileOutputStream(file).use { out ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) return false
                }
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun renderAsync(
        context: Context,
        weather: WeatherCache,
        solarLabel: String,
        lunarLabel: String,
        spec: ShareCardSpec,
        photo: Bitmap? = null
    ): Bitmap = withContext(Dispatchers.Default) {
        render(context, weather, solarLabel, lunarLabel, spec, photo)
    }

    suspend fun saveToGalleryAsync(context: Context, bitmap: Bitmap): Boolean =
        withContext(Dispatchers.IO) { saveToGallery(context, bitmap) }
}
