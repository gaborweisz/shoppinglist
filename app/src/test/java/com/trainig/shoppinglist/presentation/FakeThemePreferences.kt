package com.trainig.shoppinglist.presentation

import com.trainig.shoppinglist.data.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeThemePreferences : ThemePreferences {
    private val _isDarkMode = MutableStateFlow<Boolean?>(null)

    override val isDarkMode: Flow<Boolean?> = _isDarkMode

    override suspend fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }
}
