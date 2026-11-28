package com.mrwind.vn

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
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
import kotlin.random.Random

/** Thông số ngẫu nhiên của một thẻ ảnh: nền + câu chúc + vị trí hoa văn. */
data class ShareCardSpec(
    val themeIndex: Int,
    val greeting: String,
    val decorSeed: Int
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
        Theme(0xFF8E2DE2.toInt(), 0xFF4A00E0.toInt(), 0xFFE0C3FC.toInt())  // tím điện
    )

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

    /** Chọn ngẫu nhiên nền + câu chúc. [previous] dùng để tránh trùng với lần trước. */
    fun randomSpec(weather: WeatherCache, previous: ShareCardSpec? = null): ShareCardSpec {
        var theme: Int
        do {
            theme = Random.nextInt(THEMES.size)
        } while (previous != null && theme == previous.themeIndex && THEMES.size > 1)

        val pool = greetingPool(weather)
        var greeting: String
        var tries = 0
        do {
            greeting = pool.random()
            tries++
        } while (previous != null && greeting == previous.greeting && pool.size > 1 && tries < 10)

        return ShareCardSpec(theme, greeting, Random.nextInt())
    }

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

        // Nền gradient chéo
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, W.toFloat(), H.toFloat(), theme.top, theme.bottom, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bg)

        // Các vòng sáng mờ ngẫu nhiên
        val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        if (photo != null) drawPhotoBackground(canvas, photo) else repeat(9) {
            val r = 90f + rnd.nextFloat() * 260f
            val cx = rnd.nextFloat() * W
            val cy = rnd.nextFloat() * H
            glow.color = theme.glow
            glow.alpha = 18 + rnd.nextInt(30)
            canvas.drawCircle(cx, cy, r, glow)
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
