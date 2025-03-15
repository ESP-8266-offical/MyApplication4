package com.example.zhilan

import android.app.Application
import com.example.zhilan.utils.NetworkMonitor

class ZhilanApp : Application() {
    
    lateinit var networkMonitor: NetworkMonitor
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化网络监听器
        networkMonitor = NetworkMonitor(this)
        networkMonitor.startMonitoring()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.stopMonitoring()
    }
}