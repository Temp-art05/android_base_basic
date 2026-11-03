# Use case

Class thường ở `domain/usecase/<area>/` (vd. `usecase/post/GetPostsPageUseCase.kt`), nhận repository qua constructor. **Không base class, không
truyền dispatcher.** Trả stream thì trả thẳng `Flow<T>`, không bọc `Result`.

## Một hành động — một class có `invoke`

Một class = **một hành động nghiệp vụ**, tên `VerbNounUseCase`, cửa vào là `operator fun invoke`. Gom
theo thư mục năng lực (`usecase/post/`, `usecase/bookmark/`…), không gom thành một class to.

```kotlin
// ĐÚNG — usecase/post/GetPostsPageUseCase.kt
class GetPostsPageUseCase(
    private val postRepository: PostRepository,
) {
    suspend operator fun invoke(loaded: List<Post>): Result<Page<Post>> { ... }
}

// VM
private val getPostsPage: GetPostsPageUseCase
val page = getPostsPage(loaded = currentState.posts)

// SAI — một class gánh cả năng lực
class PostUseCase(...) {
    suspend fun firstPage(); suspend fun nextPage(...); fun observeBookmarks(); suspend fun toggleBookmark(...)
}
```

- Hàm phụ **rất sát nghĩa** với hành động chính được ở chung class (vd. `GetPostsPageUseCase.PAGE_SIZE`).
- Đặt tên: `Get…` (đọc một lần), `Observe…` (trả `Flow`), `Save/Set/Complete…` (ghi), động từ nghiệp vụ khác
  khi rõ hơn (`SearchShowsUseCase`).
- Truy vấn thuần trên model (lọc/tra trong một list) là **extension** ở `domain/model` (`XQueries.kt`),
  không phải use case. Policy thuần không phụ thuộc gì là `object` (`PlaybackQualityPolicy`).
- Use case được gọi use case khác qua constructor.

## Rule nằm ở đâu

Mọi quyết định nằm ở use case: gate, đếm, ngưỡng, lịch, phân loại lỗi, kích thước trang, trang kế,
khử trùng lặp, chuỗi gọi nhiều repository, gom/trộn nhiều stream.

ViewModel chỉ còn: map sang state, string res, navigation, analytics, job/lifecycle, nhịp UI
(debounce, animation).

- Paging trả `Page<T>(items, hasMore)`; ViewModel không so `count >= PAGE_SIZE`.
- Remote Config: `domain` không thấy `:core:analytics` ⇒ ViewModel đọc RC rồi **truyền giá trị** vào use case.
- VM inject từng use case nó dùng; **cấm** facade gói dependency (`XxxDeps`) để né constructor dài.

## Khi nào ViewModel inject thẳng, không qua use case

Chỉ ba nhóm, ngoài ra phải có use case:

| Nhóm | Ví dụ |
|---|---|
| Cổng hạ tầng (SDK sau interface) | `Analytics`, `BillingGateway` |
| Preference **hiển thị** của chính màn đó | ngôn ngữ, theme, cờ đã xem hướng dẫn |
| Nguồn cấu hình | `RemoteConfigSource`, `AppConfig` |

Dễ nhầm: nằm trong DataStore không có nghĩa là "preference". Counter, cap, số lượt… phục vụ rule
nghiệp vụ là **dữ liệu nghiệp vụ** ⇒ đi qua use case.

## DI

Khai `factory { GetPostsPageUseCase(get()) }` trong `DataModule`. Dùng `single` chỉ khi use case
giữ state in-memory có chủ đích (cache, single-flight) — ghi rõ lý do bằng comment.
