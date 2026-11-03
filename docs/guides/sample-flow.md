# Luồng mẫu

Base có hai luồng mẫu chạy thật: **Post feed + Bookmark** (CRUD thường: API, Room, phân trang) và
**Reels + Player** (video dọc: pool player, preload, cache). Xoá được độc lập nhau.

## 1. Post feed + Bookmark

Một luồng chạy thật, đủ mọi tầng — đọc theo thứ tự dưới đây là hiểu cả base. API: `https://dummyjson.com/posts`.
Mọi file của luồng nằm trong `:app` (`app/src/main/java/com/base/app/`).

```
HomeScreen ─▶ HomeViewModel ─▶ GetPostsPageUseCase ─▶ PostRepository (domain, interface)
                                                         │
                               PostRepositoryImpl (data) ─▶ PostRemoteDataSource ─▶ Ktor
           ─▶ ToggleBookmarkUseCase / ObserveBookmarkedIdsUseCase ─▶ BookmarkRepository
                               BookmarkRepositoryImpl (data) ─▶ BookmarkLocalDataSource ─▶ Room
```

| # | Tầng | File | Việc |
|---|---|---|---|
| 1 | domain/model | `domain/model/Post.kt`, `Page.kt` | model thuần Kotlin |
| 2 | domain/repository | `domain/repository/PostRepository.kt`, `BookmarkRepository.kt` | interface — `Result` cho lệnh, `Flow` cho stream |
| 3 | domain/usecase | `domain/usecase/post/`, `usecase/bookmark/` | một hành động một class; rule: kích thước trang, offset trang kế, khử trùng lặp |
| 4 | data/remote | `data/remote/dto/PostDto.kt`, `remote/datasource/PostRemoteDataSource(+Impl).kt` | gọi Ktor, trả DTO |
| 5 | data/local | `data/local/{entity,dao,database,datasource}/` | Room: `BookmarkEntity`, `BookmarkDao`, `AppDatabase`, `BookmarkLocalDataSource(+Impl)` |
| 6 | data/mapper | `data/mapper/PostMapper.kt` | DTO/Entity ↔ domain (extension function) |
| 7 | data/repository | `data/repository/PostRepositoryImpl.kt`, `BookmarkRepositoryImpl.kt` | `withCatching` + `mapError { toDomainException() }` |
| 8 | data/di | `data/di/DataModule.kt` | bind theo interface, use case là `factory` (dòng `// SAMPLE`) |
| 9 | presentation | `feature/main/presentation/main/home/`, `main/bookmarks/`, `postdetail/`, `common/PostItem.kt` | Contract + ViewModel (`processIntent`) + Screen/Content (callback tường minh) |
| 10 | navigation | `postdetail/PostDetailScreenProvider.kt`, `main/MainScreenProvider.kt`, `navigation/AppNavGraph.kt` | destination có tham số (`postId`), provider map callback → `NavigationRequest` |
| 11 | feature/di | `feature/main/di/MainModule.kt` | `viewModelOf(::HomeViewModel)` |
| 12 | test | `app/src/test/.../GetPostsPageUseCaseTest`, `PostMapperTest`, `HomeViewModelTest` | fake repository, `MainDispatcherRule` |

Những gì luồng này minh hoạ:
- **Phân trang** `Page<T>(items, hasMore)` — ViewModel không tự so `size >= PAGE_SIZE`; cuộn gần cuối thì `LoadMore`.
- **Remote + local cùng phục vụ một màn**: list từ API, trạng thái bookmark stream từ Room. Bookmark ở Detail
  thì Home và tab Bookmarks tự cập nhật.
- **Destination có tham số**: `PostDetail(postId)` → `SavedStateHandle.toRoute()` trong ViewModel.
- **Lỗi**: mất mạng → `DomainException.Network` → `UiError` → `UiErrorState` (load lần đầu) hoặc nút thử lại cuối
  list (load thêm) / toast `AppToastHost` (bookmark lỗi).
