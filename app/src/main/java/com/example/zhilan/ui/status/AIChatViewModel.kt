package com.example.zhilan.ui.status

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhilan.data.DifyApiService
import com.example.zhilan.data.DifyApiService.ChatRequest
import com.example.zhilan.utils.NetworkUtils
import com.example.zhilan.utils.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val TAG = "AIChatViewModel"

// 聊天消息数据类
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isRendered: Boolean = false  // 添加标记字段，用于跟踪消息是否已经渲染
)

class AIChatViewModel(private val context: Context) : ViewModel() {
    
    // 聊天消息列表
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 网络状态
    private val _networkStatus = MutableStateFlow(NetworkUtils.NetworkQuality.UNAVAILABLE)
    val networkStatus: StateFlow<NetworkUtils.NetworkQuality> = _networkStatus.asStateFlow()
    
    // 重试计数
    private var retryCount = 0
    private val MAX_RETRY_COUNT = 3
    
    // 保存上一次对话的 conversationId 用于持续对话
    private var lastConversationId: String? = null
    
    // 保存最后一次请求内容，用于网络恢复时重试
    private var lastRequestContent: String? = null
    
    // Dify API服务
    private val difyApiService = DifyApiService.create()
    
    // 网络监听器
    private val networkMonitor = NetworkMonitor(context)
    
    init {
        // 添加欢迎消息
        addMessage("你好，我是知澜AI助手。有什么可以帮到你的吗？", false)
        
        // 启动网络监听
        networkMonitor.startMonitoring()
        
        // 监听网络状态变化
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline: Boolean ->
                if (isOnline) {
                    // 检查网络质量
                    checkNetworkQuality()
                    // 如果有未完成的请求，尝试重新发送
                    lastRequestContent?.let { content ->
                        if (retryCount < MAX_RETRY_COUNT) {
                            addMessage("网络已恢复，正在重新连接...", false)
                            delay(1000) // 稍等片刻再重试
                            makeApiRequest(content)
                        }
                        true // 返回Boolean值以匹配类型
                    }
                } else {
                    _networkStatus.value = NetworkUtils.NetworkQuality.UNAVAILABLE
                }
            }
        }
    }
    
    /**
     * 发送消息到AI助手
     */
    fun sendMessage(content: String) {
        // 先显示用户发送的消息
        addMessage(content, true)
        
        if (!NetworkUtils.isNetworkAvailable(context)) {
            addMessage("无法连接到网络，请检查您的网络设置。", false)
            return
        }
        
        // 直接发起 API 请求
        viewModelScope.launch {
            retryCount = 0
            makeApiRequest(content)
        }
    }
    
    /**
     * 检查网络质量
     */
    private fun checkNetworkQuality() {
        NetworkUtils.checkNetworkQuality(context) { quality ->
            _networkStatus.value = quality
            Log.d(TAG, "当前网络质量: $quality")
        }
    }
    
    /**
     * 向AI发送API请求
     */
    private fun makeApiRequest(content: String) {
        _isLoading.value = true
        lastRequestContent = content // 保存请求内容，用于网络恢复时重试
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始发送API请求: $content")
                
                val requestParams = ChatRequest(
                    query = content,
                    user = "fixed_user", 
                    response_mode = "blocking",
                    conversation_id = lastConversationId
                )
                
                Log.d(TAG, "发送请求到Dify API: 参数=$requestParams")
                
                val response = difyApiService.chat(requestParams)
                
                // 请求成功，清除上次请求内容
                lastRequestContent = null
                retryCount = 0
                
                // 打印完整响应以便调试
                Log.d(TAG, "收到完整响应: $response")
                
                if (response.error != null) {
                    Log.e(TAG, "API返回错误: ${response.error}")
                    addMessage("AI服务返回错误: ${response.error}", false)
                } else if (response.answer == null) {
                    Log.e(TAG, "API返回为空")
                    addMessage("AI助手返回了空结果，请稍后重试。", false)
                } else {
                    // 记录会话ID
                    lastConversationId = response.conversationId
                    Log.d(TAG, "收到回复: ${response.answer}, 会话ID: $lastConversationId")
                    
                    // 显示回复
                    addMessage(response.answer, false)
                }
            } catch (e: Exception) {
                // 输出详细的异常类型和堆栈信息
                Log.e(TAG, "API请求异常: ${e.javaClass.name} - ${e.message}", e)
                
                // 根据异常类型显示不同消息
                when (e) {
                    is HttpException -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            Log.e(TAG, "HTTP错误 ${e.code()}: $errorBody")
                            addMessage("服务器返回错误(${e.code()}): ${errorBody ?: "未知错误"}", false)
                        } catch (e2: Exception) {
                            addMessage("请求处理异常: ${e.message ?: "未知HTTP错误"}", false)
                        }
                    }
                    is SocketTimeoutException -> {
                        handleRetry(content, "连接超时，正在重试...")
                    }
                    is UnknownHostException -> {
                        handleRetry(content, "无法解析服务器地址，正在重试...")
                    }
                    is IOException -> {
                        handleRetry(content, "网络连接异常，正在重试...")
                    }
                    else -> addMessage("处理请求时遇到问题: ${e.message ?: e.javaClass.simpleName}", false)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 处理重试逻辑
     */
    private fun handleRetry(content: String, retryMessage: String) {
        retryCount++
        if (retryCount <= MAX_RETRY_COUNT) {
            addMessage("$retryMessage (尝试 $retryCount/$MAX_RETRY_COUNT)", false)
            viewModelScope.launch {
                delay(2000)
                makeApiRequest(content)
            }
        } else {
            addMessage("多次尝试后仍无法连接到AI服务器。请检查您的网络连接，稍后再试。", false)
        }
    }
    
    /**
     * 添加消息到消息列表
     */
    private fun addMessage(content: String, isFromUser: Boolean) {
        val message = ChatMessage(content = content, isFromUser = isFromUser, isRendered = false)
        _messages.value = _messages.value + message
    }
    
    /**
     * 测试API连接（调试用）
     */
    fun testApiConnection() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "测试API连接")
                // 与正常对话使用相同的参数格式
                val requestParams = ChatRequest(
                    query = "测试消息",
                    user = "fixed_user",
                    response_mode = "blocking",
                    conversation_id = null
                )
                Log.d(TAG, "测试API参数: $requestParams")
                
                val response = difyApiService.chat(requestParams)
                
                Log.d(TAG, "测试API完整响应: $response")
                addMessage("测试API成功！响应: ${response.answer}", false)
            } catch (e: Exception) {
                Log.e(TAG, "测试API异常: ${e.javaClass.simpleName} - ${e.message}", e)
                addMessage("测试API异常: ${e.message}", false)
            }
        }
    }
    
    /**
     * 更新消息的渲染状态
     * @param index 消息索引
     * @param isRendered 是否已渲染
     */
    fun updateMessageRenderedStatus(index: Int, isRendered: Boolean) {
        if (index >= 0 && index < _messages.value.size) {
            val updatedMessages = _messages.value.toMutableList()
            val message = updatedMessages[index]
            updatedMessages[index] = message.copy(isRendered = isRendered)
            _messages.value = updatedMessages
        }
    }
    
}