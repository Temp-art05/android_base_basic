package com.base.app.feature.main.presentation.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.app.LocaleManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.app.core.designsystem.component.AppTopBar
import com.base.app.core.designsystem.component.RowTrailing
import com.base.app.core.designsystem.component.SettingsGroupLabel
import com.base.app.core.designsystem.component.SettingsRow
import com.base.app.core.designsystem.component.SettingsRows
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.core.ui.foundation.ext.CollectAsEffect
import com.base.app.core.ui.foundation.locale.AppLocale
import com.base.app.core.ui.foundation.locale.LanguageLabels
import com.base.app.R
import java.util.Locale

@Composable
fun LanguageScreen(
    viewModel: LanguageViewModel,
    onBack: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    val displayedLocale = LocalConfiguration.current.locales[0]
    val deviceLocale = rememberDeviceLocale()

    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.processIntent(LanguageIntent.ScreenStarted) }
    // Áp locale xong → config change (không recreate) → mở khoá tap.
    LaunchedEffect(displayedLocale) { viewModel.processIntent(LanguageIntent.DisplayedLocaleChanged) }

    viewModel.singleEvent.CollectAsEffect { event ->
        when (event) {
            LanguageSingleEvent.NavigateBack -> onBack()
            // Activity dựng lại với locale mới, vẫn ở màn này (không restart app).
            is LanguageSingleEvent.ApplyLocale -> {
                AppLocale.apply(event.locale)
                if (!AppLocale.changesDisplayedLanguage(event.locale, displayedLocale, deviceLocale)) {
                    viewModel.processIntent(LanguageIntent.ApplyFinished)
                }
            }
        }
    }

    LanguageContent(
        viewState = viewState,
        deviceLabel = LanguageLabels.deviceLanguage(deviceLocale),
        onBackClick = { viewModel.processIntent(LanguageIntent.BackClick) },
        onLocaleSelect = { locale -> viewModel.processIntent(LanguageIntent.SelectLocale(locale)) },
    )
}

/**
 * Ngôn ngữ **máy** (không phải của app): app đặt locale riêng thì `Locale.getDefault()` là locale app,
 * nên hỏi `LocaleManagerCompat.getSystemLocales`. Đọc lại theo cấu hình (đổi ngôn ngữ máy → recreate).
 */
@Composable
internal fun rememberDeviceLocale(): Locale {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(configuration) { LocaleManagerCompat.getSystemLocales(context)[0] ?: Locale.getDefault() }
}

/** "Theo ngôn ngữ máy" + danh sách endonym của [AppLocale]; ✓ accent, không radio Material. */
@Composable
private fun LanguageContent(
    viewState: LanguageViewState,
    deviceLabel: String,
    onBackClick: () -> Unit,
    onLocaleSelect: (AppLocale) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = stringResource(R.string.language_title),
            onBack = onBackClick,
            backContentDescription = stringResource(R.string.common_back),
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = AppTheme.dimensions.s6),
        ) {
            Text(
                text = stringResource(R.string.language_description),
                style = AppTypography.note,
                color = AppTheme.colors.text3,
                modifier = Modifier.padding(start = AppTheme.dimensions.s4, end = AppTheme.dimensions.s4, top = 2.dp),
            )
            SettingsRows(topPadding = AppTheme.dimensions.s3) {
                SettingsRow(
                    label = stringResource(R.string.language_system),
                    supporting = stringResource(R.string.language_current, deviceLabel),
                    trailing = if (viewState.selected == AppLocale.System) RowTrailing.Selected else RowTrailing.None,
                    selectable = true,
                    onClick = { onLocaleSelect(AppLocale.System) },
                    divider = false,
                )
            }
            SettingsGroupLabel(stringResource(R.string.language_choose_manually))
            val manual = AppLocale.entries.filter { it != AppLocale.System }
            SettingsRows {
                manual.forEachIndexed { index, locale ->
                    SettingsRow(
                        label = locale.endonym.orEmpty(),
                        trailing = if (viewState.selected == locale) RowTrailing.Selected else RowTrailing.None,
                        selectable = true,
                        onClick = { onLocaleSelect(locale) },
                        divider = index < manual.lastIndex,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LanguageContentPreview() {
    AppTheme {
        LanguageContent(
            viewState = LanguageViewState(),
            deviceLabel = "English (U.S.)",
            onBackClick = {},
            onLocaleSelect = {},
        )
    }
}
