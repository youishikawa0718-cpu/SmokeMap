package com.example.smokemap.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: SmokeMapDatabase? = null

    fun getDatabase(context: Context): SmokeMapDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                SmokeMapDatabase::class.java,
                "smokemap_db"
            ).build().also { INSTANCE = it }
        }
    }
}
