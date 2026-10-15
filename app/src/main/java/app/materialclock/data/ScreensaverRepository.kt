package app.materialclock.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.screensaverDataStore: DataStore<Preferences> by preferencesDataStore(name = "screensaver_settings")

class ScreensaverRepository(private val context: Context) {

    private object PreferencesKeys {
        val ENABLED = booleanPreferencesKey("screensaver_enabled")
        val CLOCK_FACE_ID = stringPreferencesKey("clock_face_id")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val BACKGROUND_TYPE = stringPreferencesKey("background_type")
        val SOLID_COLOR = longPreferencesKey("solid_color")
        val GALLERY_IMAGE_URI = stringPreferencesKey("gallery_image_uri")
        val NIGHT_MODE_ENABLED = booleanPreferencesKey("night_mode_enabled")
        val CLOCK_DIMMING_FACTOR = floatPreferencesKey("clock_dimming_factor")
        val PIXEL_SHIFT_ENABLED = booleanPreferencesKey("pixel_shift_enabled")
        val CLOCK_SHIFT_INTERVAL = intPreferencesKey("clock_shift_interval")
        val DATE_SHIFT_INTERVAL = intPreferencesKey("date_shift_interval")
        val BATTERY_SHIFT_INTERVAL = intPreferencesKey("battery_shift_interval")
    }

    val settingsFlow: Flow<ScreensaverSettings> = context.screensaverDataStore.data.map { preferences ->
        ScreensaverSettings(
            enabled = preferences[PreferencesKeys.ENABLED] ?: true,
            clockFaceId = preferences[PreferencesKeys.CLOCK_FACE_ID] ?: "default_face",
            showSeconds = preferences[PreferencesKeys.SHOW_SECONDS] ?: false,
            showDate = preferences[PreferencesKeys.SHOW_DATE] ?: true,
            dateFormat = preferences[PreferencesKeys.DATE_FORMAT] ?: "dd/MM/yyyy",
            showBattery = preferences[PreferencesKeys.SHOW_BATTERY] ?: true,
            backgroundType = runCatching {
                ScreensaverBackgroundType.valueOf(
                    preferences[PreferencesKeys.BACKGROUND_TYPE] ?: ScreensaverBackgroundType.OLED_BLACK.name
                )
            }.getOrDefault(ScreensaverBackgroundType.OLED_BLACK),
            solidBackgroundColor = preferences[PreferencesKeys.SOLID_COLOR] ?: 0xFF000000,
            galleryImageUri = preferences[PreferencesKeys.GALLERY_IMAGE_URI],
            nightModeEnabled = preferences[PreferencesKeys.NIGHT_MODE_ENABLED] ?: false,
            clockDimmingFactor = preferences[PreferencesKeys.CLOCK_DIMMING_FACTOR] ?: 0.4f,
            pixelShiftEnabled = preferences[PreferencesKeys.PIXEL_SHIFT_ENABLED] ?: true,
            clockShiftIntervalMinutes = preferences[PreferencesKeys.CLOCK_SHIFT_INTERVAL] ?: 2,
            dateShiftIntervalMinutes = preferences[PreferencesKeys.DATE_SHIFT_INTERVAL] ?: 3,
            batteryShiftIntervalMinutes = preferences[PreferencesKeys.BATTERY_SHIFT_INTERVAL] ?: 4
        )
    }

    suspend fun updateSettings(transform: (ScreensaverSettings) -> ScreensaverSettings) {
        context.screensaverDataStore.edit { preferences ->
            val current = ScreensaverSettings(
                enabled = preferences[PreferencesKeys.ENABLED] ?: true,
                clockFaceId = preferences[PreferencesKeys.CLOCK_FACE_ID] ?: "default_face",
                showSeconds = preferences[PreferencesKeys.SHOW_SECONDS] ?: false,
                showDate = preferences[PreferencesKeys.SHOW_DATE] ?: true,
                dateFormat = preferences[PreferencesKeys.DATE_FORMAT] ?: "dd/MM/yyyy",
                showBattery = preferences[PreferencesKeys.SHOW_BATTERY] ?: true,
                backgroundType = runCatching {
                    ScreensaverBackgroundType.valueOf(
                        preferences[PreferencesKeys.BACKGROUND_TYPE] ?: ScreensaverBackgroundType.OLED_BLACK.name
                    )
                }.getOrDefault(ScreensaverBackgroundType.OLED_BLACK),
                solidBackgroundColor = preferences[PreferencesKeys.SOLID_COLOR] ?: 0xFF000000,
                galleryImageUri = preferences[PreferencesKeys.GALLERY_IMAGE_URI],
                nightModeEnabled = preferences[PreferencesKeys.NIGHT_MODE_ENABLED] ?: false,
                clockDimmingFactor = preferences[PreferencesKeys.CLOCK_DIMMING_FACTOR] ?: 0.4f,
                pixelShiftEnabled = preferences[PreferencesKeys.PIXEL_SHIFT_ENABLED] ?: true,
                clockShiftIntervalMinutes = preferences[PreferencesKeys.CLOCK_SHIFT_INTERVAL] ?: 2,
                dateShiftIntervalMinutes = preferences[PreferencesKeys.DATE_SHIFT_INTERVAL] ?: 3,
                batteryShiftIntervalMinutes = preferences[PreferencesKeys.BATTERY_SHIFT_INTERVAL] ?: 4
            )
            val updated = transform(current)

            preferences[PreferencesKeys.ENABLED] = updated.enabled
            preferences[PreferencesKeys.CLOCK_FACE_ID] = updated.clockFaceId
            preferences[PreferencesKeys.SHOW_SECONDS] = updated.showSeconds
            preferences[PreferencesKeys.SHOW_DATE] = updated.showDate
            preferences[PreferencesKeys.DATE_FORMAT] = updated.dateFormat
            preferences[PreferencesKeys.SHOW_BATTERY] = updated.showBattery
            preferences[PreferencesKeys.BACKGROUND_TYPE] = updated.backgroundType.name
            preferences[PreferencesKeys.SOLID_COLOR] = updated.solidBackgroundColor
            if (updated.galleryImageUri != null) {
                preferences[PreferencesKeys.GALLERY_IMAGE_URI] = updated.galleryImageUri
            } else {
                preferences.remove(PreferencesKeys.GALLERY_IMAGE_URI)
            }
            preferences[PreferencesKeys.NIGHT_MODE_ENABLED] = updated.nightModeEnabled
            preferences[PreferencesKeys.CLOCK_DIMMING_FACTOR] = updated.clockDimmingFactor
            preferences[PreferencesKeys.PIXEL_SHIFT_ENABLED] = updated.pixelShiftEnabled
            preferences[PreferencesKeys.CLOCK_SHIFT_INTERVAL] = updated.clockShiftIntervalMinutes
            preferences[PreferencesKeys.DATE_SHIFT_INTERVAL] = updated.dateShiftIntervalMinutes
            preferences[PreferencesKeys.BATTERY_SHIFT_INTERVAL] = updated.batteryShiftIntervalMinutes
        }
    }
}
