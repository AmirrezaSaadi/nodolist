package com.example.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Setting
import com.example.todolist.data.repo.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private companion object {
        const val THEME_KEY = "is_dark_theme"
    }

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    val isDarkTheme = settingsRepository.getSetting(THEME_KEY)
        .map { it?.value?.toBoolean() ?: true } // Default to dark theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)


    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newThemeValue = !isDarkTheme.value
            settingsRepository.upsertSetting(Setting(key = THEME_KEY, value = newThemeValue.toString()))
        }
    }
}