// 示例插件: 时长过滤
package com.bilipai.plugins

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.plugin.FeedPlugin
import com.android.purebilibili.data.model.response.VideoItem

/**
 * 🎬 时长过滤插件
 * 
 * 隐藏时长小于指定秒数的视频，帮助过滤短视频。
 */
class DurationFilterPlugin : FeedPlugin {
    
    override val id = "duration_filter"
    override val name = "时长过滤"
    override val description = "隐藏时长小于指定秒数的视频"
    override val version = "1.0.0"
    override val author = "BiliPai"
    
    // 最小时长阈值（秒）
    private var minDuration = 60
    
    override suspend fun onEnable() {
        // 可在此加载配置
    }
    
    override suspend fun onDisable() {
        // 清理资源
    }
    
    override fun shouldShowItem(item: VideoItem): Boolean {
        // 返回 true = 显示, false = 隐藏
        return item.duration >= minDuration
    }
    
    @Composable
    override fun SettingsContent() {
        var threshold by remember { mutableStateOf(minDuration) }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "最小时长: ${threshold}秒",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = threshold.toFloat(),
                onValueChange = { 
                    threshold = it.toInt()
                    minDuration = threshold
                },
                valueRange = 0f..300f,
                steps = 29 // 每10秒一档
            )
            Text(
                text = "低于此时长的视频将被隐藏",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
