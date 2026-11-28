package com.mrwind.vn

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private const val APP_VERSION_NAME = "31.12-HPBD"
private const val FEEDBACK_EMAIL = "duynghiduc12@gmail.com"
private const val DEFAULT_BG_MUSIC_VOLUME = 0.15f
private const val CONTACT_FACEBOOK_URL = "https://www.facebook.com/share/1JGhc8m62i/"
private const val CONTACT_XIAOMI_COMMUNITY_URL = "https://new.c.mi.com/vn/user/69262e0d27ba478accf81225d37774a7/"
private const val CONTACT_DONATE_MBBANK_TEXT = "Ủng hộ anh ấy một cốc cà hê qua MBBank: 3231121997"

private val FOOD_AND_DRINK_OPTIONS = listOf(
    "Bún bò Huế",
    "Bánh canh xương",
    "Bánh canh chả cá",
    "Mì xào bò",
    "Mì xào giòn",
    "Cơm tấm sườn bì chả",
    "Bánh canh cá lóc",
    "Bánh bông lan trứng muối",
    "Hủ tiếu nam vang",
    "Pizza",
    "Bánh xèo",
    "Bánh mì thịt",
    "Bánh mì ốp la",
    "Xôi mặn",
    "Xôi đậu (kèm muối mè)",
    "Xôi xoài",
    "Bánh giầy kẹp chả",
    "Kem",
    "Cơm phần công ty",
    "Cơm chay",
    "Mì chay",
    "Bún chay",
    "Phở chay",
    "Phở bò",
    "Phở gà",
    "Phở heo",
    "Xiên bẩn",
    "Trà sữa",
    "Bún đậu mắm tôm",
    "Trà chanh",
    "Trà đào",
    "Trà vải",
    "Trà đường",
    "Bánh kem",
    "Chả cá lã vọng",
    "Cơm gà",
    "Cơm gà xối mỡ",
    "Cơm gà rô ti",
    "Bún thịt xào",
    "Bánh bột lọc trần",
    "Bánh Quai Dạc",
    "Bánh Căn Phan Thiết",
    "Mì Quảng miền Trung",
    "Mì Quảng Phan Thiết",
    "Bánh đa cua",
    "Mì gói",
    "Heo quay da giòn",
    "Bò né",
    "Bò quanh lửa hồng",
    "Bánh mì bơ Campuchia",
    "Chè",
    "Trà xanh",
    "Dép (Troll)",
    "Gạch ống (Troll)",
    "Thùng phi (Troll)",
    "Mì cay 7 cấp độ",
    "Mì tương đen",
    "Bánh đồng xu",
    "Lạp xưởng nướng đá",
    "Trứng vịt lột",
    "Bánh đa trộn",
    "Mì ý",
    "Bánh bao",
    "Ăn cháo đá bát",
    "Ăn dầm nằm dề",
    "Trái cây lắc muối ớt",
    "Bánh ướt lòng gà",
    "Ghẹ sữa chiên giòn",
    "Trứng gà nướng",
    "Bánh tráng nướng Phan Thiết",
    "Bánh tráng kẹp Đà Lạt",
    "Bún thịt nướng",
    "Gà chiên giòn",
    "Lagu thịt vịt",
    "Cơm cà ri",
    "Dorayaki",
    "Tokbokki",
    "Cháo thịt bằm",
    "Cháo lòng",
    "Cháo thịt bò",
    "Cháo gà",
    "Cháo trai",
    "Bò Bít tết",
    "Bánh chưng rán",
    "Trái cây tô",
    "Dokki",
    "Haidilao Hot Pot",
    "Tát (Troll)",
    "Bún nước tương",
    "Cơm chiên cá mặn",
    "Tổ yến",
    "Rau má đậu xanh cốt dừa",
    "Mì vịt tiềm",
    "Mì xá xíu",
    "Cơm bò lúc lắc",
    "Heo sữa quay",
    "Vịt nướng lu",
    "Vịt quay",
    "Cơm hến",
    "Bún ốc",
    "Ốc (Hải sản các loại)",
    "Mochi",
    "Chân gà sốt thái",
    "Bánh tráng trộn",
    "Lẩu",
    "Buffet"
)

private val FOOD_ENJOYMENT_WISHES = listOf(
    "Chúc bạn có một bữa ăn thật ngon miệng và nạp đầy năng lượng nhé!",
    "Ăn uống vui vẻ nha, nhớ thưởng thức chậm rãi cho thật đã!",
    "Chúc món này hợp khẩu vị của bạn và mang lại thật nhiều niềm vui hôm nay!",
    "Nạp năng lượng xong rồi lại tiếp tục ngày mới thật rạng rỡ nhé!",
    "Chúc bạn ăn ngon, uống đã, tâm trạng phơi phới suốt cả ngày!",
    "Một lựa chọn quá chuẩn bài, chúc bạn thưởng thức thật trọn vẹn!",
    "Chúc bạn có một bữa ngon hết sảy và thật nhiều năng lượng tích cực!",
    "Ăn ngon miệng nhé bạn ơi, nhớ uống thêm nước cho cân bằng nha!",
    "Chúc bạn có phút giây ăn uống thư giãn và thật nhiều niềm vui!",
    "Hốc nhanh rồi còn đi làm nhé!",
    "Chúc bạn ăn ngon và mau lên kí!",
    "Món này ngon nè, mình cùng thèm!",
    "Nhanh nhanh đớp thôi!",
    "Ăn nhanh chóng lớn nhé!",
    "Mồm thì chê, mà tay thì bốc!",
    "Chọn món này là đúng bài luôn, ngon tuyệt cú mèo 🐱",
    "Alo alo, món ngon đến đây!",
    "Bạn chọn món này đi! Siêu siêu ngon luôn á!",
    "Chảy nước miếng kìa! Đi ăn lẹ thôi!!!",
    "Ăn nhanh còn bán mình cho tư bản nữa kìa!",
    "Chúc bữa này của bạn thơm ngon tròn vị và ấm lòng hết mức!"
)

private data class FoodChoiceResult(
    val itemName: String,
    val wishText: String
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

private data class DailyLuckyData(
    val dateKey: String,
    val numberText: String,
    val colorName: String,
    val colorHex: Long,
    val tasks: List<String>
)

private data class LuckyColorOption(
    val name: String,
    val hex: Long
)

private val LUCKY_COLOR_OPTIONS = listOf(
    LuckyColorOption("Xanh ngọc trời", 0xFF4FC3F7L),
    LuckyColorOption("Xanh ngọc bích", 0xFF26A69AL),
    LuckyColorOption("Xanh bạc hà mát", 0xFF4DB6ACL),
    LuckyColorOption("Xanh dương đại dương", 0xFF29B6F6L),
    LuckyColorOption("Xanh coban dịu", 0xFF5C6BC0L),
    LuckyColorOption("Xanh pastel trời", 0xFF81D4FAL),
    LuckyColorOption("Xanh lá rừng", 0xFF66BB6AL),
    LuckyColorOption("Xanh chanh tươi", 0xFF9CCC65L),
    LuckyColorOption("Xanh rêu non", 0xFF8BC34AL),
    LuckyColorOption("Xanh mint pastel", 0xFFA5D6A7L),
    LuckyColorOption("Vàng nắng ấm", 0xFFFFD54FL),
    LuckyColorOption("Vàng chanh nhẹ", 0xFFFFEE58L),
    LuckyColorOption("Hổ phách mật ong", 0xFFFFB74DL),
    LuckyColorOption("Cam đào dịu", 0xFFFF8A65L),
    LuckyColorOption("Cam quýt rực rỡ", 0xFFFFA726L),
    LuckyColorOption("San hô ấm", 0xFFFF7043L),
    LuckyColorOption("Hồng phấn ngọt", 0xFFF48FB1L),
    LuckyColorOption("Hồng san hô", 0xFFF06292L),
    LuckyColorOption("Hồng dâu pastel", 0xFFF8BBD0L),
    LuckyColorOption("Tím oải hương", 0xFFBA68C8L),
    LuckyColorOption("Tím hoa cà", 0xFF9575CDL),
    LuckyColorOption("Tím tử đinh hương", 0xFFCE93D8L),
    LuckyColorOption("Đỏ gạch ấm", 0xFFEF5350L),
    LuckyColorOption("Trắng ngà ngọc trai", 0xFFF5F5F5L)
)

private val LUCKY_SUGGESTED_TASKS = listOf(
    "Pha một ly nước ấm, trà hoặc cà phê và nghỉ giải lao 10 phút",
    "Dọn gọn bàn làm việc, góc học tập hoặc không gian quanh mình",
    "Gửi một tin nhắn hỏi thăm người thân, bạn cũ hoặc đồng nghiệp",
    "Ra ngoài hít thở không khí trong lành và đi bộ nhẹ 10–15 phút",
    "Đọc vài trang sách yêu thích trước khi đi ngủ",
    "Ghi lại 1–2 điều nhỏ khiến bạn thấy vui hoặc biết ơn hôm nay",
    "Uống đủ nước và sắp xếp lại danh sách việc ưu tiên trong ngày",
    "Nghe một bản nhạc hoặc playlist chill để thư giãn đầu óc",
    "Duỗi người, giãn cơ vai gáy sau khi ngồi lâu một chỗ",
    "Kiểm tra lại điện thoại, xóa bớt ảnh/file rác cho nhẹ bộ nhớ",
    "Tự thưởng cho mình một món ăn nhẹ hoặc món uống ngon miệng",
    "Lên kế hoạch nhỏ cho ngày mai để hôm nay bớt áp lực hơn",
    "Chụp một bức ảnh lưu giữ khoảnh khắc đẹp hoặc bầu trời hôm nay",
    "Ngồi yên lặng nhắm mắt thư giãn 5 phút giữa giờ bận rộn",
    "Xem lại một mục tiêu cá nhân và hoàn thành một bước nhỏ nhất",
    "Dọn sạch giỏ rác hoặc sắp xếp lại các thư mục trên máy/điện thoại",
    "Chăm sóc cây xanh, thú cưng hoặc lau chùi mặt bàn gọn gàng",
    "Viết vài dòng nhật ký ngắn về cảm xúc hiện tại của bạn",
    "Tắt thông báo không cần thiết trong 30 phút để tập trung trọn vẹn",
    "Học thêm một mẹo nhỏ, từ vựng mới hoặc kiến thức thú vị",
    "Chuẩn bị sẵn quần áo hoặc đồ dùng cần thiết cho buổi tiếp theo",
    "Mỉm cười và nói lời cảm ơn chân thành với ai đó hôm nay",
    "Kiểm tra chi tiêu nhỏ trong ngày để giữ cân bằng ngân sách",
    "Ngâm chân nước ấm hoặc rửa mặt bằng nước mát cho tỉnh táo",
    "Thử một góc ngồi mới hoặc mở cửa sổ đón ánh sáng tự nhiên",
    "Hoàn thành dứt điểm một việc lặt vặt bạn đã trì hoãn trước đó",
    "Xem một video hài ngắn hoặc nội dung nhẹ nhàng giúp giải trí",
    "Gọi điện ngắn hỏi thăm gia đình nếu đang ở xa",
    "Sắp xếp lại tủ sách hoặc kệ đồ để dễ tìm khi cần",
    "Tập vài động tác hít thở sâu 4-7-8 để giữ tâm trạng bình tĩnh",
    "Lau sạch màn hình điện thoại, kính mắt hoặc bàn phím",
    "Tự khen bản thân vì một điều tốt bạn đã làm trong tuần này",
    "Nghe một podcast ngắn về chủ đề bạn quan tâm",
    "Đi ngủ sớm hơn thường lệ một chút để hồi phục năng lượng",
    "Khởi động nhẹ hoặc vươn vai ngay khi vừa thức dậy",
    "Dành 15 phút làm một việc hoàn toàn vì sở thích cá nhân"
)

private object DailyLuckyHelper {
    private const val PREFS_NAME = "daily_lucky_prefs"
    private const val KEY_DATE = "lucky_date_key"
    private const val KEY_NUMBER = "lucky_number_text"
    private const val KEY_COLOR_NAME = "lucky_color_name"
    private const val KEY_COLOR_HEX = "lucky_color_hex"
    private const val KEY_TASKS = "lucky_tasks_joined"

    fun todayKey(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }
        return fmt.format(Date())
    }

    fun getTodayLuckyIfExists(context: Context): DailyLuckyData? {
        val today = todayKey()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_DATE, null) ?: return null
        if (savedDate != today) return null

        val numberText = prefs.getString(KEY_NUMBER, null) ?: return null
        val colorName = prefs.getString(KEY_COLOR_NAME, null) ?: return null
        val colorHex = prefs.getLong(KEY_COLOR_HEX, 0L)
        val rawTasks = prefs.getString(KEY_TASKS, "") ?: ""
        val tasks = rawTasks.split("||").map { it.trim() }.filter { it.isNotEmpty() }
        if (colorHex == 0L || tasks.isEmpty()) return null

        return DailyLuckyData(
            dateKey = savedDate,
            numberText = numberText,
            colorName = colorName,
            colorHex = colorHex,
            tasks = tasks.take(2)
        )
    }

    fun getOrCreateTodayLucky(context: Context): DailyLuckyData {
        getTodayLuckyIfExists(context)?.let { return it }

        val today = todayKey()
        val rng = Random(System.nanoTime())
        val number = rng.nextInt(1, 100)
        val numberText = number.toString().padStart(2, '0')
        val color = LUCKY_COLOR_OPTIONS[rng.nextInt(LUCKY_COLOR_OPTIONS.size)]
        val taskCount = rng.nextInt(1, 3)
        val pickedTasks = LUCKY_SUGGESTED_TASKS.shuffled(rng).take(taskCount)

        val data = DailyLuckyData(
            dateKey = today,
            numberText = numberText,
            colorName = color.name,
            colorHex = color.hex,
            tasks = pickedTasks
        )

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DATE, data.dateKey)
            .putString(KEY_NUMBER, data.numberText)
            .putString(KEY_COLOR_NAME, data.colorName)
            .putLong(KEY_COLOR_HEX, data.colorHex)
            .putString(KEY_TASKS, data.tasks.joinToString("||"))
            .commit()

        return data
    }
}

/** Vị trí + kích thước cửa sổ Activity trên màn hình (để đổi toạ độ màn hình -> toạ độ cửa sổ). */
private fun Context.activityWindowInfo(): Pair<IntOffset, IntSize> {
    var c: Context? = this
    while (c is android.content.ContextWrapper) {
        if (c is android.app.Activity) {
            val decor = c.window.decorView
            val loc = IntArray(2)
            decor.getLocationOnScreen(loc)
            return IntOffset(loc[0], loc[1]) to IntSize(decor.width, decor.height)
        }
        c = c.baseContext
    }
    return IntOffset.Zero to IntSize.Zero
}

/**
 * Đặt thẻ tên NGAY TRÊN ĐẦU nút (căn giữa theo nút), cách nút một khoảng nhỏ,
 * và luôn giữ thẻ nằm trọn trong màn hình (nút ở góc cũng không bị cắt).
 * Toạ độ nút được truyền vào là toạ độ trong cửa sổ Activity nên đúng cả khi nút
 * nằm bên trong một Popup khác (thẻ sổ xuống).
 */