- **Event một lần**: bookmark xong → `ShowToast` → `AppToastHost`; mở màn → `OpenPostDetail` → callback điều hướng.
- **Không reload khi đổi tab**: load ở `init` của ViewModel, ViewModel scope theo Main.

## 2. Reels + Player (video dọc)

Feed: `sample-api/videos.json` trong repo, tải qua raw GitHub (đổi URL ở `VideoRemoteDataSourceImpl`). Video lấy từ
bucket test của Media3, có một video hai bản 480p/240p để thử chọn quality.

```
ReelsScreen / PlayerScreen ─▶ ReelsViewModel / PlayerViewModel ─┬─▶ EpisodeFeedPlayer (:core:media, VM sở hữu)
                                                                ├─▶ GetVideoFeedUseCase ─▶ VideoRepository
                                                                │     VideoRepositoryImpl: RAM single-flight → mạng → đĩa (offline)
                                                                ├─▶ PickVideoSourceUseCase / PlaybackQualityPolicy
                                                                └─▶ Get/SaveWatchProgress ─▶ WatchProgressRepository ─▶ Room
```

| Tầng | File | Việc |
|---|---|---|
| domain | `model/{Video,WatchProgress}.kt`, `repository/{Video,WatchProgress}Repository.kt` | `Video.sources` = quality → URL; `Quality.HighestFirst` |
| domain | `usecase/video/` | `GetVideoFeedUseCase` (bỏ video không nguồn), `PickVideoSourceUseCase` (quality ưa thích / trần / cao nhất), `PlaybackQualityPolicy` (hạ quality khi buffer > 5 s, chỉ khi Auto) |
| domain | `usecase/progress/` | resume (xem gần hết → từ đầu), lưu tiến độ |
| data | `remote/dto/VideoDto.kt`, `remote/datasource/VideoRemoteDataSource(+Impl)` | GET URL tuyệt đối |
| data | `local/datasource/VideoFeedCacheLocalDataSource(+Impl)` | **cache JSON ra đĩa**: ghi file tạm rồi rename, đọc hỏng → null |
| data | `local/{entity,dao,datasource}/WatchProgress*` | Room, `@Upsert` |
| data | `repository/VideoRepositoryImpl.kt` | Mutex single-flight + RAM; lỗi mạng → bản đĩa; feed rỗng không ghi đè bản tốt |
| presentation | `main/reels/` | tab Reels |
| presentation | `player/` + `player/component/` | màn Player, `PlayerTrack` kéo tua, `PlaybackOptionsSheet` |
| app | `BaseApp` | `mediaModule(tierOverride)` + `prepareMediaCache()` nền |
| test | `PickVideoSourceUseCaseTest`, `PlaybackQualityPolicyTest`, `GetResumePositionUseCaseTest`, `VideoRepositoryImplTest`, `ReelsViewModelTest`, `PlayerViewModelTest` | `FakeFeedPlayer` (testFixtures `:core:media`) |

Công nghệ video (ở `:core:media`, hai màn dùng chung):
- **Pool player + preload**: video hiện tại phát, lân cận (máy thường ±1, máy yếu +1) giữ player **đã prepare +
  pause** ở giây 0 → vuốt nhẹ đã thấy khung hình thật, vuốt hẳn phát ngay. Pager `beyondViewportPageCount = 1` để
  surface lân cận đã dựng. `RoleLoadControl` cho player lân cận buffer ít hơn player chính.
- **Cache byte video**: `SimpleCache` một instance, mọi player đọc/ghi chung — vuốt ngược, mở lại video đã lướt phát
  từ đĩa. Giữ N video gần nhất (`RecentVideosEvictor`, theo `MediaProfile`), xoá sạch lúc process khởi động
  (`prepareMediaCache` trên IO).
- **Theo máy**: `DeviceTier` (low-RAM / heap / tổng RAM) → `MediaProfile` (số player, buffer, dung lượng cache).
  Flavor dev ép mức: `adb shell run-as com.base.app.dev touch files/force_low_tier` rồi mở lại app.

