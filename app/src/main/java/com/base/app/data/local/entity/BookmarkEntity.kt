package com.base.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** SAMPLE — post user đã bookmark, lưu offline. */
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
