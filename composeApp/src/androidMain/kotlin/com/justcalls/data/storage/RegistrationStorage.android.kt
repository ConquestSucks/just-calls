package com.justcalls.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.justcalls.JustCallsApplication
import androidx.core.content.edit

actual class RegistrationStorage {
    private val prefs: SharedPreferences by lazy {
        JustCallsApplication.instance.getSharedPreferences("justcalls_registration", Context.MODE_PRIVATE)
    }
    
    private fun getGuidKey(email: String): String {
        return "registration_guid_$email"
    }
    
    actual fun saveRegistrationGuid(email: String, guid: String) {
        prefs.edit { putString(getGuidKey(email), guid) }
    }
    
    actual fun getRegistrationGuid(email: String): String? {
        return prefs.getString(getGuidKey(email), null)
    }
    
    actual fun clearRegistrationGuid(email: String) {
        prefs.edit { remove(getGuidKey(email)) }
    }
    
    actual fun clearAllRegistrationGuids() {
        prefs.edit { clear() }
    }
}

