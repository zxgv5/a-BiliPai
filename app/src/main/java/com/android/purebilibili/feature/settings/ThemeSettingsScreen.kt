// 文件路径: feature/settings/ThemeSettingsScreen.kt
package com.android.purebilibili.feature.settings

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.core.theme.*
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.*

/**
 *  主题设置二级页面
 * 专门管理主题相关设置：深色模式、动态取色、主题色
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    val themeBaseLevel = when (state.themeMode) {
        AppThemeMode.LIGHT -> 0.35f
        AppThemeMode.DARK -> 0.75f
        AppThemeMode.FOLLOW_SYSTEM -> 0.55f
    }
    val themeInteractionLevel = (themeBaseLevel + if (state.dynamicColor) 0.2f else 0f).coerceIn(0f, 1f)
    val themeAnimationSpeed = if (state.dynamicColor) 1.15f else 0.95f
    

    //  Dialog moved to Content


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题设置", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(CupertinoIcons.Default.ChevronBackward, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        ThemeSettingsContent(
            modifier = Modifier.padding(padding),
            state = state,
            showThemeDialog = showThemeDialog,
            onShowThemeDialogChange = { showThemeDialog = it },
            viewModel = viewModel,
            context = context
        )
    }
}

@Composable
fun ThemeSettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    showThemeDialog: Boolean,
    onShowThemeDialogChange: (Boolean) -> Unit,
    viewModel: SettingsViewModel,
    context: android.content.Context
) {
    //  Need to pass showThemeDialog state up or handle it inside Content?
    //  Actually, the Dialog is part of the Screen/Content interaction.
    //  Let's keep the Dialog inside Content or pass the state. 
    //  Ideally, Content should be self-contained but State Hoisting is better.
    //  Wait, the original code had the Dialog at the top level of Screen.
    //  If I move Content out, the Dialog should probably still be invoked from within Content or triggered by it.
    //  The simplest way for Tablet support is if the Content *includes* the logic to show the dialog, 
    //  OR if the Dialog is separate.
    //  In the original code, `showThemeDialog` controls `AlertDialog`.
    //  I'll pass the state down for now.
    
    //  Re-declaring the mutable state inside Content might Duplicate it if I use it in Screen too?
    //  No, I should hoist it.
    
    //  Actually, let's look at the original `ThemeSettingsScreen`.
    //  It defines `showThemeDialog`.
    //  And the `AlertDialog` is inside `ThemeSettingsScreen`.
    //  So if I move the `LazyColumn` to `Content`, the `AlertDialog` remains in `Screen`.
    //  BUT, `Content` triggers the dialog via `clickable { showThemeDialog = true }`.
    //  So I need to pass `onShowDialog` to `Content`.
    
    //  Wait, if I use `ThemeSettingsContent` in Tablet layout, where does the Dialog live?
    //  It should probably live inside `ThemeSettingsContent` or be handled by the parent.
    //  If `ThemeSettingsContent` is just the list, then the Dialog should be outside?
    //  No, for Tablet, the Dialog should show over the whole screen or the split pane? Usually whole screen or Center.
    //  So it's better if `ThemeSettingsContent` *contains* the Dialog?
    //  Or `Screen` contains `Dialog` + `Content`. 
    //  In Tablet, `TabletSettingsLayout` will use `Content`. Does it need to re-implement the Dialog?
    //  Yes, unless `Content` includes the Dialog.
    //  Let's include the Dialog in `Content`? 
    //  If I include Dialog in Content, then `Screen` just calls `Content`.
    //  That seems cleaner for code reuse.
    
    var localShowThemeDialog by remember { mutableStateOf(false) }
    // If we want to control it from outside, we can pass it in. 
    // But for now, letting Content manage its own "secondary" dialogs is fine.
    
    if (localShowThemeDialog) {
        AlertDialog(
            onDismissRequest = { localShowThemeDialog = false },
            title = { Text("外观模式", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    AppThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    localShowThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (state.themeMode == mode),
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    localShowThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = mode.label, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = { 
                TextButton(onClick = { localShowThemeDialog = false }) { 
                    Text("取消", color = MaterialTheme.colorScheme.primary) 
                } 
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        
        //  外观模式
        item { SettingsSectionTitle("外观模式") }
        item {
            SettingsGroup {
                // 深色模式选择
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { localShowThemeDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                CupertinoIcons.Default.MoonStars,
                                contentDescription = null,
                                tint = iOSBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "深色模式",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = state.themeMode.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            CupertinoIcons.Default.ChevronForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            //  动态取色
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item { SettingsSectionTitle("动态取色") }
                item {
                    SettingsGroup {
                        Column {
                            SettingSwitchItem(
                                icon = CupertinoIcons.Default.PaintbrushPointed,
                                title = "Material You",
                                subtitle = "跟随系统壁纸变换应用主题色",
                                checked = state.dynamicColor,
                                onCheckedChange = { viewModel.toggleDynamicColor(it) },
                                iconTint = iOSPink
                            )
                            
                            // 动态取色预览
                            AnimatedVisibility(
                                visible = state.dynamicColor,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                DynamicColorPreview()
                            }
                        }
                    }
                }
            }
            
            //  主题色
            item { SettingsSectionTitle("主题色") }
            item {
                SettingsGroup {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(iOSPurple.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    CupertinoIcons.Default.Eyedropper,
                                    contentDescription = null,
                                    tint = iOSPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "选择主题色",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.dynamicColor) "已启用动态取色" 
                                           else ThemeColorNames.getOrElse(state.themeColorIndex) { "经典蓝" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // 颜色选择器网格
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ThemeColors.size) { index ->
                                val color = ThemeColors[index]
                                val name = ThemeColorNames.getOrElse(index) { "" }
                                val isSelected = state.themeColorIndex == index && !state.dynamicColor
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable(enabled = !state.dynamicColor) { 
                                            viewModel.setThemeColorIndex(index) 
                                        }
                                        .graphicsLayer { 
                                            alpha = if (state.dynamicColor) 0.4f else 1f 
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .then(
                                                if (isSelected) Modifier.border(
                                                    3.dp, 
                                                    MaterialTheme.colorScheme.onSurface,
                                                    CircleShape
                                                ) else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                CupertinoIcons.Default.Checkmark,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        
                        // 禁用提示
                        if (state.dynamicColor) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = " 关闭动态取色后可手动选择主题色",
                                style = MaterialTheme.typography.bodySmall,
                                color = iOSOrange
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

