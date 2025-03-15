package com.example.zhilan.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

/**
 * 网络工具类，提供网络连接检查功能
 */
object NetworkUtils {
    private const val TAG = "NetworkUtils"
    
    /**
     * 检查设备是否连接到网络
     */
    @Suppress("DEPRECATION")
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork?.let { network ->
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            } ?: false
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isConnected = activeNetworkInfo?.isConnected == true
            if (isConnected) {
                Log.d(TAG, "网络类型: ${activeNetworkInfo?.typeName}")
            }
            isConnected
        }
    }
    
    /**
     * 进行网络连通性测试
     * 尝试连接到常用服务器检查是否可以实际访问互联网
     */
    fun testNetworkConnectivity(context: Context, callback: (Boolean) -> Unit) {
        if (!isNetworkAvailable(context)) {
            Log.e(TAG, "网络不可用，无需进行连通性测试")
            callback(false)
            return
        }
        
        Thread {
            try {
                // 尝试连接到Google DNS服务器
                val address = java.net.InetAddress.getByName("8.8.8.8")
                val reachable = address.isReachable(5000) // 5秒超时
                Log.d(TAG, "网络连通性测试结果: ${if (reachable) "成功" else "失败"}")
                callback(reachable)
            } catch (e: Exception) {
                Log.e(TAG, "网络连通性测试异常: ${e.message}")
                callback(false)
            }
        }.start()
    }
}