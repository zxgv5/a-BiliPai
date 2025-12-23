// 文件路径: core/plugin/external/ExternalPluginManager.kt
package com.android.purebilibili.core.plugin.external

import android.content.Context
import com.android.purebilibili.core.plugin.Plugin
import com.android.purebilibili.core.plugin.PluginInfo
import com.android.purebilibili.core.plugin.PluginManager
import com.android.purebilibili.core.plugin.PluginStore
import com.android.purebilibili.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ExternalPluginManager"

/**
 * 🔌 外部插件管理器
 * 
 * 负责管理通过 URL 安装的外部 .bpx 插件
 */
object ExternalPluginManager {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var appContext: Context
    
    /** 外部插件列表状态 */
    private val _externalPlugins = MutableStateFlow<List<ExternalPluginInfo>>(emptyList())
    val externalPlugins: StateFlow<List<ExternalPluginInfo>> = _externalPlugins.asStateFlow()
    
    private var isInitialized = false
    
    /**
     * 初始化外部插件管理器
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        isInitialized = true
        
        // 加载已安装的外部插件
        scope.launch {
            loadInstalledPlugins()
        }
        
        Logger.d(TAG, "🔌 ExternalPluginManager initialized")
    }
    
    /**
     * 加载已安装的外部插件
     */
    private suspend fun loadInstalledPlugins() {
        val manifests = BpxLoader.getInstalledPlugins(appContext)
        Logger.d(TAG, "📦 发现 ${manifests.size} 个已安装外部插件")
        
        val pluginInfos = manifests.map { manifest ->
            val dexPath = BpxLoader.getDexPath(appContext, manifest.id) ?: ""
            val enabled = PluginStore.isEnabled(appContext, "ext_${manifest.id}")
            
            // 尝试加载插件
            var loadError: String? = null
            if (enabled) {
                val plugin = PluginClassLoader.loadPlugin(appContext, manifest)
                if (plugin == null) {
                    loadError = "加载失败"
                } else {
                    // 注册到主插件管理器
                    registerToMainManager(plugin, manifest)
                }
            }
            
            ExternalPluginInfo(
                manifest = manifest,
                enabled = enabled,
                installed = true,
                dexPath = dexPath,
                loadError = loadError
            )
        }
        
        _externalPlugins.value = pluginInfos
    }
    
    /**
     * 从 URL 安装插件
     */
    suspend fun installFromUrl(url: String): Result<BpxManifest> {
        val result = BpxLoader.installFromUrl(appContext, url)
        
        if (result.isSuccess) {
            // 刷新列表
            loadInstalledPlugins()
        }
        
        return result
    }
    
    /**
     * 卸载插件
     */
    suspend fun uninstall(pluginId: String) {
        // 从主管理器移除
        PluginClassLoader.unloadPlugin(pluginId)
        
        // 删除文件
        BpxLoader.uninstall(appContext, pluginId)
        
        // 清除启用状态
        PluginStore.setEnabled(appContext, "ext_$pluginId", false)
        
        // 刷新列表
        loadInstalledPlugins()
        
        Logger.d(TAG, "🗑️ 已卸载插件: $pluginId")
    }
    
    /**
     * 启用/禁用外部插件
     */
    suspend fun setEnabled(pluginId: String, enabled: Boolean) {
        PluginStore.setEnabled(appContext, "ext_$pluginId", enabled)
        
        if (enabled) {
            // 加载插件
            val manifest = _externalPlugins.value.find { it.manifest.id == pluginId }?.manifest
            if (manifest != null) {
                val plugin = PluginClassLoader.loadPlugin(appContext, manifest)
                if (plugin != null) {
                    registerToMainManager(plugin, manifest)
                    plugin.onEnable()
                }
            }
        } else {
            // 卸载插件
            val plugin = PluginClassLoader.getLoadedPlugin(pluginId)
            plugin?.onDisable()
            PluginClassLoader.unloadPlugin(pluginId)
        }
        
        // 刷新列表
        loadInstalledPlugins()
    }
    
    /**
     * 将外部插件注册到主插件管理器
     */
    private fun registerToMainManager(plugin: Plugin, manifest: BpxManifest) {
        // 创建带标记的包装插件，表示这是外部插件
        // 这里简单处理，直接注册
        // 注意：避免重复注册
        if (PluginManager.plugins.none { it.plugin.id == manifest.id }) {
            PluginManager.register(plugin)
            Logger.d(TAG, "✅ 外部插件已注册到主管理器: ${manifest.name}")
        }
    }
    
    /**
     * 检查是否已安装
     */
    fun isInstalled(pluginId: String): Boolean {
        return _externalPlugins.value.any { it.manifest.id == pluginId }
    }
}
