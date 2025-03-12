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
    
    // 连接超时时间（毫秒）
    const val CONNECTION_TIMEOUT = 15000
    // 读取超时时间（毫秒）
    const val READ_TIMEOUT = 30000
    // 写入超时时间（毫秒）
    const val WRITE_TIMEOUT = 30000
    // 最大重试次数
    const val MAX_RETRY_COUNT = 3
    // 重试间隔（毫秒）
    const val RETRY_INTERVAL = 2000L
    
    /**
     * 检查设备是否连接到网络
     */
    @Suppress("DEPRECATION")
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork?.let { network ->
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                val hasInternet = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                val hasValidated = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
                val result = hasInternet && hasValidated
                Log.d(TAG, "网络状态: 有互联网=$hasInternet, 已验证=$hasValidated, 结果=$result")
                result
            } ?: false
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isConnected = activeNetworkInfo?.isConnected == true
            if (isConnected) {
                Log.d(TAG, "网络类型: ${activeNetworkInfo?.typeName}")
            } else {
                Log.d(TAG, "网络未连接")
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
                // 尝试连接到多个DNS服务器，提高成功率
                val servers = listOf("8.8.8.8", "114.114.114.114", "223.5.5.5")
                var reachable = false
                
                for (server in servers) {
                    try {
                        val address = java.net.InetAddress.getByName(server)
                        reachable = address.isReachable(3000) // 3秒超时
                        Log.d(TAG, "网络连通性测试结果($server): ${if (reachable) "成功" else "失败"}")
                        if (reachable) break // 只要有一个成功就可以
                    } catch (e: Exception) {
                        Log.e(TAG, "连接到${server}失败: ${e.message}")
                        // 继续尝试下一个服务器
                    }
                }
                
                callback(reachable)
            } catch (e: Exception) {
                Log.e(TAG, "网络连通性测试异常: ${e.message}")
                callback(false)
            }
        }.start()
    }
    
    /**
     * 检查网络质量
     * 返回网络质量评估：EXCELLENT(优), GOOD(良), FAIR(中), POOR(差), UNAVAILABLE(无网络)
     */
    fun checkNetworkQuality(context: Context, callback: (NetworkQuality) -> Unit) {
        if (!isNetworkAvailable(context)) {
            callback(NetworkQuality.UNAVAILABLE)
            return
        }
        
        Thread {
            try {
                val startTime = System.currentTimeMillis()
                val address = java.net.InetAddress.getByName("114.114.114.114")
                val reachable = address.isReachable(3000)
                val pingTime = System.currentTimeMillis() - startTime
                
                Log.d(TAG, "网络延迟: ${pingTime}ms, 可达: $reachable")
                
                val quality = when {
                    !reachable -> NetworkQuality.POOR
                    pingTime < 100 -> NetworkQuality.EXCELLENT
                    pingTime < 300 -> NetworkQuality.GOOD
                    pingTime < 800 -> NetworkQuality.FAIR
                    else -> NetworkQuality.POOR
                }
                
                callback(quality)
            } catch (e: Exception) {
                Log.e(TAG, "网络质量检测异常: ${e.message}")
                callback(NetworkQuality.POOR)
            }
        }.start()
    }
    
    /**
     * 网络质量枚举
     */
    enum class NetworkQuality {
        EXCELLENT, // 优
        GOOD,      // 良
        FAIR,      // 中
        POOR,      // 差
        UNAVAILABLE // 无网络
    }
}