package com.base.app.feature.main.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.feature.main.presentation.main.home.HomeScreen
import com.base.app.feature.main.presentation.main.bookmarks.BookmarksScreen
import com.base.app.feature.main.presentation.main.component.MainBottomNavBar
import com.base.app.feature.main.presentation.main.model.MainTab
import com.base.app.feature.main.presentation.main.settings.SettingsScreen

private val tabs = MainTab.entries

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenPostDetail: (postId: Int) -> Unit,
    onOpenLanguage: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = viewState.selectedTab.ordinal) { tabs.size }

    // scrollToPage chứ không animateScrollToPage: đổi tab tức thì, không trượt ngang qua tab ở giữa.
    LaunchedEffect(viewState.selectedTab) {
        pagerState.scrollToPage(viewState.selectedTab.ordinal)
    }

    BackHandler(enabled = viewState.selectedTab != MainTab.Home) {
        viewModel.processIntent(MainIntent.SelectTab(MainTab.Home))
    }

    MainContent(
        viewState = viewState,
        pagerState = pagerState,
        onTabSelected = { tab -> viewModel.processIntent(MainIntent.SelectTab(tab)) },
        tabContent = { tab ->
            // ViewModel của tab lấy bằng koinViewModel() mặc định → scope theo back stack entry
            // của Main, nên sống qua mọi lần đổi tab và chỉ bị huỷ khi Main bị pop.
            when (tab) {
                MainTab.Home -> HomeScreen(
                    onOpenPostDetail = onOpenPostDetail,
                )
                MainTab.Bookmarks -> BookmarksScreen(
                    onOpenPostDetail = onOpenPostDetail,
                    onOpenHomeTab = { viewModel.processIntent(MainIntent.SelectTab(MainTab.Home)) },
                )
                MainTab.Settings -> SettingsScreen(
                    onOpenLanguage = onOpenLanguage,
                )
            }
        },
    )
}

/**
 * Pager chỉ đóng vai "container giữ tab", không phải carousel:
 * - `userScrollEnabled = false`: không vuốt ngang đổi tab, tránh tranh gesture với list/map bên trong.
 * - `beyondViewportPageCount = tabs.lastIndex`: mọi tab nằm trong cửa sổ compose nên không
 *   bao giờ bị dispose → giữ nguyên scroll, text đang nhập, `remember` và không load lại.
 * - Tab chưa từng mở chỉ là ô trống ([MainViewState.visitedTabs]) nên mở app không phải dựng mọi tab.
 *
 * Tab đang ẩn vẫn ở lifecycle RESUMED: việc chỉ nên chạy khi tab hiện (polling, video...) thì
 * gate bằng tab đang chọn, đừng dựa vào lifecycle.
 *
 * Tab bar nằm **dưới** pager (Column), không đè: cuối danh sách dừng ở mép trên bar. Bar tự cao theo nội dung + inset.
 */
@Composable
private fun MainContent(
    viewState: MainViewState,
    pagerState: PagerState,
    onTabSelected: (MainTab) -> Unit,
    tabContent: @Composable (tab: MainTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            userScrollEnabled = false,
            beyondViewportPageCount = tabs.lastIndex,
            key = { page -> tabs[page].name },
        ) { page ->
            val tab = tabs[page]
            if (tab in viewState.visitedTabs) {
                tabContent(tab)
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        MainBottomNavBar(
            selectedTab = viewState.selectedTab,
            onTabSelected = onTabSelected,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainContentPreview() {
    AppTheme {
        MainContent(
            viewState = MainViewState(),
            pagerState = rememberPagerState { tabs.size },
            onTabSelected = {},
            tabContent = { tab ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(tab.labelRes),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
        )
    }
}
