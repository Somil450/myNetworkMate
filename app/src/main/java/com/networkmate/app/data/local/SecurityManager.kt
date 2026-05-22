package com.networkmate.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_telecom_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasGivenConsent(): Boolean {
        return sharedPrefs.getBoolean("crowdsource_consent", false)
    }

    fun setConsent(given: Boolean) {
        sharedPrefs.edit().putBoolean("crowdsource_consent", given).apply()
        if (given && getAnonymousId() == null) {
            sharedPrefs.edit().putString("anonymous_id", UUID.randomUUID().toString()).apply()
        }
    }

    fun getAnonymousId(): String? {
        return sharedPrefs.getString("anonymous_id", null)
    }
}
