package com.base.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.ext.applyIf
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography

/**
 * `.seg` của mockup 4.2: rãnh `surface-2` pill, ô chọn `surface-3` chữ text-1, ô khác text-3. Các ô chia
 * đều — đổi tab không đổi kích thước (không nhảy layout).
 */
@Composable
fun SegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = AppTheme.shapes.full
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.dimensions.s4)
            .background(colors.surfaceVariant, shape)
            .padding(3.dp)
            .selectableGroup(),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    // 36dp nhìn thấy + rãnh 3dp hai đầu ≈ 42dp; vùng chạm dọc cả rãnh.
                    .defaultMinSize(minHeight = 36.dp)
                    .clip(shape)
                    .applyIf(selected) { background(colors.surfaceContainerHighest, shape) }
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = AppTypography.segment,
                    color = if (selected) colors.onSurface else AppTheme.colors.text3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = AppTheme.dimensions.s2),
                )
            }
        }
    }
}
