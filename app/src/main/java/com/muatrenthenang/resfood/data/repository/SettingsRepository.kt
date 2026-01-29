package com.muatrenthenang.resfood.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("resfood_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "push_notification_enabled"
        private const val KEY_DARK_MODE_ENABLED = "is_dark_theme"
        private const val KEY_LANGUAGE = "app_language"
    }

    // Language
    fun setLanguage(langCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    fun getLanguage(): String? {
        return prefs.getString(KEY_LANGUAGE, null)
    }

    // Notifications
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true) // Default true
    }

    // Dark Mode (Future use, currently kept local in UI or separate)
    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)
    }
}
