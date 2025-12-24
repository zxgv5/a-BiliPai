// 文件路径: feature/bangumi/BangumiDetailScreen.kt
package com.android.purebilibili.feature.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.purebilibili.core.theme.iOSYellow
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.BangumiDetail
import com.android.purebilibili.data.model.response.BangumiEpisode

/**
 * 番剧详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BangumiDetailScreen(
    seasonId: Long,
    onBack: () -> Unit,
    onEpisodeClick: (BangumiEpisode) -> Unit,  // 点击剧集播放
    onSeasonClick: (Long) -> Unit = {},        // 🔥 点击切换季度
    viewModel: BangumiViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    
    // 加载详情
    LaunchedEffect(seasonId) {
        viewModel.loadSeasonDetail(seasonId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("番剧详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        when (val state = detailState) {
            is BangumiDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is BangumiDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadSeasonDetail(seasonId) }) {
                            Text("重试")
                        }
                    }
                }
            }
            is BangumiDetailState.Success -> {
                BangumiDetailContent(
                    detail = state.detail,
                    paddingValues = paddingValues,
                    onEpisodeClick = onEpisodeClick,
                    onSeasonClick = onSeasonClick,
                    onToggleFollow = { isFollowing ->
                        viewModel.toggleFollow(seasonId, isFollowing)
                    }
                )
            }
        }
    }
}

@Composable
private fun BangumiDetailContent(
    detail: BangumiDetail,
    paddingValues: PaddingValues,
    onEpisodeClick: (BangumiEpisode) -> Unit,
    onSeasonClick: (Long) -> Unit,
    onToggleFollow: (Boolean) -> Unit
) {
    // 🔥🔥 [修复] 使用 detail 本身作为 key，这样当 ViewModel 更新 detail 时，状态会正确同步
    val followFromApi = detail.userStatus?.follow == 1
    var isFollowing by remember(detail.seasonId, followFromApi) { 
        mutableStateOf(followFromApi) 
    }
    
    // 🔥🔥 [修复] 移除 LaunchedEffect，避免重置用户的点击状态
    // 状态同步现在通过 remember 的 key 来实现
    
    // 🔥 选集相关状态（必须在函数顶层定义）
    var showEpisodeSheet by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpInputText by remember { mutableStateOf("") }
    var jumpErrorMessage by remember { mutableStateOf<String?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 头部封面和信息
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)  // 🔥 [修复] 增大高度防止文字被裁切
                ) {
                    // 封面背景（模糊）
                    AsyncImage(
                        model = FormatUtils.fixImageUrl(detail.cover),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // 渐变遮罩
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                    
                    // 信息区域
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 封面图
                        AsyncImage(
                            model = FormatUtils.fixImageUrl(detail.cover),
                            contentDescription = detail.title,
                            modifier = Modifier
                                .width(120.dp)
                                .aspectRatio(0.75f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // 标题和信息
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = detail.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 评分
                            detail.rating?.let { rating ->
                                if (rating.score > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = iOSYellow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = String.format("%.1f", rating.score),
                                            color = iOSYellow,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = " (${rating.count}人评分)",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // 更新状态
                            detail.newEp?.desc?.let { desc ->
                                Text(
                                    text = desc,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // 播放量
                            detail.stat?.let { stat ->
                                Text(
                                    text = "${FormatUtils.formatStat(stat.views)}播放 · ${FormatUtils.formatStat(stat.favorites)}追番",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // 操作按钮
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 追番按钮
                    if (isFollowing) {
                        // 🔥 已追番：使用带边框的样式，更清晰可见
                        OutlinedButton(
                            onClick = { 
                                val wasFollowing = isFollowing
                                isFollowing = !wasFollowing
                                onToggleFollow(wasFollowing)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("已追番")
                        }
                    } else {
                        // 🔥 未追番：使用填充的主色按钮
                        Button(
                            onClick = { 
                                val wasFollowing = isFollowing
                                isFollowing = !wasFollowing
                                onToggleFollow(wasFollowing)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("追番")
                        }
                    }
                }
            }
            
            // 简介
            if (detail.evaluate.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "简介",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = detail.evaluate,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            // 剧集列表
            if (!detail.episodes.isNullOrEmpty()) {
                item {
                    // 🔥 选集标题和快速跳转
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "选集 (${detail.episodes.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        // 🔥 跳转按钮
                        Surface(
                            onClick = { 
                                jumpInputText = ""
                                jumpErrorMessage = null
                                showJumpDialog = true 
                            },
                            color = Color.Transparent
                        ) {
                            Text(
                                text = "跳转",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // 🔥 分页选择器（超过50集时显示）
                if (detail.episodes.size > 50) {
                    item {
                        val episodesPerPage = 50
                        val totalPages = (detail.episodes.size + episodesPerPage - 1) / episodesPerPage
                        var selectedPage by remember { mutableIntStateOf(0) }
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(totalPages) { page ->
                                val start = page * episodesPerPage + 1
                                val end = minOf((page + 1) * episodesPerPage, detail.episodes.size)
                                val isCurrentPage = page == selectedPage
                                
                                Surface(
                                    onClick = { selectedPage = page },
                                    color = if (isCurrentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "$start-$end",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        color = if (isCurrentPage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 🔥 剧集预览（只显示前6个，点击展开完整列表）
                item {
                    val previewEpisodes = detail.episodes.take(6)
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(previewEpisodes) { episode ->
                            EpisodeChip(
                                episode = episode,
                                onClick = { onEpisodeClick(episode) }
                            )
                        }
                        
                        // 更多按钮
                        if (detail.episodes.size > 6) {
                            item {
                                Surface(
                                    onClick = { showEpisodeSheet = true },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .aspectRatio(16f / 9f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.MoreHoriz,
                                                contentDescription = "更多",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "全部${detail.episodes.size}集",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 相关季度
            if (!detail.seasons.isNullOrEmpty() && detail.seasons.size > 1) {
                item {
                    Text(
                        text = "相关季度",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(detail.seasons) { season ->
                            val isCurrentSeason = season.seasonId == detail.seasonId
                            Surface(
                                modifier = Modifier.clickable {
                                    if (!isCurrentSeason) {
                                        onSeasonClick(season.seasonId)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrentSeason) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = season.seasonTitle.ifEmpty { season.title },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    color = if (isCurrentSeason) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 🔥 快速跳转对话框（在 LazyColumn 外部）
        if (showJumpDialog && !detail.episodes.isNullOrEmpty()) {
            AlertDialog(
                onDismissRequest = { showJumpDialog = false },
                title = { Text("跳转到第几集") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = jumpInputText,
                            onValueChange = { 
                                jumpInputText = it.filter { char -> char.isDigit() }
                                jumpErrorMessage = null
                            },
                            label = { Text("集数 (1-${detail.episodes.size})") },
                            singleLine = true,
                            isError = jumpErrorMessage != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (jumpErrorMessage != null) {
                            Text(
                                text = jumpErrorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val epNumber = jumpInputText.toIntOrNull()
                            if (epNumber == null || epNumber < 1 || epNumber > detail.episodes.size) {
                                jumpErrorMessage = "请输入 1-${detail.episodes.size} 之间的数字"
                            } else {
                                val targetEpisode = detail.episodes.getOrNull(epNumber - 1)
                                if (targetEpisode != null) {
                                    onEpisodeClick(targetEpisode)
                                }
                                showJumpDialog = false
                            }
                        }
                    ) {
                        Text("跳转")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJumpDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        
        // 🔥 官方风格：底部弹出选集面板（在 LazyColumn 外部）
        if (showEpisodeSheet && !detail.episodes.isNullOrEmpty()) {
            EpisodeSelectionSheet(
                detail = detail,
                onDismiss = { showEpisodeSheet = false },
                onEpisodeClick = { episode ->
                    onEpisodeClick(episode)
                    showEpisodeSheet = false
                },
                onSeasonClick = onSeasonClick
            )
        }
    }
}

@Composable
private fun EpisodeChip(
    episode: BangumiEpisode,
    onClick: () -> Unit
) {
    // 🔥 带封面图的设计，集数和标题在同一行
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        // 缩略图
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box {
                AsyncImage(
                    model = FormatUtils.fixImageUrl(episode.cover),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // 角标（如：会员）
                if (episode.badge.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = episode.badge,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // 🔥 集数和标题在同一行：数字在左，标题在右
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 集数数字
            Text(
                text = episode.title.ifEmpty { episode.id.toString() },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 标题
            if (episode.longTitle.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = episode.longTitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 🔥 官方风格：底部弹出选集面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeSelectionSheet(
    detail: BangumiDetail,
    onDismiss: () -> Unit,
    onEpisodeClick: (BangumiEpisode) -> Unit,
    onSeasonClick: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null  // 使用自定义标题栏
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)  // 占屏幕80%高度
        ) {
            // 🔥 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "选集 (${detail.episodes?.size ?: 0})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 🔥 季度标签（如果有多个季度）
            if (!detail.seasons.isNullOrEmpty() && detail.seasons.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(detail.seasons) { season ->
                        val isCurrentSeason = season.seasonId == detail.seasonId
                        
                        Surface(
                            onClick = {
                                if (!isCurrentSeason) {
                                    onSeasonClick(season.seasonId)
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrentSeason) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Text(
                                text = season.seasonTitle.ifEmpty { season.title },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                fontWeight = if (isCurrentSeason) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrentSeason) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
            
            // 🔥 更新信息
            detail.newEp?.desc?.let { desc ->
                Text(
                    text = desc,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            
            // 🔥 分页选择器（超过50集时显示）
            val episodes = detail.episodes ?: emptyList()
            val episodesPerPage = 50
            val totalPages = if (episodes.size > episodesPerPage) {
                (episodes.size + episodesPerPage - 1) / episodesPerPage
            } else 0
            var selectedPage by remember { mutableIntStateOf(0) }
            
            if (totalPages > 0) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(totalPages) { page ->
                        val start = page * episodesPerPage + 1
                        val end = minOf((page + 1) * episodesPerPage, episodes.size)
                        val isCurrentPage = page == selectedPage
                        
                        Surface(
                            onClick = { selectedPage = page },
                            color = if (isCurrentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "$start-$end",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = if (isCurrentPage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 🔥 剧集列表（两列网格布局）
            val displayEpisodes = if (totalPages > 0) {
                val pageStart = selectedPage * episodesPerPage
                val pageEnd = minOf(pageStart + episodesPerPage, episodes.size)
                episodes.subList(pageStart, pageEnd)
            } else {
                episodes
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    count = displayEpisodes.size,
                    key = { index -> displayEpisodes[index].id }
                ) { index ->
                    val episode = displayEpisodes[index]
                    EpisodeListItem(
                        episode = episode,
                        onClick = { onEpisodeClick(episode) }
                    )
                }
            }
        }
    }
}

/**
 * 🔥 官方风格：剧集列表项（用于底部面板）
 */
@Composable
private fun EpisodeListItem(
    episode: BangumiEpisode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 缩略图
        Box(
            modifier = Modifier
                .width(80.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(4.dp))
        ) {
            AsyncImage(
                model = FormatUtils.fixImageUrl(episode.cover),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // VIP 角标
            if (episode.badge.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = episode.badge,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 8.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        // 剧集信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 集数
            Text(
                text = "第${episode.title}话",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            
            // 标题
            if (episode.longTitle.isNotEmpty()) {
                Text(
                    text = episode.longTitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
