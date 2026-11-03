package com.base.app.feature.main.presentation.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.feature.main.presentation.main.model.MainTab

/**
 * Tab bar: nền đặc ([AppTheme.colors.tabBar]), viền trên 1dp, item cao tối thiểu 44dp. Tab chọn: icon accent +
 * nhãn tô gradient; tab khác text-3. Nằm **dưới** nội dung (MainScreen là Column), không đè lên nội dung.
 */
@Composable
fun MainBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // Bar đè lên tab: chạm vào viền/padding/inset không được lọt xuống .
            // Pass Final: item đã nhận click ở pass Main trước đó.
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final).consume()
                }
            }
            .background(AppTheme.colors.tabBar),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 6.dp, end = 6.dp, top = 9.dp, bottom = 4.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inactiveColor = AppTheme.colors.text3
    val labelStyle = if (isSelected) {
        AppTypography.tab.copy(brush = AppTheme.colors.gradientCta)
    } else {
        AppTypography.tab.copy(color = inactiveColor)
    }

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = AppTheme.dimensions.tabItemMinHeight)
            // Không ripple (user chốt 24/10): tab chọn đã đổi màu icon + nhãn gradient.
            .selectable(
                selected = isSelected,
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.tabItemGap, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = tab.icon,
            // Nhãn bên dưới đã đọc tên tab cho TalkBack.
            contentDescription = null,
            modifier = Modifier.size(AppTheme.dimensions.tabIcon),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else inactiveColor,
        )
        Text(
            text = stringResource(tab.labelRes),
            style = labelStyle,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun MainBottomNavBarPreview() {
    AppTheme {
        MainBottomNavBar(selectedTab = MainTab.Home, onTabSelected = {})
    }
}
