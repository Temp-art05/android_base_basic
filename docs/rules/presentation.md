# Presentation (ViewModel + Compose)

## Bộ file mỗi màn hình

Đặt trong `presentation/<screen>/` của feature; composable riêng của màn ở `component/`, UI model/enum ở `model/` (xem `architecture.md`).
Màn con của một container nằm **trong** package container: tab của Main ở `presentation/main/<tab>/`
(`main/home`, `main/bookmarks`, `main/settings`). Feature không có package `navigation/`.

| File | Nội dung |
|---|---|
| `FooContract.kt` | `data class FooViewState : ViewState`, `sealed interface FooIntent : ViewIntent`, `sealed interface FooSingleEvent : SingleEvent` |
| `FooViewModel.kt` | `BaseViewModel<FooIntent, FooViewState, FooSingleEvent>` |
| `FooScreen.kt` | `FooScreen` (nối ViewModel) + `FooContent` (stateless, có preview) |
| `FooScreenProvider.kt` | cùng package với màn, chỉ khi màn là một destination: `@Serializable` destination + map từng callback điều hướng sang `NavigationRequest` |

## MVI — mọi hành động đi qua `processIntent`

Mỗi màn có `sealed interface FooIntent : ViewIntent`; ViewModel override
`processIntent(intent)` → `when (intent)` → gọi hàm **private** xử lý. Không có hàm public nào khác
cho hành động của user.

```kotlin
// Contract
sealed interface FooIntent : ViewIntent {
    data object RetryClick : FooIntent
    data class SelectTab(val tab: MainTab) : FooIntent
}

// ViewModel
override fun processIntent(intent: FooIntent) {
    when (intent) {
        FooIntent.RetryClick -> load()
        is FooIntent.SelectTab -> selectTab(intent.tab)
    }
}

// Screen — chỗ DUY NHẤT gọi processIntent, mỗi hành động một dòng
FooContent(
    viewState = viewState,
    onRetryClick = { viewModel.processIntent(FooIntent.RetryClick) },
    onTabSelected = { tab -> viewModel.processIntent(FooIntent.SelectTab(tab)) },
)

// Content — stateless, mỗi hành động một callback có tên
@Composable
private fun FooContent(
    viewState: FooViewState,
    onRetryClick: () -> Unit,
    onTabSelected: (MainTab) -> Unit,
)
```

**Cấm** `onIntent: (FooIntent) -> Unit` ở Content hay composable con, cấm gom lambda vào data class/holder:
đọc chữ ký composable phải thấy ngay nó làm được gì. Callback hành động không có default `= {}` (preview tự
truyền `{}`), để sót callback là lỗi compile chứ không phải nút bấm câm.

Lý do: một cửa vào duy nhất cho mọi hành động — đọc `FooIntent` là biết màn có những hành động gì,
log/analytics hành động gắn được ở một chỗ, và luồng dữ liệu một chiều (Intent → State/Event).

## ViewModel

- Override `initState()` (state dựng lười nên được đọc dependency constructor).
- Load dữ liệu ban đầu ở `init {}`, **không** ở `LaunchedEffect` của screen — compose lại (đổi tab,
  xoay máy) không được kéo theo load lại.
- `updateState { copy(...) }` để đổi state; `sendEvent(...)` cho điều hướng/toast.
- **Loading là field của state** (`isLoading`), không phải nhánh của `Result`.
- Text lỗi trong state là `@StringRes Int` (`it.toErrorRes()`), không là `String`.
- Không giữ `Context`, không import `data` (`ArchitectureTest` chặn).

## Screen

- `viewModel.viewState.collectAsStateWithLifecycle()` — không `collectAsState()`.
- `viewModel.singleEvent.CollectAsEffect { event -> ... }` (`:core:ui-foundation`) — không `LaunchedEffect { collect }` trần.
- Màu: `MaterialTheme.colorScheme.*`, không hardcode `AppColors.*` / `Color(0x...)`.
- Ảnh: `AppAsyncImage`, không gọi thẳng Coil.
- String của app ở `app/src/main/res`; string của core nằm trong module core đó.

## Màn có video (`:core:media`)

- ViewModel **sở hữu** `EpisodeFeedPlayer` (tạo qua `EpisodeFeedPlayerFactory`, `release()` ở `onCleared`) — xoay
  máy không dựng lại player, không rebuffer. Screen vẽ `VideoSurface(slot)` cho từng trang từ `player.slots`.
