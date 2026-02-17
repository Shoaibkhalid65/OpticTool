package com.optictoolcompk.opticaltool.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.optictoolcompk.opticaltool.data.models.BillDisplaySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillDisplaySettingsManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val SHOW_PRESCRIPTION = booleanPreferencesKey("show_prescription")
        val SHOW_IPD = booleanPreferencesKey("show_ipd")
        val SHOW_CHECKED_BY = booleanPreferencesKey("show_checked_by")
        val AUTO_SAVE_PRESCRIPTIONS = booleanPreferencesKey("auto_save_prescriptions")
        val SHOW_UPLOAD_CAPTURE_IMAGES = booleanPreferencesKey("show_upload_capture_images")
    }

    val billDisplaySettingsFlow: Flow<BillDisplaySettings> = dataStore.data
        .catch { exception ->
            // Handle exceptions when reading preferences
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            BillDisplaySettings(
                showPrescription = preferences[PreferencesKeys.SHOW_PRESCRIPTION] ?: true,
                showIpd = preferences[PreferencesKeys.SHOW_IPD] ?: true,
                showCheckedBy = preferences[PreferencesKeys.SHOW_CHECKED_BY] ?: true,
                autoSavePrescriptions = preferences[PreferencesKeys.AUTO_SAVE_PRESCRIPTIONS] ?: true,
                showUploadCaptureImages = preferences[PreferencesKeys.SHOW_UPLOAD_CAPTURE_IMAGES] ?: true
            )
        }

    suspend fun updateShowPrescription(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PRESCRIPTION] = show
        }
    }

    suspend fun updateShowIpd(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_IPD] = show
        }
    }

    suspend fun updateShowCheckedBy(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_CHECKED_BY] = show
        }
    }

    suspend fun updateAutoSavePrescriptions(autoSave: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE_PRESCRIPTIONS] = autoSave
        }
    }

    suspend fun updateShowUploadCaptureImages(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_UPLOAD_CAPTURE_IMAGES] = show
        }
    }

    suspend fun updateAllSettings(settings: BillDisplaySettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PRESCRIPTION] = settings.showPrescription
            preferences[PreferencesKeys.SHOW_IPD] = settings.showIpd
            preferences[PreferencesKeys.SHOW_CHECKED_BY] = settings.showCheckedBy
            preferences[PreferencesKeys.AUTO_SAVE_PRESCRIPTIONS] = settings.autoSavePrescriptions
            preferences[PreferencesKeys.SHOW_UPLOAD_CAPTURE_IMAGES] = settings.showUploadCaptureImages
        }
    }

    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

