package com.base.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography

/** `.glabel`: nhãn nhóm viết hoa text-3. Viết hoa ở đây — string giữ dạng thường để dịch tự nhiên. */
@Composable
fun SettingsGroupLabel(text: String, modifier: Modifier = Modifier, topPadding: Dp = AppTheme.dimensions.s5) {
    Text(
        text = text.uppercase(),
        style = AppTypography.groupLabel,
        color = AppTheme.colors.text3,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = AppTheme.dimensions.s4, end = AppTheme.dimensions.s4, top = topPadding, bottom = AppTheme.dimensions.s2),
    )
}

/**
 * `.rows`: khối `surface` bo 12dp. Gọi [SettingsRow] bên trong; vạch ngăn `border` giữa các hàng
 * (không vẽ sau hàng cuối) — truyền `divider = index < lastIndex`.
 */
@Composable
fun SettingsRows(modifier: Modifier = Modifier, topPadding: Dp = 0.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .padding(start = AppTheme.dimensions.s4, end = AppTheme.dimensions.s4, top = topPadding)
            .fillMaxWidth()
            .clip(AppTheme.shapes.md)
            .background(MaterialTheme.colorScheme.surface),
        content = content,
    )
}

/** Phần cuối hàng `.r`. */
sealed interface RowTrailing {
    data object None : RowTrailing

    /** `.v` + `›` (mở màn khác). */
    data class Chevron(val value: String? = null) : RowTrailing

    /** ✓ accent — lựa chọn đang áp (theme, ngôn ngữ). */
    data object Selected : RowTrailing
}

/**
 * Hàng `.r`: icon (tuỳ chọn) + nhãn 14.5sp (+ dòng phụ `small`) + phần cuối. Cao tối thiểu 48dp (rule chạm).
 * [selectable] = true khi hàng là một lựa chọn trong nhóm (theme, ngôn ngữ) — TalkBack đọc "đã chọn".
 * [onClick] = null: hàng chỉ để đọc (Licenses) — không ripple, TalkBack không đọc "nút".
 */
@Composable
fun SettingsRow(
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    supporting: String? = null,
    trailing: RowTrailing = RowTrailing.None,
    selectable: Boolean = false,
    divider: Boolean = true,
) {
    val dimensions = AppTheme.dimensions
    val interaction = when {
        onClick == null -> Modifier
        selectable -> Modifier.selectable(selected = trailing == RowTrailing.Selected, role = Role.RadioButton, onClick = onClick)
        else -> Modifier.clickable(role = Role.Button, onClick = onClick)
    }
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = dimensions.touchTarget)
                .then(interaction)
                .padding(horizontal = dimensions.s4, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.s3),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(text = label, style = AppTypography.rowLabel, color = MaterialTheme.colorScheme.onSurface)
                if (supporting != null) {
                    Text(
                        text = supporting,
                        style = AppTypography.rowSupporting,
                        color = AppTheme.colors.text3,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            when (trailing) {
                RowTrailing.None -> Unit
                RowTrailing.Selected -> Icon(
                    AppIcons.Selected,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                is RowTrailing.Chevron -> {
                    trailing.value?.let {
                        Text(
                            text = it,
                            style = AppTypography.rowValue,
                            color = AppTheme.colors.text3,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            // Không weight: nhãn đã chiếm phần còn lại, giá trị + › dính mép cuối.
                            modifier = Modifier.widthIn(max = 168.dp),
                        )
                    }
                    Icon(AppIcons.ChevronEnd, contentDescription = null, tint = AppTheme.colors.text4, modifier = Modifier.size(16.dp))
                }
            }
        }
        if (divider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingsRowsPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background).padding(vertical = 16.dp)) {
            SettingsGroupLabel("Appearance", topPadding = 0.dp)
            SettingsRows {
                SettingsRow("Dark", onClick = {}, trailing = RowTrailing.Selected, selectable = true)
                SettingsRow("Light", onClick = {}, selectable = true, divider = false)
            }
            SettingsRows(topPadding = 12.dp) {
                SettingsRow("History", onClick = {}, icon = AppIcons.History, trailing = RowTrailing.Chevron("37"), divider = false)
            }
        }
    }
}
