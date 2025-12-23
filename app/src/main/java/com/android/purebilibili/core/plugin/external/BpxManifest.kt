// 文件路径: core/plugin/external/BpxManifest.kt
package com.android.purebilibili.core.plugin.external

import kotlinx.serialization.Serializable

/**
 * 🎯 BPX 插件清单
 * 
 * 从 .bpx 包的 manifest.json 解析
 */
@Serializable
data class BpxManifest(
    /** 插件唯一 ID */
    val id: String,
    
    /** 显示名称 */
    val name: String,
    
    /** 功能描述 */
    val description: String,
    
    /** 版本号 */
    val version: String,
    
    /** 作者 */
    val author: String,
    
    /** 插件主类的完整类名 (如 "com.example.MyPlugin") */
    val pluginClass: String,
    
    /** 插件类型: "feed", "player", "danmaku", "general" */
    val type: String = "general",
    
    /** 最低支持的应用版本 */
    val minAppVersion: String = "3.0.0",
    
    /** 图标 URL (可选) */
    val iconUrl: String? = null
)

/**
 * 外部插件信息包装类
 */
data class ExternalPluginInfo(
    val manifest: BpxManifest,
    val enabled: Boolean,
    val installed: Boolean,
    val dexPath: String,        // DEX 文件路径
    val loadError: String? = null // 加载错误信息
)
