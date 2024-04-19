package com.example.mapsapp.view.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPrefs(private val context: Context) {
    // Create a DataBase
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
        val STORE_USERNAME = stringPreferencesKey("store_username")
        val STORE_USERPASS = stringPreferencesKey("store_userpass")
    }
    val getUserData: Flow<List<String>> = context.dataStore.data.map { prefs ->
        listOf(
            prefs[STORE_USERNAME]?: "",
            prefs[STORE_USERPASS]?: ""
        )
    }
    // Save the User Data
    suspend fun saveUserData(userName: String, userPass: String) {
        this.context.dataStore.edit { prefs ->
            prefs[STORE_USERNAME] = userName
            prefs[STORE_USERPASS] = userPass
        }
    }

    // Delete User Data
    suspend fun deleteUserData() {
        this.context.dataStore.edit { prefs ->
            prefs[STORE_USERNAME] = ""
            prefs[STORE_USERPASS] = ""
        }
    }
}