package com.optictoolcompk.opticaltool.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notebookPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notebook_preferences"
)

@Singleton
class NotebookPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.notebookPreferencesDataStore

    private object PreferencesKeys {
        val SELECTED_SECTION_ID = longPreferencesKey("selected_section_id")
    }

    companion object {
        // Special value to indicate "View All Sections"
        const val VIEW_ALL_SECTIONS_ID = -1L
        // Default value when no section is selected yet (will show first section)
        const val NO_SELECTION_ID = 0L
    }

    /**
     * Flow of selected section ID
     * Returns:
     * - Actual section ID (positive number) for specific section
     * - VIEW_ALL_SECTIONS_ID (-1) for "View All Sections"
     * - NO_SELECTION_ID (0) for no selection (will show first section by default)
     */
    val selectedSectionIdFlow: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_SECTION_ID] ?: NO_SELECTION_ID
        }

    /**
     * Save selected section ID
     * @param sectionId - Section ID to save (can be actual ID, VIEW_ALL_SECTIONS_ID, or NO_SELECTION_ID)
     */
    suspend fun saveSelectedSectionId(sectionId: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_SECTION_ID] = sectionId
        }
    }

    /**
     * Clear selected section (will default to NO_SELECTION_ID)
     */
    suspend fun clearSelectedSection() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SELECTED_SECTION_ID)
        }
    }

    /**
     * Get current selected section ID synchronously
     */
    suspend fun getSelectedSectionId(): Long {
        var selectedId = NO_SELECTION_ID
        dataStore.edit { preferences ->
            selectedId = preferences[PreferencesKeys.SELECTED_SECTION_ID] ?: NO_SELECTION_ID
        }
        return selectedId
    }
}