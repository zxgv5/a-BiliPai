// 文件路径: core/util/Logger.kt
package com.android.purebilibili.core.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.android.purebilibili.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 🔥 统一日志工具类
 * 
 * 在 Release 版本中自动禁用日志输出，减少性能开销
 * 同时收集日志到内存缓冲区，支持导出供用户反馈
 */
object Logger {
    
    private val isDebug = BuildConfig.DEBUG
    
    /**
     * Debug 日志 - 仅在 Debug 版本输出
     */
    fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
        LogCollector.add("D", tag, message)
    }
    
    /**
     * Info 日志 - 仅在 Debug 版本输出
     */
    fun i(tag: String, message: String) {
        if (isDebug) Log.i(tag, message)
        LogCollector.add("I", tag, message)
    }
    
    /**
     * Warning 日志 - 始终输出
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) {
            "$message\n${throwable.stackTraceToString()}"
        } else message
        
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
        LogCollector.add("W", tag, fullMessage)
    }
    
    /**
     * Error 日志 - 始终输出
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) {
            "$message\n${throwable.stackTraceToString()}"
        } else message
        
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
        LogCollector.add("E", tag, fullMessage)
    }
}

/**
 * 📋 日志收集器
 * 
 * 使用环形缓冲区保留最近 1000 条日志，支持导出分享
 */
object LogCollector {
    
    private const val MAX_ENTRIES = 1000
    private val buffer = ConcurrentLinkedDeque<LogEntry>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * 日志条目
     */
    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val tag: String,
        val message: String
    ) {
        fun format(): String {
            val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
            return "[$time] $level/$tag: $message"
        }
    }
    
    /**
     * 添加日志条目（带隐私过滤）
     */
    fun add(level: String, tag: String, message: String) {
        // 🔒 隐私过滤：脱敏敏感信息
        val sanitizedMessage = sanitizeMessage(message)
        
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = sanitizedMessage
        )
        
        buffer.addLast(entry)
        
        // 保持缓冲区大小
        while (buffer.size > MAX_ENTRIES) {
            buffer.pollFirst()
        }
    }
    
    /**
     * 🔒 隐私脱敏：移除敏感信息
     */
    private fun sanitizeMessage(message: String): String {
        var sanitized = message
        
        // 脱敏 Cookie 值
        sanitized = sanitized.replace(Regex("SESSDATA=[^;\\s]+"), "SESSDATA=***")
        sanitized = sanitized.replace(Regex("bili_jct=[^;\\s]+"), "bili_jct=***")
        sanitized = sanitized.replace(Regex("DedeUserID=[^;\\s]+"), "DedeUserID=***")
        sanitized = sanitized.replace(Regex("DedeUserID__ckMd5=[^;\\s]+"), "DedeUserID__ckMd5=***")
        sanitized = sanitized.replace(Regex("sid=[^;\\s]+"), "sid=***")
        sanitized = sanitized.replace(Regex("buvid3=[^;\\s]+"), "buvid3=***")
        
        // 脱敏 Token
        sanitized = sanitized.replace(Regex("access_token=[^&\\s]+"), "access_token=***")
        sanitized = sanitized.replace(Regex("refresh_token=[^&\\s]+"), "refresh_token=***")
        sanitized = sanitized.replace(Regex("\"token\":\"[^\"]+\""), "\"token\":\"***\"")
        
        // 脱敏手机号（11位数字）
        sanitized = sanitized.replace(Regex("\\b1[3-9]\\d{9}\\b"), "1**********")
        
        // 脱敏邮箱
        sanitized = sanitized.replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) { 
            val email = it.value
            val atIndex = email.indexOf('@')
            if (atIndex > 2) {
                email.substring(0, 2) + "***" + email.substring(atIndex)
            } else {
                "***" + email.substring(atIndex)
            }
        }
        
        return sanitized
    }
    
    /**
     * 获取所有日志条目
     */
    fun getEntries(): List<LogEntry> = buffer.toList()
    
    /**
     * 获取日志条目数量
     */
    fun getCount(): Int = buffer.size
    
    /**
     * 清空日志
     */
    fun clear() {
        buffer.clear()
    }
    
    /**
     * 导出日志到文件并通过系统分享
     */
    fun exportAndShare(context: Context) {
        try {
            val entries = getEntries()
            if (entries.isEmpty()) {
                Toast.makeText(context, "暂无日志记录", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 生成日志内容
            val header = buildString {
                appendLine("========================================")
                appendLine("BiliPai 应用日志导出")
                appendLine("========================================")
                appendLine("导出时间: ${dateFormat.format(Date())}")
                appendLine("应用版本: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                appendLine("设备信息: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                appendLine("Android版本: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                appendLine("日志条数: ${entries.size}")
                appendLine("========================================")
                appendLine()
            }
            
            val content = header + entries.joinToString("\n") { it.format() }
            
            // 写入缓存目录
            val fileName = "bilipai_log_${fileDateFormat.format(Date())}.txt"
            val cacheDir = File(context.cacheDir, "logs")
            cacheDir.mkdirs()
            val logFile = File(cacheDir, fileName)
            logFile.writeText(content)
            
            // 通过 FileProvider 分享
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                logFile
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "BiliPai 日志反馈")
                putExtra(Intent.EXTRA_TEXT, "请查看附件中的日志文件")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "分享日志"))
            
        } catch (e: Exception) {
            Log.e("LogCollector", "导出日志失败", e)
            Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
