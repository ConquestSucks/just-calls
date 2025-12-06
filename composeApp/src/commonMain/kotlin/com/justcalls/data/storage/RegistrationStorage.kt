package com.justcalls.data.storage

expect class RegistrationStorage() {
    fun saveRegistrationGuid(email: String, guid: String)
    fun getRegistrationGuid(email: String): String?
    fun clearRegistrationGuid(email: String)
    fun clearAllRegistrationGuids()
}

