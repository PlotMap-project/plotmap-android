package com.plotmap.app.core.data
import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private companion object {
        const val KEY_TOKEN = "jwt_token"
        const val KEY_USER_NAME = "user_name"
    }

    private val masterKey =
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    private val prefs =
        EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token.trim()) }
    }

    fun saveUserName(userName: String) {
        prefs.edit { putString(KEY_USER_NAME, userName) }
    }

    fun clear() = prefs.edit { clear() }
}
