# Checklist tạo project mới từ base

Xem `README.md` cho cấu trúc, `docs/rules/` cho quy ước code.

## 1. Đổi định danh

- [ ] `settings.gradle.kts` → `rootProject.name`
- [ ] `app/build.gradle.kts` → `namespace` + `applicationId`
- [ ] Package `com.base.app` → package thật: thư mục `app/src/{main,test}/java/...` và `core/*/src/.../java/...`,
      `namespace` của từng `core/*/build.gradle.kts`, `AndroidManifest.xml` (`BaseApp`, `MainActivity`, FCM service)
      (`build-logic` giữ `com.base.app.convention` cũng được)
- [ ] `app/src/main/res/values/strings.xml` → `app_name`
- [ ] Launcher icon: `app/src/main/res/mipmap-*` + `ic_launcher_background/foreground.xml`

## 2. Brand

- [ ] `core/design-system/.../theme/AppColor.kt` → màu brand (giữ tên token)
- [ ] `AppTheme.kt` → chỉnh `LightColorScheme` / `DarkColorScheme` nếu cần
- [ ] Font trong `core/design-system/src/main/res/font/` — đổi thì sửa `AppTypography.kt`
- [ ] `Wordmark` (`core/design-system/.../component/Brand.kt`) → logo chữ của app

## 3. API

- [ ] `app/build.gradle.kts` → `BASE_URL` cho flavor `dev` và `prod` (hiện `https://dummyjson.com/` phục vụ luồng mẫu)
- [ ] Field build-time mới: sửa `AppConfig` (`:core:utils/config`) + `di/ConfigModule.kt`
- [ ] `ApiErrorBody` (`data/remote/dto`) → khớp body lỗi của BE
- [ ] Mã lỗi nghiệp vụ cần phân nhánh → subclass `DomainException` + map trong `toDomainException()`
- [ ] Mỗi API: DTO → `XRemoteDataSource` + `Impl` → `XRepository` (`domain`) + `Impl` → use case một hành động
      → khai trong `DataModule` (xem `docs/guides/new-api.md`)

## 4. Firebase (tuỳ chọn)

- [ ] Tạo project Firebase, đăng ký app (cả `.dev` nếu giữ `applicationIdSuffix`)
- [ ] Thả `google-services.json` vào `app/` — 2 plugin tự bật, không sửa code
- [ ] Khai báo remote config key ở `:core:analytics` → `RemoteKeys.ALL_KEYS`

## 5. Ads (tuỳ chọn)

- [ ] `AndroidManifest.xml` → `APPLICATION_ID` AdMob thật (đang là app id test của Google)
- [ ] Ad unit id thật trong enum `AdType` của `InterstitialAdManager` / `RewardAdManager` / `NativeAdManager`
- [ ] Placement của app trong `AdPlacement` (`:core:ads/policy`) + key `ads_config` trên Remote Config
- [ ] Không dùng ads: bỏ `:core:ads` khỏi `settings.gradle.kts`, `app/build.gradle.kts`, `BaseApp`

## 6. Signing

- [ ] Tạo keystore (không commit — `*.jks` đã gitignore)
- [ ] Thêm vào `local.properties`:
      ```
      RELEASE_STORE_FILE=key/release.jks
      RELEASE_STORE_PASSWORD=...
      RELEASE_KEY_ALIAS=...
      RELEASE_KEY_PASSWORD=...
      ```

## 7. Ngôn ngữ

- [ ] Thêm/bớt locale sửa **cả 3 chỗ**: `res/xml/locales_config.xml`, `localeFilters` trong
      `app/build.gradle.kts`, enum `AppLocale` (`:core:ui-foundation`)

## 8. Thay phần mẫu

- [ ] Xoá luồng mẫu theo mục "Xoá khi làm project thật" trong `docs/guides/sample-flow.md`
- [ ] Sửa enum `MainTab` + `MainScreen` cho tab thật (giữ `MainBottomNavBar`, Settings, Language nếu dùng)
- [ ] Không dùng video: bỏ `:core:media` (settings, `app/build.gradle.kts`, `mediaModule()` trong `BaseApp`)

## 9. Nên làm sớm

- [ ] CI: build dev + prod, lint, unit test (gồm `ArchitectureTest`)
- [ ] detekt / ktlint
- [ ] Baseline profile sau khi app có màn hình thật
