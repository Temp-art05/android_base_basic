package com.base.app.fcm

import android.content.Intent
import com.base.app.MainActivity
import com.base.app.R
import com.base.app.core.utils.notification.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("FCM message received from: ${remoteMessage.from}")

        remoteMessage.notification?.let { notification ->
            NotificationHelper.showNotification(
                context = this,
                title = notification.title,
                body = notification.body,
                contentIntent = Intent(this, MainActivity::class.java),
                smallIcon = R.mipmap.ic_launcher,
            )
        }
    }

    @Deprecated("FirebaseMessagingService.onNewToken is deprecated in Firebase BOM 34")
    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Gửi token lên BE ở đây khi project có push. Không log token: nó là định danh thiết bị.
        Timber.d("FCM token refreshed")
    }
}
