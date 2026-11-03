# Tầng data

## Cấu trúc `data` (package trong `:app`)

```
data/
  remote/
    http/         HttpClientProvider, ApiConfig, qualifier
    error/        ApiException, toDomainException()
    dto/          DTO + envelope của BE (BaseResponse, ApiErrorBody)
    log/          NetworkLogger
    datasource/   XRemoteDataSource (interface) + XRemoteDataSourceImpl
  local/          (thêm khi cần) dao/, database/, entity/, datasource/
  mapper/         extension toDomain() / toEntity() / toDto()
  repository/     XRepositoryImpl
  util/           withCatching, withLocalCatching, mapError
  di/             DataModule
```

## Repository và DataSource đều có interface

| Thành phần | Interface ở | Impl ở |
|---|---|---|
| `XRepository` | `domain/repository` | `data/repository/XRepositoryImpl` |
| `XRemoteDataSource` | `data/remote/datasource` | cùng package, `XRemoteDataSourceImpl` |
| `XLocalDataSource` | `data/local/datasource` | cùng package, `XLocalDataSourceImpl` |

Lý do: repository impl test được bằng fake datasource, đổi nguồn (Ktor → gRPC, Room → DataStore)
không động tới repository, và Find Usages trỏ đúng tầng.

```kotlin
// data/remote/datasource
interface ProfileRemoteDataSource {
    suspend fun getProfile(): ProfileDto
}

class ProfileRemoteDataSourceImpl(
    private val httpClient: HttpClient,
) : ProfileRemoteDataSource {
    override suspend fun getProfile(): ProfileDto = httpClient.get("me").body()
}

// data/repository
class ProfileRepositoryImpl(
    private val remote: ProfileRemoteDataSource,
    private val local: ProfileLocalDataSource,
) : ProfileRepository {
    override suspend fun refresh(): Result<Unit> = withCatching {
        local.save(remote.getProfile().toEntity())
    }.mapError { it.toDomainException() }

    override fun observeProfile(): Flow<Profile> = local.observe().map { it.toDomain() }
}

// data/di/DataModule
single<ProfileRemoteDataSource> { ProfileRemoteDataSourceImpl(get()) }
single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
```

### Nguồn theo flavor

Một interface có thể có hai impl cùng nói DTO (vd. `XRemoteDataSourceImpl` gọi Ktor và `XAssetDataSourceImpl`
đọc JSON trong `app/src/dev/assets`). `DataModule` chọn theo một field của `AppConfig`. Impl giả thiếu dữ liệu
phải ném `ApiException(404)` để chuỗi lỗi giống hệt mạng.

### Cache in-memory + single-flight

Dữ liệu nhiều màn cùng cần lúc mở app cache trong repository `single`, gọi đồng thời chỉ ra nguồn **một lần**
(`Mutex`). Không cache kết quả lỗi, để Retry gọi lại được.

## Trách nhiệm từng tầng

| Tầng | Làm | Không làm |
|---|---|---|
| DataSource | gọi Ktor/Room, nói chuyện bằng DTO/Entity | bắt exception, biết domain model |
| Repository | `withCatching { }` (IO + bắt lỗi), `mapError { it.toDomainException() }`, map DTO/Entity → domain, trả `Result<T>` / `Flow<T>` | rule nghiệp vụ |
| UseCase | rule nghiệp vụ (xem `usecase.md`) | đổi thread, try/catch |

`withCatching` = `withContext(Dispatchers.IO)` + `runCatching` + **ném tiếp `CancellationException`**.
Repository là chỗ **duy nhất** có try/catch.
Ghi Room/DataStore dùng `withLocalCatching { }` (→ `DomainException.Unknown`): `toDomainException()`
dịch `IOException` thành `Network`, sai với lỗi file local. Đường ghi cũng trả `Result<Unit>`, không
để exception lọt lên ViewModel.

## Mapper

Extension function trong `data/mapper/XMapper.kt`, không dùng interface mapper:

```kotlin
fun ProfileDto.toDomain(): Profile = Profile(id = id, name = name)
fun ProfileDto.toEntity(): ProfileEntity = ...
fun List<ProfileDto>.toDomain(): List<Profile> = map { it.toDomain() }
```

## DTO

DTO tả **đúng contract của BE** — đừng ép mọi API vào một envelope chung. BE dùng
`{ statusCode, message, data, errorCode }` thì dùng `BaseResponse<T>`; khác thì viết DTO riêng.
Field nullable / có default khi BE có thể thiếu.

## Network

- `provideHttpClient` tự ném `ApiException` cho status ngoài 2xx (body lỗi parse vào `ApiErrorBody`) —
  datasource không tự kiểm status.
- Base URL tự thêm `/` cuối; path trong datasource viết tương đối (`"me"`, không `"/me"`).
- Nhiều BE → mỗi BE một client, phân biệt bằng `httpClientQualifier("Name")`.

## Room

Đã cấu hình sẵn trong `data/build.gradle.kts` (plugin `ksp` + `androidx.room`, schema export ở `data/schemas/`).
`AppDatabase` ở `data/local/database`; thêm entity vào `entities = [...]`, DAO vào database, bind DAO trong
`DataModule`. **Không** `fallbackToDestructiveMigration()` — bump version thì viết Migration.

Mẫu đủ tầng (remote + local + mapper + repository + DI): `docs/guides/sample-flow.md`. Thêm API mới từng bước:
`docs/guides/new-api.md`.