Hành vi màn:
- **Reels**: chỉ phát khi tab đang chọn **và** màn STARTED; ngoài ra `setActive(false)` nhả mọi decoder (mở Player
  từ Reels không có hai pool cùng giữ decoder). Hết reel → tự cuộn sang reel kế. Không lưu tiến độ (xem thử).
  Feed lấy quality ≤ `FEED_QUALITY_CAP` cho preload rẻ.
- **Player**: vuốt dọc qua cả feed; mở video nào cũng xem tiếp chỗ đã dừng (Room). Lưu mỗi 5 s, khi vuốt đi, pause,
  vào nền, back. Vào nền: pause + `setPreloadActive(false)`; quay lại phát tiếp nếu trước đó đang phát. Auto quality
  hạ khi buffer lâu, có nút quay lại quality cao. Video/tốc độ/quality trong `SavedStateHandle` (sống qua process
  death). Xoay máy không rebuffer (VM giữ player; STOP do đổi cấu hình bị bỏ qua).
- **Tiến độ tách khỏi state**: `playbackProgress` là `StateFlow` riêng, chỉ thanh track đọc trong lambda → tick vị trí
  không recompose cả màn. Gắn `itemId` để vừa lật trang không vẽ vị trí của video trước.

## Xoá khi làm project thật

### Luồng post

- `domain`: `model/{Post,Page}.kt` (giữ `Page` nếu có phân trang), `repository/{Post,Bookmark}Repository.kt`,
  `usecase/post/`, `usecase/bookmark/`
- `data`: `remote/dto/PostDto.kt`, `remote/datasource/Post*`, `local/{entity,dao}/Bookmark*`,
  `local/datasource/Bookmark*`, `mapper/PostMapper.kt`, `repository/{Post,Bookmark}RepositoryImpl.kt`,
  các dòng `// SAMPLE` trong `DataModule`; thay entity trong `AppDatabase` (giữ file nếu dùng Room)
- `app/schemas/` khi đổi entity (hoặc bump version + viết Migration)
- `feature/main/presentation/main/{home,bookmarks}/`, `postdetail/`, `common/PostItem.kt` → thay bằng màn thật;
  sửa `MainTab`, `MainScreen`, `MainScreenProvider`, `AppNavGraph`, `MainModule`
- String `<!-- SAMPLE -->` trong `app/src/main/res/values*/strings.xml`
- Test mẫu trong `app/src/test` (giữ `ArchitectureTest`, `MainDispatcherRule`)
- `BASE_URL` trong `app/build.gradle.kts`

### Luồng video (thay bằng nguồn video thật — giữ cơ chế)

Có video: giữ `main/reels/`, `player/`, use case `video/` + `progress/`, `VideoRepositoryImpl` (đổi
`VideoRemoteDataSourceImpl` sang API thật, sửa `VideoDto`). Không có video:
- `domain`: `model/{Video,WatchProgress}.kt`, `repository/{Video,WatchProgress}Repository.kt`, `usecase/video/`, `usecase/progress/`
- `data`: `remote/dto/VideoDto.kt`, `remote/datasource/Video*`, `local/datasource/{VideoFeedCache,WatchProgress}*`,
  `local/{entity,dao}/WatchProgress*`, `mapper/VideoMapper.kt`, `repository/{Video,WatchProgress}RepositoryImpl.kt`,
  dòng `// SAMPLE (video)` trong `DataModule` và `MainModule`; bỏ `WatchProgressEntity` khỏi `AppDatabase`
- `feature/main/presentation/main/reels/`, `player/`; tab `Reels` trong `MainTab`/`MainScreen`, `onOpenPlayer` ở
  `MainScreenProvider`, `PlayerScreenProvider` ở `AppNavGraph`
- String `<!-- SAMPLE (video) -->`, `sample-api/`, test video trong `app/src/test`
- Rồi bỏ `:core:media` (xem `TODO.md`)

Không dùng Room: bỏ plugin `ksp` + `androidx.room`, block `room {}`, `bundles.room`, `room.compiler` khỏi
`app/build.gradle.kts`, và hai dòng `apply false` ở `build.gradle.kts` gốc.
