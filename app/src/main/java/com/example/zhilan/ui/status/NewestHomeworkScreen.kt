package com.example.zhilan.ui.status

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhilan.model.Task
import com.example.zhilan.model.TaskType
import java.text.SimpleDateFormat
import java.util.*

/**
 * 最新作业界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewestHomeworkScreen(onBackClick: () -> Unit) {
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(LocalContext.current))
    val tasks by taskViewModel.tasks.collectAsState()
    
    // 排序状态
    var sortByDate by remember { mutableStateOf(true) } // true: 按日期排序, false: 按名称排序
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("最新作业") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 排序按钮
                    IconButton(onClick = { sortByDate = !sortByDate }) {
                        Icon(Icons.Default.Sort, contentDescription = "排序")
                    }
                }
            )
        }
    ) { paddingValues ->
        // 筛选作业类型的任务
        val homeworkTasks = tasks.filter { it.taskType == TaskType.HOMEWORK }
        
        // 根据排序方式对任务进行排序
        val sortedTasks = if (sortByDate) {
            homeworkTasks.sortedByDescending { it.dueDate } // 按截止日期降序排序（最新的在前）
        } else {
            homeworkTasks.sortedBy { it.title } // 按标题字母顺序排序
        }
        
        if (sortedTasks.isEmpty()) {
            // 显示空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无作业",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "当前排序: ${if (sortByDate) "按日期" else "按名称"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                items(sortedTasks) { task ->
                    HomeworkCard(
                        task = task,
                        onClick = { /* 点击作业项的处理 */ },
                        onCompleteClick = { taskViewModel.completeTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeworkCard(task: Task, onClick: () -> Unit, onCompleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (task.isCompleted) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                
                // 完成按钮
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onCompleteClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 地点信息
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "位置",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.location,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 截止日期
            Text(
                text = "截止日期: ${formatDate(task.dueDate)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            
            // 如果有描述，显示描述
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// 格式化日期
private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}