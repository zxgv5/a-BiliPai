// 文件路径: feature/dynamic/components/DynamicTopBar.kt
package com.android.purebilibili.feature.dynamic.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 🍎 Cupertino Icons - iOS SF Symbols 风格图标
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.*
import io.github.alexzhirkevich.cupertino.icons.filled.*

// 🔥 动态页面布局模式
enum class DynamicDisplayMode {
    SIDEBAR,     // 侧边栏模式（默认，UP主列表在左侧）
    HORIZONTAL   // 横向模式（UP主列表在顶部，类似 Telegram）
}

/**
 * 🔥 带Tab的顶栏
 */
@Composable
fun DynamicTopBarWithTabs(
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    displayMode: DynamicDisplayMode = DynamicDisplayMode.SIDEBAR,
    onDisplayModeChange: (DynamicDisplayMode) -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density).let { with(density) { it.toDp() } }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column {
            Spacer(modifier = Modifier.height(statusBarHeight))
            
            // 🔥 标题行：返回按钮 + 标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔥 返回视频首页按钮
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = CupertinoIcons.Default.ChevronBackward,
                        contentDescription = "返回首页",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // 标题
                Text(
                    "动态",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 🔥 布局模式切换按钮
                IconButton(
                    onClick = {
                        val newMode = if (displayMode == DynamicDisplayMode.SIDEBAR) 
                            DynamicDisplayMode.HORIZONTAL else DynamicDisplayMode.SIDEBAR
                        onDisplayModeChange(newMode)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (displayMode == DynamicDisplayMode.SIDEBAR)
                            CupertinoIcons.Default.ListBullet else CupertinoIcons.Default.RectangleStack,
                        contentDescription = "切换布局模式",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            // Tab栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onTabSelected(index) }
                            .padding(end = 24.dp)
                    ) {
                        Text(
                            tab,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