private class TooltipAboveAnchorPositionProvider(
    private val anchor: IntRect,
    private val activityWindowSize: IntSize,
    private val gapPx: Int,
    private val edgeMarginPx: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val winW = if (activityWindowSize.width > 0) activityWindowSize.width else windowSize.width
        val centeredX = anchor.left + (anchor.width - popupContentSize.width) / 2
        val maxX = (winW - popupContentSize.width - edgeMarginPx).coerceAtLeast(edgeMarginPx)
        val x = centeredX.coerceIn(edgeMarginPx, maxX)

        val aboveY = anchor.top - popupContentSize.height - gapPx
        val y = if (aboveY >= edgeMarginPx) aboveY else anchor.bottom + gapPx
        return IntOffset(x, y)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CircleActionTooltipWrapper(
    tooltipText: String,
    palette: AdaptivePalette,
    accentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var showTooltip by remember { mutableStateOf(false) }
    var tooltipAnchor by remember { mutableStateOf<IntRect?>(null) }
    var windowSize by remember { mutableStateOf(IntSize.Zero) }
    val anchorCoordinates = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val currentEnabled by rememberUpdatedState(enabled)
    val currentOnClick by rememberUpdatedState(onClick)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .onGloballyPositioned { anchorCoordinates[0] = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        kotlinx.coroutines.coroutineScope {
                            // Phân biệt nhấn giữ (hiện thẻ tên) và chạm nhanh (bấm nút)
                            var longPressed = false
                            val timerJob = launch {
                                delay(260L)
                                longPressed = true
                                val coords = anchorCoordinates[0]
                                if (coords != null && coords.isAttached) {
                                    val topLeft = coords.localToScreen(Offset.Zero)
                                    val (origin, size) = context.activityWindowInfo()
                                    val left = topLeft.x.roundToInt() - origin.x
                                    val top = topLeft.y.roundToInt() - origin.y
                                    tooltipAnchor = IntRect(
                                        left = left,
                                        top = top,
                                        right = left + coords.size.width,
                                        bottom = top + coords.size.height
                                    )
                                    windowSize = size
                                    showTooltip = true
                                }
                            }
                            val released = tryAwaitRelease()
                            timerJob.cancel()
                            showTooltip = false
                            if (released && !longPressed && currentEnabled) {
                                currentOnClick()
                            }
                        }
                    }
                )
            }
    ) {
        content()

        val anchor = tooltipAnchor
        if (showTooltip && anchor != null && tooltipText.isNotBlank()) {
            val positionProvider = remember(anchor, windowSize) {
                TooltipAboveAnchorPositionProvider(
                    anchor = anchor,
                    activityWindowSize = windowSize,
                    gapPx = with(density) { 10.dp.roundToPx() },
                    edgeMarginPx = with(density) { 8.dp.roundToPx() }
                )
            }
            Popup(
                popupPositionProvider = positionProvider,
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    clippingEnabled = false
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .width(104.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.liquidGlassBrush)
                        .border(
                            width = 1.dp,
                            brush = palette.liquidGlassBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tooltipText,
                            color = palette.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE,
                                initialDelayMillis = 250,
                                velocity = 28.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarQuickMenuButton(
    palette: AdaptivePalette,
    accentColor: Color,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = palette.cardBg,
        modifier = modifier
            .size(35.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = if (isExpanded) 0.90f else 0.45f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                val w = size.width
                val h = size.height
                val strokeW = 1.8.dp.toPx()
                val ys = listOf(h * 0.24f, h * 0.50f, h * 0.76f)
                ys.forEach { y ->
                    drawLine(
                        color = accentColor,
                        start = Offset(w * 0.14f, y),
                        end = Offset(w * 0.86f, y),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBarFoodChoiceButton(
    palette: AdaptivePalette,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    tooltipText: String = "Gợi ý món ăn",
    modifier: Modifier = Modifier
) {
    val scale: Float
    val tilt: Float
    if (isActive) {
        val transition = rememberInfiniteTransition(label = "top_food_choice_anim")
        scale = transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.07f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "food_btn_scale"
        ).value
        tilt = transition.animateFloat(
            initialValue = -7f,
            targetValue = 7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "food_btn_tilt"
        ).value
    } else {
        scale = 1f
        tilt = 0f
    }

    CircleActionTooltipWrapper(
        tooltipText = tooltipText,
        palette = palette,
        accentColor = accentColor,
        onClick = onClick,
        modifier = modifier.size(35.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cardBg,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "🍽️",
                    fontSize = 16.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = tilt
                    }
                )
            }
        }
    }
}

@Composable
private fun TopBarLuckyButton(
    palette: AdaptivePalette,
    accentColor: Color,
    isActive: Boolean,
    isRolling: Boolean,
    onClick: () -> Unit,
    tooltipText: String = "Số may mắn",
    modifier: Modifier = Modifier
) {
    val iconScale: Float
    val iconTilt: Float
    if (isRolling) {
        val transition = rememberInfiniteTransition(label = "top_lucky_btn_pulse")
        iconScale = transition.animateFloat(
            initialValue = 0.90f,
            targetValue = 1.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "lucky_btn_scale"
        ).value
        iconTilt = transition.animateFloat(
            initialValue = -12f,
            targetValue = 12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "lucky_btn_tilt"
        ).value
    } else if (isActive) {
        val transition = rememberInfiniteTransition(label = "top_lucky_idle_anim")
        iconScale = transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.07f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "lucky_idle_scale"
        ).value
        iconTilt = transition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1250, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "lucky_idle_tilt"
        ).value
    } else {
        iconScale = 1f
        iconTilt = 0f
    }

    CircleActionTooltipWrapper(
        tooltipText = tooltipText,
        palette = palette,
        accentColor = accentColor,
        enabled = !isRolling,
        onClick = onClick,
        modifier = modifier.size(35.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cardBg,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = if (isRolling) 0.85f else 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "🍀",
                    fontSize = 16.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        rotationZ = iconTilt
                    }
                )
            }
        }
    }
}

private data class CoinTossOutcome(
    val isYes: Boolean,
    val faceLabel: String,
    val message: String
)

private enum class DailyMoodType(
    val id: String,
    val label: String,
    val emoji: String
) {
    HAPPY("happy", "Vui", "😄"),
    SAD("sad", "Buồn", "😔"),
    CRYING("crying", "Muốn khóc", "🥲"),
    SICK("sick", "Bệnh", "🤒"),
    IN_LOVE("in_love", "Đang yêu", "🥰"),
    ANGRY("angry", "Tức giận", "😠"),
    CALM("calm", "Thản nhiên", "😌"),
    ANXIOUS("anxious", "Lo lắng", "😰"),
    TIRED("tired", "Mệt mỏi", "😫"),
    SURPRISED("surprised", "Ngạc nhiên", "😲"),
    OVERWHELMED("overwhelmed", "Vỡ òa", "🤩"),
    STRESSED("stressed", "Căng thẳng", "😣");

    companion object {
        fun fromId(id: String?): DailyMoodType? = entries.firstOrNull { it.id == id }
    }
}

private data class DailyMoodState(
    val dateKey: String,
    val selectedMoodId: String?,
    val previousMoodId: String?
)

private object DailyMoodHelper {
    private const val PREFS_NAME = "daily_mood_prefs"
    private const val KEY_DATE = "mood_date_key"
    private const val KEY_SELECTED = "mood_selected_id"
    private const val KEY_PREVIOUS = "mood_previous_id"

    fun todayKey(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }
        return fmt.format(Date())
    }

    fun loadOrResetToday(context: Context): DailyMoodState {
        val today = todayKey()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_DATE, null)
        if (savedDate != today) {
            prefs.edit()
                .putString(KEY_DATE, today)
                .remove(KEY_SELECTED)
                .remove(KEY_PREVIOUS)
                .commit()
            return DailyMoodState(dateKey = today, selectedMoodId = null, previousMoodId = null)
        }
        val selected = prefs.getString(KEY_SELECTED, null)
        val previous = prefs.getString(KEY_PREVIOUS, null)
        return DailyMoodState(dateKey = today, selectedMoodId = selected, previousMoodId = previous)
    }

    fun selectMoodForToday(context: Context, newMood: DailyMoodType): DailyMoodState {
        val current = loadOrResetToday(context)
        val nextPrevious = current.selectedMoodId ?: current.previousMoodId
        val updated = DailyMoodState(
            dateKey = current.dateKey,
            selectedMoodId = newMood.id,
            previousMoodId = nextPrevious
        )
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DATE, updated.dateKey)
            .putString(KEY_SELECTED, updated.selectedMoodId)
            .putString(KEY_PREVIOUS, updated.previousMoodId)
            .commit()
        return updated
    }
}

@Composable
private fun AnimatedMoodIcon(
    mood: DailyMoodType,
    isActive: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    modifier: Modifier = Modifier
) {
    val scale: Float
    val tilt: Float
    val offsetY: Float

    if (!isActive) {
        scale = 1f
        tilt = 0f
        offsetY = 0f
    } else {
        val transition = rememberInfiniteTransition(label = "mood_anim_${mood.id}")
        when (mood) {
            DailyMoodType.HAPPY -> {
                scale = transition.animateFloat(
                    initialValue = 0.94f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "happy_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -8f,
                    targetValue = 8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "happy_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = 0f,
                    targetValue = -3.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "happy_y"
                ).value
            }
            DailyMoodType.SAD -> {
                scale = transition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.02f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sad_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -5f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sad_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = -1f,
                    targetValue = 2.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sad_y"
                ).value
            }
            DailyMoodType.CRYING -> {
                scale = transition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "cry_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "cry_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = -1f,
                    targetValue = 2.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "cry_y"
                ).value
            }
            DailyMoodType.SICK -> {
                scale = transition.animateFloat(
                    initialValue = 0.93f,
                    targetValue = 1.04f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sick_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -9f,
                    targetValue = 9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1350, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sick_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = -1.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sick_y"
                ).value
            }
            DailyMoodType.IN_LOVE -> {
                scale = transition.animateFloat(
                    initialValue = 0.94f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "love_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -7f,
                    targetValue = 7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "love_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = 1f,
                    targetValue = -2.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "love_y"
                ).value
            }
            DailyMoodType.ANGRY -> {
                scale = transition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "angry_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -10f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "angry_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = -1.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "angry_y"
                ).value
            }
            DailyMoodType.CALM -> {
                scale = transition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "calm_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -3.5f,
                    targetValue = 3.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "calm_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = -1f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "calm_y"
                ).value
            }
            DailyMoodType.ANXIOUS -> {
                scale = transition.animateFloat(
                    initialValue = 0.94f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "anx_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -9f,
                    targetValue = 9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "anx_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "anx_y"
                ).value
            }
            DailyMoodType.TIRED -> {
                scale = transition.animateFloat(
                    initialValue = 0.94f,
                    targetValue = 1.01f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "tired_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -7f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1750, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "tired_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = -1f,
                    targetValue = 3.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "tired_y"
                ).value
            }
            DailyMoodType.SURPRISED -> {
                scale = transition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.18f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "surprised_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -5f,
                    targetValue = 5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "surprised_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = 1f,
                    targetValue = -4.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "surprised_y"
                ).value
            }
            DailyMoodType.OVERWHELMED -> {
                scale = transition.animateFloat(
                    initialValue = 0.93f,
                    targetValue = 1.20f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "overwhelmed_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -11f,
                    targetValue = 11f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 440, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "overwhelmed_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = 1.5f,
                    targetValue = -4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "overwhelmed_y"
                ).value
            }
            DailyMoodType.STRESSED -> {
                scale = transition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.07f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "stressed_scale"
                ).value
                tilt = transition.animateFloat(
                    initialValue = -11f,
                    targetValue = 11f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "stressed_tilt"
                ).value
                offsetY = transition.animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "stressed_y"
                ).value
            }
        }
    }

    Text(
        text = mood.emoji,
        fontSize = fontSize,
        maxLines = 1,
        softWrap = false,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            rotationZ = tilt
            translationY = offsetY.dp.toPx()
        }
    )
}

@Composable
private fun Row2CoinTossButton(
    palette: AdaptivePalette,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    tooltipText: String = "Tung đồng xu",
    modifier: Modifier = Modifier
) {
    val coinScale: Float
    val coinTilt: Float
    if (isActive) {
        val transition = rememberInfiniteTransition(label = "row2_coin_toss_idle")
        coinScale = transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.07f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1080, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "coin_idle_scale"
        ).value
        coinTilt = transition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1220, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "coin_idle_tilt"
        ).value
    } else {
        coinScale = 1f
        coinTilt = 0f
    }

    CircleActionTooltipWrapper(
        tooltipText = tooltipText,
        palette = palette,
        accentColor = accentColor,
        onClick = onClick,
        modifier = modifier.size(35.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cardBg,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "🪙",
                    fontSize = 16.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.graphicsLayer {
                        scaleX = coinScale
                        scaleY = coinScale
                        rotationZ = coinTilt
                    }
                )
            }
        }
    }
}

@Composable
private fun Row2MoodButton(
    palette: AdaptivePalette,
    accentColor: Color,
    isActive: Boolean,
    selectedMood: DailyMoodType?,
    onClick: () -> Unit,
    tooltipText: String = "Cảm xúc hiện tại",
    modifier: Modifier = Modifier
) {
    var randomIdx by remember { mutableIntStateOf(0) }

    LaunchedEffect(isActive, selectedMood) {
        if (!isActive || selectedMood != null) return@LaunchedEffect
        while (true) {
            randomIdx = Random.nextInt(DailyMoodType.entries.size)
            delay(550L)
        }
    }

    CircleActionTooltipWrapper(
        tooltipText = tooltipText,
        palette = palette,
        accentColor = accentColor,
        onClick = onClick,
        modifier = modifier.size(35.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cardBg,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (selectedMood != null) {
                    AnimatedMoodIcon(
                        mood = selectedMood,
                        isActive = isActive,
                        fontSize = 16.sp
                    )
                } else {
                    val staticMood = DailyMoodType.entries[randomIdx.coerceIn(0, DailyMoodType.entries.lastIndex)]
                    Text(
                        text = staticMood.emoji,
                        fontSize = 16.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

private enum class MainTab(val label: String, val icon: ImageVector) {
    WEATHER("Thời tiết", Icons.Default.Cloud),
    CALENDAR("Lịch", Icons.Default.CalendarMonth),
    REMINDERS("Nhắc nhở", Icons.Default.Notifications),
    SETTINGS("Cài đặt", Icons.Default.Settings)
}

@Composable
private fun MainBottomTabBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    accentColor: Color,
    palette: AdaptivePalette
) {
    val barShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    Surface(
        shape = barShape,
        color = palette.cardBg,
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, palette.border, barShape)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            MainTab.entries.forEach { tab ->
                val isSel = tab == selected
                val pillBg by animateFloatAsState(
                    targetValue = if (isSel) 1f else 0f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
                    label = "tab_pill_${tab.name}"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(tab) }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                val sc = 1f + 0.12f * pillBg
                                scaleX = sc
                                scaleY = sc
                            }
                            .size(width = 52.dp, height = 32.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(accentColor.copy(alpha = 0.22f * pillBg))
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSel) accentColor else palette.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        tab.label,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSel) accentColor else palette.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private enum class ExpandableSection {
    APP_APPEARANCE_AUDIO,
    CLOCK_THEME_BG,
    ACCENT_COLOR,
    LOCATION_SIGNATURE,
    FONT_SCALE,
    TODO,
    LUNAR_LOOKUP,
    LUNAR_USEFUL_INFO,
    AGE_CALC,
    GOOD_BAD_DAY,
    BIRTHDAY,
    HOLIDAY,
    BUG_REPORT
}

private enum class BatteryWarningTarget {
    TOP_WEATHER_BG,
    BG_MUSIC,
    CARD_BOUNCE
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

private fun isDaytimeWindow(hourOfDay: Int): Boolean {
    return hourOfDay in 6..17
}

private fun buildCrescentMoonPath(
    outerCenter: Offset,
    outerRadius: Float,
    innerCenter: Offset,
    innerRadius: Float
): Path {
    val outerPath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = outerCenter.x - outerRadius,
                top = outerCenter.y - outerRadius,
                right = outerCenter.x + outerRadius,
                bottom = outerCenter.y + outerRadius
            )
        )
    }
    val innerPath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = innerCenter.x - innerRadius,
                top = innerCenter.y - innerRadius,
                right = innerCenter.x + innerRadius,
                bottom = innerCenter.y + innerRadius
            )
        )
    }
    return Path().apply {
        op(outerPath, innerPath, androidx.compose.ui.graphics.PathOperation.Difference)
    }
}

private fun buildSoftCloudPath(baseX: Float, baseY: Float, scale: Float): Path {
    return Path().apply {
        moveTo(baseX + 6f * scale, baseY + 11f * scale)
        cubicTo(
            baseX + 1f * scale, baseY + 11f * scale,
            baseX - 1f * scale, baseY + 5.5f * scale,
            baseX + 3.5f * scale, baseY + 3.5f * scale
        )
        cubicTo(
            baseX + 3.5f * scale, baseY - 0.5f * scale,
            baseX + 8.5f * scale, baseY - 1.5f * scale,
            baseX + 11.2f * scale, baseY + 1.5f * scale
        )
        cubicTo(
            baseX + 13.5f * scale, baseY - 2.2f * scale,
            baseX + 19.5f * scale, baseY - 1.8f * scale,
            baseX + 21.2f * scale, baseY + 2.4f * scale
        )
        cubicTo(
            baseX + 25.2f * scale, baseY + 2.8f * scale,
            baseX + 25.8f * scale, baseY + 7.8f * scale,
            baseX + 22.5f * scale, baseY + 9.8f * scale
        )
        cubicTo(
            baseX + 21.8f * scale, baseY + 11.4f * scale,
            baseX + 18.8f * scale, baseY + 11.6f * scale,
            baseX + 16.5f * scale, baseY + 10.6f * scale
        )
        cubicTo(
            baseX + 14.5f * scale, baseY + 12.2f * scale,
            baseX + 10.2f * scale, baseY + 12.2f * scale,
            baseX + 8.2f * scale, baseY + 10.7f * scale
        )
        cubicTo(
            baseX + 7.2f * scale, baseY + 11.1f * scale,
            baseX + 6.5f * scale, baseY + 11f * scale,
            baseX + 6f * scale, baseY + 11f * scale
        )
        close()
    }
}

