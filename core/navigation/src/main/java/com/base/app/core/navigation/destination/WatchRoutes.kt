package com.base.app.core.navigation.destination

/**
 * Lối vào luồng xem phim (`:feature:watch`) cho feature khác. Feature không import nhau, nên
 * `:app` cài interface này bằng destination thật rồi truyền vào `ScreenProvider` của feature cần.
 */
interface WatchRoutes {
    fun showDetail(showId: String): Destination

    /** [episodeIndex] là vị trí trong mảng `episodes` — đề nói `number` không chắc liên tục. */
    fun player(showId: String, episodeIndex: Int): Destination
}
