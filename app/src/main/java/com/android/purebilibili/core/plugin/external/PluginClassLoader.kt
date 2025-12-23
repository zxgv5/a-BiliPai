// 文件路径: core/plugin/external/PluginClassLoader.kt
package com.android.purebilibili.core.plugin.external

import android.content.Context
import com.android.purebilibili.core.plugin.Plugin
import com.android.purebilibili.core.util.Logger
import dalvik.system.DexClassLoader

private const val TAG = "PluginClassLoader"

/**
 * 🔌 插件类加载器
 * 
 * 使用 DexClassLoader 动态加载外部插件的 DEX 文件
 */
object PluginClassLoader {
    
    // 缓存已加载的插件实例
    private val loadedPlugins = mutableMapOf<String, Plugin>()
    
    /**
     * 加载插件
     * 
     * @param context Application Context
     * @param manifest 插件清单
     * @return 插件实例，加载失败返回 null
     */
    fun loadPlugin(context: Context, manifest: BpxManifest): Plugin? {
        // 检查缓存
        loadedPlugins[manifest.id]?.let { return it }
        
        try {
            val dexPath = BpxLoader.getDexPath(context, manifest.id)
            if (dexPath == null) {
                Logger.e(TAG, "❌ DEX 文件不存在: ${manifest.id}")
                return null
            }
            
            Logger.d(TAG, "📦 加载 DEX: $dexPath")
            Logger.d(TAG, "🎯 目标类: ${manifest.pluginClass}")
            
            // 优化后的 DEX 输出目录
            val optimizedDir = context.getDir("dex_opt", Context.MODE_PRIVATE)
            
            // 创建 DexClassLoader
            val classLoader = DexClassLoader(
                dexPath,
                optimizedDir.absolutePath,
                null,  // 无原生库
                context.classLoader  // 父类加载器
            )
            
            // 加载插件主类
            val pluginClass = classLoader.loadClass(manifest.pluginClass)
            Logger.d(TAG, "✅ 类加载成功: ${pluginClass.name}")
            
            // 检查是否实现 Plugin 接口
            if (!Plugin::class.java.isAssignableFrom(pluginClass)) {
                Logger.e(TAG, "❌ ${manifest.pluginClass} 未实现 Plugin 接口")
                return null
            }
            
            // 实例化插件
            val plugin = pluginClass.getDeclaredConstructor().newInstance() as Plugin
            Logger.d(TAG, "✅ 插件实例化成功: ${plugin.name}")
            
            // 缓存
            loadedPlugins[manifest.id] = plugin
            return plugin
            
        } catch (e: ClassNotFoundException) {
            Logger.e(TAG, "❌ 找不到类: ${manifest.pluginClass}", e)
        } catch (e: NoSuchMethodException) {
            Logger.e(TAG, "❌ 缺少无参构造函数: ${manifest.pluginClass}", e)
        } catch (e: Exception) {
            Logger.e(TAG, "❌ 加载插件失败: ${manifest.id}", e)
        }
        
        return null
    }
    
    /**
     * 卸载插件
     */
    fun unloadPlugin(pluginId: String) {
        loadedPlugins.remove(pluginId)
        Logger.d(TAG, "🗑️ 插件已卸载: $pluginId")
    }
    
    /**
     * 获取已加载的插件
     */
    fun getLoadedPlugin(pluginId: String): Plugin? = loadedPlugins[pluginId]
    
    /**
     * 清空所有缓存
     */
    fun clearCache() {
        loadedPlugins.clear()
        Logger.d(TAG, "🗑️ 已清空所有插件缓存")
    }
}
