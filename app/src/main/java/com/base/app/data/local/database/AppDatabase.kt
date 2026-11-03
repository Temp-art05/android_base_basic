package com.base.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.base.app.data.local.dao.BookmarkDao
import com.base.app.data.local.dao.WatchProgressDao
import com.base.app.data.local.entity.BookmarkEntity
import com.base.app.data.local.entity.WatchProgressEntity

/** Bump version thì viết Migration — không dùng destructive migration (mất dữ liệu user). */
@Database(
    entities = [BookmarkEntity::class, WatchProgressEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    abstract fun watchProgressDao(): WatchProgressDao

    companion object {
        const val NAME = "app.db"
    }
}
