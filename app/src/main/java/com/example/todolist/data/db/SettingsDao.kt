package com.example.todolist.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.todolist.data.model.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Upsert
    suspend fun upsertSetting(setting: Setting)

    @Query("SELECT * FROM settings_table WHERE `key` = :key")
    fun getSetting(key: String): Flow<Setting?>
}