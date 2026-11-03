package com.base.app.core.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

/**
 * Hiển thị notification hệ thống. Không biết Activity hay icon của app — `:app` truyền vào,
 * nên module nào cũng gọi được mà không phụ thuộc ngược lên `:app`.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "default_channel"
    private const val CHANNEL_NAME = "General"

    /** Channel chỉ có từ API 26; dưới đó notification không cần channel. */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** [contentIntent] là Intent mở app khi bấm notification (thường trỏ tới MainActivity). */
    fun showNotification(
        context: Context,
        title: String?,
        body: String?,
        contentIntent: Intent,
        @DrawableRes smallIcon: Int,
    ) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
