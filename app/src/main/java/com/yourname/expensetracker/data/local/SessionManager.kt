package com.yourname.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "expense_tracker_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_AUTO_LOCK_TIMEOUT_SEC = longPreferencesKey("auto_lock_timeout_sec")
        private val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val KEY_LAST_ACTIVE_TIME = longPreferencesKey("last_active_time")
        private val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_STAFF_ROLE = stringPreferencesKey("active_staff_role")
        private val KEY_STAFF_NAME = stringPreferencesKey("active_staff_name")
    }

    val activeProfileId: Flow<Long?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_ACTIVE_PROFILE_ID]
        if (id == 0L) null else id
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ONBOARDING_COMPLETE] ?: false
    }

    val pinHash: Flow<String?> = context.dataStore.data.map { it[KEY_PIN_HASH] }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val autoLockTimeoutSec: Flow<Long> = context.dataStore.data.map {
        it[KEY_AUTO_LOCK_TIMEOUT_SEC] ?: 0L
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_IS_PREMIUM] ?: false
    }

    val lastActiveTime: Flow<Long> = context.dataStore.data.map {
        it[KEY_LAST_ACTIVE_TIME] ?: 0L
    }

    val currencySymbol: Flow<String> = context.dataStore.data.map {
        it[KEY_CURRENCY_SYMBOL] ?: "₹"
    }

    val themeMode: Flow<String> = context.dataStore.data.map {
        it[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val appLanguage: Flow<String> = context.dataStore.data.map {
        it[KEY_LANGUAGE] ?: "en"
    }

    val activeStaffRole: Flow<String> = context.dataStore.data.map {
        it[KEY_STAFF_ROLE] ?: "OWNER"
    }

    val activeStaffName: Flow<String> = context.dataStore.data.map {
        it[KEY_STAFF_NAME] ?: "Owner"
    }

    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setActiveStaff(role: String, name: String) {
        context.dataStore.edit {
            it[KEY_STAFF_ROLE] = role
            it[KEY_STAFF_NAME] = name
        }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { it[KEY_CURRENCY_SYMBOL] = symbol }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setActiveProfileId(id: Long) {
        context.dataStore.edit { it[KEY_ACTIVE_PROFILE_ID] = id }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { it[KEY_PIN_HASH] = hash }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAutoLockTimeoutSec(sec: Long) {
        context.dataStore.edit { it[KEY_AUTO_LOCK_TIMEOUT_SEC] = sec }
    }

    suspend fun setIsPremium(premium: Boolean) {
        context.dataStore.edit { it[KEY_IS_PREMIUM] = premium }
    }

    suspend fun setLastActiveTime(time: Long) {
        context.dataStore.edit { it[KEY_LAST_ACTIVE_TIME] = time }
    }

    suspend fun reset() {
        context.dataStore.edit { it.clear() }
    }
}
