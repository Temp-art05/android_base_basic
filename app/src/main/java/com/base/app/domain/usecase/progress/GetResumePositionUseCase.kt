package com.base.app.domain.usecase.progress

import com.base.app.domain.repository.WatchProgressRepository

/** Vị trí mở lại một video: chỗ đã dừng; chưa xem hoặc đã xem gần hết thì từ đầu. */
class GetResumePositionUseCase(
    private val watchProgressRepository: WatchProgressRepository,
) {
    suspend operator fun invoke(videoId: String): Long {
        val progress = watchProgressRepository.get(videoId) ?: return 0
        val finished = progress.durationMs > 0 && progress.durationMs - progress.positionMs < FINISHED_MARGIN_MS
        return if (finished) 0 else progress.positionMs
    }

    companion object {
        /** Dừng cách cuối dưới 3 s coi như xem hết — mở lại xem từ đầu thay vì thấy ngay màn kết thúc. */
        const val FINISHED_MARGIN_MS = 3_000L
    }
}
