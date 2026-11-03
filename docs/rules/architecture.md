# Kiến trúc & tổ chức thư mục

Clean Architecture: `presentation → domain ← data`. `domain` là tâm, không phụ thuộc tầng nào.

Base này **ít module**: `domain`, `data`, `feature` là **package trong `:app`**, chỉ hạ tầng dùng chung mới tách
thành `:core:*`. Hợp với app nhỏ/vừa (sync Gradle nhanh, ít boilerplate). Ranh giới tầng không còn do Gradle
chặn nên `ArchitectureTest` (`app/src/test`) soát import thay — xem [Quy tắc cứng](#quy-tắc-cứng).

## Module (8)

| Module | Package gốc | Chứa |
|---|---|---|
| `:app` | `com.base.app` | `BaseApp`, `MainActivity`, `navigation/AppNavGraph`, `di/`, `fcm/` + 3 tầng `domain/`, `data/`, `feature/` |
| `:core:utils` | `com.base.app.core.utils` | `config/AppConfig`, `error/DomainException` (hợp đồng dùng chung mọi tầng), `extension/`, `network/`, `notification/` |
| `:core:navigation` | `com.base.app.core.navigation` | `core/` (ScreenProvider, NavigationRequest, throttle), `destination/`, `deeplink/`, `launcher/` |
| `:core:analytics` | `com.base.app.core.analytics` | `Analytics`, `plugin/`, `crash/`, `remoteconfig/` — mọi thứ Firebase, sau interface |
| `:core:ui-foundation` | `com.base.app.core.ui.foundation` | `vm/` (BaseViewModel), `ext/` (CollectAsEffect), `error/` (→ string res), `locale/`, `format/` |
| `:core:design-system` | `com.base.app.core.designsystem` | `theme/`, `component/`, `icon/` |
| `:core:media` | `com.base.app.core.media` | ExoPlayer: pool player cho feed video dọc, cache, `DeviceTier` |
| `:core:ads` | `com.base.app.core.ads` | `InterstitialAdManager`, `RewardAdManager`, `NativeAdManager`, `AdsInitializer`, `policy/` (`AdsConfig`, `AdsPolicy`, use case bật/tắt placement), `config/` (đọc `ads_config` từ Remote Config). Chỉ module này phụ thuộc lib ads (`com.github.dinhn:ads-*`, vendor ở `third_party/maven`) |

```
:app                → mọi :core:*
:core:ads           → core:utils, core:analytics, core:design-system (+ lib ads)
:core:ui-foundation → core:utils
:core:utils, :core:navigation, :core:analytics, :core:design-system, :core:media → (không module nào)
```

`:core:*` **không bao giờ** phụ thuộc `:app`. Kiểu mà cả core lẫn app cần (cấu hình build, lỗi domain) nằm ở
`:core:utils`; nghiệp vụ ads (placement, policy) nằm luôn trong `:core:ads`.

## Tầng trong `:app`

```
app/src/main/java/com/base/app/
  BaseApp.kt  MainActivity.kt
  di/                 ConfigModule (BuildConfig → AppConfig), AppModule (AppScope, ThemeHolder)
  navigation/         AppNavGraph — đăng ký mọi ScreenProvider
  domain/             Kotlin thuần
    model/            Post.kt, Page.kt
    repository/       PostRepository.kt (interface)
    usecase/<area>/   GetPostsPageUseCase.kt — một hành động một class
  data/
    remote/{http,error,dto,log,datasource}
    local/{dao,database,entity,datasource}
    mapper/  repository/  util/
    di/DataModule.kt
  feature/<name>/
    di/<Name>Module.kt
    presentation/
      <screen>/
        FooContract.kt        FooViewState + FooIntent + FooSingleEvent
        FooViewModel.kt
        FooScreen.kt          FooScreen + FooContent (stateless, có preview)
        FooScreenProvider.kt  khi màn là destination (Destination @Serializable khai bên trong)
        <child>/              màn con của container (tab của Main)
        component/  model/
      common/                 composable dùng chung trong feature
```

Mẫu thật: `feature/main` — `presentation/main/` (container 3 tab: `home/`, `bookmarks/`, `settings/`),
`presentation/postdetail/`, `presentation/language/`. Luồng đủ tầng: `docs/guides/sample-flow.md`.

## Quy tắc cứng

`ArchitectureTest` fail khi vi phạm — chạy cùng `./gradlew :app:testDevDebugUnitTest`.

- `domain` không import `android.*`/`androidx.*`, `data`, `feature`, `core.*` (trừ `core.utils.error` và
  `core.utils.config`).
- `feature` không import `data` — chỉ thấy interface ở `domain`, DI bind impl.
- `data` không import `feature`.
- `feature/a` không import `feature/b` — mở màn của nhau qua `Destination` (ScreenProvider), dùng chức năng
  của nhau qua interface ở `domain` hoặc `:core:utils`.
- `:core:*` không chứa nghiệp vụ của app.

## Feature mới

- Màn mới của luồng có sẵn → package trong `feature/main/presentation/`.
- Luồng riêng (auth, checkout…) → `feature/<name>/` với `di/<Name>Module.kt`, thêm module vào `startKoin`
  trong `BaseApp`, đăng ký ScreenProvider trong `AppNavGraph`.
- Dữ liệu chỉ feature đó dùng vẫn đặt ở `domain/` + `data/` chung (gom theo `usecase/<area>/`) — không tạo
  `domain/data` lồng trong feature.

## Deeplink & launcher (khi app có)

Đặt ở `:core:navigation`:

```
core/navigation/
  deeplink/   DeepLinkConstants (scheme/host), DeepLinkType.fromUri(uri) → loại deeplink
  launcher/   LauncherAction (sealed, @Serializable) — hành động khi mở app từ notification/intent
              LauncherActionHandler.from(intent) — đọc extra / payload FCM ra LauncherAction
```

`MainActivity` đọc `LauncherAction` từ intent (chỉ khi `savedInstanceState == null` và không mở từ Recents),
map sang `Destination`, rồi điều hướng sau `NavigationThrottle.awaitWindow()`.

## DI

`BaseApp.startKoin` gom: `configModule`, `appModule` (`:app/di`), `analyticsModule` + `remoteConfigModule`
(`:core:analytics`), `dataModule` (`data/di`), `adsModule` (`:core:ads`), `mediaModule()` (`:core:media`),
và một module cho mỗi feature (`mainModule`).

## Khi nào tách module

Mặc định **không**. Tách thêm `:core:<tên>` chỉ khi là hạ tầng dùng lại giữa nhiều app hoặc SDK bên thứ ba
nhiều code (billing, ads) — luôn sau interface, không chứa nghiệp vụ. App lớn lên (nhiều team, build chậm) thì
tách `domain`/`data`/`feature` thành module là việc cơ học: package đã đúng ranh giới, `ArchitectureTest`
đã giữ chiều phụ thuộc.

Module mới: build file chỉ cần `plugins { id("base.android.library") }` + `namespace` + dependency riêng.
Cấu hình chung ở `build-logic/convention`.
