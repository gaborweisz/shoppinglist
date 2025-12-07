package com.trainig.shoppinglist.presentation

import com.trainig.shoppinglist.data.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeThemePreferences : ThemePreferences {
    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    private val _language = MutableStateFlow<String?>(null)

    override val isDarkMode: Flow<Boolean?> = _isDarkMode
    override val language: Flow<String?> = _language

    override suspend fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }

    override suspend fun setLanguage(languageCode: String) {
        _language.value = languageCode
    }
}
