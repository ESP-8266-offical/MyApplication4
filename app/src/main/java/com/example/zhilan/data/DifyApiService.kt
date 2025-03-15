package com.example.zhilan.data

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.net.InetAddress
import java.util.concurrent.TimeUnit

private const val TAG = "DifyApiService"

interface DifyApiService {

    @POST("chat-messages")
    suspend fun chat(
        @Body chatRequest: ChatRequest,
        @Header("Authorization") apiKey: String = "Bearer $API_KEY"
    ): ChatResponse

    data class ChatRequest(
        val query: String,
        val user: String = "user_1",
        val conversation_id: String? = null,
        val inputs: Map<String, String> = emptyMap(),
        val response_mode: String = "blocking",
        val auto_generate_name: Boolean = true
    )

    // 修改 ChatResponse 数据类，确保与 API 返回字段完全匹配
    data class ChatResponse(
        val answer: String?,  // 可能为null，使用可空类型
        @Json(name = "conversation_id") val conversationId: String? = null,
        @Json(name = "created_at") val createdAt: Long = 0,
        @Json(name = "message_id") val messageId: String? = null,
        val id: String? = null,  // 某些响应可能使用id而非message_id
        val error: String? = null,
        @Json(name = "error_code") val errorCode: Int? = null,
        val metadata: ResponseMetadata? = null
    )

    data class ResponseMetadata(
        val usage: UsageInfo? = null,
        @Json(name = "retriever_resources") val retrieverResources: List<RetrieverResource>? = null
    )

    data class UsageInfo(
        @Json(name = "prompt_tokens") val promptTokens: Int = 0,
        @Json(name = "completion_tokens") val completionTokens: Int = 0,
        @Json(name = "total_tokens") val totalTokens: Int = 0,
        @Json(name = "total_price") val totalPrice: String? = null
    )

    data class RetrieverResource(
        val position: Int = 0,
        @Json(name = "dataset_id") val datasetId: String? = null,
        @Json(name = "dataset_name") val datasetName: String? = null,
        val content: String? = null,
        val score: Double = 0.0
    )

    companion object {
        // 修改为新的 API 地址
        const val BASE_URL = "http://115.190.93.105/v1/"
        const val API_KEY = "app-qJSNEU6CJmS35GLnKrhgr7yX"

        fun create(): DifyApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // 自定义 DNS，返回仅 IPv4 地址
            val customDns = object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    return InetAddress.getAllByName(hostname).filter { it.address.size == 4 }
                }
            }

            val okHttpClient = OkHttpClient.Builder()
                .dns(customDns)
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .method(original.method, original.body)
                        .build()
                    
                    // 打印请求体
                    request.body?.let { body ->
                        try {
                            val buffer = okio.Buffer()
                            body.writeTo(buffer)
                            val requestBody = buffer.readUtf8()
                            Log.d(TAG, "请求体: $requestBody")
                        } catch (e: Exception) {
                            Log.e(TAG, "无法记录请求体: ${e.message}")
                        }
                    }
                    
                    Log.d(TAG, "发送请求: ${request.url}, 方法: ${request.method}")
                    
                    // 其余逻辑不变
                    val startTime = System.currentTimeMillis()
                    val response = chain.proceed(request)
                    val endTime = System.currentTimeMillis()
                    Log.d(TAG, "请求完成，耗时: ${endTime - startTime}ms")
                    Log.d(TAG, "收到响应: 状态码=${response.code}")
                    if (!response.isSuccessful) {
                        Log.e(TAG, "响应错误: ${response.code}")
                        try {
                            val errorBody = response.peekBody(Long.MAX_VALUE).string()
                            Log.e(TAG, "错误详情: $errorBody")
                        } catch (e: Exception) {
                            Log.e(TAG, "无法读取错误响应体: ${e.message}")
                        }
                    }
                    response
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(DifyApiService::class.java)
        }
    }
}