package com.android.purebilibili.feature.download

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronBackward
import java.io.File

/**
 * 🔧 [新增] 离线视频播放器
 * 用于在无网络状态下播放本地缓存的视频文件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineVideoPlayerScreen(
    taskId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tasks by DownloadManager.tasks.collectAsState()
    val task = tasks[taskId]
    
    if (task == null || task.filePath == null) {
        // 任务不存在或文件不存在
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("视频文件不存在", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("返回")
                }
            }
        }
        return
    }
    
    val file = File(task.filePath!!)
    if (!file.exists()) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("视频文件已被删除", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("返回")
                }
            }
        }
        return
    }
    
    // 创建播放器
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
    
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text(task.title, color = Color.White, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            CupertinoIcons.Default.ChevronBackward,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
