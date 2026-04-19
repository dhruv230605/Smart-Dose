package com.example.mucproject.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log

private val Context.dataStore by preferencesDataStore("user_prefs")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val userSession: Flow<UserSession?> = dataStore.data.map { prefs ->
        val email = prefs[EMAIL]
        val roleStr = prefs[ROLE]
        val isLoggedIn = prefs[IS_LOGGED_IN] ?: false

        if (email != null && roleStr != null) {
            try {
                UserSession(
                    email = email,
                    role = UserRole.valueOf(roleStr),
                    isLoggedIn = isLoggedIn
                )
            } catch (e: Exception) {
                Log.e("DataStoreManager", "Error parsing session role: $roleStr", e)
                null
            }
        } else null
    }

    suspend fun saveSession(email: String, role: UserRole, rememberMe: Boolean) {
        dataStore.edit { prefs ->
            prefs[EMAIL] = email
            prefs[ROLE] = role.name
            prefs[REMEMBER_ME] = rememberMe
            prefs[IS_LOGGED_IN] = true
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = false
            if (prefs[REMEMBER_ME] != true) {
                prefs.remove(EMAIL)
                prefs.remove(ROLE)
            }
        }
    }
}
