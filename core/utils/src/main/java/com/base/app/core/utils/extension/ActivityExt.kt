package com.base.app.core.utils.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** `LocalContext` có thể là ContextWrapper (Hilt/AppCompat/Dialog) — không cast thẳng sang Activity. */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
