package com.base.app.core.media

import kotlinx.coroutines.flow.StateFlow

/**
 * Player cho luồng vuốt dọc kiểu TikTok: tập hiện tại phát, các tập lân cận mỗi tập giữ **một player
 * đã prepare + pause** (đã có khung hình đầu và vài giây buffer) — vuốt nhẹ là thấy video thật, vuốt
 * hẳn là phát ngay. ViewModel giữ và điều khiển nó mà không import Media3, test dùng bản giả. Gọi
 * [release] khi chủ sở hữu bị huỷ.
 *
 * Mục được chỉ định bằng **vị trí trong playlist**, không phải id nghiệp vụ. [state] và các lệnh
 * phát luôn nói về mục hiện tại.
 */
interface EpisodeFeedPlayer {
    val state: StateFlow<PlaybackState>

    /** Vị trí → player đang giữ mục đó (hiện tại + lân cận). UI vẽ surface của slot lên đúng trang. */
    val slots: StateFlow<Map<Int, VideoSlot>>

    /**
     * Thay danh sách mục (lần đầu, hoặc đổi quality/ngôn ngữ làm URL đổi). Không tự đổi mục đang
     * phát: gọi [setCurrent] sau đó để nạp URL mới.
     */
    fun setPlaylist(items: List<FeedItem>)

    /** Phát mục ở [position]. Mục không có URL (tập khoá) thì dừng phát, không nạp gì. */
    fun setCurrent(position: Int, startPositionMs: Long, playWhenReady: Boolean)

    /** Tắt khi màn vào nền: nhả player lân cận (decoder, RAM); bật lại thì chuẩn bị lại từ cache đĩa. */
    fun setPreloadActive(active: Boolean)

    /**
     * `false` = nhả **mọi** player (kể cả tập hiện tại): decoder, buffer RAM trả hết, chỉ còn cache đĩa.
     * Dùng khi feed bị che hẳn (tab ẩn, mở Player từ tab) — trên máy yếu không bao giờ để hai pool cùng
     * giữ decoder. `true` = dựng lại tập hiện tại + lân cận ở giây 0, **pause**; gọi [play] để phát.
     */
    fun setActive(active: Boolean)

    /** Câm tập hiện tại và không giành audio focus (preview của Browse). */
    fun setMuted(muted: Boolean)

    fun play()

    fun pause()

    fun seekTo(positionMs: Long)

    fun setSpeed(speed: Float)

    /** Vị trí thật lúc gọi — [PlaybackState.positionMs] chỉ cập nhật mỗi nhịp tick. */
    fun currentPositionMs(): Long

    fun release()
}

/** Một player của pool, nhìn từ UI: chỉ cần biết đã có khung hình chưa để thôi hiện poster. */
interface VideoSlot {
    val firstFrameRendered: StateFlow<Boolean>
}

/**
 * [url] null = không phát được (tập khoá): không giữ player, không nạp. Player lân cận luôn đứng sẵn
 * ở **giây 0** — trong feed, vuốt tới tập nào cũng phát từ đầu.
 */
data class FeedItem(val id: String, val url: String?)

data class PlaybackState(
    /**
     * [FeedItem.id] mà các số đo bên dưới thuộc về; null = chưa có mục nào. UI so với mục của mình để không vẽ
     * vị trí của tập trước trong khung đầu sau khi lật trang.
     */
    val itemId: String? = null,
    /** Đang thực sự chạy (đã sẵn sàng, không buffer, không pause). */
    val isPlaying: Boolean = false,
    /** User muốn phát — vẫn `true` khi đang buffer giữa chừng. */
    val playWhenReady: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0,
    /** Lấy từ player: `durationSeconds` của server luôn là 0. 0 = chưa biết. */
    val durationMs: Long = 0,
    val bufferedMs: Long = 0,
    val ended: Boolean = false,
    val error: PlaybackError? = null,
)

data class PlaybackError(val message: String?)

/** ViewModel tạo player qua factory để không phải giữ `Context`. */
fun interface EpisodeFeedPlayerFactory {
    fun create(): EpisodeFeedPlayer
}

/**
 * Vị trí + thời lượng **gắn với mục đo được** ([itemId]). Trang trong feed hỏi theo id của mình: lật sang
 * tập mới mà player chưa kịp báo thì trang mới đọc 0 thay vì vị trí của tập trước (hết nháy % cũ → 0%).
 */
data class PlaybackProgress(
    val itemId: String? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
) {
    fun positionFor(id: String): Long = if (id == itemId) positionMs else 0

    fun durationFor(id: String): Long = if (id == itemId) durationMs else 0

    fun fractionFor(id: String): Float {
        val duration = durationFor(id)
        return if (duration > 0) (positionFor(id).toFloat() / duration).coerceIn(0f, 1f) else 0f
    }
}

val PlaybackState.progress: PlaybackProgress get() = PlaybackProgress(itemId, positionMs, durationMs)
