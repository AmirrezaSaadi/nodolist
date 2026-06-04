package com.example.todolist.data.repo

import com.example.todolist.data.db.SettingsDao
import com.example.todolist.data.model.Setting
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {

    fun getSetting(key: String): Flow<Setting?> = settingsDao.getSetting(key)

    suspend fun upsertSetting(setting: Setting) {
        settingsDao.upsertSetting(setting)
    }
}