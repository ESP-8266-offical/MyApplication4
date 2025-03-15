package com.example.zhilan.ui.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// 不需要再定义ChatMessage数据类，直接使用AIChatViewModel.kt中的定义

/**
 * AI聊天界面组件
 * @param modifier 修饰符
 * @param viewModel 聊天视图模型
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatSection(
    modifier: Modifier = Modifier,
    viewModel: AIChatViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 聊天标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "知澜AI助手",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 顶部工具栏，添加测试按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { viewModel.testApiConnection() },
                enabled = !isLoading
            ) {
                Text("测试API")
            }
        }
        
        // 聊天消息列表
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 默认欢迎消息
            if (messages.isEmpty()) {
                item {
                    ChatBubble(
                        message = ChatMessage(
                            content = "你好，我是知澜AI助手。有什么可以帮到你的吗？",
                            isFromUser = false
                        )
                    )
                }
            } else {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
                
                // 显示加载指示器
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
        
        // 输入栏
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 文本输入框
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("请输入问题...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                
                // 发送按钮
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                            coroutineScope.launch {
                                // 滑动到底部
                                listState.animateScrollToItem(messages.size)
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 聊天气泡组件
 * @param message 消息对象
 */
@Composable
fun ChatBubble(message: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isFromUser) 16.dp else 4.dp,
                topEnd = if (message.isFromUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.isFromUser) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}