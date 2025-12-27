// File: feature/video/ui/overlay/BottomControlBar.kt
package com.android.purebilibili.feature.video.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
// 🍎 Cupertino Icons - iOS SF Symbols 风格图标
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.*
import io.github.alexzhirkevich.cupertino.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.feature.video.ui.components.VideoAspectRatio

/**
 * Bottom Control Bar Component
 * 
 * Displays the bottom control bar with:
 * - Play/pause button
 * - Progress bar
 * - Time display
 * - Speed selector
 * - Fullscreen toggle
 * 
 * Requirement Reference: AC2.3 - Reusable BottomControlBar
 */

/**
 * Player progress data class
 */
data class PlayerProgress(
    val current: Long = 0L,
    val duration: Long = 0L,
    val buffered: Long = 0L
)

@Composable
fun BottomControlBar(
    isPlaying: Boolean,
    progress: PlayerProgress,
    isFullscreen: Boolean,
    currentSpeed: Float = 1.0f,
    currentRatio: VideoAspectRatio = VideoAspectRatio.FIT,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedClick: () -> Unit = {},
    onRatioClick: () -> Unit = {},
    onToggleFullscreen: () -> Unit,
    // 🔥🔥 [新增] 竖屏模式弹幕开关
    danmakuEnabled: Boolean = true,
    onDanmakuToggle: () -> Unit = {},
    // 🔥🔥 [新增] 竖屏模式清晰度选择
    currentQualityLabel: String = "",
    onQualityClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)  // 🔥 减小水平 padding
            .padding(bottom = 4.dp)
            // 🔥🔥 只在全屏横屏时才需要避开导航栏
            // 竖屏时导航栏在页面底部，不在播放器区域内
            .let { if (isFullscreen) it.navigationBarsPadding() else it }
    ) {
        VideoProgressBar(
            currentPosition = progress.current,
            duration = progress.duration,
            bufferedPosition = progress.buffered,
            onSeek = onSeek
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            // 🔥 使用 SpaceBetween 确保两端元素始终可见
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧：播放按钮和时间
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(36.dp)  // 🔥 缩小按钮
                ) {
                    Icon(
                        if (isPlaying) CupertinoIcons.Default.Pause else CupertinoIcons.Default.Play,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)  // 🔥 缩小图标
                    )
                }

                Text(
                    text = "${FormatUtils.formatDuration((progress.current / 1000).toInt())} / ${FormatUtils.formatDuration((progress.duration / 1000).toInt())}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,  // 🔥 缩小字体
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            
            // 中间：功能按钮（自适应空间）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1f)
            ) {
                // Speed button
                Surface(
                    onClick = onSpeedClick,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (currentSpeed == 1.0f) "倍速" else "${currentSpeed}x",
                        color = if (currentSpeed != 1.0f) MaterialTheme.colorScheme.primary else Color.White,
                        fontSize = 10.sp,  // 🔥 缩小字体
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)  // 🔥 缩小 padding
                    )
                }
                
                Spacer(modifier = Modifier.width(3.dp))  // 🔥 缩小间距
                
                // 🔥 Aspect Ratio button
                Surface(
                    onClick = onRatioClick,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = currentRatio.displayName,
                        color = if (currentRatio != VideoAspectRatio.FIT) MaterialTheme.colorScheme.primary else Color.White,
                        fontSize = 10.sp,  // 🔥 缩小字体
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)  // 🔥 缩小 padding
                    )
                }
                
                // 🔥🔥 [新增] 竖屏模式弹幕开关和清晰度
                if (!isFullscreen) {
                    Spacer(modifier = Modifier.width(2.dp))  // 🔥 缩小间距
                    
                    IconButton(
                        onClick = onDanmakuToggle,
                        modifier = Modifier.size(26.dp)  // 🔥 缩小按钮
                    ) {
                        Icon(
                            if (danmakuEnabled) CupertinoIcons.Default.TextBubble else CupertinoIcons.Outlined.TextBubble,
                            contentDescription = if (danmakuEnabled) "关闭弹幕" else "开启弹幕",
                            tint = if (danmakuEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)  // 🔥 缩小图标
                        )
                    }
                    
                    // 🔥🔥 清晰度选择 - 优化布局确保完整显示
                    if (currentQualityLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Surface(
                            onClick = onQualityClick,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentQualityLabel,
                                color = Color.White,
                                fontSize = 10.sp,  // 🔥 统一字体大小
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                maxLines = 1,  // 🔥 确保单行显示
                                softWrap = false  // 🔥 禁止换行
                            )
                        }
                    }
                }
            }
            
            // 🔥 右侧：全屏按钮 - 始终显示，不会被挤出
            IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier.size(36.dp)  // 🔥 缩小按钮
            ) {
                Icon(
                    if (isFullscreen) CupertinoIcons.Default.ArrowDownRightAndArrowUpLeft else CupertinoIcons.Default.ArrowUpLeftAndArrowDownRight,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)  // 🔥 缩小图标
                )
            }
        }
    }
}

/**
 * Video Progress Bar - 自定义细进度条
 */
@Composable
fun VideoProgressBar(
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
    val bufferedProgress = if (duration > 0) bufferedPosition.toFloat() / duration else 0f
    var tempProgress by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(progress) {
        if (!isDragging) {
            tempProgress = progress
        }
    }
    
    val displayProgress = if (isDragging) tempProgress else progress
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek((newProgress * duration).toLong())
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        tempProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        tempProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek((tempProgress * duration).toLong())
                    },
                    onDragCancel = {
                        isDragging = false
                        tempProgress = progress
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // 背景轨道
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.5.dp))
        )
        
        // 缓冲进度
        Box(
            modifier = Modifier
                .fillMaxWidth(bufferedProgress.coerceIn(0f, 1f))
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(1.5.dp))
        )
        
        // 当前进度
        Box(
            modifier = Modifier
                .fillMaxWidth(displayProgress.coerceIn(0f, 1f))
                .height(3.dp)
                .background(primaryColor, RoundedCornerShape(1.5.dp))
        )
        
        // 滑块（圆点）
        Box(
            modifier = Modifier
                .fillMaxWidth(displayProgress.coerceIn(0f, 1f))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(12.dp)
                    .offset(x = 6.dp)
                    .background(primaryColor, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
