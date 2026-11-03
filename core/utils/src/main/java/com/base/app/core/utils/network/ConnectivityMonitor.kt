package com.base.app.core.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Trạng thái mạng cho rule kiểu "chỉ tự phát trên Wi-Fi". Sau interface để test VM bằng bản giả. */
interface ConnectivityMonitor {
    /**
     * Mạng mặc định không tính phí (`NET_CAPABILITY_NOT_METERED`) — đúng ý "Wi-Fi only" hơn hỏi
     * `TRANSPORT_WIFI`: hotspot điện thoại là Wi-Fi nhưng tính phí, Ethernet thì không.
     */
    val isUnmetered: StateFlow<Boolean>
}

/** Một callback cho cả process (Koin single) — `registerDefaultNetworkCallback` có từ API 24. */
class AndroidConnectivityMonitor(context: Context) : ConnectivityMonitor {

    private val manager = context.getSystemService(ConnectivityManager::class.java)

    private val _isUnmetered = MutableStateFlow(manager?.currentUnmetered() ?: false)
    override val isUnmetered: StateFlow<Boolean> = _isUnmetered.asStateFlow()

    init {
        manager?.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    _isUnmetered.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                }

                override fun onLost(network: Network) {
                    _isUnmetered.value = false
                }
            },
        )
    }

    private fun ConnectivityManager.currentUnmetered(): Boolean =
        getNetworkCapabilities(activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true
}
