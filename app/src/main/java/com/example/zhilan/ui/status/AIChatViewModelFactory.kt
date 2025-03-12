package com.example.zhilan.ui.status

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * AI聊天视图模型工厂，用于提供带有Context的ViewModel实例
 */
class AIChatViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AIChatViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}