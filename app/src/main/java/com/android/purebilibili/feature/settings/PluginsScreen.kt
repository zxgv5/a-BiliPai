// 文件路径: feature/settings/PluginsScreen.kt
package com.android.purebilibili.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.plugin.PluginInfo
import com.android.purebilibili.core.plugin.PluginManager
import com.android.purebilibili.core.theme.BiliPink
import com.android.purebilibili.core.theme.iOSBlue
import com.android.purebilibili.core.theme.iOSGreen
import com.android.purebilibili.core.theme.iOSOrange
import com.android.purebilibili.core.theme.iOSPurple
import com.android.purebilibili.core.theme.iOSTeal
import io.github.alexzhirkevich.cupertino.CupertinoSwitch
import kotlinx.coroutines.launch

/**
 * 🔌 插件中心页面
 * 
 * 显示所有可用插件，支持启用/禁用和配置。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginsScreen(
    onBack: () -> Unit
) {
    val plugins by PluginManager.pluginsFlow.collectAsState()
    val jsonPlugins by com.android.purebilibili.core.plugin.json.JsonPluginManager.plugins.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 展开状态追踪
    var expandedPluginId by remember { mutableStateOf<String?>(null) }
    
    // 🆕 导入插件对话框状态
    var showImportDialog by remember { mutableStateOf(false) }
    var importUrl by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }
    
    // 🔥🔥 [修复] 编辑插件状态移至顶层，避免在 LazyColumn 内嵌套 LazyColumn 导致闪退
    var editingPlugin by remember { mutableStateOf<com.android.purebilibili.core.plugin.json.JsonRulePlugin?>(null) }
    
    // 🆕 如果正在编辑插件，显示编辑器全屏覆盖
    editingPlugin?.let { plugin ->
        JsonPluginEditorScreen(
            plugin = plugin,
            onBack = { editingPlugin = null },
            onSave = { updated ->
                com.android.purebilibili.core.plugin.json.JsonPluginManager.updatePlugin(updated)
            }
        )
        return
    }
    
    // 🔥🔥 [修复] 设置导航栏透明，确保底部手势栏沉浸式效果
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val originalNavBarColor = window?.navigationBarColor ?: android.graphics.Color.TRANSPARENT
        
        if (window != null) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
        
        onDispose {
            if (window != null) {
                window.navigationBarColor = originalNavBarColor
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("插件中心", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        // 🔥🔥 [修复] 禁用 Scaffold 默认的 WindowInsets 消耗，避免底部填充
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 标题说明
            item {
                Text(
                    text = "已安装插件".uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
                )
            }
            
            // 插件列表
            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column {
                        plugins.forEachIndexed { index, pluginInfo ->
                            PluginItem(
                                pluginInfo = pluginInfo,
                                isExpanded = expandedPluginId == pluginInfo.plugin.id,
                                iconTint = getPluginColor(index),
                                onToggle = { enabled ->
                                    scope.launch {
                                        PluginManager.setEnabled(pluginInfo.plugin.id, enabled)
                                    }
                                },
                                onExpandToggle = {
                                    expandedPluginId = if (expandedPluginId == pluginInfo.plugin.id) {
                                        null
                                    } else {
                                        pluginInfo.plugin.id
                                    }
                                }
                            )
                            if (index < plugins.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .padding(start = 66.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        }
                    }
                }
            }
            
            // 统计信息
            item {
                val enabledCount = plugins.count { it.enabled }
                Text(
                    text = "${plugins.size} 个插件，$enabledCount 个已启用",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp, top = 16.dp)
                )
            }
            
            // 🆕 导入外部插件按钮
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "外部插件",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
                )
            }
            
            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showImportDialog = true },
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iOSBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudDownload,
                                contentDescription = null,
                                tint = iOSBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "导入外部插件",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "通过 URL 安装 JSON 规则插件",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = iOSBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // 🆕 已安装的 JSON 插件列表
            if (jsonPlugins.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    val filterStats by com.android.purebilibili.core.plugin.json.JsonPluginManager.filterStats.collectAsState()
                    
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Column {
                            jsonPlugins.forEachIndexed { index, loadedPlugin ->
                                JsonPluginItem(
                                    loaded = loadedPlugin,
                                    filterCount = filterStats[loadedPlugin.plugin.id] ?: 0,
                                    onToggle = { enabled ->
                                        com.android.purebilibili.core.plugin.json.JsonPluginManager.setEnabled(
                                            loadedPlugin.plugin.id, enabled
                                        )
                                    },
                                    onEdit = {
                                        // 🔥 使用顶层的 editingPlugin 状态
                                        editingPlugin = loadedPlugin.plugin
                                    },
                                    onDelete = {
                                        com.android.purebilibili.core.plugin.json.JsonPluginManager.removePlugin(
                                            loadedPlugin.plugin.id
                                        )
                                    }
                                )
                                if (index < jsonPlugins.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 52.dp)
                                            .height(0.5.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 底部说明
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "插件可以扩展应用功能，如自动跳过广告、过滤推荐内容等。\n启用插件后可点击展开查看详细设置。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
    
    // 🆕 导入插件对话框
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { 
                showImportDialog = false
                importUrl = ""
                importError = null
            },
            icon = { Icon(Icons.Outlined.CloudDownload, contentDescription = null) },
            title = { Text("导入外部插件") },
            text = {
                Column {
                    Text(
                        text = "输入 JSON 规则插件的下载链接",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = importUrl,
                        onValueChange = { 
                            importUrl = it
                            importError = null
                        },
                        label = { Text("插件 URL") },
                        placeholder = { Text("https://xxx.json") },
                        singleLine = true,
                        isError = importError != null,
                        supportingText = importError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (isImporting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在安装...")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importUrl.isBlank()) {
                            importError = "请输入 URL"
                            return@TextButton
                        }
                        
                        if (!importUrl.endsWith(".json")) {
                            importError = "链接必须以 .json 结尾"
                            return@TextButton
                        }
                        
                        isImporting = true
                        scope.launch {
                            val result = com.android.purebilibili.core.plugin.json.JsonPluginManager.importFromUrl(importUrl)
                            isImporting = false
                            
                            if (result.isSuccess) {
                                showImportDialog = false
                                importUrl = ""
                                importError = null
                                // 🆕 显示成功 Toast
                                android.widget.Toast.makeText(
                                    context,
                                    "插件 \"${result.getOrNull()?.name}\" 安装成功！",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                importError = result.exceptionOrNull()?.message ?: "安装失败"
                            }
                        }
                    },
                    enabled = !isImporting
                ) {
                    Text("安装")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showImportDialog = false
                        importUrl = ""
                        importError = null
                    },
                    enabled = !isImporting
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun PluginItem(
    pluginInfo: PluginInfo,
    isExpanded: Boolean,
    iconTint: Color,
    onToggle: (Boolean) -> Unit,
    onExpandToggle: () -> Unit
) {
    val plugin = pluginInfo.plugin
    
    Column {
        // 主行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = plugin.icon ?: Icons.Outlined.Extension,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // 标题和描述
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "v${plugin.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = plugin.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 🆕 显示作者
                if (plugin.author != "Unknown") {
                    Text(
                        text = "by ${plugin.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 开关
            CupertinoSwitch(
                checked = pluginInfo.enabled,
                onCheckedChange = onToggle
            )
            
            // 展开箭头
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(20.dp)
            )
        }
        
        // 展开的配置区域
        AnimatedVisibility(
            visible = isExpanded && pluginInfo.enabled,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 66.dp, end = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                plugin.SettingsContent()
            }
        }
    }
}

/**
 * 获取插件对应的颜色
 */
private fun getPluginColor(index: Int): Color {
    val colors = listOf(iOSTeal, iOSOrange, iOSBlue, iOSGreen, iOSPurple, BiliPink)
    return colors[index % colors.size]
}

/**
 * JSON 规则插件列表项
 */
@Composable
private fun JsonPluginItem(
    loaded: com.android.purebilibili.core.plugin.json.LoadedJsonPlugin,
    filterCount: Int,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val plugin = loaded.plugin
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iOSPurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Code,
                contentDescription = null,
                tint = iOSPurple,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        // 信息
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plugin.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "v${plugin.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Text(
                text = plugin.description.ifEmpty { plugin.type },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "by ${plugin.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = iOSPurple
                )
                // 🆕 过滤统计显示
                if (filterCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• 已过滤 $filterCount 项",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // 开关
        CupertinoSwitch(
            checked = loaded.enabled,
            onCheckedChange = onToggle
        )
        
        // 删除按钮
        IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除插件") },
            text = { Text("确定要删除插件 \"${plugin.name}\" 吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

