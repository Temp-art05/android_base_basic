# CLAUDE.md

Hướng dẫn cho Claude Code khi làm việc trong repo này.

Android base: Clean Architecture, 100% Compose, **ít module** — `domain`, `data`, `feature` là package trong `:app`,
hạ tầng dùng chung ở 7 module `:core:*` (xem `docs/rules/architecture.md`). Checklist dựng project mới: `TODO.md`.

## Rule bắt buộc — đọc trước khi viết code

| File | Nội dung |
|---|---|
| [`docs/rules/architecture.md`](docs/rules/architecture.md) | module, tầng trong `:app`, chiều phụ thuộc (`ArchitectureTest` giữ), khi nào tách module |
| [`docs/rules/usecase.md`](docs/rules/usecase.md) | use case **một hành động một class + `invoke`**, rule nằm ở đâu, khi nào VM inject thẳng |
| [`docs/rules/data-layer.md`](docs/rules/data-layer.md) | repository + datasource **có interface/Impl**, mapper, DTO, network, Room |
| [`docs/rules/presentation.md`](docs/rules/presentation.md) | ViewModel/Screen, **MVI: mọi hành động qua `processIntent`**, **callback tường minh**, navigation, Main tab |
| [`docs/rules/error-handling.md`](docs/rules/error-handling.md) | `Result`, chuỗi `ApiException → DomainException → @StringRes` |

Hướng dẫn & mẫu:

| File | Nội dung |
|---|---|
| [`docs/guides/sample-flow.md`](docs/guides/sample-flow.md) | luồng mẫu post feed + bookmark đủ tầng — đọc để hiểu base; cách xoá |
| [`docs/guides/new-api.md`](docs/guides/new-api.md) | thêm một API mới từng bước (domain → data → presentation) |
| [`docs/guides/utils.md`](docs/guides/utils.md) | Toast Compose, Context ext, NetworkUtils, NotificationHelper |

Quy ước presentation hay bị làm sai:
- **Không** truyền `onNavigate: (NavigationRequest) -> Unit` hay `onIntent: (FooIntent) -> Unit` xuống Screen/Content.
  Mỗi đích điều hướng một callback (`onBack`, `onOpenPostDetail(postId)`), mỗi hành động một lambda có tên.
  Screen là chỗ duy nhất gọi `processIntent`; `ScreenProvider` (cùng package với màn) là chỗ duy nhất tạo `NavigationRequest`.
- Màn con của container nằm trong package container (tab của Main ở `presentation/main/<tab>/`).

## Build & test

```bash
./gradlew :app:assembleDevDebug          # dev flavor, debug
./gradlew :app:assembleProdRelease       # prod flavor, release (R8 + shrinkResources)
./gradlew lintDevDebug
./gradlew :app:testDevDebugUnitTest :core:ui-foundation:testDebugUnitTest :core:ads:testDebugUnitTest :core:media:testDebugUnitTest
```

Sau khi sửa build script hoặc nâng thư viện, **luôn chạy cả `assembleProdRelease`**: AGP 9 bật
`android.r8.strictFullModeForKeepRules` mặc định nên release có thể gãy dù debug xanh.

`ArchitectureTest` (`app/src/test`) soát import: `domain` thuần Kotlin, `feature` không import `data`, `data` không
import `feature`, feature không import nhau. Test đỏ = sai tầng, sửa code chứ đừng nới test.

## Toolchain

AGP 9.2 · Gradle 9.8 · JDK 17 · Kotlin 2.4 · compileSdk/targetSdk 37 · minSdk 24.

AGP ghim ở 9.2.1 vì Android Studio Quail 1 (2026.1.1) chỉ sync được tới bản này. Nâng AGP phải đối chiếu bản Studio
của cả team: `version.properties` trong `Android Studio.app/Contents/plugins/android/lib/libagp-version.jar`
→ `lastStableBuildVersion`.

AGP 9 dùng **built-in Kotlin**: module Android **không** apply `org.jetbrains.kotlin.android`. Không có block
`kotlinOptions`. JDK 17 do toolchain quy định; máy chưa có thì Gradle tự tải (plugin foojay).

