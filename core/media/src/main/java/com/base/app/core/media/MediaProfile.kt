package com.base.app.core.media

/**
 * Ngân sách media theo [DeviceTier]. Số khởi điểm (chưa A/B). Mọi máy đều giữ player cho tập trước và
 * tập kế (vuốt hai chiều phải tức thì); máy yếu chỉ bớt buffer và giới hạn 720p (decoder 720p rẻ hơn nhiều).
 */
data class MediaProfile(
    val tier: DeviceTier,
    /**
     * Khoảng cách tới tập hiện tại (âm = phía trước) → số ms buffer sẵn cho player lân cận. Khoá
     * của map cũng là các tập lân cận được giữ player.
     */
    val neighborBufferMs: Map<Int, Long>,
    val minBufferMs: Int,
    val maxBufferMs: Int,
    val bufferForPlaybackMs: Int,
    /** Trần byte buffer; null = mặc định của ExoPlayer. */
    val targetBufferBytes: Int?,
    /** Trần độ phân giải track (video dọc: rộng × cao); null = không giới hạn. */
    val maxVideoSize: Pair<Int, Int>?,
    /** Cache đĩa theo phiên: số video gần nhất giữ lại và trần byte. */
    val maxCachedVideos: Int,
    val cacheBytes: Long,
) {
    /** Tập hiện tại + các tập lân cận. */
    val poolSize: Int get() = 1 + neighborBufferMs.size

    companion object {
        fun forTier(tier: DeviceTier): MediaProfile = when (tier) {
            DeviceTier.NORMAL -> MediaProfile(
                tier = tier,
                neighborBufferMs = mapOf(1 to 5_000L, -1 to 2_000L),
                minBufferMs = 10_000,
                maxBufferMs = 30_000,
                bufferForPlaybackMs = 750,
                targetBufferBytes = null,
                maxVideoSize = null,
                maxCachedVideos = 10,
                cacheBytes = 256L * 1024 * 1024,
            )
            DeviceTier.LOW -> MediaProfile(
                tier = tier,
                neighborBufferMs = mapOf(1 to 2_500L, -1 to 1_000L),
                minBufferMs = 6_000,
                maxBufferMs = 15_000,
                bufferForPlaybackMs = 500,
                targetBufferBytes = 16 * 1024 * 1024,
                maxVideoSize = 720 to 1280,
                maxCachedVideos = 5,
                cacheBytes = 96L * 1024 * 1024,
            )
        }
    }
}
