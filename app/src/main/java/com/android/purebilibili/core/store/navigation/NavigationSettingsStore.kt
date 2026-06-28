package com.android.purebilibili.core.store.navigation

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.purebilibili.core.store.AppNavigationSettings
import com.android.purebilibili.core.store.mapAppNavigationSettingsFromPreferences
import com.android.purebilibili.core.store.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

object NavigationSettingsStore {
    private val keyTabletUseSidebar = booleanPreferencesKey("tablet_use_sidebar")
    private val keyPredictiveBackEnabled = booleanPreferencesKey("predictive_back_enabled")
    private val keyPredictiveBackAnimationStyle = stringPreferencesKey("predictive_back_animation_style")

    internal fun mapFromPreferences(preferences: Preferences): AppNavigationSettings {
        return mapAppNavigationSettingsFromPreferences(preferences)
    }

    fun observe(context: Context): Flow<AppNavigationSettings> {
        return context.settingsDataStore.data
            .map(::mapFromPreferences)
            .distinctUntilChanged()
    }

    suspend fun setTabletUseSidebar(context: Context, useSidebar: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[keyTabletUseSidebar] = useSidebar
        }
    }

    suspend fun setPredictiveBackEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[keyPredictiveBackEnabled] = enabled
        }
    }

    suspend fun setPredictiveBackAnimationStyle(context: Context, style: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[keyPredictiveBackAnimationStyle] = style
        }
    }
}
