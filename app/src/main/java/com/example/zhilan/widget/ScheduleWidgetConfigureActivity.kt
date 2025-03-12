package com.example.zhilan.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.zhilan.ui.theme.ZhiLanTheme

/**
 * 课程表小组件配置活动
 * 用于配置小组件的显示内容和更新频率
 */
class ScheduleWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置结果为取消，如果用户按了返回键，小组件将不会被添加
        setResult(Activity.RESULT_CANCELED)

        // 获取小组件ID
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // 如果没有获取到有效的小组件ID，则结束活动
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // 获取当前小组件设置
        val showTodayOnly = ScheduleWidgetProvider.getShowTodayOnly(this)
        val updateInterval = ScheduleWidgetProvider.getUpdateInterval(this)

        setContent {
            ZhiLanTheme {
                WidgetConfigScreen(
                    showTodayOnly = showTodayOnly,
                    updateInterval = updateInterval,
                    onSaveClicked = { newShowTodayOnly, newUpdateInterval ->
                        // 保存设置
                        ScheduleWidgetProvider.setShowTodayOnly(this, newShowTodayOnly)
                        ScheduleWidgetProvider.setUpdateInterval(this, newUpdateInterval)

                        // 更新小组件
                        val appWidgetManager = AppWidgetManager.getInstance(this)
                        ScheduleWidgetProvider().updateAppWidget(this, appWidgetManager, appWidgetId)

                        // 设置结果为成功并结束活动
                        val resultValue = Intent()
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setResult(Activity.RESULT_OK, resultValue)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun WidgetConfigScreen(
    showTodayOnly: Boolean,
    updateInterval: Int,
    onSaveClicked: (Boolean, Int) -> Unit
) {
    var showTodayOnlyState by remember { mutableStateOf(showTodayOnly) }
    var updateIntervalState by remember { mutableStateOf(updateInterval.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "小组件设置",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 显示设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 只显示今天课程设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("只显示今天课程")
                    Switch(
                        checked = showTodayOnlyState,
                        onCheckedChange = { showTodayOnlyState = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 更新间隔设置
                OutlinedTextField(
                    value = updateIntervalState,
                    onValueChange = { 
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            updateIntervalState = it
                        }
                    },
                    label = { Text("更新间隔（分钟）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 保存按钮
        Button(
            onClick = {
                val interval = updateIntervalState.toIntOrNull() ?: 20
                onSaveClicked(showTodayOnlyState, interval)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存设置")
        }
    }
}