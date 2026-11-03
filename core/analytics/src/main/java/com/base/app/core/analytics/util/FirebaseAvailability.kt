package com.base.app.core.analytics.util

import android.content.Context
import com.google.firebase.FirebaseApp

/**
 * Base được đẩy lên repo KHÔNG kèm google-services.json, nên FirebaseApp có thể chưa
 * được khởi tạo. Mọi chỗ đụng tới Firebase phải hỏi hàm này trước, nếu không app sẽ
 * crash ngay khi Koin resolve một Firebase singleton.
 */
fun isFirebaseAvailable(context: Context): Boolean =
    runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)
