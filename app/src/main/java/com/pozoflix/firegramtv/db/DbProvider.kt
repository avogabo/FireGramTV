package com.pozoflix.firegramtv.db

import android.content.Context
import androidx.room.Room

object DbProvider {
    @Volatile private var db: AppDb? = null
    fun get(ctx: Context): AppDb = db ?: synchronized(this) {
        db ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "firegram.db")
            .fallbackToDestructiveMigration()
            .build().also { db = it }
    }
}
