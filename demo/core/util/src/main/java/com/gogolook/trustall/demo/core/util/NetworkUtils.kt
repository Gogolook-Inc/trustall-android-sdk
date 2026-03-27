package com.gogolook.trustall.demo.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Checks whether the device is currently connected to the internet. This checks for
 * NET_CAPABILITY_INTERNET which means there is an active network interface and it has internet
 * connectivity.
 *
 * @return true if internet is available, false otherwise.
 */
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    if (connectivityManager == null) return false

    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
