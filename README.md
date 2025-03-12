# 智蓝课程表应用

## 项目概述

智蓝课程表是一款专为学生设计的Android课程管理应用，采用现代化的Jetpack Compose UI框架开发，提供直观的课程表视图、课程管理功能和个性化设置。应用使用MVVM架构模式，结合Kotlin协程和Flow实现了响应式的数据更新机制。

## 技术架构

### 架构模式

应用采用MVVM（Model-View-ViewModel）架构模式：

- **Model**: 数据模型层，包含Course等数据类和CourseRepository数据仓库
- **View**: 基于Jetpack Compose的UI组件，如ScheduleScreen、ScheduleEditScreen等
- **ViewModel**: 业务逻辑层，如ScheduleViewModel，负责处理UI事件和管理数据状态

### 核心技术

- **Jetpack Compose**: 声明式UI框架，用于构建现代化的用户界面
- **Kotlin Flow**: 实现数据的响应式更新和UI状态管理
- **SQLite**: 本地数据存储，通过CourseDBHelper和CourseRepository实现
- **Kotlin协程**: 处理异步操作，如数据库读写

## 模块说明

### 数据模型模块

#### Course类

```kotlin
data class Course(
    val id: Int = 0,
    val name: String = "",
    val location: String = "",
    val teacher: String = "",
    val dayOfWeek: Int = 1, // 1-7 表示周一到周日
    val startSection: Int = 1, // 开始节次
    val endSection: Int = 2, // 结束节次
    val startWeek: Int = 1, // 开始周
    val endWeek: Int = 16, // 结束周
    val weekType: WeekType = WeekType.ALL, // 周类型：每周、单周、双周
    val color: Int = 0, // 课程卡片颜色
    val alpha: Float = 0.5f // 课程卡片透明度
)
```

#### WeekType枚举

```kotlin
enum class WeekType {
    ALL,   // 每周
    ODD,   // 单周
    EVEN   // 双周
}
```

#### ScheduleSettings类

用于存储课程表的各种配置，包括：
- 是否显示周末
- 学期总周数
- 每天课程节数
- 课程时间表等

### 数据库模块

#### CourseDBHelper

负责创建和升级SQLite数据库：
- 创建coursedata表，包含课程的各项属性
- 提供数据库版本管理和升级策略

#### CourseRepository

数据仓库类，负责课程数据的CRUD操作：
- 使用ContentValues构建数据操作
- 通过MutableStateFlow实现数据的实时更新
- 提供addCourse、updateCourse、deleteCourse等方法
- 实现周数据的编码和解码（单双周处理）

实现细节：
- 使用位图字符串（"1010..."）表示课程的周次信息
- 通过Flow机制实现数据变更的自动通知
- 在数据变更后自动重新加载数据并更新UI

### 视图模型模块

#### ScheduleViewModel

课程表视图模型，负责：
- 管理课程数据状态
- 处理课程的添加、更新和删除
- 检查课程时间冲突
- 管理当前周次信息

实现特点：
- 使用Flow订阅数据库变化，实现数据的实时同步
- 提供课程冲突检测算法，避免课程时间重叠
- 支持单双周课程的特殊处理

### UI模块

#### ScheduleScreen

主课程表界面，包含：
- 周数选择器
- 时间列显示
- 课程网格视图
- 添加课程的浮动按钮

#### ScheduleEditScreen

课程编辑界面，提供：
- 课程基本信息编辑（名称、教师、地点）
- 时间设置（星期、节次、周次）
- 单双周设置
- 课程颜色选择

### 主要Activity

#### MainActivity

应用主界面，实现：
- 底部导航栏，包含课程表、状态、个人和设置四个选项
- 使用NavHost管理不同页面的导航
- 处理课程编辑结果回调

#### ScheduleActivity

课程表专用Activity，负责：
- 初始化ScheduleViewModel
- 显示ScheduleScreen
- 处理课程点击和编辑事件

#### ScheduleEditActivity

课程编辑Activity，用于：
- 新增课程
- 编辑现有课程
- 删除课程

## 数据流转机制

应用采用响应式数据流模式：

1. **数据变更**：通过Repository的方法修改数据库
2. **数据通知**：Repository使用MutableStateFlow通知数据变化
3. **ViewModel订阅**：ViewModel通过Flow.collectLatest订阅数据变化
4. **UI更新**：Compose UI通过collectAsState()自动响应数据变化

这种机制确保了数据的一致性和UI的实时更新，无需手动刷新。

## 特色功能

### 课程冲突检测

- 添加或更新课程时自动检测时间冲突
- 考虑课程的周次、星期和节次进行全面检测
- 支持单双周的特殊处理

### 周次过滤

- 支持按周次筛选显示课程
- 自动处理单双周课程的显示逻辑

### 课程卡片样式

- 支持自定义课程卡片颜色
- 使用渐变色提升视觉效果

## 安装和使用

### 系统要求

- Android 5.0 (API 21) 或更高版本
- 推荐Android 8.0 (API 26) 或更高版本以获得最佳体验

### 安装步骤

1. 下载APK文件
2. 在Android设备上安装应用
3. 首次启动时授予必要权限

### 基本使用

1. 点击右下角的"+"按钮添加新课程
2. 填写课程信息并保存
3. 在主界面查看课程表
4. 点击课程卡片可编辑或删除课程
5. 使用顶部的周次选择器切换不同周次的课程视图

## 开发者信息

### 项目依赖

- Kotlin 1.8.0+
- Jetpack Compose 1.4.0+
- AndroidX Core KTX 1.9.0+
- AndroidX Lifecycle 2.6.0+
- Material3 Components 1.1.0+

### 构建项目

1. 克隆项目代码
2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 构建并运行项目

## 未来计划

- 添加课程提醒功能
- 支持导入导出课程数据
- 增加更多主题和自定义选项
- 优化平板设备的界面布局