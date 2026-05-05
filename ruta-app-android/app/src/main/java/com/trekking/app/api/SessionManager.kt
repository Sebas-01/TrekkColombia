package com.trekking.app.api

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object SessionManager {
    private const val PREF_NAME = "trekking_session"
    private const val KEY_USER = "user_data"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_THEME = "dark_mode"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isDarkMode(context: Context): Boolean? {
        val prefs = getPrefs(context)
        return if (prefs.contains(KEY_THEME)) prefs.getBoolean(KEY_THEME, false) else null
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_THEME, isDark).apply()
    }

    fun saveSession(context: Context, user: Usuario, token: String) {
        val prefs = getPrefs(context)
        val gson = Gson()
        val userJson = gson.toJson(user)
        
        prefs.edit().apply {
            putString(KEY_USER, userJson)
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    fun getUser(context: Context): Usuario? {
        val userJson = getPrefs(context).getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                Gson().fromJson(userJson, Usuario::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN, null)
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
