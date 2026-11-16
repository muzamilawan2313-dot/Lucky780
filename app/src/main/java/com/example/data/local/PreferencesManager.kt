package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "luck780_prefs")

data class SavedCredentials(
    val savePassword: Boolean = true,
    val countryCode: String = "+92",
    val phoneNumber: String = "",
    val password: String = "",
    val activePhoneKey: String? = null
)

data class AdminControlSettings(
    val marqueeAnnouncement: String = "Winner Alert: Zainab_Royale won 12,500 Coins on Lucky Spin! Free Spin Event Live!",
    val freeSpinPromoEnabled: Boolean = true
)

class PreferencesManager(private val context: Context) {
    private object Keys {
        val SAVE_PASSWORD = booleanPreferencesKey("save_password")
        val COUNTRY_CODE = stringPreferencesKey("saved_country_code")
        val PHONE_NUMBER = stringPreferencesKey("saved_phone_number")
        val PASSWORD = stringPreferencesKey("saved_password")
        val ACTIVE_PHONE_KEY = stringPreferencesKey("active_phone_key")

        val MARQUEE_ANNOUNCEMENT = stringPreferencesKey("admin_marquee_announcement")
        val FREE_SPIN_PROMO_ENABLED = booleanPreferencesKey("admin_free_spin_promo_enabled")
        val PENDING_REFERRAL_CODE = stringPreferencesKey("pending_referral_code")
    }

    val savedCredentialsFlow: Flow<SavedCredentials> = context.dataStore.data.map { prefs ->
        val hasSavedPhone = prefs.contains(Keys.PHONE_NUMBER)
        val savePwd = prefs[Keys.SAVE_PASSWORD] ?: true
        SavedCredentials(
            savePassword = savePwd,
            countryCode = prefs[Keys.COUNTRY_CODE] ?: "+92",
            phoneNumber = if (hasSavedPhone) prefs[Keys.PHONE_NUMBER] ?: "" else "3001234567",
            password = if (hasSavedPhone) {
                if (savePwd) prefs[Keys.PASSWORD] ?: "" else ""
            } else {
                "luck780"
            },
            activePhoneKey = prefs[Keys.ACTIVE_PHONE_KEY]
        )
    }

    val adminSettingsFlow: Flow<AdminControlSettings> = context.dataStore.data.map { prefs ->
        AdminControlSettings(
            marqueeAnnouncement = prefs[Keys.MARQUEE_ANNOUNCEMENT]
                ?: "Winner Alert: Zainab_Royale won 12,500 Coins on Lucky Spin! Free Spin Event Live!",
            freeSpinPromoEnabled = prefs[Keys.FREE_SPIN_PROMO_ENABLED] ?: true
        )
    }

    val pendingReferralCodeFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.PENDING_REFERRAL_CODE]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveLoginPreference(
        savePassword: Boolean,
        countryCode: String,
        phoneNumber: String,
        password: String,
        activePhoneKey: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SAVE_PASSWORD] = savePassword
            prefs[Keys.COUNTRY_CODE] = countryCode
            prefs[Keys.PHONE_NUMBER] = phoneNumber
            prefs[Keys.PASSWORD] = if (savePassword) password else ""
            prefs[Keys.ACTIVE_PHONE_KEY] = activePhoneKey
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACTIVE_PHONE_KEY)
        }
    }

    suspend fun updateAdminSettings(
        marqueeAnnouncement: String? = null,
        freeSpinPromoEnabled: Boolean? = null
    ) {
        context.dataStore.edit { prefs ->
            marqueeAnnouncement?.let { prefs[Keys.MARQUEE_ANNOUNCEMENT] = it }
            freeSpinPromoEnabled?.let { prefs[Keys.FREE_SPIN_PROMO_ENABLED] = it }
        }
    }

    suspend fun savePendingReferralCode(code: String) {
        val clean = code.trim().uppercase()
        if (clean.isBlank()) return
        context.dataStore.edit { prefs ->
            prefs[Keys.PENDING_REFERRAL_CODE] = clean
        }
    }

    suspend fun clearPendingReferralCode() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.PENDING_REFERRAL_CODE)
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