@Composable
private fun BatteryOptimizationIconGlyph(
    tint: Color,
    isAnimated: Boolean,
    modifier: Modifier = Modifier
) {
    val pulseScale: Float
    if (isAnimated) {
        val transition = rememberInfiniteTransition(label = "battery_opt_glyph_pulse")
        pulseScale = transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.07f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bolt_pulse"
        ).value
    } else {
        pulseScale = 1f
    }

    Canvas(
        modifier = modifier.graphicsLayer {
            scaleX = pulseScale
            scaleY = pulseScale
        }
    ) {
        val w = size.width
        val h = size.height
        val strokeW = w * 0.085f

        val bodyLeft = w * 0.24f
        val bodyTop = h * 0.26f
        val bodyWidth = w * 0.52f
        val bodyHeight = h * 0.54f
        drawRoundRect(
            color = tint,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(w * 0.11f, w * 0.11f),
            style = Stroke(width = strokeW, join = StrokeJoin.Round)
        )

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.40f, h * 0.15f),
            size = Size(w * 0.20f, h * 0.08f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )

        val boltPath = Path().apply {
            moveTo(w * 0.53f, h * 0.35f)
            lineTo(w * 0.41f, h * 0.55f)
            lineTo(w * 0.51f, h * 0.55f)
            lineTo(w * 0.47f, h * 0.71f)
            lineTo(w * 0.60f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.50f)
            close()
        }
        drawPath(path = boltPath, color = tint)
    }
}

@Composable
private fun TopBarBatteryOptimizeButton(
    palette: AdaptivePalette,
    accentColor: Color,
    isActive: Boolean,
    isOptimizing: Boolean,
    onClick: () -> Unit,
    tooltipText: String = "Tối ưu hóa ứng dụng",
    modifier: Modifier = Modifier
) {
    val ringRotation: Float
    if (isOptimizing) {
        val transition = rememberInfiniteTransition(label = "top_battery_opt_spin")
        ringRotation = transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "opt_ring_rotation"
        ).value
    } else {
        ringRotation = 0f
    }

    val idleTilt: Float
    if (!isOptimizing && isActive) {
        val transition = rememberInfiniteTransition(label = "top_battery_idle_tilt")
        idleTilt = transition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "battery_idle_tilt"
        ).value
    } else {
        idleTilt = 0f
    }

    CircleActionTooltipWrapper(
        tooltipText = tooltipText,
        palette = palette,
        accentColor = accentColor,
        enabled = !isOptimizing,
        onClick = onClick,
        modifier = modifier.size(35.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cardBg,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = if (isOptimizing) 0.80f else 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isOptimizing) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        rotate(degrees = ringRotation) {
                            drawArc(
                                color = accentColor,
                                startAngle = 0f,
                                sweepAngle = 115f,
                                useCenter = false,
                                topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
                BatteryOptimizationIconGlyph(
                    tint = accentColor,
                    isAnimated = isOptimizing || isActive,
                    modifier = Modifier
                        .size(19.dp)
                        .graphicsLayer {
                            rotationZ = idleTilt
                        }
                )
            }
        }
    }
}

@Composable
private fun TopBarDayNightIndicator(
    palette: AdaptivePalette,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit = {},
    tooltipText: String = "Chào ngày mới",
    modifier: Modifier = Modifier
) {
    var isDay by remember {
        val h = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).get(Calendar.HOUR_OF_DAY)
        mutableStateOf(isDaytimeWindow(h))
    }

    LaunchedEffect(isActive) {
        if (!isActive) return@LaunchedEffect
        while (true) {
            val h = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).get(Calendar.HOUR_OF_DAY)
            isDay = isDaytimeWindow(h)
            delay(60_000L)
        }
    }

    val moonGoldColor = Color(0xFFFFCA63)
    val cloudIceBlueColor = Color(0xFFB5E3F2)

    val sunSpin: Float
    val moonTilt: Float
    val cloudGlidePx: Float
    val cloudBobPx: Float

    if (isActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "day_night_top_badge")
        sunSpin = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sun_spin"
        ).value
        moonTilt = infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3_600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "moon_tilt"
        ).value
        cloudGlidePx = infiniteTransition.animateFloat(
            initialValue = -2.4f,
            targetValue = 2.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3_200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cloud_glide"
        ).value
        cloudBobPx = infiniteTransition.animateFloat(
            initialValue = -0.8f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2_400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cloud_bob"
        ).value
    } else {
        sunSpin = 0f
        moonTilt = 0f
        cloudGlidePx = 0f
        cloudBobPx = 0f
    }

    CircleActionTooltipWrapper(
        tooltipText = tooltipText,
        palette = palette,
        accentColor = accentColor,
        onClick = onClick,
        modifier = modifier.size(35.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cardBg,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = if (isDay) Color(0xFFFFB74D).copy(alpha = 0.45f) else moonGoldColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isDay) {
                Text(
                    text = "☀️",
                    fontSize = 17.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.rotate(sunSpin * 0.15f)
                )
            } else {
                Canvas(modifier = Modifier.size(27.dp)) {
                    val w = size.width
                    val h = size.height

                    rotate(
                        degrees = moonTilt - 18f,
                        pivot = Offset(w * 0.56f, h * 0.44f)
                    ) {
                        val moonPath = buildCrescentMoonPath(
                            outerCenter = Offset(w * 0.56f, h * 0.44f),
                            outerRadius = w * 0.34f,
                            innerCenter = Offset(w * 0.68f, h * 0.35f),
                            innerRadius = w * 0.28f
                        )
                        drawPath(
                            path = moonPath,
                            color = moonGoldColor
                        )
                    }

                    val dx = cloudGlidePx.dp.toPx()
                    val dy = cloudBobPx.dp.toPx()
                    translate(left = dx, top = dy) {
                        val cloudScale = w / 28f
                        val cloudPath = buildSoftCloudPath(
                            baseX = w * 0.04f,
                            baseY = h * 0.48f,
                            scale = cloudScale
                        )
                        drawPath(
                            path = cloudPath,
                            color = cloudIceBlueColor
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun BottomContactCircleButton(
    palette: AdaptivePalette,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    tooltipText: String = "Thông tin",
    modifier: Modifier = Modifier
) {
    val gentleScale: Float
    val gentleTilt: Float
    if (isActive) {
        val transition = rememberInfiniteTransition(label = "bottom_contact_icon_anim")
        gentleScale = transition.animateFloat(
            initialValue = 0.93f,
            targetValue = 1.07f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1250, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "contact_icon_scale"
        ).value
        gentleTilt = transition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "contact_icon_tilt"
        ).value
    } else {
        gentleScale = 1f
        gentleTilt = 0f
    }

    CircleActionTooltipWrapper(
        tooltipText = tooltipText,
        palette = palette,
        accentColor = accentColor,
        onClick = onClick,
        modifier = modifier.size(36.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cardBg,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Thông tin liên hệ",
                    tint = accentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = gentleScale
                            scaleY = gentleScale
                            rotationZ = gentleTilt
                        }
                )
            }
        }
    }
}

@Composable
private fun BottomDownloadCircleButton(
    palette: AdaptivePalette,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    tooltipText: String = "Tải xuống",
    modifier: Modifier = Modifier
) {
    val bobY: Float
    val pulse: Float
    if (isActive) {
        val transition = rememberInfiniteTransition(label = "bottom_download_icon_anim")
        bobY = transition.animateFloat(
            initialValue = -1.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "download_icon_bob"
        ).value
        pulse = transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "download_icon_pulse"
        ).value
    } else {
        bobY = 0f
        pulse = 1f
    }

    CircleActionTooltipWrapper(
        tooltipText = tooltipText,
        palette = palette,
        accentColor = accentColor,
        onClick = onClick,
        modifier = modifier.size(36.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.cardBg,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Tải về phiên bản mới",
                    tint = accentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            translationY = bobY.dp.toPx()
                            scaleX = pulse
                            scaleY = pulse
                        }
                )
            }
        }
    }
}

private const val APP_RELEASES_URL = "https://github.com/duynghiduc2000-vn/thoi-tiet-am-lich/releases"

private val GREETINGS_MORNING = listOf(
    "Chúc bạn một buổi sáng tràn đầy năng lượng và niềm vui!",
    "Mặt trời lên rồi, dậy thôi nào, thế giới đang chờ bạn tỏa sáng!",
    "Hít thở thật sâu, mỉm cười và đón chào buổi sáng rực rỡ.",
    "Chúc bạn một ngày bình yên, an lành và nhiều niềm vui nhỏ bé.",
    "Chúc bạn ngày mới không nhầm người, không nhầm đường, không nhầm deadline.",
    "Sáng nay trời đẹp lắm, chỉ thiếu động lực thôi.",
    "Dậy đi, ước mơ không tự hoàn thành đâu!",
    "Sáng rồi đó, cố gắng lên… tối còn ngủ tiếp."
)

private val GREETINGS_NOON = listOf(
    "Nghỉ tay ăn trưa thôi, chúc bạn ăn ngon miệng và nạp đầy năng lượng cho buổi chiều hiệu quả.",
    "Chúc bạn có giờ nghỉ trưa thư giãn, ngủ một giấc thật êm để chiều bứt phá công việc.",
    "Buổi trưa vui vẻ nhé! Chúc bạn ăn ngon và giữ tinh thần thật thoải mái.",
    "Trưa rồi, gác lại việc nào, ăn trưa ngon miệng và nạp pin thôi!",
    "Bỏ bữa trưa là \"kẻ thù\" của năng lượng và sức khỏe đó, đi ăn ngay thôi!",
    "Chúc bạn buổi trưa ăn ngon, ngủ ngon, chiều làm việc \"nhẹ như lông hồng\"."
)

private val GREETINGS_AFTERNOON = listOf(
    "Chúc bạn buổi chiều thật vui vẻ và nhiều năng lượng nhé!",
    "Chúc một buổi chiều nhẹ nhàng, bình an và đầy niềm vui!",
    "Chúc buổi chiều vui vẻ, mạnh khỏe, công việc suôn sẻ và vạn sự bình an.",
    "Cười lên rồi đón buổi chiều nào, mong mọi cố gắng hôm nay đều xứng đáng!",
    "Cố gắng lên, xíu nữa thôi là tan làm rồi!",
    "Buổi chiều tốt lành! Chúc công việc của bạn diễn ra thật suôn sẻ và hiệu quả.",
    "Chúc bạn buổi chiều làm việc tập trung, nhiều cảm hứng và đạt mục tiêu đề ra."
)

private val GREETINGS_EVENING = listOf(
    "Chúc bạn có một buổi tối bình yên và gác lại mọi mệt mỏi sau ngày dài.",
    "Buổi tối ấm áp nhé, nhớ ăn uống đầy đủ và nghỉ ngơi sớm.",
    "Mong tối nay của bạn thật nhẹ nhàng, nhiều niềm vui.",
    "Giờ này không ra đường tìm người yêu đi, còn vào đây làm gì 😁",
    "Có phải bạn không có người yêu nên giờ này mới tìm đến mình, phải không? 😁",
    "Phố lên đèn, lòng xuống nhịp. Chúc buổi tối bình an.",
    "Một ly trà, một bản nhạc, một buổi tối dịu êm.",
    "Hôm nay khép lại, dịu dàng nghỉ ngơi thôi."
)

private val GREETINGS_NIGHT = listOf(
    "Chúc bạn ngủ ngon!",
    "Khuya rồi, không ngủ đi còn vào đây làm gì?",
    "Thức khuya làm bạn không còn xinh đẹp nữa đấy, đi ngủ đi!",
    "Cất điện thoại rồi đi ngủ sớm đi nhé, bạn yêu!",
    "Muốn mai trễ làm hay gì mà giờ này còn thức?",
    "Bỏ lại lo âu, ngủ một giấc thật sâu nào.",
    "Chúc bạn đêm an lành và nhiều mơ đẹp.",
    "Ngủ ngon nhé, hẹn gặp lại vào sáng mai.",
    "Người yêu bạn ngủ rồi, bạn cũng ngủ đi thôi? Hay bạn không có người yêu? 😁😁😁",
    "Người yêu đang chờ bạn trong mơ kìa, ngủ đi!!!!!",
    "Xía, bạn không ngủ, thì mình ngủ đây. Bye bye 👋👋👋!"
)

/** Chọn ngẫu nhiên 1 câu chúc theo khung giờ hiện tại (giờ Việt Nam). */
private fun pickRandomGreetingForNow(): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
    val minutesOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val pool = when {
        minutesOfDay in (6 * 60) until (11 * 60) -> GREETINGS_MORNING          // 06:00 - 10:59
        minutesOfDay in (11 * 60) until (14 * 60) -> GREETINGS_NOON            // 11:00 - 13:59
        minutesOfDay in (14 * 60) until (18 * 60) -> GREETINGS_AFTERNOON       // 14:00 - 17:59
        minutesOfDay in (18 * 60) until (21 * 60 + 30) -> GREETINGS_EVENING    // 18:00 - 21:29
        else -> GREETINGS_NIGHT                                                // 21:30 - 05:59
    }
    return pool.random()
}

/** Lưu ảnh mã QR ủng hộ vào thư viện ảnh của máy (Pictures/ThoiTietAmLich). Trả về true nếu thành công. */
private fun saveDonateQrToGallery(context: Context): Boolean {
    return try {
        val drawable = ContextCompat.getDrawable(context, R.drawable.donate_qr) ?: return false
        val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1024
        val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1024
        val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)

        val fileName = "QR_UngHo_ThoiTietAmLich.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = android.content.ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ThoiTietAmLich")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            try {
                val stream = resolver.openOutputStream(uri) ?: throw java.io.IOException("Không mở được luồng ghi")
                stream.use { out ->
                    if (!bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)) {
                        throw java.io.IOException("Không ghi được ảnh")
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
            // Android 9 trở xuống: ghi thẳng vào thư mục Pictures (cần quyền ghi bộ nhớ)
            val dir = java.io.File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ThoiTietAmLich"
            )
            if (!dir.exists() && !dir.mkdirs()) return false
            val file = java.io.File(dir, fileName)
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/png"),
                null
            )
        }
        true
    } catch (e: Exception) {
        false
    }
}

