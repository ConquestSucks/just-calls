package com.justcalls.utils

import platform.UIKit.UIDevice


actual fun getDeviceName(): String {
    return UIDevice.currentDevice.name
}