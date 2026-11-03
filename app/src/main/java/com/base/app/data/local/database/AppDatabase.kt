package com.base.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.base.app.data.local.dao.BookmarkDao
import com.base.app.data.local.entity.BookmarkEntity

/** Bump version thì viết Migration — không dùng destructive migration (mất dữ liệu user). */
@Database(
    entities = [BookmarkEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val NAME = "app.db"
    }
}
