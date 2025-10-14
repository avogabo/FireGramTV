package com.pozoflix.firegramtv.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MediaProgress::class, Favorite::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
    abstract fun favoritesDao(): FavoritesDao
}
