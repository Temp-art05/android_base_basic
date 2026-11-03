package com.base.app.core.ui.foundation.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collect event một lần (điều hướng, toast...) theo lifecycle. Dùng cho `singleEvent` của
 * BaseViewModel thay vì `LaunchedEffect { collect }` trần — trần thì vẫn nhận event khi app ở
 * nền và điều hướng lúc NavController không sẵn sàng.
 */
@Composable
fun <T> Flow<T>.CollectAsEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEvent: (T) -> Unit,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            collect { currentOnEvent(it) }
        }
    }
}
