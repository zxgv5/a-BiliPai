# BiliPai 插件开发指南

> 📌 适用版本: BiliPai v3.1.1+

## 目录

1. [架构概述](#架构概述)
2. [核心接口](#核心接口)
3. [可开发的插件类型](#可开发的插件类型)
4. [开发示例](#开发示例)
5. [如何注册插件](#如何注册插件)
6. [配置持久化](#配置持久化)
7. [内置插件参考](#内置插件参考)

---

## 架构概述

BiliPai 采用模块化插件系统，支持三种类型的插件：

| 类型 | 接口 | 钩子点 |
|------|------|--------|
| **播放器插件** | `PlayerPlugin` | 视频加载、播放位置更新、视频结束 |
| **弹幕插件** | `DanmakuPlugin` | 弹幕过滤、弹幕样式 |
| **信息流插件** | `FeedPlugin` | 首页推荐过滤 |

### 应用模块结构

```text
feature/
├── home/       # 首页推荐 (FeedPlugin 生效点)
├── video/      # 视频播放 (PlayerPlugin 生效点)
├── dynamic/    # 动态页面
├── search/     # 搜索功能
├── bangumi/    # 番剧播放
├── live/       # 直播功能
├── download/   # 离线缓存
├── history/    # 历史记录
├── space/      # 用户空间
└── settings/   # 设置页面
```

---

## 核心接口

### Plugin（基础接口）

```kotlin
interface Plugin {
    val id: String          // 唯一标识符，如 "myplugin"
    val name: String        // 显示名称
    val description: String // 功能描述
    val version: String     // 版本号 "1.0.0"
    val author: String      // 作者署名
        get() = "Unknown"
    val icon: ImageVector?  // 可选图标
    
    suspend fun onEnable()  // 启用时调用
    suspend fun onDisable() // 禁用时调用
    
    @Composable
    fun SettingsContent()   // 配置界面（可选）
}
```

### PlayerPlugin

```kotlin
interface PlayerPlugin : Plugin {
    // 视频加载时回调 (获取 bvid/cid)
    suspend fun onVideoLoad(bvid: String, cid: Long)
    
    // 播放位置更新 (约每 500ms 调用一次)
    suspend fun onPositionUpdate(positionMs: Long): SkipAction?
    
    // 视频播放结束
    fun onVideoEnd()
}

// 跳过动作
sealed class SkipAction {
    object None : SkipAction()
    data class SkipTo(val positionMs: Long, val reason: String) : SkipAction()
    data class ShowButton(val skipToMs: Long, val label: String, val segmentId: String) : SkipAction()
}
```

### DanmakuPlugin

```kotlin
interface DanmakuPlugin : Plugin {
    // 过滤弹幕，返回 null = 屏蔽
    fun filterDanmaku(danmaku: DanmakuItem): DanmakuItem?
    
    // 自定义弹幕样式
    fun styleDanmaku(danmaku: DanmakuItem): DanmakuStyle?
}
```

### FeedPlugin

```kotlin
interface FeedPlugin : Plugin {
    // 判断是否显示该推荐项
    fun shouldShowItem(item: VideoItem): Boolean
}
```

---

## 可开发的插件类型

### 🎬 播放器类 (PlayerPlugin)

| 插件想法 | 功能描述 | 实现难度 |
|---------|---------|----------|
| **SponsorBlock** ✅ | 跳过赞助/广告片段 | ⭐⭐⭐ |
| **自动跳过片头片尾** | 检测并跳过 OP/ED | ⭐⭐⭐⭐ |
| **播放速度记忆** | 记住每个 UP 主的播放速度 | ⭐⭐ |
| **自动连播控制** | 智能连播规则 | ⭐⭐ |
| **AI 摘要** | 视频内容 AI 总结 | ⭐⭐⭐⭐⭐ |
| **进度同步** | 多设备播放进度同步 | ⭐⭐⭐⭐ |

### 💬 弹幕类 (DanmakuPlugin)

| 插件想法 | 功能描述 | 实现难度 |
|---------|---------|----------|
| **弹幕增强** ✅ | 关键词屏蔽/高亮 | ⭐⭐ |
| **同传高亮** | 高亮翻译弹幕 | ⭐⭐ |
| **剧透保护** | 屏蔽剧透弹幕 | ⭐⭐ |
| **弹幕翻译** | 实时翻译外语弹幕 | ⭐⭐⭐⭐ |
| **用户拉黑** | 屏蔽特定用户弹幕 | ⭐⭐ |
| **情感分析** | 分析弹幕情感走向 | ⭐⭐⭐⭐ |

### 📰 信息流类 (FeedPlugin)

| 插件想法 | 功能描述 | 实现难度 |
|---------|---------|----------|
| **去广告增强** ✅ | 过滤广告/推广 | ⭐⭐ |
| **标题党过滤** | 过滤震惊体标题 | ⭐⭐ |
| **UP 主拉黑** | 屏蔽特定 UP 主 | ⭐⭐ |
| **时长过滤** | 过滤短/长视频 | ⭐ |
| **分区过滤** | 隐藏特定分区内容 | ⭐⭐ |
| **低质量过滤** | 根据播放/点赞比过滤 | ⭐⭐ |

### 🌙 其他类 (Plugin)

| 插件想法 | 功能描述 | 实现难度 |
|---------|---------|----------|
| **夜间护眼** ✅ | 护眼提醒、暖色滤镜 | ⭐⭐⭐ |
| **使用统计** | 观看时长统计 | ⭐⭐ |
| **通知提醒** | UP 主更新通知 | ⭐⭐⭐ |
| **主题切换** | 自定义 UI 主题 | ⭐⭐⭐⭐ |

---

## 开发示例

### 示例1: 时长过滤插件

```kotlin
class DurationFilterPlugin : FeedPlugin {
    override val id = "duration_filter"
    override val name = "时长过滤"
    override val description = "隐藏时长小于60秒的视频"
    override val version = "1.0.0"
    override val author = "YourName"
    
    override fun shouldShowItem(item: VideoItem): Boolean {
        return item.duration >= 60  // 只显示60秒以上的视频
    }
}
```

### 示例2: 自动速度记忆插件

```kotlin
class SpeedMemoryPlugin : PlayerPlugin {
    override val id = "speed_memory"
    override val name = "速度记忆"
    override val description = "记住每个UP主的播放速度"
    override val version = "1.0.0"
    override val author = "YourName"
    
    private val speedMap = mutableMapOf<Long, Float>()  // mid -> speed
    
    override suspend fun onVideoLoad(bvid: String, cid: Long) {
        // 可以通过 API 获取 UP 主信息并恢复速度
    }
    
    override suspend fun onPositionUpdate(positionMs: Long): SkipAction? {
        return SkipAction.None  // 不需要跳过
    }
}
```

### 示例3: 弹幕用户拉黑

```kotlin
class UserBlockPlugin : DanmakuPlugin {
    override val id = "user_block"
    override val name = "用户拉黑"
    override val description = "屏蔽特定用户的弹幕"
    override val version = "1.0.0"
    override val author = "YourName"
    
    private val blockedUsers = setOf<String>()  // 拉黑的用户ID
    
    override fun filterDanmaku(danmaku: DanmakuItem): DanmakuItem? {
        return if (danmaku.userId in blockedUsers) null else danmaku
    }
}
```

---

## 如何注册插件

在 `BiliPaiApplication.kt` 中：

```kotlin
class BiliPaiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PluginManager.initialize(this)
        
        // 注册内置插件
        PluginManager.register(SponsorBlockPlugin())
        PluginManager.register(AdFilterPlugin())
        PluginManager.register(DanmakuEnhancePlugin())
        PluginManager.register(EyeProtectionPlugin())
        
        // 🆕 注册你的自定义插件
        PluginManager.register(MyCustomPlugin())
    }
}
```

---

## 配置持久化

使用 `PluginStore` 保存/读取配置：

```kotlin
// 定义配置类
@Serializable
data class MyPluginConfig(
    val enabled: Boolean = true,
    val threshold: Int = 1000
)

// 保存配置
val json = Json.encodeToString(config)
PluginStore.setConfigJson(context, pluginId, json)

// 读取配置
val jsonStr = PluginStore.getConfigJson(context, pluginId)
val config = Json.decodeFromString<MyPluginConfig>(jsonStr)
```

---

## 内置插件参考

| 插件 | ID | 类型 | 作者 |
|------|-----|------|------|
| 空降助手 | `sponsor_block` | PlayerPlugin | YangY |
| 去广告增强 | `adfilter` | FeedPlugin | YangY |
| 弹幕增强 | `danmaku_enhance` | DanmakuPlugin | YangY |
| 夜间护眼 | `eye_protection` | Plugin | YangY |

---

## API 参考

### 获取插件实例

```kotlin
// 获取所有已启用的 PlayerPlugin
val plugins = PluginManager.getEnabledPlayerPlugins()

// 获取特定插件
val plugin = PluginManager.plugins.find { it.plugin.id == "my_plugin" }
```

### 获取应用上下文

```kotlin
val context = PluginManager.getContext()
```

---

如有问题，欢迎在 [GitHub Issues](https://github.com/jay3-yy/BiliPai/issues) 反馈！

---

## 完整开发教程：从零开始创建插件

### Step 1: Fork 项目

```bash
git clone https://github.com/jay3-yy/BiliPai.git
cd BiliPai
```

### Step 2: 创建插件文件

在 `app/src/main/java/com/android/purebilibili/feature/plugin/` 目录下创建新文件：

```
MyAwesomePlugin.kt
```

### Step 3: 实现插件类（完整模板）

```kotlin
// 文件路径: feature/plugin/MyAwesomePlugin.kt
package com.android.purebilibili.feature.plugin

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.plugin.FeedPlugin
import com.android.purebilibili.core.plugin.PluginStore
import com.android.purebilibili.data.model.response.VideoItem
import io.github.alexzhirkevich.cupertino.CupertinoSwitch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 🌟 我的插件
 */
class MyAwesomePlugin : FeedPlugin {
    
    // ====== 基本信息 ======
    override val id = "my_awesome_plugin"        // 唯一ID，不要与其他插件重复
    override val name = "我的插件"                // 显示名称
    override val description = "这是插件功能描述"  // 功能描述
    override val version = "1.0.0"               // 版本号
    override val author = "YourGitHubName"       // 你的名字
    override val icon: ImageVector = Icons.Outlined.Star  // 图标
    
    // ====== 配置 ======
    private var config: MyPluginConfig = MyPluginConfig()
    
    // ====== 生命周期 ======
    override suspend fun onEnable() {
        loadConfigSuspend()
        // 插件启用时的初始化代码
    }
    
    override suspend fun onDisable() {
        // 插件禁用时的清理代码
    }
    
    // ====== 核心功能（这里是 FeedPlugin 的实现） ======
    override fun shouldShowItem(item: VideoItem): Boolean {
        if (!config.filterEnabled) return true
        
        // 示例：过滤时长小于某个值的视频
        return item.duration >= config.minDuration
    }
    
    // ====== 配置界面 ======
    @Composable
    override fun SettingsContent() {
        val context = LocalContext.current
        var filterEnabled by remember { mutableStateOf(config.filterEnabled) }
        var minDuration by remember { mutableStateOf(config.minDuration) }
        
        // 加载配置
        LaunchedEffect(Unit) {
            loadConfig(context)
            filterEnabled = config.filterEnabled
            minDuration = config.minDuration
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("启用过滤", style = MaterialTheme.typography.bodyLarge)
                CupertinoSwitch(
                    checked = filterEnabled,
                    onCheckedChange = { newValue ->
                        filterEnabled = newValue
                        config = config.copy(filterEnabled = newValue)
                        saveConfig(context)
                    }
                )
            }
            
            // 滑块设置
            if (filterEnabled) {
                Column {
                    Text("最小时长: ${minDuration}秒")
                    Slider(
                        value = minDuration.toFloat(),
                        onValueChange = { newValue ->
                            minDuration = newValue.toInt()
                            config = config.copy(minDuration = newValue.toInt())
                        },
                        onValueChangeFinished = { saveConfig(context) },
                        valueRange = 0f..300f
                    )
                }
            }
        }
    }
    
    // ====== 配置加载/保存 ======
    private suspend fun loadConfigSuspend() {
        try {
            val context = com.android.purebilibili.core.plugin.PluginManager.getContext()
            val jsonStr = PluginStore.getConfigJson(context, id)
            if (jsonStr != null) {
                config = Json.decodeFromString<MyPluginConfig>(jsonStr)
            }
        } catch (e: Exception) {
            // 加载失败使用默认配置
        }
    }
    
    private fun loadConfig(context: Context) {
        runBlocking {
            val jsonStr = PluginStore.getConfigJson(context, id)
            if (jsonStr != null) {
                try {
                    config = Json.decodeFromString<MyPluginConfig>(jsonStr)
                } catch (e: Exception) { }
            }
        }
    }
    
    private fun saveConfig(context: Context) {
        runBlocking {
            PluginStore.setConfigJson(context, id, Json.encodeToString(config))
        }
    }
}

/**
 * 插件配置（可序列化）
 */
@Serializable
data class MyPluginConfig(
    val filterEnabled: Boolean = true,
    val minDuration: Int = 60
)
```

### Step 4: 注册插件

打开 `app/src/main/java/com/android/purebilibili/BiliPaiApplication.kt`，添加：

```kotlin
class BiliPaiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PluginManager.initialize(this)
        
        // ... 其他内置插件 ...
        
        // 🆕 注册你的插件
        PluginManager.register(MyAwesomePlugin())
    }
}
```

### Step 5: 编译测试

```bash
./gradlew :app:assembleDebug
```

安装到设备后，进入 **设置 → 插件中心** 查看你的插件！

---

## 常用 API

### VideoItem 数据结构

```kotlin
data class VideoItem(
    val bvid: String,           // BV号
    val title: String,          // 标题
    val duration: Int,          // 时长（秒）
    val owner: Owner,           // UP主信息
    val stat: Stat,             // 统计信息
    val pic: String,            // 封面URL
    val pubdate: Long           // 发布时间戳
)

data class Owner(
    val mid: Long,              // UP主ID
    val name: String,           // UP主名称
    val face: String            // 头像URL
)

data class Stat(
    val view: Int,              // 播放量
    val danmaku: Int,           // 弹幕数
    val like: Int,              // 点赞数
    val coin: Int,              // 投币数
    val favorite: Int           // 收藏数
)
```

### DanmakuItem 数据结构

```kotlin
data class DanmakuItem(
    val id: Long,               // 弹幕ID
    val content: String,        // 弹幕内容
    val timeMs: Long,           // 出现时间（毫秒）
    val type: Int,              // 1=滚动, 4=底部, 5=顶部
    val color: Int,             // 颜色值
    val userId: String          // 发送者ID
)
```

### 日志输出

```kotlin
import com.android.purebilibili.core.util.Logger

Logger.d("MyPlugin", "调试信息")
Logger.i("MyPlugin", "普通信息")
Logger.w("MyPlugin", "警告信息")
Logger.e("MyPlugin", "错误信息", exception)
```

---

## 提交插件

1. 完成开发并测试
2. Fork BiliPai 仓库
3. 提交 Pull Request
4. 在 PR 描述中说明插件功能

优秀的社区插件会被合并到官方版本！

---

## 🆕 JSON 规则插件（推荐）

**无需编程！** 通过简单的 JSON 配置即可创建插件。

### 文件格式

```json
{
  "id": "my_plugin",
  "name": "我的插件",
  "description": "插件描述",
  "version": "1.0.0",
  "author": "你的名字",
  "type": "feed",
  "rules": [...]
}
```

### 插件类型

| type | 用途 |
|------|------|
| `feed` | 过滤首页推荐视频 |
| `danmaku` | 过滤/高亮弹幕 |

---

### 📰 信息流规则 (type: "feed")

#### 可用字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `title` | String | 视频标题 |
| `duration` | Int | 时长（秒） |
| `owner.mid` | Long | UP主ID |
| `owner.name` | String | UP主名称 |
| `stat.view` | Int | 播放量 |
| `stat.like` | Int | 点赞数 |
| `stat.danmaku` | Int | 弹幕数 |

#### 示例：短视频过滤

```json
{
  "id": "short_video_filter",
  "name": "短视频过滤",
  "type": "feed",
  "rules": [
    { "field": "duration", "op": "lt", "value": 60, "action": "hide" }
  ]
}
```

#### 示例：UP主屏蔽

```json
{
  "id": "up_blocker",
  "name": "UP主屏蔽",
  "type": "feed",
  "rules": [
    { "field": "owner.mid", "op": "eq", "value": 12345678, "action": "hide" },
    { "field": "owner.name", "op": "contains", "value": "某UP主", "action": "hide" }
  ]
}
```

#### 示例：标题关键词过滤

```json
{
  "id": "keyword_filter",
  "name": "标题党过滤",
  "type": "feed",
  "rules": [
    { "field": "title", "op": "contains", "value": "广告", "action": "hide" },
    { "field": "title", "op": "regex", "value": "震惊.*必看", "action": "hide" }
  ]
}
```

---

### 💬 弹幕规则 (type: "danmaku")

#### 可用字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `content` | String | 弹幕内容 |
| `userId` | String | 发送者ID |
| `type` | Int | 1滚动/4底部/5顶部 |

#### 示例：弹幕过滤

```json
{
  "id": "danmaku_filter",
  "name": "弹幕过滤",
  "type": "danmaku",
  "rules": [
    { "field": "content", "op": "contains", "value": "剧透", "action": "hide" },
    { "field": "content", "op": "regex", "value": "^[哈]{5,}$", "action": "hide" }
  ]
}
```

#### 示例：同传高亮

```json
{
  "id": "translator_highlight",
  "name": "同传高亮",
  "type": "danmaku",
  "rules": [
    {
      "field": "content",
      "op": "startsWith",
      "value": "【",
      "action": "highlight",
      "style": { "color": "#FFD700", "bold": true }
    }
  ]
}
```

---

### 操作符列表

| 操作符 | 说明 | 适用类型 |
|--------|------|---------|
| `eq` | 等于 | 所有 |
| `ne` | 不等于 | 所有 |
| `lt` | 小于 | 数字 |
| `le` | 小于等于 | 数字 |
| `gt` | 大于 | 数字 |
| `ge` | 大于等于 | 数字 |
| `contains` | 包含 | 字符串 |
| `startsWith` | 开头匹配 | 字符串 |
| `endsWith` | 结尾匹配 | 字符串 |
| `regex` | 正则匹配 | 字符串 |
| `in` | 在列表中 | 所有 |

### 动作列表

| 动作 | 说明 |
|------|------|
| `hide` | 隐藏 |
| `highlight` | 高亮（仅弹幕） |

### 高亮样式

```json
"style": {
  "color": "#FFD700",
  "bold": true,
  "scale": 1.2
}
```

---

### 使用方法

1. 将 JSON 文件上传到 GitHub 或其他公开 URL
2. 在 BiliPai 中进入 **设置 → 插件中心 → 导入外部插件**
3. 粘贴 URL（以 `.json` 结尾）
4. 点击 **安装**，完成！

### 示例插件

官方示例插件：[plugins/samples/](../plugins/samples/)
