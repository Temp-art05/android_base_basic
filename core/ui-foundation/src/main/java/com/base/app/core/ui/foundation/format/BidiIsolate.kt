package com.base.app.core.ui.foundation.format

/**
 * Cô lập hướng chữ của phần **nội dung** (tên phim, thể loại) khi ghép với chữ giao diện bằng " · ". Ngôn ngữ UI và ngôn ngữ
 * nội dung chọn riêng, nên có lúc trộn hướng (UI tiếng Pháp + nội dung tiếng Ả Rập): không cô lập thì
 * "فانتازيا · 45 ép. · terminée" hiện thành "45 فانتازيا · ép. · terminée". FSI…PDI (Unicode 6.3) — Android hỗ trợ từ API 18.
 */
fun String.bidiIsolated(): String = "$FIRST_STRONG_ISOLATE$this$POP_DIRECTIONAL_ISOLATE"

private const val FIRST_STRONG_ISOLATE = '⁨'
private const val POP_DIRECTIONAL_ISOLATE = '⁩'
