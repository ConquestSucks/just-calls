package com.justcalls.utils

import android.os.Build

actual fun getDeviceName(): String {
    return "${Build.MANUFACTURER} ${Build.MODEL}"
}