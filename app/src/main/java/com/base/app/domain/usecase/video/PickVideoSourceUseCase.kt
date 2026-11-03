package com.base.app.domain.usecase.video

import com.base.app.domain.model.Quality
import com.base.app.domain.model.Video
import com.base.app.domain.model.VideoSource

class PickVideoSourceUseCase {

    /**
     * URL để phát: [preferredQuality] nếu video có, không thì quality cao nhất **không vượt** [preferredQuality]
     * (trần do Auto hạ), không có nữa thì cao nhất. Không có nguồn → null.
     */
    operator fun invoke(video: Video, preferredQuality: String? = null): VideoSource? {
        val qualities = qualitiesOf(video)
        if (qualities.isEmpty()) return null
        val quality = preferredQuality?.let { preferred ->
            preferred.takeIf { it in video.sources } ?: highestAtMost(preferred, qualities)
        } ?: qualities.first()
        return VideoSource(quality, video.sources.getValue(quality))
    }

    /** Quality có thật của video, cao → thấp — không bịa 1080p. */
    fun qualitiesOf(video: Video): List<String> =
        video.sources.filterValues { it.isNotBlank() }.keys.sortedWith(Quality.HighestFirst)

    private fun highestAtMost(cap: String, qualities: List<String>): String? {
        val capHeight = Quality.heightOf(cap) ?: return null
        return qualities.firstOrNull { (Quality.heightOf(it) ?: return@firstOrNull false) <= capHeight }
    }
}
