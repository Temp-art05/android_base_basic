package com.base.app.feature.main.presentation.player.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.app.R
import com.base.app.core.designsystem.component.ChoiceChip
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.core.media.PlaybackSpeed

/** Nội dung sheet tuỳ chọn (đặt trong `AppBottomSheet`): tốc độ → quality có thật trong `sources`. */
@Composable
internal fun PlaybackOptionsSheet(
    speed: Float,
    qualities: List<String>,
    quality: String?,
    onSpeedSelect: (speed: Float) -> Unit,
    onAutoQualitySelect: () -> Unit,
    onQualitySelect: (quality: String) -> Unit,
) {
    Text(
        text = stringResource(R.string.player_options),
        style = AppTypography.title.copy(fontSize = 18.sp),
        color = MaterialTheme.colorScheme.onSurface,
    )
    GroupLabel(stringResource(R.string.video_speed), Modifier.padding(top = 18.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s2),
    ) {
        PlaybackSpeed.options.forEach { option ->
            ChoiceChip(
                text = stringResource(R.string.video_speed_value, PlaybackSpeed.label(option)),
                selected = option == speed,
                onClick = { onSpeedSelect(option) },
            )
        }
    }

    GroupLabel(stringResource(R.string.player_quality), Modifier.padding(top = AppTheme.dimensions.s5))
    OptionRow(
        title = stringResource(R.string.player_quality_auto),
        subtitle = stringResource(R.string.player_quality_auto_hint),
        selected = quality == null,
        onClick = onAutoQualitySelect,
    )
    qualities.forEach { option ->
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        OptionRow(
            title = option,
            subtitle = null,
            selected = quality == option,
            onClick = { onQualitySelect(option) },
        )
    }
}

@Composable
private fun GroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = AppTypography.micro.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
        color = AppTheme.colors.text3,
        modifier = modifier.padding(bottom = AppTheme.dimensions.s2),
    )
}

@Composable
private fun OptionRow(title: String, subtitle: String?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppTheme.dimensions.touchTarget)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTypography.body.copy(fontSize = 14.5.sp), color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = AppTypography.caption.copy(fontSize = 11.5.sp),
                    color = AppTheme.colors.text3,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        if (selected) {
            Icon(AppIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        } else {
            Spacer(Modifier.width(AppTheme.dimensions.s3))
        }
    }
}
