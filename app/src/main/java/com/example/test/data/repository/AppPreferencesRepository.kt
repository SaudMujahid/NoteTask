package com.example.test.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPrefsDataStore by preferencesDataStore(name = "app_preferences")

class AppPreferencesRepository private constructor(private val context: Context) {

    companion object {
        @Volatile private var INSTANCE: AppPreferencesRepository? = null

        fun getInstance(context: Context): AppPreferencesRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }

        private val KEY_DARK_THEME  = booleanPreferencesKey("dark_theme")
        private val KEY_PALETTE_IDX = intPreferencesKey("palette_index")
    }

    val isDarkThemeFlow: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[KEY_DARK_THEME] ?: false }

    val paletteIndexFlow: Flow<Int> = context.appPrefsDataStore.data
        .map { prefs -> prefs[KEY_PALETTE_IDX] ?: 0 }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.appPrefsDataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    suspend fun setPaletteIndex(index: Int) {
        context.appPrefsDataStore.edit { it[KEY_PALETTE_IDX] = index }
    }
}