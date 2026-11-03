# Kết quả & xử lý lỗi

Dùng **`kotlin.Result`** của stdlib, không tự định nghĩa kiểu kết quả riêng.

## Chuỗi lỗi

```
Ktor ném (status ngoài 2xx → ApiException, IO, timeout, serialization)
  → Repository: withCatching { ... }.mapError { it.toDomainException() }     (data)
  → DomainException.Network / Timeout / Unauthorized / Server / Unknown      (core:utils, domain dùng)
  → ViewModel: .onFailure { updateState { copy(errorRes = it.toErrorRes()) } }  (:core:ui-foundation)
```

- ViewModel chỉ thấy `DomainException`, không import `data` / Ktor.
- Lỗi nghiệp vụ cần phân nhánh (hết lượt, hết hạn gói, tên không hợp lệ…) → thêm subclass
  `DomainException`, map ở tầng data (từ `ApiException.errorCode`) hoặc trả từ use case.
- `toErrorRes()` map về string dùng chung ở `:core:ui-foundation`: `error_network`, `error_timeout`,
  `error_server`, `error_unauthorized`, `error_unexpected`. Lỗi riêng của màn thì `when` ở ViewModel.

## Cấm

- try/catch ở use case hay ViewModel — repository đã lo.
- Nuốt `CancellationException` (dùng `withCatching`, đừng `runCatching` trần quanh suspend call).
- Hiện `ApiException.serverMessage` cho user — text server sinh, không theo ngôn ngữ app.
- Giữ `String` lỗi đã resolve trong state.

## Flow

Stream không bọc `Result`. Lỗi trong stream: `.catch { }` ở repository (map sang giá trị mặc định
hoặc ném `DomainException`), hoặc `catchAndLog()` (`:core:utils`) khi chỉ cần log.
