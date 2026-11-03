package com.base.app.core.utils.extension

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import timber.log.Timber

/** Mở URL bằng app phù hợp (trình duyệt...). Trả false nếu máy không có app nào mở được. */
fun Context.openUrl(url: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: ActivityNotFoundException) {
    Timber.w(e, "No app to open $url")
    false
}

/** Mở bảng chia sẻ hệ thống với [text]. */
fun Context.shareText(text: String, chooserTitle: String? = null) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Copy [text] vào clipboard. Từ Android 13 hệ thống tự hiện xác nhận — đừng toast thêm. */
fun Context.copyToClipboard(text: String, label: String = "text") {
    getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText(label, text))
}
