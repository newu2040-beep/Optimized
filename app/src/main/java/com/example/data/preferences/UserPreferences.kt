package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeStyle(val displayName: String, val description: String) {
    SYSTEM_DEFAULT("System Default", "Adapts to your Android system appearance"),
    DARK("Dark", "Near-black background with elevated dark surfaces"),
    LIGHT("Light", "Soft neutral background, crisp surfaces and subtle shadows"),
    GRAPHITE("Graphite", "Premium charcoal-gray aesthetic with modern contrast"),
    CREAM("Cream", "Warm minimalist luxury appearance"),
    MIDNIGHT("Midnight", "Deep navy blue-black aesthetic"),
    AURORA("Aurora", "Subtle futuristic dark slate with muted teal accents"),
    CYBERPUNK("Cyberpunk Neon", "High-contrast electric violet & cyber yellow"),
    SUNSET("Sunset Glow", "Vibrant twilight indigo & radiant warm amber"),
    EMERALD("Emerald Matrix", "Obsidian dark surfaces with electric mint green"),
    OCEANIC("Oceanic Breeze", "Deep marine blue with vivid turquoise accents"),
    AMOLED("AMOLED Pure Black", "True 100% #000000 black for maximum battery savings")
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("optimized_prefs", Context.MODE_PRIVATE)

    private val _themeStyle = MutableStateFlow(getSavedThemeStyle())
    val themeStyle: StateFlow<ThemeStyle> = _themeStyle.asStateFlow()

    private val _hardwareAcceleration = MutableStateFlow(prefs.getBoolean(KEY_HW_ACCEL, true))
    val hardwareAcceleration: StateFlow<Boolean> = _hardwareAcceleration.asStateFlow()

    private val _keepScreenAwake = MutableStateFlow(prefs.getBoolean(KEY_KEEP_AWAKE, true))
    val keepScreenAwake: StateFlow<Boolean> = _keepScreenAwake.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _removeMetadata = MutableStateFlow(prefs.getBoolean(KEY_REMOVE_METADATA, false))
    val removeMetadata: StateFlow<Boolean> = _removeMetadata.asStateFlow()

    private val _compactMode = MutableStateFlow(prefs.getBoolean(KEY_COMPACT_MODE, false))
    val compactMode: StateFlow<Boolean> = _compactMode.asStateFlow()

    private val _autoCompactOnSmallScreens = MutableStateFlow(prefs.getBoolean(KEY_AUTO_COMPACT, true))
    val autoCompactOnSmallScreens: StateFlow<Boolean> = _autoCompactOnSmallScreens.asStateFlow()

    private val _autoSaveToGallery = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SAVE_GALLERY, true))
    val autoSaveToGallery: StateFlow<Boolean> = _autoSaveToGallery.asStateFlow()

    fun setThemeStyle(style: ThemeStyle) {
        prefs.edit().putString(KEY_THEME_STYLE, style.name).apply()
        _themeStyle.value = style
    }

    fun setHardwareAcceleration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HW_ACCEL, enabled).apply()
        _hardwareAcceleration.value = enabled
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_AWAKE, enabled).apply()
        _keepScreenAwake.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setRemoveMetadata(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMOVE_METADATA, enabled).apply()
        _removeMetadata.value = enabled
    }

    fun setCompactMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_COMPACT_MODE, enabled).apply()
        _compactMode.value = enabled
    }

    fun setAutoCompactOnSmallScreens(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_COMPACT, enabled).apply()
        _autoCompactOnSmallScreens.value = enabled
    }

    fun setAutoSaveToGallery(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE_GALLERY, enabled).apply()
        _autoSaveToGallery.value = enabled
    }

    private fun getSavedThemeStyle(): ThemeStyle {
        val saved = prefs.getString(KEY_THEME_STYLE, ThemeStyle.GRAPHITE.name)
        return try {
            ThemeStyle.valueOf(saved ?: ThemeStyle.GRAPHITE.name)
        } catch (_: Exception) {
            ThemeStyle.GRAPHITE
        }
    }

    companion object {
        private const val KEY_THEME_STYLE = "key_theme_style"
        private const val KEY_HW_ACCEL = "key_hw_accel"
        private const val KEY_KEEP_AWAKE = "key_keep_awake"
        private const val KEY_NOTIFICATIONS = "key_notifications"
        private const val KEY_REMOVE_METADATA = "key_remove_metadata"
        private const val KEY_COMPACT_MODE = "key_compact_mode"
        private const val KEY_AUTO_COMPACT = "key_auto_compact"
        private const val KEY_AUTO_SAVE_GALLERY = "key_auto_save_gallery"
    }
}