private fun openExternalUrlSafely(context: Context, url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Liên kết", url))
        Toast.makeText(
            context,
            "Không mở được trình duyệt. Đã sao chép liên kết vào bộ nhớ tạm.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun TopWeatherCanvasEffect(
    weatherCode: Int,
    accentColor: Color,
    isDark: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val progress: Float
    if (isActive) {
        val transition = rememberInfiniteTransition(label = "top_weather_canvas")
        progress = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "weather_progress"
        ).value
    } else {
        progress = 0.25f
    }

    val isThunder = weatherCode in listOf(95, 96, 99)
    val isRain = weatherCode in listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82) || isThunder
    val isFog = weatherCode in listOf(45, 48)
    val isCloudy = weatherCode in listOf(1, 2, 3)

    Box(modifier = modifier.clip(RoundedCornerShape(28.dp))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val sparkleCount = 14
            val sparkleColor = if (isDark) {
                accentColor.copy(alpha = 0.20f)
            } else {
                accentColor.copy(alpha = 0.24f)
            }
            for (i in 0 until sparkleCount) {
                val baseX = ((i * 97) % 100) / 100f
                val baseY = ((i * 61 + 13) % 85) / 100f
                val x = (((baseX + progress * (0.18f + (i % 4) * 0.05f)) % 1.15f) - 0.07f) * w
                val wave = sin((progress * 2.0 * PI + i * 0.8)).toFloat()
                val y = (baseY * h + wave * 8.dp.toPx()).coerceIn(6.dp.toPx(), h - 12.dp.toPx())
                val radius = (2.dp.toPx() + (i % 3) * 1.1.dp.toPx()) * (0.75f + 0.35f * wave)
                drawCircle(
                    color = sparkleColor,
                    radius = radius.coerceAtLeast(1.5.dp.toPx()),
                    center = Offset(x, y)
                )
            }

            if (isCloudy || isRain || isFog || weatherCode !in listOf(0)) {
                val cloudColor = if (isDark) {
                    Color.White.copy(alpha = if (isRain) 0.11f else 0.09f)
                } else {
                    Color(0xFF78909C).copy(alpha = if (isRain) 0.18f else 0.15f)
                }
                val cloudRows = listOf(
                    Triple(0.05f, 0.14f, 0.72f),
                    Triple(0.55f, 0.26f, 0.90f),
                    Triple(0.18f, 0.46f, 0.85f),
                    Triple(0.72f, 0.58f, 0.68f),
                    Triple(0.40f, 0.74f, 0.95f)
                )
                cloudRows.forEachIndexed { idx, (startX, yFrac, scale) ->
                    val speed = 0.22f + idx * 0.05f
                    val cx = (((startX + progress * speed) % 1.35f) - 0.18f) * w
                    val cy = yFrac * h
                    val cw = 78.dp.toPx() * scale
                    val ch = 18.dp.toPx() * scale
                    drawRoundRect(
                        color = cloudColor,
                        topLeft = Offset(cx, cy),
                        size = Size(cw, ch),
                        cornerRadius = CornerRadius(ch / 2f, ch / 2f)
                    )
                    drawCircle(
                        color = cloudColor,
                        radius = ch * 0.68f,
                        center = Offset(cx + cw * 0.28f, cy)
                    )
                    drawCircle(
                        color = cloudColor,
                        radius = ch * 0.52f,
                        center = Offset(cx + cw * 0.60f, cy + ch * 0.08f)
                    )
                }
            }

            if (weatherCode in listOf(0, 1, 2)) {
                val sunRayColor = Color(0xFFFFCA28).copy(alpha = if (isDark) 0.11f else 0.15f)
                for (i in 0 until 6) {
                    val shiftX = (((0.12f + i * 0.16f + progress * 0.12f) % 1.1f) - 0.05f) * w
                    val topY = (0.04f + (i % 3) * 0.12f) * h
                    drawLine(
                        color = sunRayColor,
                        start = Offset(shiftX, topY),
                        end = Offset(shiftX - 18.dp.toPx(), topY + 26.dp.toPx()),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            if (isFog) {
                val fogColor = if (isDark) {
                    Color.White.copy(alpha = 0.09f)
                } else {
                    Color.White.copy(alpha = 0.36f)
                }
                for (i in 0 until 6) {
                    val xShift = (((i * 0.22f + progress * (0.2f + i * 0.03f)) % 1.3f) - 0.15f) * w
                    val yPos = h * (0.12f + i * 0.14f)
                    drawRoundRect(
                        color = fogColor,
                        topLeft = Offset(xShift, yPos),
                        size = Size(w * 0.45f, 10.dp.toPx()),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )
                }
            }

            if (isRain) {
                val dropColor = if (isDark) {
                    Color(0xFF81D4FA).copy(alpha = 0.24f)
                } else {
                    Color(0xFF0288D1).copy(alpha = 0.25f)
                }
                val rainCycle = (progress * 4f) % 1f
                val dropCount = 22
                val dropLen = 15.dp.toPx()
                for (i in 0 until dropCount) {
                    val lane = ((i + 0.4f) / dropCount) * w
                    val yOffset = ((i * 59) % 280).toFloat()
                    val y = (yOffset + rainCycle * (h + 90f)) % (h + 50f) - 25f
                    val x = lane - rainCycle * 24.dp.toPx()
                    drawLine(
                        color = dropColor,
                        start = Offset(x, y),
                        end = Offset(x - 5.dp.toPx(), y + dropLen),
                        strokeWidth = 1.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            if (isThunder) {
                val flashWave = sin(progress * 6.0 * PI).toFloat()
                if (flashWave > 0.82f) {
                    val boltColor = Color(0xFFE1BEE7).copy(alpha = if (isDark) 0.26f else 0.22f)
                    val bx = w * 0.72f
                    val by = h * 0.18f
                    drawLine(
                        color = boltColor,
                        start = Offset(bx, by),
                        end = Offset(bx - 10.dp.toPx(), by + 18.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = boltColor,
                        start = Offset(bx - 10.dp.toPx(), by + 18.dp.toPx()),
                        end = Offset(bx + 2.dp.toPx(), by + 18.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = boltColor,
                        start = Offset(bx + 2.dp.toPx(), by + 18.dp.toPx()),
                        end = Offset(bx - 12.dp.toPx(), by + 38.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.76f to Color.Transparent,
                    1.0f to (if (isDark) Color(0xFF09090B) else Color(0xFFF4F5F7))
                )
            )
        }
    }
}

@Composable
private fun LiquidGlassVolumeSlider(
    volume: Float,
    accentColor: Color,
    palette: AdaptivePalette,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val currentOnVolumeChange by rememberUpdatedState(onVolumeChange)
    val clamped = volume.coerceIn(0f, 1f)
    val percent = (clamped * 100f).roundToInt()

    val glassFill = remember(palette.isDark, accentColor) {
        if (palette.isDark) {
            Brush.linearGradient(
                listOf(
                    Color(0xFF283244).copy(alpha = 0.72f),
                    Color(0xFF171C26).copy(alpha = 0.62f),
                    accentColor.copy(alpha = 0.18f),
                    Color(0xFF11151D).copy(alpha = 0.75f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.92f),
                    accentColor.copy(alpha = 0.14f),
                    Color(0xFFEFF4FA).copy(alpha = 0.82f)
                )
            )
        }
    }

    val glassBorder = remember(palette.isDark, accentColor) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (palette.isDark) 0.34f else 0.95f),
                accentColor.copy(alpha = 0.48f),
                Color.White.copy(alpha = if (palette.isDark) 0.12f else 0.65f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(glassFill)
            .border(1.2.dp, glassBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Âm lượng nhạc nền",
                    fontSize = 12.5.sp,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accentColor.copy(alpha = 0.18f),
                    modifier = Modifier.border(
                        width = 0.8.dp,
                        color = accentColor.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(999.dp)
                    )
                ) {
                    Text(
                        text = "$percent%",
                        fontSize = 11.5.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val thumbSize = 20.dp
            val trackHeight = 6.dp

            BoxWithConstraints(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val thumbPx = with(density) { thumbSize.toPx() }
                            val usableWidth = (size.width.toFloat() - thumbPx).coerceAtLeast(1f)
                            val ratio = ((offset.x - thumbPx / 2f) / usableWidth).coerceIn(0f, 1f)
                            currentOnVolumeChange(ratio)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val thumbPx = with(density) { thumbSize.toPx() }
                            val usableWidth = (size.width.toFloat() - thumbPx).coerceAtLeast(1f)
                            val ratio = ((change.position.x - thumbPx / 2f) / usableWidth).coerceIn(0f, 1f)
                            currentOnVolumeChange(ratio)
                        }
                    }
            ) {
                val maxWidthPx = with(density) { maxWidth.toPx() }
                val thumbPx = with(density) { thumbSize.toPx() }
                val usableWidthPx = (maxWidthPx - thumbPx).coerceAtLeast(1f)
                val thumbOffsetPx = (clamped * usableWidthPx).roundToInt()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trackHeight)
                        .clip(CircleShape)
                        .background(
                            if (palette.isDark) Color.White.copy(alpha = 0.14f)
                            else Color.Black.copy(alpha = 0.12f)
                        )
                )

                Box(
                    modifier = Modifier
                        .width(with(density) { (thumbOffsetPx + thumbPx / 2f).toDp() })
                        .height(trackHeight)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.72f),
                                    accentColor
                                )
                            )
                        )
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset { IntOffset(thumbOffsetPx, 0) }
                        .size(thumbSize)
                        .shadow(elevation = 4.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(2.2.dp, Color.White, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.90f))
                    )
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WeatherService.scheduleTwoHourlySync(this)
        BirthdayHelper.runDailyBackgroundReminders(this)
        WeatherLunarWidget.updateAllWidgets(this)
        val initialTasks = TodoHelper.ensureCurrentDayOrReset(this)
        TodoHelper.scheduleAllTodayAlarms(this, initialTasks)
        intent?.getStringExtra(TodoHelper.EXTRA_TASK_ID)?.let { taskId ->
            TodoHelper.setPendingPopupTaskId(this, taskId, commitNow = true)
        }

        setContent {
            ThoiTietAmLichApp()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(TodoHelper.EXTRA_TASK_ID)?.let { taskId ->
            TodoHelper.setPendingPopupTaskId(this, taskId, commitNow = true)
        }
        BirthdayHelper.runDailyBackgroundReminders(this)
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
    isActive: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color
) {
    var clockText by remember(showSeconds) {
        val fmt = SimpleDateFormat(if (showSeconds) "HH:mm:ss" else "HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }
        mutableStateOf(fmt.format(Date()))
    }

    LaunchedEffect(showSeconds, isActive) {
        val fmt = SimpleDateFormat(if (showSeconds) "HH:mm:ss" else "HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }
        clockText = fmt.format(Date())
        if (!isActive) return@LaunchedEffect
        while (true) {
            val nowMs = System.currentTimeMillis()
            val waitMs = if (showSeconds) {
                1_000L - (nowMs % 1_000L).coerceIn(0L, 999L)
            } else {
                60_000L - (nowMs % 60_000L).coerceIn(0L, 59_999L)
            }
            delay(waitMs.coerceAtLeast(250L))
            clockText = fmt.format(Date())
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
    isClockActive: Boolean,
    showWeatherEffect: Boolean,
    showLunarDate: Boolean,
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
                    if (showWeatherEffect) {
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
                        isActive = isClockActive,
                        fontSize = clockSp,
                        color = primaryText
                    )
                }

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

@Composable
private fun AnimatedPopupOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    dismissOnBackdropClick: Boolean = false,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)),
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
                    if (dismissOnBackdropClick) {
                        onDismiss()
                    }
                }
                .padding(horizontal = 24.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    initialScale = 0.80f,
                    animationSpec = spring(
                        dampingRatio = 0.68f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { fullHeight -> fullHeight / 5 } + fadeIn(
                    animationSpec = tween(durationMillis = 190, easing = FastOutSlowInEasing)
                ),
                exit = scaleOut(
                    targetScale = 0.80f,
                    animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing)
                ) + slideOutVertically(
                    animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing)
                ) { fullHeight -> fullHeight / 5 } + fadeOut(
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ThoiTietAmLichApp() {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val baseDensity = LocalDensity.current
    val lockedFontScaleDensity = remember(baseDensity) {
        Density(density = baseDensity.density, fontScale = 1f)
    }

    val nowCal = remember { Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")) }
    val currentSolarYear = remember { nowCal.get(Calendar.YEAR) }
    val allDefaultHolidayIds = remember {
        BirthdayHelper.DEFAULT_VIETNAM_AND_INTERNATIONAL_HOLIDAYS.map { it.id }.toSet()
    }

    var isOptimizingBattery by remember { mutableStateOf(false) }
    var optimizationStepText by remember { mutableStateOf("Đang chuẩn bị tối ưu hóa...") }
    var optimizationProgress by remember { mutableFloatStateOf(0f) }

    var isSearchingFood by remember { mutableStateOf(false) }
    var pendingFoodResult by remember { mutableStateOf<FoodChoiceResult?>(null) }
    var foodResultPopup by remember { mutableStateOf<FoodChoiceResult?>(null) }

    var isRollingLucky by remember { mutableStateOf(false) }
    var rollingPreviewNumber by remember { mutableStateOf("01") }
    var luckyResultPopup by remember { mutableStateOf<DailyLuckyData?>(null) }
    var showLuckyDisclaimerPopup by remember { mutableStateOf(false) }

    var goodBadAdvicePopup by remember { mutableStateOf<GoodBadDayAdvice?>(null) }
    var showGoodBadDisclaimerPopup by remember { mutableStateOf(false) }

    var lunarCustomEvents by remember { mutableStateOf(AppPrefs.getLunarCustomEvents(context)) }
    var customLunarEventTitleInput by remember { mutableStateOf("") }
    var customLunarEventDayInput by remember { mutableStateOf("") }
    var customLunarEventMonthInput by remember { mutableStateOf("") }
    var customLunarEventLeap by remember { mutableStateOf(false) }
    var customLunarEventError by remember { mutableStateOf<String?>(null) }
    var showContactPopup by remember { mutableStateOf(false) }
    var showDonateQrPopup by remember { mutableStateOf(false) }
    var showDownloadPopup by remember { mutableStateOf(false) }
    var showGreetingPopup by remember { mutableStateOf(false) }
    var greetingPopupText by remember { mutableStateOf("") }

    var isTossingCoin by remember { mutableStateOf(false) }
    var coinOutcomePopup by remember { mutableStateOf<CoinTossOutcome?>(null) }

    var dailyMoodState by remember { mutableStateOf(DailyMoodHelper.loadOrResetToday(context)) }
    var showMoodPickerPopup by remember { mutableStateOf(false) }
    var moodConfirmedPopup by remember { mutableStateOf<DailyMoodType?>(null) }
    var moodConfirmedPreviousLabel by remember { mutableStateOf<String?>(null) }

    var appThemeMode by remember { mutableIntStateOf(AppPrefs.getAppThemeMode(context)) }
    var cardBounceEnabled by remember { mutableStateOf(AppPrefs.isCardBounceEnabled(context)) }
    var topWeatherBgEnabled by remember { mutableStateOf(false) }

    var bgMusicEnabled by remember { mutableStateOf(AppPrefs.isBgMusicEnabled(context)) }
    var bgMusicVolume by remember {
        mutableFloatStateOf(
            try {
                AppPrefs.getBgMusicVolume(context)
            } catch (_: Exception) {
                DEFAULT_BG_MUSIC_VOLUME
            }
        )
    }
    var isAppResumed by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun releaseBgMusicPlayer() {
        val current = mediaPlayer
        mediaPlayer = null
        if (current != null) {
            try {
                if (current.isPlaying) {
                    current.stop()
                }
                current.release()
            } catch (_: Exception) {
            }
        }
    }

    fun getOrCreateBgMusicPlayer(): MediaPlayer? {
        mediaPlayer?.let { return it }
        return try {
            MediaPlayer.create(appContext, R.raw.app_bg_music)?.apply {
                isLooping = true
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer = this
            }
        } catch (_: Exception) {
            null
        }
    }

    fun applyBgMusicState() {
        val clampedVol = bgMusicVolume.coerceIn(0f, 1f)
        if (!bgMusicEnabled) {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        mp.pause()
                    }
                } catch (_: IllegalStateException) {
                }
            }
            return
        }

        val mp = getOrCreateBgMusicPlayer() ?: return
        try {
            mp.setVolume(clampedVol, clampedVol)
            if (isAppResumed) {
                if (!mp.isPlaying) {
                    mp.start()
                }
            } else {
                if (mp.isPlaying) {
                    mp.pause()
                }
            }
        } catch (_: IllegalStateException) {
        }
    }

    var widgetBgMode by remember { mutableIntStateOf(AppPrefs.getWidgetBgMode(context)) }
    var accentColorInt by remember { mutableIntStateOf(AppPrefs.getAccentColor(context)) }
    var showSeconds by remember { mutableStateOf(AppPrefs.isShowSeconds(context)) }
    var showSecondsWarningPopup by remember { mutableStateOf(false) }
    var pendingBatteryWarningTarget by remember { mutableStateOf<BatteryWarningTarget?>(null) }
    var showWeatherEffect by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        AppPrefs.setShowWeatherEffect(context, false)
    }
    var showLunarDate by remember { mutableStateOf(AppPrefs.isShowLunarDate(context)) }
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

    var ramMung1ReminderEnabled by remember { mutableStateOf(AppPrefs.isRamMung1ReminderEnabled(context)) }
    var activeRamMung1Alerts by remember {
        mutableStateOf(BirthdayHelper.getActiveRamMung1AlertsForToday(context))
    }
    var showRamMung1Popup by remember { mutableStateOf(false) }

    var feedbackCategory by remember { mutableStateOf(FeedbackCategory.BUG) }
    var feedbackMessageInput by remember { mutableStateOf("") }
    var feedbackError by remember { mutableStateOf<String?>(null) }

    var expandedSection by remember { mutableStateOf<ExpandableSection?>(null) }
    fun toggleSection(section: ExpandableSection) {
        expandedSection = if (expandedSection == section) null else section
    }

    var selectedMainTab by rememberSaveable { mutableStateOf(MainTab.WEATHER) }
    val mainListState = rememberLazyListState()
    LaunchedEffect(selectedMainTab) {
        expandedSection = null
        mainListState.scrollToItem(0)
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
            TodoHelper.saveTasks(context, marked, commitNow = true)
            TodoHelper.setPendingPopupTaskId(context, null, commitNow = true)
            todoTasks = marked
            activeTodoPopup = popupTarget.copy(triggeredToday = true)
        } else if (currentList != todoTasks) {
            todoTasks = currentList
        }
    }

    fun startFoodRandomizer() {
        if (isSearchingFood) return
        val pickedFood = FOOD_AND_DRINK_OPTIONS[Random.nextInt(FOOD_AND_DRINK_OPTIONS.size)]
        val pickedWish = FOOD_ENJOYMENT_WISHES[Random.nextInt(FOOD_ENJOYMENT_WISHES.size)]
        pendingFoodResult = FoodChoiceResult(itemName = pickedFood, wishText = pickedWish)
        foodResultPopup = null
        isSearchingFood = true
    }

    LaunchedEffect(isSearchingFood) {
        if (!isSearchingFood) return@LaunchedEffect
        val durationMs = (2000L..4000L).random()
        delay(durationMs)
        val result = pendingFoodResult
        isSearchingFood = false
        if (result != null) {
            foodResultPopup = result
        }
    }

    // Pop-up tải phiên bản mới: tự tắt sau 2 - 4 giây rồi mở trang tải về
    LaunchedEffect(showDownloadPopup) {
        if (!showDownloadPopup) return@LaunchedEffect
        delay((2000L..4000L).random())
        showDownloadPopup = false
        openExternalUrlSafely(context, APP_RELEASES_URL)
    }

    LaunchedEffect(isTossingCoin) {
        if (!isTossingCoin) return@LaunchedEffect
        delay((2000L..3000L).random())
        val isYes = Random.nextBoolean()
        isTossingCoin = false
        coinOutcomePopup = if (isYes) {
            CoinTossOutcome(
                isYes = true,
                faceLabel = "Có",
                message = "Ý trời rồi, bạn nên làm việc này thôi"
            )
        } else {
            CoinTossOutcome(
                isYes = false,
                faceLabel = "Không",
                message = "Thôi dẹp đi, hôm khác rồi hẵn làm"
            )
        }
    }

    LaunchedEffect(moodConfirmedPopup) {
        if (moodConfirmedPopup == null) return@LaunchedEffect
        delay(2000L)
        moodConfirmedPopup = null
    }

    LaunchedEffect(isRollingLucky) {
        if (!isRollingLucky) return@LaunchedEffect
        val durationMs = (2000L..4000L).random()
        val stepMs = 90L
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < durationMs) {
            rollingPreviewNumber = Random.nextInt(1, 100).toString().padStart(2, '0')
            delay(stepMs)
        }
        val todayLucky = DailyLuckyHelper.getOrCreateTodayLucky(appContext)
        isRollingLucky = false
        luckyResultPopup = todayLucky
    }

    LaunchedEffect(isOptimizingBattery) {
        if (!isOptimizingBattery) return@LaunchedEffect

        optimizationProgress = 0.20f
        optimizationStepText = "Đang rà soát & đồng bộ báo thức việc cần làm..."
        val syncedTasks = TodoHelper.ensureCurrentDayOrReset(appContext)
        TodoHelper.scheduleAllTodayAlarms(appContext, syncedTasks)
        todoTasks = syncedTasks
        delay(500L)

        optimizationProgress = 0.62f
        optimizationStepText = "Đang kiểm tra nhắc nhở ngày mới & lịch âm..."
        BirthdayHelper.runDailyBackgroundReminders(appContext)
        delay(500L)

        optimizationProgress = 0.92f
        optimizationStepText = "Đang đồng bộ trạng thái Widget..."
        WeatherLunarWidget.updateAllWidgets(appContext)
        delay(450L)

        optimizationProgress = 1.0f
        isOptimizingBattery = false
        if (isAppResumed) {
            Toast.makeText(
                appContext,
                "Đã tối ưu hóa pin và ứng dụng",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    isAppResumed = true
                    applyBgMusicState()
                    TodoHelper.ensureChannel(context)
                    dailyMoodState = DailyMoodHelper.loadOrResetToday(context)
                    refreshTodosAndCheckDueTasks()
                    BirthdayHelper.runDailyBackgroundReminders(context)
                }
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    isAppResumed = false
                    applyBgMusicState()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(bgMusicEnabled, bgMusicVolume, isAppResumed) {
        applyBgMusicState()
    }

    DisposableEffect(Unit) {
        onDispose {
            releaseBgMusicPlayer()
        }
    }

    val hasBlockingPopup = remember(
        isOptimizingBattery,
        isSearchingFood,
        foodResultPopup,
        isRollingLucky,
        luckyResultPopup,
        showLuckyDisclaimerPopup,
        goodBadAdvicePopup,
        showGoodBadDisclaimerPopup,
        showContactPopup,
        showDownloadPopup,
        showGreetingPopup,
        isTossingCoin,
        coinOutcomePopup,
        showMoodPickerPopup,
        moodConfirmedPopup,
        showHolidayPopup,
        activeHolidayAlerts,
        showRamMung1Popup,
        activeRamMung1Alerts,
        activeTodoPopup,
        ageResultPopup,
        showAgeHistoryClearedPopup,
        lunarResultPopup,
        showHistoryClearedPopup,
        showSecondsWarningPopup,
        pendingBatteryWarningTarget,
        showBirthdayPopup,
        todayBirthdays,
        showAdminBirthdayToday
    ) {
        isOptimizingBattery ||
            isSearchingFood ||
            foodResultPopup != null ||
            isRollingLucky ||
            luckyResultPopup != null ||
            showLuckyDisclaimerPopup ||
            goodBadAdvicePopup != null ||
            showGoodBadDisclaimerPopup ||
            showContactPopup ||
            showDonateQrPopup ||
            showDownloadPopup ||
            showGreetingPopup ||
            isTossingCoin ||
            coinOutcomePopup != null ||
            showMoodPickerPopup ||
            moodConfirmedPopup != null ||
            (showHolidayPopup && activeHolidayAlerts.isNotEmpty()) ||
            (showRamMung1Popup && activeRamMung1Alerts.isNotEmpty()) ||
            activeTodoPopup != null ||
            ageResultPopup != null ||
            showAgeHistoryClearedPopup ||
            lunarResultPopup != null ||
            showHistoryClearedPopup ||
            showSecondsWarningPopup ||
            pendingBatteryWarningTarget != null ||
            (showBirthdayPopup && (todayBirthdays.isNotEmpty() || showAdminBirthdayToday))
    }

    val canAnimateVisuals = isAppResumed && !hasBlockingPopup

    val previewSevenDays = remember(weather) {
        resolvePreviewSevenDays(context, weather.weatherCode)
    }

    suspend fun syncWeatherData(refreshLocation: Boolean = false) {
        if (isSyncing) return
        isSyncing = true
        try {
            weather = WeatherService.syncWeather(
                context = context,
                refreshLocation = refreshLocation
            )
        } finally {
            isSyncing = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        scope.launch {
            syncWeatherData(refreshLocation = true)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    fun triggerManualWeatherSync() {
        if (isSyncing) return
        val needsLocationPerm = autoLocation &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED

        if (needsLocationPerm) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            scope.launch {
                syncWeatherData(refreshLocation = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        TodoHelper.ensureChannel(context)
        refreshTodosAndCheckDueTasks()

        BirthdayHelper.runDailyBackgroundReminders(context)

        val currentAlerts = BirthdayHelper.getActiveHolidaysForToday(context)
        activeHolidayAlerts = currentAlerts
        if (currentAlerts.isNotEmpty() &&
            AppPrefs.getLastHolidayAlertKey(context) != BirthdayHelper.todayDateKey()
        ) {
            showHolidayPopup = true
        }

        val currentRamMung1Alerts = BirthdayHelper.getActiveRamMung1AlertsForToday(context)
        activeRamMung1Alerts = currentRamMung1Alerts
        if (currentRamMung1Alerts.isNotEmpty() &&
            AppPrefs.getLastRamMung1AlertKey(context) != BirthdayHelper.todayDateKey()
        ) {
            showRamMung1Popup = true
        }

        if (BirthdayHelper.shouldShowAppPopupToday(context)) {
            showBirthdayPopup = true
            BirthdayHelper.markAppPopupShownToday(context)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }

        syncWeatherData(refreshLocation = false)

        while (true) {
            delay(60_000L)
            dailyMoodState = DailyMoodHelper.loadOrResetToday(context)
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
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        MainBottomTabBar(
                            selected = selectedMainTab,
                            onSelect = { selectedMainTab = it },
                            accentColor = accentCompose,
                            palette = palette
                        )
                    },
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = palette.textPrimary,
                                actionIconContentColor = palette.textPrimary
                            ),
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(accentCompose)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Thời tiết & Âm lịch",
                                        color = palette.textPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.3).sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            actions = {
                                var showQuickToolsMenu by remember { mutableStateOf(false) }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Box {
                                        TopBarQuickMenuButton(
                                            palette = palette,
                                            accentColor = accentCompose,
                                            isExpanded = showQuickToolsMenu,
                                            onClick = { showQuickToolsMenu = !showQuickToolsMenu }
                                        )

                                        val quickMenuVisibleState = remember { MutableTransitionState(false) }
                                        quickMenuVisibleState.targetState = showQuickToolsMenu
                                        // Nút cao 35dp + khoảng cách 12dp giữa nút và thẻ
                                        val quickMenuOffsetY = with(LocalDensity.current) { (35.dp + 12.dp).roundToPx() }

                                        if (quickMenuVisibleState.currentState || quickMenuVisibleState.targetState) {
                                            Popup(
                                                alignment = Alignment.TopCenter,
                                                offset = IntOffset(x = 0, y = quickMenuOffsetY),
                                                onDismissRequest = { showQuickToolsMenu = false },
                                                properties = PopupProperties(
                                                    focusable = true,
                                                    dismissOnBackPress = true,
                                                    dismissOnClickOutside = true,
                                                    clippingEnabled = false
                                                )
                                            ) {
                                                androidx.compose.animation.AnimatedVisibility(
                                                    visibleState = quickMenuVisibleState,
                                                    enter = fadeIn(animationSpec = tween(durationMillis = 160)) +
                                                        scaleIn(
                                                            initialScale = 0.55f,
                                                            transformOrigin = TransformOrigin(0.5f, 0f),
                                                            animationSpec = spring(
                                                                dampingRatio = 0.62f,
                                                                stiffness = Spring.StiffnessMediumLow
                                                            )
                                                        ) +
                                                        slideInVertically(
                                                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                                                        ) { fullHeight -> -fullHeight / 5 },
                                                    exit = fadeOut(animationSpec = tween(durationMillis = 130)) +
                                                        scaleOut(
                                                            targetScale = 0.7f,
                                                            transformOrigin = TransformOrigin(0.5f, 0f),
                                                            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                                                        )
                                                ) {
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = Color.Transparent,
                                                    shadowElevation = 14.dp,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(palette.liquidGlassBrush)
                                                        .border(
                                                            width = 1.2.dp,
                                                            brush = palette.liquidGlassBorder,
                                                            shape = RoundedCornerShape(20.dp)
                                                        )
                                                ) {
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.padding(14.dp)
                                                    ) {
                                                        // Hàng 1: Gợi ý món ăn, Số may mắn, Tung đồng xu
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            TopBarFoodChoiceButton(
                                                                palette = palette,
                                                                accentColor = accentCompose,
                                                                isActive = canAnimateVisuals,
                                                                tooltipText = "Gợi ý món ăn",
                                                                onClick = {
                                                                    showQuickToolsMenu = false
                                                                    startFoodRandomizer()
                                                                },
                                                                modifier = Modifier.size(38.dp)
                                                            )
                                                            TopBarLuckyButton(
                                                                palette = palette,
                                                                accentColor = accentCompose,
                                                                isActive = canAnimateVisuals,
                                                                isRolling = isRollingLucky,
                                                                tooltipText = "Số may mắn",
                                                                onClick = {
                                                                    showQuickToolsMenu = false
                                                                    val existingToday = DailyLuckyHelper.getTodayLuckyIfExists(appContext)
                                                                    if (existingToday != null) {
                                                                        luckyResultPopup = existingToday
                                                                    } else if (!isRollingLucky) {
                                                                        rollingPreviewNumber = Random.nextInt(1, 100).toString().padStart(2, '0')
                                                                        isRollingLucky = true
                                                                    }
                                                                },
                                                                modifier = Modifier.size(38.dp)
                                                            )
                                                            Row2CoinTossButton(
                                                                palette = palette,
                                                                accentColor = accentCompose,
                                                                isActive = canAnimateVisuals,
                                                                tooltipText = "Tung đồng xu",
                                                                onClick = {
                                                                    showQuickToolsMenu = false
                                                                    if (!isTossingCoin) {
                                                                        coinOutcomePopup = null
                                                                        isTossingCoin = true
                                                                    }
                                                                },
                                                                modifier = Modifier.size(38.dp)
                                                            )
                                                        }

                                                        // Hàng 2: Tối ưu hóa ứng dụng + 2 ô trống dự phòng
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            TopBarBatteryOptimizeButton(
                                                                palette = palette,
                                                                accentColor = accentCompose,
                                                                isActive = canAnimateVisuals,
                                                                isOptimizing = isOptimizingBattery,
                                                                tooltipText = "Tối ưu hóa ứng dụng",
                                                                onClick = {
                                                                    showQuickToolsMenu = false
                                                                    if (!isOptimizingBattery) {
                                                                        optimizationProgress = 0.05f
                                                                        optimizationStepText = "Đang khởi động tối ưu hóa..."
                                                                        isOptimizingBattery = true
                                                                    }
                                                                },
                                                                modifier = Modifier.size(38.dp)
                                                            )
                                                            Spacer(modifier = Modifier.size(38.dp))
                                                            Spacer(modifier = Modifier.size(38.dp))
                                                        }

                                                        // Hàng 3: 3 ô trống dự phòng (đủ khung lưới 3x3)
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Spacer(modifier = Modifier.size(38.dp))
                                                            Spacer(modifier = Modifier.size(38.dp))
                                                            Spacer(modifier = Modifier.size(38.dp))
                                                        }
                                                    }
                                                }
                                                }
                                            }
                                        }
                                    }

                                    val currentSelectedMood = remember(dailyMoodState) {
                                        if (dailyMoodState.dateKey == DailyMoodHelper.todayKey()) {
                                            DailyMoodType.fromId(dailyMoodState.selectedMoodId)
                                        } else {
                                            null
                                        }
                                    }
                                    Row2MoodButton(
                                        palette = palette,
                                        accentColor = accentCompose,
                                        isActive = canAnimateVisuals,
                                        selectedMood = currentSelectedMood,
                                        tooltipText = "Cảm xúc hiện tại",
                                        onClick = {
                                            showQuickToolsMenu = false
                                            dailyMoodState = DailyMoodHelper.loadOrResetToday(context)
                                            showMoodPickerPopup = true
                                        },
                                        modifier = Modifier.size(35.dp)
                                    )
                                    TopBarDayNightIndicator(
                                        palette = palette,
                                        accentColor = accentCompose,
                                        isActive = canAnimateVisuals,
                                        tooltipText = "Chào ngày mới",
                                        onClick = {
                                            showQuickToolsMenu = false
                                            greetingPopupText = pickRandomGreetingForNow()
                                            showGreetingPopup = true
                                        }
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    LazyColumn(
                        state = mainListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedMainTab == MainTab.WEATHER) {
                        item(key = "top_bento_editorial") {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (topWeatherBgEnabled) {
                                    TopWeatherCanvasEffect(
                                        weatherCode = weather.weatherCode,
                                        accentColor = accentCompose,
                                        isDark = palette.isDark,
                                        isActive = canAnimateVisuals,
                                        modifier = Modifier.matchParentSize()
                                    )
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp, vertical = 4.dp)
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp)
                                    ) {
                                        Text(
                                            solarToday.uppercase(Locale.getDefault()),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = accentCompose,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
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
                                                isClockActive = canAnimateVisuals,
                                                showWeatherEffect = showWeatherEffect,
                                                showLunarDate = showLunarDate,
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
                                            color = Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(palette.liquidGlassBrush)
                                                .border(1.dp, palette.liquidGlassBorder, RoundedCornerShape(20.dp))
                                                .clickable(enabled = !isSyncing) {
                                                    triggerManualWeatherSync()
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
                                            color = Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(palette.liquidGlassBrush)
                                                .border(1.dp, palette.liquidGlassBorder, RoundedCornerShape(20.dp))
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
                        }
                        }

                        if (selectedMainTab == MainTab.SETTINGS) {
                        item(key = "accent_color_section") {
                            CollapsibleSectionCard(
                                title = "Màu chủ đề (Áp dụng App & Widget)",
                                badgeText = selectedAccentName,
                                accentColor = accentCompose,
                                palette = palette,
                                bounceEnabled = cardBounceEnabled,
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
                        }

                        if (selectedMainTab == MainTab.SETTINGS) {
                        item(key = "app_and_widget_appearance_row_section") {
                            val themeLabels = listOf("Hệ thống", "Sáng", "Tối")
                            val currentThemeLabel = themeLabels.getOrElse(appThemeMode) { "Hệ thống" }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CompactDualCard(
                                        title = "Giao diện, Hiệu ứng & Nhạc nền ứng dụng",
                                        badgeText = currentThemeLabel,
                                        accentColor = accentCompose,
                                        palette = palette,
                                        bounceEnabled = cardBounceEnabled,
                                        expanded = expandedSection == ExpandableSection.APP_APPEARANCE_AUDIO,
                                        onToggle = { toggleSection(ExpandableSection.APP_APPEARANCE_AUDIO) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    CompactDualCard(
                                        title = "Giao diện, đồng hồ và nền Widget",
                                        badgeText = if (showSeconds) "HH:mm:ss" else "HH:mm",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        bounceEnabled = cardBounceEnabled,
                                        expanded = expandedSection == ExpandableSection.CLOCK_THEME_BG,
                                        onToggle = { toggleSection(ExpandableSection.CLOCK_THEME_BG) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = expandedSection == ExpandableSection.APP_APPEARANCE_AUDIO,
                                    enter = fadeIn(tween(140)) + slideInVertically(animationSpec = tween(140)) { it / 14 },
                                    exit = fadeOut(tween(100))
                                ) {
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
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                            ) {
                                                Text(
                                                    "Nền ứng dụng",
                                                    fontSize = 13.5.sp,
                                                    color = palette.textPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    themeLabels.forEachIndexed { idx, label ->
                                                        val selected = appThemeMode == idx
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
                                                                appThemeMode = idx
                                                                AppPrefs.setAppThemeMode(context, idx)
                                                            },
                                                            label = {
                                                                Text(
                                                                    label,
                                                                    fontSize = 12.sp,
                                                                    maxLines = 1,
                                                                    softWrap = false,
                                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                                )
                                                            },
                                                            modifier = Modifier.height(34.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            SectionHairline(palette)

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                            ) {
                                                Text(
                                                    "Hiệu ứng nảy khi bấm vào các thẻ",
                                                    fontSize = 13.5.sp,
                                                    color = palette.textPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Clip,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clipToBounds()
                                                        .basicMarquee(
                                                            iterations = Int.MAX_VALUE,
                                                            repeatDelayMillis = 1200,
                                                            velocity = 24.dp
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Switch(
                                                    checked = cardBounceEnabled,
                                                    colors = SwitchDefaults.colors(checkedTrackColor = accentCompose),
                                                    onCheckedChange = { checked ->
                                                        if (checked) {
                                                            pendingBatteryWarningTarget = BatteryWarningTarget.CARD_BOUNCE
                                                        } else {
                                                            cardBounceEnabled = false
                                                            AppPrefs.setCardBounceEnabled(context, false)
                                                        }
                                                    }
                                                )
                                            }

                                            SectionHairline(palette)

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                            ) {
                                                Text(
                                                    "Hiệu ứng thời tiết trong nền ứng dụng",
                                                    fontSize = 13.5.sp,
                                                    color = palette.textPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Clip,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clipToBounds()
                                                        .basicMarquee(
                                                            iterations = Int.MAX_VALUE,
                                                            repeatDelayMillis = 1200,
                                                            velocity = 24.dp
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Switch(
                                                    checked = topWeatherBgEnabled,
                                                    colors = SwitchDefaults.colors(checkedTrackColor = accentCompose),
                                                    onCheckedChange = { checked ->
                                                        if (checked) {
                                                            pendingBatteryWarningTarget = BatteryWarningTarget.TOP_WEATHER_BG
                                                        } else {
                                                            topWeatherBgEnabled = false
                                                        }
                                                    }
                                                )
                                            }

                                            SectionHairline(palette)

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                            ) {
                                                Text(
                                                    "Nhạc nền trong ứng dụng",
                                                    fontSize = 13.5.sp,
                                                    color = palette.textPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Switch(
                                                    checked = bgMusicEnabled,
                                                    colors = SwitchDefaults.colors(checkedTrackColor = accentCompose),
                                                    onCheckedChange = { checked ->
                                                        if (checked) {
                                                            pendingBatteryWarningTarget = BatteryWarningTarget.BG_MUSIC
                                                        } else {
                                                            bgMusicEnabled = false
                                                            AppPrefs.setBgMusicEnabled(context, false)
                                                        }
                                                    }
                                                )
                                            }

                                            if (bgMusicEnabled) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                LiquidGlassVolumeSlider(
                                                    volume = bgMusicVolume,
                                                    accentColor = accentCompose,
                                                    palette = palette,
                                                    onVolumeChange = { v ->
                                                        val clamped = v.coerceIn(0f, 1f)
                                                        bgMusicVolume = clamped
                                                        try {
                                                            AppPrefs.setBgMusicVolume(context, clamped)
                                                        } catch (_: Exception) {
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                AnimatedVisibility(
                                    visible = expandedSection == ExpandableSection.CLOCK_THEME_BG,
                                    enter = fadeIn(tween(140)) + slideInVertically(animationSpec = tween(140)) { it / 14 },
                                    exit = fadeOut(tween(100))
                                ) {
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
                                }
                            }
                        }
                        }

                        if (selectedMainTab == MainTab.WEATHER) {
                        item(key = "location_and_signature_section") {
                            CollapsibleSectionCard(
                                title = "Vị trí cập nhật thời tiết & Chữ ký trên Widget",
                                badgeText = if (autoLocation) "GPS" else "Tùy chọn",
                                accentColor = accentCompose,
                                palette = palette,
                                bounceEnabled = cardBounceEnabled,
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
                                            syncWeatherData(refreshLocation = false)
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
                                                        syncWeatherData(refreshLocation = false)
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
                        }

                        if (selectedMainTab == MainTab.SETTINGS) {
                        item(key = "font_scale_section") {
                            CollapsibleSectionCard(
                                title = "Kích cỡ chữ trên Widget",
                                badgeText = "1 – 20",
                                accentColor = accentCompose,
                                palette = palette,
                                bounceEnabled = cardBounceEnabled,
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
                        }

                        if (selectedMainTab == MainTab.REMINDERS) {
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
                                    bounceEnabled = cardBounceEnabled,
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
                                            TodoHelper.saveTasks(context, updated, commitNow = true)
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
                                                            TodoHelper.saveTasks(context, updated, commitNow = true)
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
                        }

                        if (selectedMainTab == MainTab.CALENDAR) {
                        item(key = "lunar_and_age_row_section") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                        bounceEnabled = cardBounceEnabled,
                                        expanded = expandedSection == ExpandableSection.LUNAR_LOOKUP,
                                        onToggle = { toggleSection(ExpandableSection.LUNAR_LOOKUP) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    CompactDualCard(
                                        title = "Tính tuổi",
                                        badgeText = "${ageHistory.size} lưu",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        bounceEnabled = cardBounceEnabled,
                                        expanded = expandedSection == ExpandableSection.AGE_CALC,
                                        onToggle = { toggleSection(ExpandableSection.AGE_CALC) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = expandedSection == ExpandableSection.LUNAR_LOOKUP,
                                    enter = fadeIn(tween(140)) + slideInVertically(animationSpec = tween(140)) { it / 14 },
                                    exit = fadeOut(tween(100))
                                ) {
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

                                AnimatedVisibility(
                                    visible = expandedSection == ExpandableSection.AGE_CALC,
                                    enter = fadeIn(tween(140)) + slideInVertically(animationSpec = tween(140)) { it / 14 },
                                    exit = fadeOut(tween(100))
                                ) {
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
                        }

                        if (selectedMainTab == MainTab.CALENDAR) {
                        item(key = "good_bad_day_section") {
                            GoodBadDayExpandableSection(
                                expanded = expandedSection == ExpandableSection.GOOD_BAD_DAY,
                                onToggle = { toggleSection(ExpandableSection.GOOD_BAD_DAY) },
                                accentColor = accentCompose,
                                palette = palette,
                                bounceEnabled = cardBounceEnabled,
                                onShowAdvicePopup = { advice -> goodBadAdvicePopup = advice }
                            )
                        }
                        }

                        if (selectedMainTab == MainTab.CALENDAR) {
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
                                    bounceEnabled = cardBounceEnabled,
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
                        }

                        if (selectedMainTab == MainTab.REMINDERS) {
                        item(key = "birthday_and_holiday_row_section") {
                            val activeHolidayCount = if (holidayReminderEnabled) enabledHolidayIds.size else 0

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                        bounceEnabled = cardBounceEnabled,
                                        expanded = expandedSection == ExpandableSection.BIRTHDAY,
                                        onToggle = { toggleSection(ExpandableSection.BIRTHDAY) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    CompactDualCard(
                                        title = "Nhắc ngày lễ & kỷ niệm",
                                        badgeText = if (holidayReminderEnabled) "$activeHolidayCount bật" else "Tắt",
                                        accentColor = accentCompose,
                                        palette = palette,
                                        bounceEnabled = cardBounceEnabled,
                                        expanded = expandedSection == ExpandableSection.HOLIDAY,
                                        onToggle = { toggleSection(ExpandableSection.HOLIDAY) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = expandedSection == ExpandableSection.BIRTHDAY,
                                    enter = fadeIn(tween(140)) + slideInVertically(animationSpec = tween(140)) { it / 14 },
                                    exit = fadeOut(tween(100))
                                ) {
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

                                AnimatedVisibility(
                                    visible = expandedSection == ExpandableSection.HOLIDAY,
                                    enter = fadeIn(tween(140)) + slideInVertically(animationSpec = tween(140)) { it / 14 },
                                    exit = fadeOut(tween(100))
                                ) {
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
                                                title = "Nhắc ngày Rằm & Mùng 1",
                                                subtitle = if (ramMung1ReminderEnabled)
                                                    "Đang bật nhắc ngày Rằm (15) & Mùng 1 Âm lịch (trước 1 ngày & đúng ngày)"
                                                else
                                                    "Gạt bật để nhận thông báo & Pop-up trước 1 ngày và đúng ngày Rằm, Mùng 1",
                                                checked = ramMung1ReminderEnabled,
                                                accentColor = accentCompose,
                                                palette = palette,
                                                onCheckedChange = { checked ->
                                                    ramMung1ReminderEnabled = checked
                                                    AppPrefs.setRamMung1ReminderEnabled(context, checked)
                                                    if (checked) {
                                                        BirthdayHelper.notifyTodayRamMung1OncePerDay(context)
                                                    }
                                                    activeRamMung1Alerts = BirthdayHelper.getActiveRamMung1AlertsForToday(context)
                                                    if (checked && activeRamMung1Alerts.isNotEmpty()) {
                                                        showRamMung1Popup = true
                                                    }
                                                }
                                            )

                                            SectionHairline(palette)
                                            Spacer(modifier = Modifier.height(10.dp))

                                            LunarCustomEventsManagerCard(
                                                events = lunarCustomEvents,
                                                onEventsChange = { lunarCustomEvents = it },
                                                titleInput = customLunarEventTitleInput,
                                                onTitleInputChange = { customLunarEventTitleInput = it },
                                                dayInput = customLunarEventDayInput,
                                                onDayInputChange = { customLunarEventDayInput = it },
                                                monthInput = customLunarEventMonthInput,
                                                onMonthInputChange = { customLunarEventMonthInput = it },
                                                isLeapMonth = customLunarEventLeap,
                                                onLeapMonthChange = { customLunarEventLeap = it },
                                                errorText = customLunarEventError,
                                                onErrorChange = { customLunarEventError = it },
                                                accentColor = accentCompose,
                                                palette = palette,
                                                scope = scope,
                                                snackbarHostState = snackbarHostState,
                                                onPersist = { updated ->
                                                    AppPrefs.saveLunarCustomEvents(context, updated)
                                                    activeHolidayAlerts = BirthdayHelper.getActiveHolidaysForToday(context)
                                                }
                                            )

                                            SectionHairline(palette)
                                            Spacer(modifier = Modifier.height(10.dp))

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
                        }

                        if (selectedMainTab == MainTab.SETTINGS) {
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
                                    bounceEnabled = cardBounceEnabled,
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
                        }

                        if (selectedMainTab == MainTab.SETTINGS) {
                        item(key = "bottom_contact_button_row") {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 8.dp)
                            ) {
                                BottomDownloadCircleButton(
                                    palette = palette,
                                    accentColor = accentCompose,
                                    isActive = canAnimateVisuals,
                                    tooltipText = "Tải xuống",
                                    onClick = { showDownloadPopup = true }
                                )
                                BottomContactCircleButton(
                                    palette = palette,
                                    accentColor = accentCompose,
                                    isActive = canAnimateVisuals,
                                    tooltipText = "Thông tin",
                                    onClick = { showContactPopup = true }
                                )
                            }
                        }
                        }
                    }
                }

                // Pop-up tải xuống phiên bản mới (tự tắt sau 2 - 4 giây rồi mở trang tải về)
                AnimatedPopupOverlay(
                    visible = showDownloadPopup,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val dlTransition = rememberInfiniteTransition(label = "download_popup_anim")
                    val arrowBob by dlTransition.animateFloat(
                        initialValue = -6f,
                        targetValue = 8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "download_arrow_bob"
                    )
                    val ringScale by dlTransition.animateFloat(
                        initialValue = 0.92f,
                        targetValue = 1.18f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "download_ring_scale"
                    )
                    val ringAlpha by dlTransition.animateFloat(
                        initialValue = 0.55f,
                        targetValue = 0.12f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "download_ring_alpha"
                    )

                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Tải xuống phiên bản mới",
                                style = MaterialTheme.typography.headlineSmall,
                                color = accentCompose,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(124.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(104.dp)
                                        .graphicsLayer {
                                            scaleX = ringScale
                                            scaleY = ringScale
                                            alpha = ringAlpha
                                        }
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.30f))
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(2.dp, accentCompose.copy(alpha = 0.65f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = accentCompose,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .graphicsLayer {
                                                translationY = arrowBob.dp.toPx()
                                            }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Đang dẫn đến trang tải về...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.popupTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Pop-up câu chúc ngẫu nhiên theo khung giờ + nút Ok
                AnimatedPopupOverlay(
                    visible = showGreetingPopup,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = greetingPopupText,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.popupTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { showGreetingPopup = false },
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
                                    text = "Ok",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Pop-up đang tung đồng xu (2 - 3 giây)
                AnimatedPopupOverlay(
                    visible = isTossingCoin,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val tossTransition = rememberInfiniteTransition(label = "coin_toss_popup_anim")
                    val tossY by tossTransition.animateFloat(
                        initialValue = 10f,
                        targetValue = -22f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 460, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "coin_toss_y"
                    )
                    val tossScaleX by tossTransition.animateFloat(
                        initialValue = 0.25f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "coin_toss_flip_x"
                    )
                    val tossTilt by tossTransition.animateFloat(
                        initialValue = -16f,
                        targetValue = 16f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 460, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "coin_toss_tilt"
                    )

                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Đang tung đồng xu",
                                style = MaterialTheme.typography.headlineSmall,
                                color = accentCompose,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(96.dp)
                                    .graphicsLayer {
                                        translationY = tossY.dp.toPx()
                                        scaleX = tossScaleX
                                        rotationZ = tossTilt
                                    }
                                    .clip(CircleShape)
                                    .background(accentCompose.copy(alpha = 0.18f))
                                    .border(2.5.dp, accentCompose, CircleShape)
                            ) {
                                Text(
                                    text = "🪙",
                                    fontSize = 42.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(modifier = Modifier.height(22.dp))
                        }
                    }
                }

                // Pop-up kết quả tung đồng xu (Có / Không)
                AnimatedPopupOverlay(
                    visible = coinOutcomePopup != null,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val outcome = coinOutcomePopup
                    LiquidGlassPopupSurface(palette = palette) {
                        if (outcome != null) {
                            val badgeColor = if (outcome.isYes) accentCompose else Color(0xFFFF8A65)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(104.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor.copy(alpha = 0.18f))
                                        .border(3.dp, badgeColor, CircleShape)
                                ) {
                                    Text(
                                        text = outcome.faceLabel,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = palette.popupTextPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = outcome.message,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = { coinOutcomePopup = null },
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
                                        text = "Ok",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Pop-up chọn cảm xúc trong ngày (8 ô nhỏ cảm xúc động + Đóng)
                AnimatedPopupOverlay(
                    visible = showMoodPickerPopup,
                    onDismiss = { showMoodPickerPopup = false },
                    dismissOnBackdropClick = false
                ) {
                    val pickerTitle = "Cảm xúc của bạn hiện tại là"

                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = pickerTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.popupTextPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val moodRows = remember { DailyMoodType.entries.chunked(6) }
                            val sharedMoodScrollState = rememberScrollState()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(sharedMoodScrollState)
                                    .padding(horizontal = 2.dp)
                            ) {
                                val colCount = moodRows.maxOfOrNull { it.size } ?: 0
                                for (colIdx in 0 until colCount) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        moodRows.forEach { rowItems ->
                                            val moodItem = rowItems.getOrNull(colIdx)
                                            if (moodItem != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = palette.popupBoxBg,
                                                    modifier = Modifier
                                                        .width(86.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .border(
                                                            1.dp,
                                                            accentCompose.copy(alpha = 0.35f),
                                                            RoundedCornerShape(16.dp)
                                                        )
                                                        .clickable {
                                                            val freshBeforePick = DailyMoodHelper.loadOrResetToday(context)
                                                            val prevBeforePick = DailyMoodType.fromId(freshBeforePick.selectedMoodId)?.label
                                                            val updatedState = DailyMoodHelper.selectMoodForToday(
                                                                context = context,
                                                                newMood = moodItem
                                                            )
                                                            dailyMoodState = updatedState
                                                            showMoodPickerPopup = false
                                                            moodConfirmedPreviousLabel = prevBeforePick
                                                            moodConfirmedPopup = moodItem
                                                        }
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 12.dp, horizontal = 6.dp)
                                                    ) {
                                                        AnimatedMoodIcon(
                                                            mood = moodItem,
                                                            isActive = true,
                                                            fontSize = 26.sp
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = moodItem.label,
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = palette.popupTextPrimary,
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            overflow = TextOverflow.Clip,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clipToBounds()
                                                                .basicMarquee(
                                                                    iterations = Int.MAX_VALUE,
                                                                    repeatDelayMillis = 900,
                                                                    velocity = 24.dp
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedButton(
                                onClick = { showMoodPickerPopup = false },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "Đóng",
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Pop-up thông báo cảm xúc hiện tại (tự tắt sau ~2 giây)
                AnimatedPopupOverlay(
                    visible = moodConfirmedPopup != null,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val chosenMood = moodConfirmedPopup
                    val prevLabel = moodConfirmedPreviousLabel
                    LiquidGlassPopupSurface(palette = palette) {
                        if (chosenMood != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                if (prevLabel != null) {
                                    val annotatedTitle = buildAnnotatedString {
                                        append("Cảm xúc của bạn trước đó là ")
                                        withStyle(
                                            style = SpanStyle(
                                                color = accentCompose,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        ) {
                                            append(prevLabel)
                                        }
                                        append(". Hiện tại bạn cảm thấy:")
                                    }
                                    Text(
                                        text = annotatedTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.popupTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "Cảm xúc hiện tại của bạn là",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.popupTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(2.dp, accentCompose.copy(alpha = 0.65f), CircleShape)
                                ) {
                                    AnimatedMoodIcon(
                                        mood = chosenMood,
                                        isActive = true,
                                        fontSize = 42.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = chosenMood.label,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = accentCompose,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Pop-up tìm món ăn / thức uống (2 - 4 giây)
                AnimatedPopupOverlay(
                    visible = isSearchingFood,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val searchTransition = rememberInfiniteTransition(label = "food_search_question_anim")
                    val qScale by searchTransition.animateFloat(
                        initialValue = 0.90f,
                        targetValue = 1.12f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "food_q_scale"
                    )
                    val qTilt by searchTransition.animateFloat(
                        initialValue = -14f,
                        targetValue = 14f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "food_q_tilt"
                    )

                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Bây giờ nên ăn gì nhỉ?",
                                style = MaterialTheme.typography.headlineSmall,
                                color = accentCompose,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(accentCompose.copy(alpha = 0.16f))
                                    .border(2.dp, accentCompose.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Text(
                                    text = "?",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentCompose,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = qScale
                                        scaleY = qScale
                                        rotationZ = qTilt
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Đang tìm món ăn/thức uống cho bạn ...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.popupTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Pop-up kết quả món ăn / thức uống + lời chúc + nút Ok
                AnimatedPopupOverlay(
                    visible = foodResultPopup != null,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val foodData = foodResultPopup
                    LiquidGlassPopupSurface(palette = palette) {
                        if (foodData != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Text(
                                    text = "Bây giờ mình ăn/uống:",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = accentCompose,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = palette.popupBoxBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 16.dp)
                                    ) {
                                        Text(
                                            text = foodData.itemName,
                                            fontSize = 26.sp,
                                            lineHeight = 32.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = palette.popupTextPrimary,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clipToBounds()
                                                .basicMarquee(
                                                    iterations = Int.MAX_VALUE,
                                                    repeatDelayMillis = 900,
                                                    velocity = 30.dp
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = foodData.wishText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.popupTextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { foodResultPopup = null },
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
                                        text = "Ok",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Pop-up Thông tin liên hệ
                AnimatedPopupOverlay(
                    visible = showContactPopup,
                    onDismiss = { showContactPopup = false },
                    dismissOnBackdropClick = false
                ) {
                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Thông tin liên hệ",
                                style = MaterialTheme.typography.headlineSmall,
                                color = palette.popupTextPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Mr.Wind",
                                fontSize = 30.sp,
                                lineHeight = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentCompose,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(palette.liquidGlassBrush)
                                        .border(1.1.dp, palette.liquidGlassBorder, RoundedCornerShape(16.dp))
                                        .clickable {
                                            openExternalUrlSafely(context, CONTACT_FACEBOOK_URL)
                                        }
                                        .padding(horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = "Facebook",
                                        fontSize = 12.sp,
                                        letterSpacing = (-0.2).sp,
                                        color = palette.popupTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(palette.liquidGlassBrush)
                                        .border(1.1.dp, palette.liquidGlassBorder, RoundedCornerShape(16.dp))
                                        .clickable {
                                            openExternalUrlSafely(context, CONTACT_XIAOMI_COMMUNITY_URL)
                                        }
                                        .padding(horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = "Xiaomi Community",
                                        fontSize = 12.sp,
                                        letterSpacing = (-0.2).sp,
                                        color = palette.popupTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(palette.liquidGlassBrush)
                                    .border(1.1.dp, palette.liquidGlassBorder, RoundedCornerShape(16.dp))
                                    .clickable {
                                        showContactPopup = false
                                        showDonateQrPopup = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = CONTACT_DONATE_MBBANK_TEXT,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { showContactPopup = false },
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
                                    text = "Ok",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Pop-up mã QR & thông tin chuyển khoản khi bấm vào mục ủng hộ
                AnimatedPopupOverlay(
                    visible = showDonateQrPopup,
                    onDismiss = { showDonateQrPopup = false },
                    dismissOnBackdropClick = false
                ) {
                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(22.dp)
                        ) {
                            Text(
                                text = "Ủng hộ qua mã QR",
                                style = MaterialTheme.typography.headlineSmall,
                                color = palette.popupTextPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(210.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, accentCompose.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                                    .padding(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.donate_qr),
                                    contentDescription = "Mã QR chuyển khoản MBBank",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(palette.liquidGlassBrush)
                                    .border(1.1.dp, palette.liquidGlassBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 14.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "NGUYEN PHUOC DUY",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.popupTextPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "3231121997",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = accentCompose,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            clipboard?.setPrimaryClip(ClipData.newPlainText("Số tài khoản MBBank", "3231121997"))
                                            Toast.makeText(
                                                context,
                                                "Đã sao chép số tài khoản 3231121997",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Sao chép STK",
                                            color = palette.popupTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    // Nút nhỏ: tải ảnh mã QR về máy
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                val ok = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                    saveDonateQrToGallery(context)
                                                }
                                                Toast.makeText(
                                                    context,
                                                    if (ok) "Đã lưu mã QR vào thư viện ảnh" else "Không lưu được mã QR, vui lòng thử lại",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(width = 46.dp, height = 40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Tải mã QR về máy",
                                            tint = accentCompose,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { showDonateQrPopup = false },
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
                                    text = "Đóng",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Pop-up 1: Đang tìm số may mắn cho bạn... (2 - 4 giây, lần đầu trong ngày)
                AnimatedPopupOverlay(
                    visible = isRollingLucky,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val rollTransition = rememberInfiniteTransition(label = "lucky_roll_halo")
                    val rollAngle by rollTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1100, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "lucky_roll_angle"
                    )

                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(92.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = accentCompose.copy(alpha = 0.14f),
                                        radius = size.minDimension * 0.46f
                                    )
                                    rotate(degrees = rollAngle) {
                                        drawArc(
                                            color = accentCompose,
                                            startAngle = 0f,
                                            sweepAngle = 145f,
                                            useCenter = false,
                                            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                                            size = Size(
                                                size.width - 6.dp.toPx(),
                                                size.height - 6.dp.toPx()
                                            ),
                                            style = Stroke(
                                                width = 3.dp.toPx(),
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.5.dp, accentCompose.copy(alpha = 0.55f), CircleShape)
                                ) {
                                    Text(
                                        text = rollingPreviewNumber,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentCompose,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Đang tìm số may mắn cho bạn...",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.popupTextPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Pop-up 2: Kết quả theo bản phác thảo (Số may mắn + Màu sắc chạy chữ + Việc nên làm + Đồng ý)
                AnimatedPopupOverlay(
                    visible = luckyResultPopup != null,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val luckyData = luckyResultPopup
                    LiquidGlassPopupSurface(palette = palette) {
                        if (luckyData != null) {
                            val luckyColor = Color(luckyData.colorHex)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Text(
                                    text = "Số may mắn hôm nay là",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.popupTextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(94.dp)
                                        .clip(CircleShape)
                                        .background(luckyColor.copy(alpha = 0.18f))
                                        .border(2.5.dp, luckyColor, CircleShape)
                                ) {
                                    Text(
                                        text = luckyData.numberText,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = palette.popupTextPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = palette.popupBoxBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Màu sắc may mắn:",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = palette.popupTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(luckyColor)
                                                    .border(
                                                        1.dp,
                                                        palette.popupTextPrimary.copy(alpha = 0.35f),
                                                        CircleShape
                                                    )
                                            )
                                            Text(
                                                text = luckyData.colorName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = palette.popupTextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Clip,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clipToBounds()
                                                    .basicMarquee(
                                                        iterations = Int.MAX_VALUE,
                                                        repeatDelayMillis = 900,
                                                        velocity = 28.dp
                                                    )
                                            )
                                        }

                                        Text(
                                            text = "Việc nên làm:",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = palette.popupTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )

                                        luckyData.tasks.forEach { taskText ->
                                            Text(
                                                text = "• $taskText",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = palette.popupTextSecondary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        luckyResultPopup = null
                                        showLuckyDisclaimerPopup = true
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
                                        text = "Đồng ý",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Pop-up 3: Lưu ý ngẫu nhiên + nút Ok
                AnimatedPopupOverlay(
                    visible = showLuckyDisclaimerPopup,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(22.dp)
                        ) {
                            Text(
                                text = "Đây chỉ là thông tin ngẫu nhiên, đừng quá tin vào nó nhé!",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.popupTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { showLuckyDisclaimerPopup = false },
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
                                    text = "Ok",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                AnimatedPopupOverlay(
                    visible = isOptimizingBattery,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val animTransition = rememberInfiniteTransition(label = "liquid_battery_opt_popup")
                    val haloAngle by animTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "halo_angle"
                    )
                    val animatedProgress by animateFloatAsState(
                        targetValue = optimizationProgress.coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                        label = "opt_progress_bar"
                    )

                    LiquidGlassPopupSurface(palette = palette) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = accentCompose.copy(alpha = 0.14f),
                                        radius = size.minDimension * 0.46f
                                    )
                                    rotate(degrees = haloAngle) {
                                        drawArc(
                                            color = accentCompose,
                                            startAngle = 0f,
                                            sweepAngle = 140f,
                                            useCenter = false,
                                            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                                            size = Size(
                                                size.width - 6.dp.toPx(),
                                                size.height - 6.dp.toPx()
                                            ),
                                            style = Stroke(
                                                width = 3.dp.toPx(),
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(accentCompose.copy(alpha = 0.16f))
                                        .border(1.2.dp, accentCompose.copy(alpha = 0.45f), CircleShape)
                                ) {
                                    BatteryOptimizationIconGlyph(
                                        tint = accentCompose,
                                        isAnimated = true,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Đang tối ưu hóa pin và ứng dụng...",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.popupTextPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                optimizationStepText,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.popupTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                color = accentCompose,
                                trackColor = palette.popupBoxBg,
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }

                AnimatedPopupOverlay(
                    visible = showHolidayPopup && activeHolidayAlerts.isNotEmpty(),
                    onDismiss = {
                        BirthdayHelper.markAppPopupShownToday(context)
                        showHolidayPopup = false
                    },
                    dismissOnBackdropClick = true
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

                GoodBadDayDualPopups(
                    advicePopup = goodBadAdvicePopup,
                    showDisclaimerPopup = showGoodBadDisclaimerPopup,
                    onCloseAdviceAndOpenDisclaimer = {
                        goodBadAdvicePopup = null
                        showGoodBadDisclaimerPopup = true
                    },
                    onCloseDisclaimer = { showGoodBadDisclaimerPopup = false },
                    accentColor = accentCompose,
                    palette = palette
                )

                AnimatedPopupOverlay(
                    visible = showRamMung1Popup && activeRamMung1Alerts.isNotEmpty(),
                    onDismiss = {
                        BirthdayHelper.markRamMung1AlertHandledToday(context)
                        showRamMung1Popup = false
                    },
                    dismissOnBackdropClick = true
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
                                        BirthdayHelper.markRamMung1AlertHandledToday(context)
                                        showRamMung1Popup = false
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
                                "Nhắc ngày Rằm & Mùng 1",
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
                                activeRamMung1Alerts.forEach { alert ->
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
                                    BirthdayHelper.markRamMung1AlertHandledToday(context)
                                    showRamMung1Popup = false
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

                AnimatedPopupOverlay(
                    visible = activeTodoPopup != null,
                    onDismiss = {},
                    dismissOnBackdropClick = false
                ) {
                    val todoData = activeTodoPopup
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

                AnimatedPopupOverlay(
                    visible = ageResultPopup != null,
                    onDismiss = { ageResultPopup = null },
                    dismissOnBackdropClick = true
                ) {
                    val ageData = ageResultPopup
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

                AnimatedPopupOverlay(
                    visible = showAgeHistoryClearedPopup,
                    onDismiss = { showAgeHistoryClearedPopup = false },
                    dismissOnBackdropClick = true
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

                AnimatedPopupOverlay(
                    visible = lunarResultPopup != null,
                    onDismiss = { lunarResultPopup = null },
                    dismissOnBackdropClick = true
                ) {
                    val data = lunarResultPopup
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

                AnimatedPopupOverlay(
                    visible = showHistoryClearedPopup,
                    onDismiss = { showHistoryClearedPopup = false },
                    dismissOnBackdropClick = true
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

                AnimatedPopupOverlay(
                    visible = showSecondsWarningPopup || pendingBatteryWarningTarget != null,
                    onDismiss = {
                        showSecondsWarningPopup = false
                        pendingBatteryWarningTarget = null
                    },
                    dismissOnBackdropClick = true
                ) {
                    val closeBatteryWarningPopup = {
                        showSecondsWarningPopup = false
                        pendingBatteryWarningTarget = null
                    }
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
                                    onClick = closeBatteryWarningPopup,
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
                                    onClick = closeBatteryWarningPopup,
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
                                        when {
                                            showSecondsWarningPopup -> {
                                                showSeconds = true
                                                AppPrefs.setShowSeconds(context, true)
                                                WeatherLunarWidget.updateAllWidgets(context)
                                            }
                                            pendingBatteryWarningTarget == BatteryWarningTarget.TOP_WEATHER_BG -> {
                                                topWeatherBgEnabled = true
                                            }
                                            pendingBatteryWarningTarget == BatteryWarningTarget.BG_MUSIC -> {
                                                bgMusicEnabled = true
                                                AppPrefs.setBgMusicEnabled(context, true)
                                            }
                                            pendingBatteryWarningTarget == BatteryWarningTarget.CARD_BOUNCE -> {
                                                cardBounceEnabled = true
                                                AppPrefs.setCardBounceEnabled(context, true)
                                            }
                                        }
                                        closeBatteryWarningPopup()
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

                AnimatedPopupOverlay(
                    visible = showBirthdayPopup && (todayBirthdays.isNotEmpty() || showAdminBirthdayToday),
                    onDismiss = { showBirthdayPopup = false },
                    dismissOnBackdropClick = true
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

@Composable
private fun rememberCardBounceScale(
    bounceEnabled: Boolean,
    pulseKey: Any?,
    label: String
): Float {
    var pulseToken by remember { mutableIntStateOf(0) }
    var firstRender by remember { mutableStateOf(true) }

    LaunchedEffect(pulseKey, bounceEnabled) {
        if (firstRender) {
            firstRender = false
            return@LaunchedEffect
        }
        if (bounceEnabled) {
            pulseToken += 1
        }
    }

    var targetScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(pulseToken) {
        if (pulseToken > 0 && bounceEnabled) {
            targetScale = 1.035f
            delay(95L)
            targetScale = 1f
        } else {
            targetScale = 1f
        }
    }

    val animated by animateFloatAsState(
        targetValue = if (bounceEnabled) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = label
    )
    return if (bounceEnabled) animated else 1f
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactDualCard(
    title: String,
    badgeText: String,
    accentColor: Color,
    palette: AdaptivePalette,
    bounceEnabled: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardScale = rememberCardBounceScale(
        bounceEnabled = bounceEnabled,
        pulseKey = expanded,
        label = "compact_card_bounce"
    )
    val chevronAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "compact_chevron_rotation"
    )
    val borderColor = if (expanded) accentColor.copy(alpha = 0.65f) else palette.border

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = palette.cardBg,
        contentColor = palette.textPrimary,
        modifier = modifier
            .height(64.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(RoundedCornerShape(20.dp))
            .border(width = if (expanded) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
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
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (expanded) accentColor else accentColor.copy(alpha = 0.65f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .clipToBounds()
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            repeatDelayMillis = 1200,
                            velocity = 24.dp
                        )
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (expanded) accentColor.copy(alpha = 0.22f) else palette.subtleBg
                ) {
                    Text(
                        badgeText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (expanded) accentColor else palette.textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (expanded) accentColor else palette.textSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(chevronAngle)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollapsibleSectionCard(
    title: String,
    badgeText: String? = null,
    accentColor: Color,
    palette: AdaptivePalette,
    bounceEnabled: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cardScale = rememberCardBounceScale(
        bounceEnabled = bounceEnabled,
        pulseKey = expanded,
        label = "section_card_bounce"
    )
    val chevronAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "chevron_rotation"
    )
    val borderColor = if (expanded) accentColor.copy(alpha = 0.65f) else palette.border

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.cardBg,
            contentColor = palette.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(width = if (expanded) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
                .clickable(onClick = onToggle)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (expanded) accentColor else accentColor.copy(alpha = 0.65f))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds()
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                repeatDelayMillis = 1200,
                                velocity = 24.dp
                            )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!badgeText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (expanded) accentColor.copy(alpha = 0.22f) else palette.subtleBg
                        ) {
                            Text(
                                badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (expanded) accentColor else palette.textSecondary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = if (expanded) accentColor else palette.textSecondary,
                        modifier = Modifier.rotate(chevronAngle)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(140)) + slideInVertically(animationSpec = tween(140)) { it / 14 },
            exit = fadeOut(tween(100))
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                contentColor = palette.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(palette.liquidGlassBrush)
                    .border(1.3.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SectionHairline(
    palette: AdaptivePalette,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    HorizontalDivider(
        thickness = 1.dp,
        color = palette.border,
        modifier = Modifier.padding(top = topPadding)
    )
}

@OptIn(ExperimentalFoundationApi::class)
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
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 1200,
                        velocity = 24.dp
                    )
            )
            Text(
                subtitle,
                color = palette.textSecondary,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
        )
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
        label = { Text(label, color = palette.textSecondary) },
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
            Text(
                label,
                color = palette.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp
            )
            Text(
                "$value/20",
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
        }
        Slider(
            value = value.coerceIn(1, 20).toFloat(),
            onValueChange = { floatVal ->
                val intVal = floatVal.roundToInt().coerceIn(1, 20)
                if (intVal != value) {
                    onValueChange(intVal)
                }
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

@Composable
private fun GenderSymbolIcon(
    gender: AuspiciousGender,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.12f
        if (gender == AuspiciousGender.MALE) {
            val circleCenter = Offset(w * 0.42f, h * 0.58f)
            val radius = w * 0.25f
            drawCircle(
                color = tint,
                radius = radius,
                center = circleCenter,
                style = Stroke(width = stroke)
            )
            val start = Offset(circleCenter.x + radius * 0.70f, circleCenter.y - radius * 0.70f)
            val end = Offset(w * 0.84f, h * 0.16f)
            drawLine(tint, start, end, strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, end, Offset(end.x - w * 0.18f, end.y), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, end, Offset(end.x, end.y + h * 0.18f), strokeWidth = stroke, cap = StrokeCap.Round)
        } else {
            val circleCenter = Offset(w * 0.50f, h * 0.38f)
            val radius = w * 0.25f
            drawCircle(
                color = tint,
                radius = radius,
                center = circleCenter,
                style = Stroke(width = stroke)
            )
            val stemBottom = Offset(w * 0.50f, h * 0.88f)
            drawLine(tint, Offset(w * 0.50f, circleCenter.y + radius), stemBottom, strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(
                tint,
                Offset(w * 0.32f, h * 0.72f),
                Offset(w * 0.68f, h * 0.72f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

private data class GoodBadDayHistoryItem(
    val day: Int,
    val month: Int,
    val year: Int,
    val isMale: Boolean,
    val activityName: String,
    val viewedAt: Long
) {
    fun sameQuery(other: GoodBadDayHistoryItem): Boolean =
        day == other.day && month == other.month && year == other.year &&
            isMale == other.isMale && activityName == other.activityName
}

private object GoodBadDayHistoryStore {
    private const val PREFS_NAME = "good_bad_day_history_prefs"
    private const val KEY_ENTRIES = "entries"
    const val MAX_ITEMS = 20

    fun load(context: Context): List<GoodBadDayHistoryItem> {
        return try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ENTRIES, null) ?: return emptyList()
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                GoodBadDayHistoryItem(
                    day = o.optInt("d"),
                    month = o.optInt("m"),
                    year = o.optInt("y"),
                    isMale = o.optBoolean("male", true),
                    activityName = o.optString("a"),
                    viewedAt = o.optLong("t")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, items: List<GoodBadDayHistoryItem>) {
        try {
            val arr = JSONArray()
            items.forEach {
                arr.put(
                    JSONObject()
                        .put("d", it.day)
                        .put("m", it.month)
                        .put("y", it.year)
                        .put("male", it.isMale)
                        .put("a", it.activityName)
                        .put("t", it.viewedAt)
                )
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ENTRIES, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GoodBadDayExpandableSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    accentColor: Color,
    palette: AdaptivePalette,
    bounceEnabled: Boolean,
    onShowAdvicePopup: (GoodBadDayAdvice) -> Unit,
    modifier: Modifier = Modifier
) {
    val bringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val historyContext = LocalContext.current
    var history by remember { mutableStateOf(GoodBadDayHistoryStore.load(historyContext)) }

    var birthDayText by remember { mutableStateOf("1") }
    var birthMonthText by remember { mutableStateOf("1") }
    var birthYearText by remember { mutableStateOf("2000") }
    var selectedGender by remember { mutableStateOf(AuspiciousGender.MALE) }
    var selectedActivity by remember { mutableStateOf(AuspiciousActivity.BUSINESS) }
    var activityDropdownOpen by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoView)
    ) {
        CollapsibleSectionCard(
            title = "Ngày tốt / xấu theo việc",
            badgeText = selectedActivity.label.substringBefore(" /"),
            accentColor = accentColor,
            palette = palette,
            bounceEnabled = bounceEnabled,
            expanded = expanded,
            onToggle = {
                activityDropdownOpen = false
                onToggle()
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Nhập ngày tháng năm sinh",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ModernTextField(
                            value = birthDayText,
                            onValueChange = {
                                birthDayText = it.filter { ch -> ch.isDigit() }.take(2)
                                formError = null
                            },
                            label = "Ngày",
                            accentColor = accentColor,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        ModernTextField(
                            value = birthMonthText,
                            onValueChange = {
                                birthMonthText = it.filter { ch -> ch.isDigit() }.take(2)
                                formError = null
                            },
                            label = "Tháng",
                            accentColor = accentColor,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        ModernTextField(
                            value = birthYearText,
                            onValueChange = {
                                birthYearText = it.filter { ch -> ch.isDigit() }.take(4)
                                formError = null
                            },
                            label = "Năm",
                            accentColor = accentColor,
                            palette = palette,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Chọn giới tính",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(AuspiciousGender.MALE, AuspiciousGender.FEMALE).forEach { g ->
                            val isSel = selectedGender == g
                            val iconTint = if (isSel) palette.onAccentText else accentColor
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSel) accentColor else palette.subtleBg)
                                    .border(
                                        width = 1.2.dp,
                                        color = if (isSel) accentColor else palette.border,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        selectedGender = g
                                        formError = null
                                    }
                            ) {
                                GenderSymbolIcon(
                                    gender = g,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Chọn việc cần xem",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.subtleBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .clickable {
                                activityDropdownOpen = !activityDropdownOpen
                                if (activityDropdownOpen) {
                                    scope.launch { delay(120); bringIntoView.bringIntoView() }
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                selectedActivity.label,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            val arrowAngle by animateFloatAsState(
                                targetValue = if (activityDropdownOpen) 180f else 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.62f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "activity_arrow"
                            )
                            Icon(
                                Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.rotate(arrowAngle)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = expanded && activityDropdownOpen,
                        enter = fadeIn(tween(150)) +
                            scaleIn(
                                initialScale = 0.86f,
                                transformOrigin = TransformOrigin(0.5f, 0f),
                                animationSpec = spring(
                                    dampingRatio = 0.62f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) +
                            slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = 0.68f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { -it / 4 },
                        exit = fadeOut(tween(130)) +
                            scaleOut(
                                targetScale = 0.88f,
                                transformOrigin = TransformOrigin(0.5f, 0f),
                                animationSpec = tween(140)
                            )
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(palette.liquidGlassBrush)
                                .border(1.2.dp, palette.liquidGlassBorder, RoundedCornerShape(18.dp))
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 6.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(AuspiciousActivity.entries, key = { it.name }) { item ->
                                    val chosen = item == selectedActivity
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedActivity = item
                                                activityDropdownOpen = false
                                                formError = null
                                            }
                                            .background(
                                                if (chosen) accentColor.copy(alpha = 0.16f) else Color.Transparent
                                            )
                                            .padding(horizontal = 14.dp, vertical = 11.dp)
                                    ) {
                                        Text(
                                            item.label,
                                            color = if (chosen) accentColor else palette.textPrimary,
                                            fontWeight = if (chosen) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        if (chosen) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (formError != null) {
                    Text(
                        formError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val d = birthDayText.toIntOrNull() ?: 0
                        val m = birthMonthText.toIntOrNull() ?: 0
                        val y = birthYearText.toIntOrNull() ?: 0
                        val curYear = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
                            .get(Calendar.YEAR)
                        if (y !in 1900..curYear || m !in 1..12 || d !in 1..31) {
                            formError = "Vui lòng nhập ngày sinh hợp lệ (1900–$curYear)."
                            return@Button
                        }
                        try {
                            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).apply {
                                isLenient = false
                                set(Calendar.YEAR, y)
                                set(Calendar.MONTH, m - 1)
                                set(Calendar.DAY_OF_MONTH, d)
                            }
                            cal.timeInMillis
                            activityDropdownOpen = false
                            formError = null
                            val newItem = GoodBadDayHistoryItem(
                                day = d,
                                month = m,
                                year = y,
                                isMale = selectedGender == AuspiciousGender.MALE,
                                activityName = selectedActivity.name,
                                viewedAt = System.currentTimeMillis()
                            )
                            val updatedHistory = (listOf(newItem) + history.filterNot { it.sameQuery(newItem) })
                                .take(GoodBadDayHistoryStore.MAX_ITEMS)
                            history = updatedHistory
                            GoodBadDayHistoryStore.save(historyContext, updatedHistory)
                            onShowAdvicePopup(
                                BirthdayHelper.evaluateGoodBadDayForActivity(
                                    birthDay = d,
                                    birthMonth = m,
                                    birthYear = y,
                                    gender = selectedGender,
                                    activity = selectedActivity
                                )
                            )
                        } catch (_: Exception) {
                            formError = "Ngày tháng năm sinh không tồn tại."
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = palette.onAccentText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Xem", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }

                if (history.isNotEmpty()) {
                    HorizontalDivider(color = palette.border)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Lịch sử đã xem (${history.size})",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    history = emptyList()
                                    GoodBadDayHistoryStore.save(historyContext, emptyList())
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Xóa toàn bộ lịch sử",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Xóa tất cả",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    val historyTimeFormat = remember {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).apply {
                            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
                        }
                    }
                    history.forEach { item ->
                        val activity = AuspiciousActivity.entries.firstOrNull { it.name == item.activityName }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = palette.cardBg.copy(alpha = 0.72f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(0.8.dp, palette.border, RoundedCornerShape(14.dp))
                                .clickable(enabled = activity != null) {
                                    if (activity != null) {
                                        val gender = if (item.isMale) AuspiciousGender.MALE else AuspiciousGender.FEMALE
                                        birthDayText = item.day.toString()
                                        birthMonthText = item.month.toString()
                                        birthYearText = item.year.toString()
                                        selectedGender = gender
                                        selectedActivity = activity
                                        formError = null
                                        activityDropdownOpen = false
                                        onShowAdvicePopup(
                                            BirthdayHelper.evaluateGoodBadDayForActivity(
                                                birthDay = item.day,
                                                birthMonth = item.month,
                                                birthYear = item.year,
                                                gender = gender,
                                                activity = activity
                                            )
                                        )
                                    }
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        activity?.label ?: item.activityName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "%02d/%02d/%d • %s".format(
                                            item.day,
                                            item.month,
                                            item.year,
                                            if (item.isMale) "Nam" else "Nữ"
                                        ),
                                        fontSize = 12.sp,
                                        color = accentColor
                                    )
                                    Text(
                                        "Xem lúc ${historyTimeFormat.format(Date(item.viewedAt))}",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val updatedHistory = history.filterNot { it === item }
                                        history = updatedHistory
                                        GoodBadDayHistoryStore.save(historyContext, updatedHistory)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Xóa mục lịch sử",
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
private fun GoodBadDayDualPopups(
    advicePopup: GoodBadDayAdvice?,
    showDisclaimerPopup: Boolean,
    onCloseAdviceAndOpenDisclaimer: () -> Unit,
    onCloseDisclaimer: () -> Unit,
    accentColor: Color,
    palette: AdaptivePalette
) {
    AnimatedPopupOverlay(
        visible = advicePopup != null,
        onDismiss = {},
        dismissOnBackdropClick = false
    ) {
        LiquidGlassPopupSurface(palette = palette) {
            if (advicePopup != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = advicePopup.verdictTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (advicePopup.isGoodDay) accentColor else Color(0xFFFF8A65),
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = advicePopup.todayLunarInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.popupTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.popupBoxBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = advicePopup.adviceText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.popupTextPrimary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    if (advicePopup.betterDays.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = palette.popupBoxBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Text(
                                    text = "Ngày phù hợp hơn để ${advicePopup.activityLabel.lowercase()}:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                                advicePopup.betterDays.forEach { dayItem ->
                                    Text(
                                        text = "• ${dayItem.solarDateStr} (${dayItem.lunarDateStr} • ${dayItem.dayCanChi} • ${dayItem.score}đ)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.popupTextPrimary
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        onClick = onCloseAdviceAndOpenDisclaimer,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = palette.onAccentText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Ok", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    AnimatedPopupOverlay(
        visible = showDisclaimerPopup,
        onDismiss = {},
        dismissOnBackdropClick = false
    ) {
        LiquidGlassPopupSurface(palette = palette) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = "Thông tin ứng dụng đưa ra chỉ để bạn tham khảo! Đừng quá tin vào nó nhé.",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.popupTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onCloseDisclaimer,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = palette.onAccentText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Ok", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LunarCustomEventsManagerCard(
    events: List<LunarCustomEventItem>,
    onEventsChange: (List<LunarCustomEventItem>) -> Unit,
    titleInput: String,
    onTitleInputChange: (String) -> Unit,
    dayInput: String,
    onDayInputChange: (String) -> Unit,
    monthInput: String,
    onMonthInputChange: (String) -> Unit,
    isLeapMonth: Boolean,
    onLeapMonthChange: (Boolean) -> Unit,
    errorText: String?,
    onErrorChange: (String?) -> Unit,
    accentColor: Color,
    palette: AdaptivePalette,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onPersist: (List<LunarCustomEventItem>) -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                "Ngày giỗ / sự kiện Âm lịch riêng",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Text(
                "Tự động nhắc trước 1 ngày và đúng ngày Âm lịch hằng năm",
                fontSize = 12.sp,
                color = palette.textSecondary
            )
            ModernTextField(
                value = titleInput,
                onValueChange = {
                    onTitleInputChange(it)
                    onErrorChange(null)
                },
                label = "Tên sự kiện (VD: Giỗ Ông Nội, Kỷ niệm...)",
                accentColor = accentColor,
                palette = palette,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ModernTextField(
                    value = dayInput,
                    onValueChange = {
                        onDayInputChange(it.filter { ch -> ch.isDigit() }.take(2))
                        onErrorChange(null)
                    },
                    label = "Ngày (Âm)",
                    accentColor = accentColor,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
                ModernTextField(
                    value = monthInput,
                    onValueChange = {
                        onMonthInputChange(it.filter { ch -> ch.isDigit() }.take(2))
                        onErrorChange(null)
                    },
                    label = "Tháng (Âm)",
                    accentColor = accentColor,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val title = titleInput.trim()
                        val d = dayInput.toIntOrNull() ?: 0
                        val m = monthInput.toIntOrNull() ?: 0
                        if (title.isEmpty()) {
                            onErrorChange("Vui lòng nhập tên ngày giỗ hoặc sự kiện.")
                            return@Button
                        }
                        if (d !in 1..30 || m !in 1..12) {
                            onErrorChange("Ngày âm (1–30) và tháng âm (1–12) chưa hợp lệ.")
                            return@Button
                        }
                        val newItem = LunarCustomEventItem(
                            id = System.currentTimeMillis().toString(),
                            title = title,
                            lunarDay = d,
                            lunarMonth = m,
                            isLeapMonth = isLeapMonth
                        )
                        val updated = events + newItem
                        onEventsChange(updated)
                        onPersist(updated)
                        onTitleInputChange("")
                        onDayInputChange("")
                        onMonthInputChange("")
                        onLeapMonthChange(false)
                        onErrorChange(null)
                        scope.launch {
                            snackbarHostState.showSnackbar("Đã thêm sự kiện Âm lịch: $title ($d/$m ÂL)")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = palette.onAccentText
                    ),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Thêm", fontWeight = FontWeight.Bold)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Tháng Âm lịch nhuận",
                    fontSize = 13.sp,
                    color = palette.textSecondary
                )
                Switch(
                    checked = isLeapMonth,
                    onCheckedChange = onLeapMonthChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = palette.onAccentText,
                        checkedTrackColor = accentColor
                    )
                )
            }

            if (errorText != null) {
                Text(
                    errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (events.isEmpty()) {
                Text(
                    "Chưa có ngày giỗ hoặc sự kiện Âm lịch riêng nào.",
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    events.forEach { ev ->
                        val leapStr = if (ev.isLeapMonth) " nhuận" else ""
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = palette.cardBg.copy(alpha = 0.72f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.8.dp, palette.border, RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        ev.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        "Mùng ${ev.lunarDay} tháng ${ev.lunarMonth}$leapStr Âm lịch",
                                        fontSize = 12.sp,
                                        color = accentColor
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val updated = events.filterNot { it.id == ev.id }
                                        onEventsChange(updated)
                                        onPersist(updated)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Đã xóa: ${ev.title}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Xóa sự kiện âm lịch",
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

