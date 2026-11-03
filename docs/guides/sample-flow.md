# Luồng mẫu: Post feed + Bookmark

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

## Xoá khi làm project thật

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

Không dùng Room: bỏ plugin `ksp` + `androidx.room`, block `room {}`, `bundles.room`, `room.compiler` khỏi
`app/build.gradle.kts`, và hai dòng `apply false` ở `build.gradle.kts` gốc.
