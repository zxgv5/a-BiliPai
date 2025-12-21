// 文件路径: core/ui/blur/BlurStyles.kt
package com.android.purebilibili.core.ui.blur

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.materials.HazeMaterials

/**
 * 🎨 模糊样式管理
 * 
 * 根据Android版本提供最优的模糊配置：
 * - Android 16+: expressiveBlur (配合Material 3 Expressive)
 * - Android 12-15: standardBlur (标准Material模糊)
 * - Android 11: experimentalBlur (轻量，减少性能开销)
 */
object BlurStyles {
    
    /**
     * Android 16 Material 3 Expressive风格
     * 更轻盈的模糊，配合系统设计语言
     */
    @RequiresApi(35)
    fun expressiveBlur(): HazeStyle {
        return HazeStyle(
            blurRadius = 15.dp,  // 比thin(20dp)更轻
            tint = Color.Transparent,
            noiseFactor = 0.05f  // 轻微噪点，更自然
        )
    }
    
    /**
     * Android 12-15 标准风格
     * 使用HazeMaterials预设
     */
    fun standardBlur(): HazeStyle {
        return HazeMaterials.thin()  // 20dp
    }
    
    /**
     * Android 11 实验性（轻量）
     * 减少模糊强度以降低性能开销
     */
    fun experimentalBlur(): HazeStyle {
        return HazeMaterials.ultraThin()  // 12dp，更轻
    }
    
    /**
     * 自动选择最优风格
     * 
     * @return 适合当前Android版本的最优HazeStyle
     */
    @Composable
    fun rememberOptimalBlurStyle(): HazeStyle {
        return remember {
            getOptimalBlurStyle()
        }
    }
    
    /**
     * 获取最优模糊风格（非Compose版本）
     */
    fun getOptimalBlurStyle(): HazeStyle {
        return when {
            Build.VERSION.SDK_INT >= 35 -> expressiveBlur()
            Build.VERSION.SDK_INT >= 31 -> standardBlur()
            Build.VERSION.SDK_INT >= 30 -> experimentalBlur()
            else -> standardBlur()  // fallback
        }
    }
}
