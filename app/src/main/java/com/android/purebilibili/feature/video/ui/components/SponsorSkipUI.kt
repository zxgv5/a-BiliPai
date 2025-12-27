// 文件路径: feature/video/SponsorSkipUI.kt
package com.android.purebilibili.feature.video.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.data.model.response.SponsorSegment

/**
 * 空降助手跳过按钮 UI
 * 显示在视频右下角，允许用户跳过广告片段
 */
@Composable
fun SponsorSkipButton(
    segment: SponsorSegment?,
    visible: Boolean,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && segment != null,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(200)),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(200)),
        modifier = modifier
    ) {
        segment?.let { seg ->
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color.Black.copy(alpha = 0.8f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 类别标签
                    Text(
                        text = seg.categoryName,
                        color = Color(0xFFFFA500),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // 跳过按钮
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSkip() },
                        color = Color(0xFF00C853)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = CupertinoIcons.Default.ChevronForward,
                                contentDescription = "跳过",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "跳过",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // 关闭按钮
                    Icon(
                        imageVector = CupertinoIcons.Default.Xmark,
                        contentDescription = "忽略",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDismiss() }
                    )
                }
            }
        }
    }
}

/**
 * 自动跳过提示 Toast
 * 屏幕顶部短暂显示
 */
@Composable
fun SponsorSkipToast(
    message: String?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        message?.let {
            Surface(
                modifier = Modifier
                    .padding(top = 60.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color(0xFF00C853).copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = CupertinoIcons.Default.ChevronForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = it,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
