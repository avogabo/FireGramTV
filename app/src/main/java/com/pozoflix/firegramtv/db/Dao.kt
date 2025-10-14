package com.pozoflix.firegramtv.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(p: MediaProgress)
    @Query("SELECT * FROM media_progress WHERE key=:k LIMIT 1") suspend fun find(k: String): MediaProgress?
    @Query("SELECT * FROM media_progress ORDER BY updatedAt DESC LIMIT :limit") fun recent(limit: Int = 20): Flow<List<MediaProgress>>
}

@Dao
interface FavoritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun add(f: Favorite)
    @Query("DELETE FROM favorites WHERE key=:k") suspend fun remove(k: String)
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE key=:k)") suspend fun isFav(k: String): Boolean
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC LIMIT :limit") fun list(limit: Int = 50): Flow<List<Favorite>>
}