- Feed vuốt dọc: **pool** player theo `MediaProfile` (máy thường: trang −1/0/+1, máy yếu: 0/+1); pager
  `beyondViewportPageCount = 1`, báo `PageSettled` → VM.
- Feed bị che hẳn (tab ẩn, mở màn khác, STOP) → `player.setActive(false)` nhả decoder; hiện lại → `setActive(true)`.
  Không để hai pool cùng giữ decoder. Lifecycle là intent (`ScreenStarted`/`ScreenStopped`), VM quyết pause/lưu vị trí.
- Vào nền ở màn phát chính (Player): `pause()` + `setPreloadActive(false)` (nhả player lân cận, giữ player hiện tại).
- STOP do xoay máy (`isChangingConfigurations`) bị bỏ qua — không khựng tiếng.
- Tiến độ phát: `StateFlow<PlaybackProgress>` riêng, đọc trong lambda (`drawBehind`/track) — không đưa `positionMs`
  vào ViewState (tick recompose cả màn). Hỏi theo id trang (`fractionFor(id)`), không theo trang hiện tại.
- VM là nguồn sự thật của trang hiện tại: pager báo `PageSettled`; tự sang trang kế thì VM gửi `ScrollToPage`, mở
  video ở `PageSettled` như user vuốt.
- Mẫu đủ: `main/reels/` (tab, gate theo `isSelected`) và `player/` — xem `docs/guides/sample-flow.md` mục 2.

## Popup

Không dùng popup/dialog/sheet/menu mặc định của Material — dùng component của `:core:design-system`
(`AppBottomSheet`, `AppToastHost`…).

## Navigation

- Destination là `@Serializable data object/class : Destination`, khai **bên trong** `ScreenProvider`.
- Đăng ký ở `AppNavGraph`: `applyDestination(FooScreenProvider())`.
- **Mỗi đích một callback có tên**, không truyền `onNavigate: (NavigationRequest) -> Unit` xuống Screen:

```kotlin
// FooScreen — không import NavigationRequest hay destination màn khác
@Composable
fun FooScreen(
    viewModel: FooViewModel,
    onBack: () -> Unit,
    onOpenPostDetail: (postId: Int) -> Unit,
    onOpenLanguage: () -> Unit,
)

// FooScreenProvider — chỗ duy nhất đổi callback thành NavigationRequest
FooScreen(
    viewModel = viewModel,
    onBack = { onNavigationRequest(NavigationRequest.PopBackStack) },
    onOpenPostDetail = { postId -> onNavigationRequest(NavigationRequest.NavigateTo(PostDetailScreenProvider.PostDetail(postId))) },
    onOpenLanguage = { onNavigationRequest(NavigationRequest.NavigateTo(LanguageScreenProvider.Language)) },
)
```

  Container (Main) nhận đủ callback của các tab rồi chuyển cho từng tab đúng cái nó cần; đổi tab nội bộ cũng là
  callback riêng (`onOpenHomeTab` của Bookmarks), không phải `onSwitchTab(MainTab)`.
- `NavigationRequest`: `NavigateTo`, `NavigateToAndClearBackStack`, `PopBackStack`, `NavigateUp`, `PopTo`. Có throttle
  chống double-tap; điều hướng không do user bấm (deeplink, hết phiên) gọi `NavigationThrottle.awaitWindow()` trước.

## Main 4 tab (Home, Reels, Bookmarks, Settings)

`HorizontalPager` làm container giữ tab, không phải carousel: `userScrollEnabled = false`, đổi tab bằng
`scrollToPage` (không trượt qua tab giữa), `beyondViewportPageCount = tabs.lastIndex` (tab đã mở không
bị dispose → giữ scroll/state, không load lại), tab chưa mở chưa compose (`visitedTabs`). ViewModel tab
lấy bằng `koinViewModel()` → scope theo back stack entry của Main. Thêm tab: entry trong `MainTab` + nhánh
trong `MainScreen`.

Tab bar (`MainBottomNavBar`) nằm **dưới** pager (Column), không đè: cuối danh sách dừng ở mép trên bar.

Bẫy: tab ẩn **vẫn RESUMED** — việc chỉ nên chạy khi tab hiện (polling, video) thì gate theo `selectedTab`.

## DI

ViewModel khai trong module Koin của feature (`feature/<name>/di`, `viewModelOf(::FooViewModel)`).
