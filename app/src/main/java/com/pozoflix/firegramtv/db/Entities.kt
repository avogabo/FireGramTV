package com.pozoflix.firegramtv.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_progress")
data class MediaProgress(
    @PrimaryKey val key: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val key: String, // m:<tmdbId> o t:<tmdbId>
    val tmdbId: Long,
    val isTv: Boolean,
    val addedAt: Long = System.currentTimeMillis()
)
