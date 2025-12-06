package com.justcalls.data.storage

import platform.Foundation.NSUserDefaults

actual class RegistrationStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    private fun getGuidKey(email: String): String {
        return "registration_guid_$email"
    }
    
    actual fun saveRegistrationGuid(email: String, guid: String) {
        userDefaults.setObject(guid, forKey = getGuidKey(email))
    }
    
    actual fun getRegistrationGuid(email: String): String? {
        return userDefaults.stringForKey(getGuidKey(email))
    }
    
    actual fun clearRegistrationGuid(email: String) {
        userDefaults.removeObjectForKey(getGuidKey(email))
    }
    
    actual fun clearAllRegistrationGuids() {
        // Удаляем все ключи, начинающиеся с "registration_guid_"
        val keys = userDefaults.dictionaryRepresentation().keys
        keys.forEach { key ->
            if (key is String && key.startsWith("registration_guid_")) {
                userDefaults.removeObjectForKey(key)
            }
        }
    }
}

