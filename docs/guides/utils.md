# Tiện ích có sẵn (`:core:utils`)

## Toast — `AppToastHost` (Compose, `:core:design-system`)

Toast của app vẽ bằng Compose (đề cấm XML layout, nên `Toasty` của base đã gỡ):

```kotlin
val toastState = rememberToastState()
viewModel.singleEvent.CollectAsEffect { event ->
    if (event is XxxSingleEvent.ShowToast) toastState.show(text = resources.getString(event.messageRes), success = true)
}
AppToastHost(state = toastState, modifier = Modifier.align(Alignment.BottomCenter))
```

- 2 s, chấm xanh = thành công, chấm vàng = thông báo khoá/không khả dụng.
- ViewModel chỉ phát `SingleEvent` mang `@StringRes`, không giữ Context.

## Context extension

| Hàm | Việc |
|---|---|
| `context.openUrl(url)` | mở URL, trả `false` nếu không có app mở được |
| `context.shareText(text)` | bảng chia sẻ hệ thống |
| `context.copyToClipboard(text)` | copy (Android 13+ hệ thống tự hiện xác nhận) |
| `context.findActivity()` | lấy Activity từ ContextWrapper |

## Khác

| Thứ | Ở đâu | Việc |
|---|---|---|
| `NetworkUtils.isOnline(context)` | `network/` | có internet đã xác thực (không chỉ "đã nối wifi") |
| `NotificationHelper` | `notification/` | channel mặc định + hiện notification; `:app` truyền Intent + icon |
| `Flow.throttleFirst` / `select` / `catchAndLog` | `extension/FlowExt.kt` | chống double-click, chọn một phần state, log lỗi stream |
| `bridge/` | — | interface gọi chức năng chéo feature, xem `docs/rules/architecture.md` |

Thêm tiện ích mới: không UI, không nghiệp vụ, dùng ≥ 2 nơi → `:core:utils`. Có Compose UI → `:core:design-system`
hoặc `:core:ui-foundation`.
