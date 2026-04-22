package com.plotmap.app.core.data

import android.content.Context

class PreferencesManager(context: Context) {
    private companion object {
        const val PREFS_NAME = "app_preferences"
        const val KEY_THEME = "theme"
        const val KEY_LANGUAGE = "language"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_LIST_HEIGHT = "list_height"
        const val KEY_SORT_BY = "sort_by"
        const val KEY_AUTO_BACKUP = "auto_backup"
        const val KEY_PROJECTS_JSON = "projects_json"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTheme(): String = prefs.getString(KEY_THEME, "light") ?: "light"

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "ru") ?: "ru"

    fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getFontSize(): String = prefs.getString(KEY_FONT_SIZE, "medium") ?: "medium"

    fun setFontSize(size: String) {
        prefs.edit().putString(KEY_FONT_SIZE, size).apply()
    }

    fun getListHeight(): String = prefs.getString(KEY_LIST_HEIGHT, "medium") ?: "medium"

    fun setListHeight(height: String) {
        prefs.edit().putString(KEY_LIST_HEIGHT, height).apply()
    }

    fun getSortBy(): String = prefs.getString(KEY_SORT_BY, "date_modified") ?: "date_modified"

    fun setSortBy(sortBy: String) {
        prefs.edit().putString(KEY_SORT_BY, sortBy).apply()
    }

    fun isAutoBackupEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_BACKUP, true)

    fun setAutoBackup(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply()
    }

    fun getProjectsJson(): String = prefs.getString(KEY_PROJECTS_JSON, "") ?: ""

    fun setProjectsJson(projectsJson: String) {
        prefs.edit().putString(KEY_PROJECTS_JSON, projectsJson).apply()
    }
}
