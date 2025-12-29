// 文件路径: feature/video/ui/components/VideoSettingsPanel.kt
package com.android.purebilibili.feature.video.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 🍎 Cupertino Icons
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.*

/**
 * 🔥 视频设置面板 - 竖屏模式下的高级设置底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSettingsPanel(
    // 定时关闭
    sleepTimerMinutes: Int?,
    onSleepTimerChange: (Int?) -> Unit,
    
    // 视频控制
    onReload: () -> Unit,
    
    // 画质 - 内联选择
    currentQualityLabel: String,
    qualityLabels: List<String> = emptyList(),
    qualityIds: List<Int> = emptyList(),
    onQualitySelected: (Int) -> Unit = {},
    
    // 倍速
    currentSpeed: Float = 1.0f,
    onSpeedChange: (Float) -> Unit = {},
    
    // 镜像翻转
    isFlippedHorizontal: Boolean = false,
    isFlippedVertical: Boolean = false,
    onFlipHorizontal: () -> Unit = {},
    onFlipVertical: () -> Unit = {},
    
    // 音频模式
    isAudioOnly: Boolean = false,
    onAudioOnlyToggle: () -> Unit = {},
    
    // 🔥 CDN 线路切换
    currentCdnIndex: Int = 0,
    cdnCount: Int = 1,
    onSwitchCdn: () -> Unit = {},
    onSwitchCdnTo: (Int) -> Unit = {},
    
    // 关闭面板
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            // iOS 风格拖拽指示器
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // 🔥 定时关闭 - 垂直布局，选项在下一行
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = CupertinoIcons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "定时关闭",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // 定时选项按钮组 - 支持横向滚动
                SleepTimerOptions(
                    currentMinutes = sleepTimerMinutes,
                    onSelect = onSleepTimerChange
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            // 🔥 重载视频
            SettingsItem(
                icon = CupertinoIcons.Default.ArrowClockwise,
                title = "重载视频",
                onClick = {
                    onReload()
                    onDismiss()
                }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            // 🔥 镜像翻转按钮组
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 左右翻转
                FlipButton(
                    icon = CupertinoIcons.Default.ArrowLeftArrowRight,
                    label = "左右翻转",
                    isActive = isFlippedHorizontal,
                    onClick = onFlipHorizontal,
                    modifier = Modifier.weight(1f)
                )
                
                // 上下翻转
                FlipButton(
                    icon = CupertinoIcons.Default.ArrowUpArrowDown,
                    label = "上下翻转",
                    isActive = isFlippedVertical,
                    onClick = onFlipVertical,
                    modifier = Modifier.weight(1f)
                )
                
                // 听视频（音频模式）
                FlipButton(
                    icon = CupertinoIcons.Default.Headphones,
                    label = "听视频",
                    isActive = isAudioOnly,
                    onClick = onAudioOnlyToggle,
                    modifier = Modifier.weight(1f)
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            // 🔥 选择画质 - 内联选择
            if (qualityLabels.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CupertinoIcons.Default.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "选择画质",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "当前 $currentQualityLabel",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // 画质选项
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        qualityLabels.forEachIndexed { index, label ->
                            val isSelected = label == currentQualityLabel
                            Surface(
                                onClick = { 
                                    if (!isSelected) {
                                        onQualitySelected(index)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
            
            // 🔥 播放线路 (CDN) - 仅在有多个线路时显示
            if (cdnCount > 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CupertinoIcons.Default.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "播放线路",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "当前 线路${currentCdnIndex + 1}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // CDN 线路选项
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(cdnCount) { index ->
                            val isSelected = index == currentCdnIndex
                            Surface(
                                onClick = { 
                                    if (!isSelected) {
                                        onSwitchCdnTo(index)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "线路${index + 1}",
                                        fontSize = 13.sp,
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
            
            // 🔥 播放倍速 - 内联选择
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = CupertinoIcons.Default.Speedometer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "播放倍速",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentSpeed == 1.0f) "正常" else "${currentSpeed}x",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // 倍速选项
                SpeedOptions(
                    currentSpeed = currentSpeed,
                    onSelect = onSpeedChange
                )
            }
        }
    }
}

/**
 * 设置项组件
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 标题和副标题
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 右侧内容
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = CupertinoIcons.Default.ChevronForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 定时关闭选项
 */
@Composable
private fun SleepTimerOptions(
    currentMinutes: Int?,
    onSelect: (Int?) -> Unit
) {
    val options = listOf(
        null to "关闭",
        15 to "15分钟",
        30 to "30分钟",
        60 to "1小时",
        90 to "1.5小时"
    )
    
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (minutes, label) ->
            val isSelected = currentMinutes == minutes
            Surface(
                onClick = { onSelect(minutes) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 播放倍速选项
 */
@Composable
private fun SpeedOptions(
    currentSpeed: Float,
    onSelect: (Float) -> Unit
) {
    val options = listOf(
        0.5f to "0.5x",
        0.75f to "0.75x",
        1.0f to "正常",
        1.25f to "1.25x",
        1.5f to "1.5x",
        2.0f to "2x"
    )
    
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (speed, label) ->
            val isSelected = currentSpeed == speed
            Surface(
                onClick = { onSelect(speed) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 翻转/模式切换按钮
 */
@Composable
private fun FlipButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = if (isActive) null else null,
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                color = if (isActive) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
