package com.base.app.core.ui.foundation.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.text.DateFormat
import java.util.Calendar

/**
 * Định dạng giờ ngắn theo **cài đặt 12/24h của máy** (`21:45` / `9:45 PM`) — `DateFormat.getTimeInstance`
 * của Java chỉ nhìn locale nên en-US bật 24h vẫn ra "9:45 PM".
 */
@Composable
fun rememberTimeFormat(): DateFormat {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) { android.text.format.DateFormat.getTimeFormat(context) }
}

/** Giờ:phút theo [format] — vd. `rememberTimeFormat().formatClock(21, 45)`. */
fun DateFormat.formatClock(hour: Int, minute: Int): String =
    format(
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.time,
    )
