package com.base.app.core.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Kéo quá phần này chiều cao sheet (hoặc vuốt nhanh xuống) thì đóng. */
private const val DISMISS_FRACTION = 0.3f
private const val DISMISS_VELOCITY = 1_800f

/**
 * Bottom sheet của app (`.sheet` + `.dim` + `.grab` trong mockup) — thay `ModalBottomSheet` của Material.
 *
 * Vẽ **cùng cửa sổ** với màn (không phải window riêng), nên toast/overlay của màn đặt sau sheet vẫn nổi
 * trên scrim. Đặt nó là con cuối của một `Box` phủ toàn màn. Đóng bằng: chạm scrim, Back, kéo tay nắm
 * xuống. [visible] = false chạy animation thoát — caller giữ nội dung cũ trong lúc đó nếu cần.
 */
@Composable
fun AppBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(enabled = visible, onBack = onDismiss)
    Box(modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.media.sheetScrim)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            SheetPanel(onDismiss = onDismiss, content = content)
        }
    }
}

@Composable
private fun SheetPanel(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    var heightPx by remember { mutableIntStateOf(0) }
    var dismissed by remember { mutableStateOf(false) }

    val dragState = rememberDraggableState { delta ->
        scope.launch { dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f)) }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Trần 90% màn, cao theo nội dung, dính đáy.
            .fillMaxHeight(0.9f)
            .wrapContentHeight(align = Alignment.Bottom)
            .offset { IntOffset(0, dragOffset.value.roundToInt()) }
            .onSizeChanged { heightPx = it.height }
            .clip(AppTheme.shapes.sheet)
            .background(MaterialTheme.colorScheme.surface)
            // Chặn chạm xuyên xuống scrim (scrim đóng sheet).
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {}),
    ) {
        // Tay nắm + vùng kéo: chỉ phần đầu sheet kéo được để không giành cử chỉ cuộn của nội dung.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        if (!dismissed && (dragOffset.value > heightPx * DISMISS_FRACTION || velocity > DISMISS_VELOCITY)) {
                            dismissed = true
                            onDismiss()
                        } else {
                            dragOffset.animateTo(0f)
                        }
                    },
                )
                .padding(top = 10.dp, bottom = AppTheme.dimensions.s4),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, AppTheme.shapes.full),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.dimensions.s5)
                .navigationBarsPadding()
                .padding(bottom = 22.dp),
            content = content,
        )
    }
}

@Preview
@Composable
private fun AppBottomSheetPreview() {
    AppTheme {
        AppBottomSheet(visible = true, onDismiss = {}) {
            Text("Playback options", style = AppTypography.title, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
