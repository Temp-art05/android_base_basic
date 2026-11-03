package com.base.app.domain.usecase.progress

import com.base.app.domain.model.WatchProgress
import com.base.app.domain.repository.WatchProgressRepository

class SaveWatchProgressUseCase(
    private val watchProgressRepository: WatchProgressRepository,
) {
    /** Chưa biết thời lượng (player chưa nạp xong) thì không lưu — bản ghi không dùng được để resume. */
    suspend operator fun invoke(progress: WatchProgress): Result<Unit> {
        if (progress.durationMs <= 0) return Result.success(Unit)
        return watchProgressRepository.save(progress.copy(positionMs = progress.positionMs.coerceIn(0, progress.durationMs)))
    }
}
