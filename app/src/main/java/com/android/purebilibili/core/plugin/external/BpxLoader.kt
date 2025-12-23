// 文件路径: core/plugin/external/BpxLoader.kt
package com.android.purebilibili.core.plugin.external

import android.content.Context
import com.android.purebilibili.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

private const val TAG = "BpxLoader"

/**
 * 🔧 BPX 加载器
 * 
 * 负责下载、解压、验证 .bpx 插件包
 */
object BpxLoader {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * 从 URL 下载并安装 BPX 插件
     */
    suspend fun installFromUrl(context: Context, url: String): Result<BpxManifest> {
        return withContext(Dispatchers.IO) {
            try {
                Logger.d(TAG, "📥 开始下载插件: $url")
                
                // 1. 下载 .bpx 文件
                val bpxBytes = URL(url).readBytes()
                Logger.d(TAG, "✅ 下载完成: ${bpxBytes.size} bytes")
                
                // 2. 解析并安装
                installFromBytes(context, bpxBytes)
            } catch (e: Exception) {
                Logger.e(TAG, "❌ 下载插件失败", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * 从本地文件安装 BPX 插件
     */
    suspend fun installFromFile(context: Context, file: File): Result<BpxManifest> {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = file.readBytes()
                installFromBytes(context, bytes)
            } catch (e: Exception) {
                Logger.e(TAG, "❌ 读取文件失败", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * 从字节数组安装插件
     */
    private fun installFromBytes(context: Context, bpxBytes: ByteArray): Result<BpxManifest> {
        try {
            // 创建临时解压目录
            val tempDir = File(context.cacheDir, "bpx_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            // 解压 .bpx (ZIP 格式)
            ZipInputStream(bpxBytes.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val file = File(tempDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            
            // 读取 manifest.json
            val manifestFile = File(tempDir, "manifest.json")
            if (!manifestFile.exists()) {
                tempDir.deleteRecursively()
                return Result.failure(Exception("缺少 manifest.json"))
            }
            
            val manifest = json.decodeFromString<BpxManifest>(manifestFile.readText())
            Logger.d(TAG, "📋 解析 manifest: ${manifest.name} v${manifest.version}")
            
            // 检查 classes.dex
            val dexFile = File(tempDir, "classes.dex")
            if (!dexFile.exists()) {
                tempDir.deleteRecursively()
                return Result.failure(Exception("缺少 classes.dex"))
            }
            
            // 移动到插件目录
            val pluginsDir = getPluginsDir(context)
            val pluginDir = File(pluginsDir, manifest.id)
            if (pluginDir.exists()) {
                pluginDir.deleteRecursively()
            }
            tempDir.renameTo(pluginDir)
            
            Logger.d(TAG, "✅ 插件安装完成: ${manifest.name}")
            return Result.success(manifest)
            
        } catch (e: Exception) {
            Logger.e(TAG, "❌ 安装插件失败", e)
            return Result.failure(e)
        }
    }
    
    /**
     * 获取插件目录
     */
    fun getPluginsDir(context: Context): File {
        val dir = File(context.filesDir, "external_plugins")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
    
    /**
     * 获取已安装的插件列表
     */
    fun getInstalledPlugins(context: Context): List<BpxManifest> {
        val pluginsDir = getPluginsDir(context)
        if (!pluginsDir.exists()) return emptyList()
        
        return pluginsDir.listFiles()?.mapNotNull { pluginDir ->
            try {
                val manifestFile = File(pluginDir, "manifest.json")
                if (manifestFile.exists()) {
                    json.decodeFromString<BpxManifest>(manifestFile.readText())
                } else null
            } catch (e: Exception) {
                Logger.w(TAG, "⚠️ 无法读取 manifest: ${pluginDir.name}")
                null
            }
        } ?: emptyList()
    }
    
    /**
     * 卸载插件
     */
    fun uninstall(context: Context, pluginId: String): Boolean {
        val pluginDir = File(getPluginsDir(context), pluginId)
        return if (pluginDir.exists()) {
            pluginDir.deleteRecursively()
        } else false
    }
    
    /**
     * 获取插件的 DEX 路径
     */
    fun getDexPath(context: Context, pluginId: String): String? {
        val dexFile = File(getPluginsDir(context), "$pluginId/classes.dex")
        return if (dexFile.exists()) dexFile.absolutePath else null
    }
}
