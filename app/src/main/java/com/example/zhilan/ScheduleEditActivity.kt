package com.example.zhilan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhilan.model.Course
import com.example.zhilan.ui.schedule.ScheduleEditScreen
import com.example.zhilan.ui.schedule.ScheduleViewModelFactory
import com.example.zhilan.ui.theme.ZhiLanTheme

class ScheduleEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val course = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("course", Course::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("course") as? Course
        }

        setContent {
            ZhiLanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ScheduleViewModel = viewModel(factory = ScheduleViewModelFactory(this@ScheduleEditActivity))
                    
                    ScheduleEditScreen(
                        course = course,
                        onSave = { updatedCourse ->
                            // 如果是编辑现有课程
                            if (course != null) {
                                viewModel.updateCourse(updatedCourse)
                            } else {
                                // 如果是添加新课程
                                viewModel.addCourse(updatedCourse)
                            }
                            Toast.makeText(this@ScheduleEditActivity, "课程已保存", Toast.LENGTH_SHORT).show()
                            val resultIntent = Intent()
                            resultIntent.putExtra("RESULT_COURSE_CHANGED", true)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        },
                        onCancel = {
                            setResult(RESULT_CANCELED)
                            finish()
                        },
                        onDelete = { courseId ->
                            viewModel.deleteCourse(courseId)
                            Toast.makeText(this@ScheduleEditActivity, "课程已删除", Toast.LENGTH_SHORT).show()
                            val resultIntent = Intent()
                            resultIntent.putExtra("RESULT_COURSE_CHANGED", true)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun createIntent(context: Context, course: Course? = null): Intent {
            return Intent(context, ScheduleEditActivity::class.java).apply {
                putExtra("course", course)
            }
        }
        const val REQUEST_CODE_EDIT_SCHEDULE = 1001
    }
}