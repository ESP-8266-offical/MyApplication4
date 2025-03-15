package com.example.zhilan.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 网络状态监听器
 */
class NetworkMonitor(private val context: Context) {
    private val TAG = "NetworkMonitor"
    
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "网络连接可用")
            _isOnline.value = true
        }
        
        override fun onLost(network: Network) {
            Log.d(TAG, "网络连接丢失")
            _isOnline.value = false
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            val isValidated = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )
            Log.d(TAG, "网络能力变化: 有互联网=$hasInternet, 已验证=$isValidated")
            _isOnline.value = hasInternet && isValidated
        }
    }
    
    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // 初始化当前网络状态
        _isOnline.value = NetworkUtils.isNetworkAvailable(context)
        Log.d(TAG, "开始监听网络状态，当前状态: ${if (_isOnline.value) "在线" else "离线"}")
    }
    
    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Log.d(TAG, "停止监听网络状态")
        } catch (e: Exception) {
            Log.e(TAG, "停止网络监听失败: ${e.message}")
        }
    }
}