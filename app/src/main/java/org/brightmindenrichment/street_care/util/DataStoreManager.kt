package org.brightmindenrichment.street_care.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.brightmindenrichment.street_care.util.Constants.IS_APP_ON_BACKGROUND
import org.brightmindenrichment.street_care.util.Constants.ROOM_DB_IS_INITIALIZED
import java.io.IOException

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        private val roomDBIsInitialized = booleanPreferencesKey(ROOM_DB_IS_INITIALIZED)
        private val isAppOnBackground = booleanPreferencesKey(IS_APP_ON_BACKGROUND)
    }

    suspend fun setRoomDBIsInitialized(isInitialized: Boolean) {
        dataStore.edit { settings ->
            settings[roomDBIsInitialized] = isInitialized
        }
    }

    fun getRoomDBIsInitialized(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if(exception is IOException) {
                    emit(emptyPreferences())
                }
                else {
                    throw exception
                }
            }
            .map { preferences ->
                // No type safety.
                preferences[roomDBIsInitialized] ?: false
            }
    }

    suspend fun setIsAppOnBackground(isOnBackground: Boolean) {
        dataStore.edit { settings ->
            settings[isAppOnBackground] = isOnBackground
        }
    }

    fun getIsAppOnBackground(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if(exception is IOException) {
                    emit(emptyPreferences())
                }
                else {
                    throw exception
                }
            }
            .map { preferences ->
                // No type safety.
                preferences[isAppOnBackground] ?: true
            }
    }

}