R8: module library không minify — keep rule của library phải nằm trong `consumer-rules.pro`.
Ktor / kotlinx-serialization / Navigation tự kèm rule.

## build-logic (convention plugins)

| Plugin | Dùng cho |
|---|---|
| `base.android.application` | `:app` |
| `base.android.library` | mọi module `:core:*` |
| `base.android.compose` | module có Compose (áp thêm lên 2 cái trên) |

`compileSdk`/`targetSdk`/`minSdk`/Java version khai **một chỗ**: `build-logic/.../ProjectExt.kt` → `object AndroidConfig`.
`:app` tự apply `kotlin.serialization` (Destination `@Serializable`), `ksp` + `androidx.room`.

## BuildConfig isolation

BuildConfig (`BASE_URL`, `IS_DEBUG`, flavor, version) **chỉ tồn tại trong `:app`**. `ConfigModule` map nó vào
`AppConfig` (`:core:utils/config`) và bind làm Koin single; nơi khác inject `get<AppConfig>()`.
Thêm field build-time: sửa `AppConfig` + `ConfigModule.kt`.

Flavor `dev` có `applicationIdSuffix = ".dev"`. Signing release đọc từ `local.properties`
(`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`); thiếu thì APK unsigned.
Dùng Firebase thật mà chỉ đăng ký một app thì bỏ `applicationIdSuffix` của `dev`.

## DI (Koin)

`BaseApp.onCreate()` gọi `startKoin` với `configModule`, `appModule` (`AppScope`, `ThemeHolder`, `ConnectivityMonitor`),
`analyticsModule(isDebug)`, `remoteConfigModule`, `dataModule`, `adsModule`, `mediaModule()` và một module cho mỗi
feature (`mainModule`). Sau đó `AdsInitializer.start()`. ViewModel inject qua `koinViewModel()`.

## Core

- `:core:*` **không** phụ thuộc `:app`. Kiểu dùng chung cho cả core lẫn app nằm ở `:core:utils`
  (`AppConfig`, `DomainException`); nghiệp vụ ads (`AdsConfig`, `AdsPolicy`, use case placement) ở `:core:ads/policy`.
- Lib ads `com.github.dinhn:ads-core|ads-admob|ads-firebase` 1.1.1 vendor ở `third_party/maven`. Chỉ `:core:ads`
  phụ thuộc lib; `ads-admob` kéo adapter mediation → cần repo Pangle + Mintegral trong `settings.gradle.kts`.
  Native dựng `NativeAdView` bằng code (không XML layout).
- Firebase nằm ở `:core:analytics`, sau interface, mặc định no-op (`Analytics`, `CrashReporter`, `RemoteConfigSource`).
  Guard duy nhất: `isFirebaseAvailable(context)`. Có `app/google-services.json` thì 2 plugin Firebase tự bật.

## Theme, i18n, ảnh

- `AppTheme` mặc định **dark**, light là bộ token tạm. Chủ ý **không** bật dynamic color.
- Ngôn ngữ UI: 10 locale `en, hi, ta, id, ar, fr, es, pt-BR, tr, de` (`LaunchLanguages` ở `:core:ui-foundation`; `ar` RTL).
  Thư mục: `values-hi/ta/in/ar/fr/es/pt-rBR/tr/de` (Indonesia dùng mã cũ `in`). Thêm locale sửa **cả 3 chỗ**:
  `res/xml/locales_config.xml`, `localeFilters` trong `app/build.gradle.kts`, enum `AppLocale` (`:core:ui-foundation`).
- **Thêm/sửa string là phải dịch luôn đủ 9 ngôn ngữ còn lại** trong cùng thay đổi. Chuỗi không dịch đánh
  `translatable="false"`. Lint `MissingTranslation` (error) chặn nếu sót.
- Ảnh qua `AppAsyncImage`; `appImageLoader`: memory cache theo % RAM, disk cache 100 MB TTL 7 ngày.
