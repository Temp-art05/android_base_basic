# Android Base (basic)

Base project Android: Clean Architecture, 100% Jetpack Compose, MVI, Koin, Ktor, Room, DataStore.
**Ít module** — `domain`, `data`, `feature` là package trong `:app`; chỉ hạ tầng dùng chung tách thành `:core:*`.
Hợp với app nhỏ/vừa; app lớn lên thì tách module theo đúng các package sẵn có.

## Setup

**Cần:** Android Studio Quail 1 (2026.1.1) trở lên · Android SDK 37 · JDK 17 (thiếu thì Gradle tự tải).

```bash
git clone https://github.com/Temp-art05/android_base_basic.git && cd android_base_basic
./gradlew :app:assembleDevDebug      # hoặc mở bằng Android Studio rồi Run
```

- Build bằng dòng lệnh mà chưa từng mở Studio thì cần `ANDROID_HOME` (hoặc `sdk.dir` trong `local.properties`).
- **Không cần Firebase** để chạy: thiếu `app/google-services.json` thì Analytics / Crashlytics / Remote Config là no-op.
  Thả file vào `app/` là 2 plugin Firebase tự bật (`app/build.gradle.kts`).
- Lib ads `com.github.dinhn:ads-*` 1.1.1 vendor sẵn ở `third_party/maven` — clone sạch là build được, không cần token.

| Flavor | `applicationId` | Ghi chú |
|---|---|---|
| `dev` (mặc định) | `com.base.app.dev` | `versionName` thêm `-dev` |
| `prod` | `com.base.app` | `assembleProdRelease`: R8 full mode + shrink; ký khi `local.properties` có `RELEASE_*` |

## Cấu trúc

```
app/                         com.base.app
  BaseApp, MainActivity, di/, navigation/AppNavGraph, fcm/
  domain/                    model · repository (interface) · usecase/<area>/ — Kotlin thuần
  data/                      remote (Ktor) · local (Room, DataStore) · mapper · repository (impl) · di
  feature/main/              di · presentation/<screen>/ (Contract, ViewModel, Screen, ScreenProvider)
core/
  utils/                     AppConfig, DomainException, network, notification, extension
  navigation/                ScreenProvider, NavigationRequest, throttle, deeplink, launcher
  analytics/                 Analytics, Crash, Remote Config — Firebase sau interface, mặc định no-op
  ui-foundation/             BaseViewModel (MVI), CollectAsEffect, error → string, AppLocale
  design-system/             theme (dark mặc định), component, icon
  media/                     ExoPlayer pool cho feed video dọc: preload lân cận, cache đĩa, DeviceTier
  ads/                       Interstitial / Reward / Native manager, bật tắt theo Remote Config
build-logic/                 convention plugin: base.android.application | library | compose
```

Chiều phụ thuộc `presentation → domain ← data` do `ArchitectureTest` giữ (`app/src/test`) — vi phạm là test đỏ.
Chi tiết: [`docs/rules/architecture.md`](docs/rules/architecture.md).

## Luồng mẫu

Hai luồng đủ mọi tầng: post feed từ `https://dummyjson.com/posts` và video dọc (Reels + Player):

- **Home** — feed phân trang (cuộn gần cuối tự tải), bookmark từng post, skeleton / lỗi + thử lại.
- **Bookmarks** — danh sách bookmark stream từ Room, trạng thái rỗng.
- **Post detail** — destination có tham số `postId`, bookmark, back.
- **Reels** — feed video dọc kiểu TikTok: pool player preload video lân cận, cache đĩa, chỉ phát khi tab hiện,
  tự sang reel kế. Feed `sample-api/videos.json` (cache JSON ra đĩa → mở offline vẫn có feed).
- **Player** — vuốt dọc qua feed, xem tiếp chỗ đã dừng (Room), kéo tua, tốc độ, quality (Auto tự hạ khi mạng chậm),
  pause + nhả preload khi vào nền, giữ trạng thái qua xoay máy / process death.
- **Settings** — theme Dark/Light (DataStore), mở **Language** (10 ngôn ngữ UI, đổi ngay không restart, `ar` RTL).

Đọc theo thứ tự: [`docs/guides/sample-flow.md`](docs/guides/sample-flow.md) — có mục "Xoá khi làm project thật".

## Quy ước chính

- **MVI**: mọi hành động của user là một `Intent` đi qua `viewModel.processIntent(...)`; event một lần (điều hướng,
  toast) là `SingleEvent`.
- **Callback tường minh**: Screen nhận mỗi đích điều hướng một callback (`onBack`, `onOpenPostDetail(postId)`),
  Content nhận mỗi hành động một lambda (`onRetry`, `onBookmarkClick(post)`). Không `onNavigate`/`onIntent` gộp.
  Chỉ `ScreenProvider` tạo `NavigationRequest`.
- **Use case** một hành động một class (`GetPostsPageUseCase` + `invoke`), rule nghiệp vụ nằm ở use case.
- **Repository + DataSource** có interface/Impl; lỗi hạ tầng → `DomainException` → `@StringRes`.

| Rule | |
|---|---|
| [`architecture.md`](docs/rules/architecture.md) | module, tầng, chiều phụ thuộc, khi nào tách module |
| [`presentation.md`](docs/rules/presentation.md) | ViewModel/Screen, MVI, navigation, Main tab |
| [`usecase.md`](docs/rules/usecase.md) | use case, rule nằm ở đâu |
| [`data-layer.md`](docs/rules/data-layer.md) | repository, datasource, mapper, DTO, network, Room |
| [`error-handling.md`](docs/rules/error-handling.md) | `Result`, `ApiException → DomainException → @StringRes` |
| [`new-api.md`](docs/guides/new-api.md) | thêm một API từng bước |
| [`utils.md`](docs/guides/utils.md) | Toast, Context ext, NetworkUtils, NotificationHelper |

Tạo project mới từ base: [`TODO.md`](TODO.md).

## Lệnh

```bash
./gradlew :app:assembleDevDebug
./gradlew :app:assembleProdRelease       # R8 — chạy sau khi đổi build script / nâng thư viện
./gradlew lintDevDebug
./gradlew :app:testDevDebugUnitTest :core:ui-foundation:testDebugUnitTest :core:ads:testDebugUnitTest :core:media:testDebugUnitTest
```
