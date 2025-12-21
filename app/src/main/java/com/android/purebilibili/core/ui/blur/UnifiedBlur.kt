// 文件路径: core/ui/blur/UnifiedBlur.kt
package com.android.purebilibili.core.ui.blur

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

/**
 * 🎨 统一的模糊Modifier
 * 
 * 自动根据Android版本选择最优的模糊样式
 * 
 * @param hazeState Haze状态
 * @param enabled 是否启用模糊
 * @return 应用了版本自适应模糊的Modifier
 */
@Composable
fun Modifier.unifiedBlur(
    hazeState: HazeState,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    
    val blurStyle = BlurStyles.rememberOptimalBlurStyle()
    
    this.hazeChild(
        state = hazeState,
        style = blurStyle
    )
}
