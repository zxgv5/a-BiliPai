package com.android.purebilibili.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============== UP主空间 API 响应模型 ===============

// /x/space/wbi/acc/info 用户信息响应
@Serializable
data class SpaceInfoResponse(
    val code: Int = 0,
    val message: String = "",
    val data: SpaceUserInfo? = null
)

@Serializable
data class SpaceUserInfo(
    val mid: Long = 0,
    val name: String = "",
    val sex: String = "",
    val face: String = "",
    val sign: String = "",
    val level: Int = 0,
    @SerialName("fans_badge")
    val fansBadge: Boolean = false,
    val official: SpaceOfficial = SpaceOfficial(),
    val vip: SpaceVip = SpaceVip(),
    @SerialName("is_followed")
    val isFollowed: Boolean = false,
    @SerialName("top_photo")
    val topPhoto: String = "",
    @SerialName("live_room")
    val liveRoom: SpaceLiveRoom? = null
)

@Serializable
data class SpaceOfficial(
    val role: Int = 0,
    val title: String = "",
    val desc: String = "",
    val type: Int = -1  // -1无认证 0个人认证 1机构认证
)

@Serializable
data class SpaceVip(
    val type: Int = 0,  // 0无 1月度 2年度及以上
    val status: Int = 0,
    val label: SpaceVipLabel = SpaceVipLabel()
)

@Serializable
data class SpaceVipLabel(
    val text: String = ""
)

@Serializable
data class SpaceLiveRoom(
    val roomStatus: Int = 0,  // 0无房间 1有房间
    val liveStatus: Int = 0,  // 0未开播 1直播中
    val url: String = "",
    val title: String = "",
    val cover: String = "",
    @SerialName("roomid")
    val roomId: Long = 0
)

// /x/space/wbi/arc/search UP主投稿视频列表
@Serializable
data class SpaceVideoResponse(
    val code: Int = 0,
    val message: String = "",
    val data: SpaceVideoData? = null
)

@Serializable
data class SpaceVideoData(
    val list: SpaceVideoList = SpaceVideoList(),
    val page: SpacePage = SpacePage()
)

@Serializable
data class SpaceVideoList(
    val vlist: List<SpaceVideoItem> = emptyList()
)

@Serializable
data class SpacePage(
    val pn: Int = 1,  // 当前页
    val ps: Int = 30, // 每页数量
    val count: Int = 0 // 总视频数
)

@Serializable
data class SpaceVideoItem(
    val aid: Long = 0,
    val bvid: String = "",
    val title: String = "",
    val pic: String = "",
    val description: String = "",
    val play: Int = 0,
    val comment: Int = 0,
    val length: String = "",  // "10:24" 格式
    val created: Long = 0,    // 发布时间戳
    val author: String = "",
    val typeid: Int = 0,      // 🔥 分区 ID
    val typename: String = "" // 🔥 分区名称
)

// /x/relation/stat 粉丝关注数
@Serializable
data class RelationStatResponse(
    val code: Int = 0,
    val message: String = "",
    val data: RelationStatData? = null
)

@Serializable
data class RelationStatData(
    val mid: Long = 0,
    val following: Int = 0,
    val follower: Int = 0
)

// /x/space/upstat UP主播放量获赞数
@Serializable
data class UpStatResponse(
    val code: Int = 0,
    val message: String = "",
    val data: UpStatData? = null
)

@Serializable
data class UpStatData(
    val archive: ArchiveStatInfo = ArchiveStatInfo(),
    val likes: Long = 0
)

@Serializable
data class ArchiveStatInfo(
    val view: Long = 0  // 总播放量
)

// 🔥 视频分类
data class SpaceVideoCategory(
    val tid: Int,       // 分类 ID
    val name: String,   // 分类名称
    val count: Int      // 该分类下的视频数量
)

// 🔥 视频排序方式
enum class VideoSortOrder(val apiValue: String, val displayName: String) {
    PUBDATE("pubdate", "最新发布"),
    CLICK("click", "最多播放"),
    STOW("stow", "最多收藏")
}

// ========== 🔥 合集和系列 Models ==========

@kotlinx.serialization.Serializable
data class SeasonsSeriesListResponse(
    val code: Int = 0,
    val message: String = "",
    val data: SeasonsSeriesData? = null
)

@kotlinx.serialization.Serializable
data class SeasonsSeriesData(
    val items_lists: SeasonsSeriesItems? = null
)

@kotlinx.serialization.Serializable
data class SeasonsSeriesItems(
    val seasons_list: List<SeasonItem> = emptyList(),  // 合集列表
    val series_list: List<SeriesItem> = emptyList()    // 系列列表
)

@kotlinx.serialization.Serializable
data class SeasonItem(
    val meta: SeasonMeta = SeasonMeta(),
    val recent_aids: List<Long> = emptyList()
)

@kotlinx.serialization.Serializable
data class SeasonMeta(
    val season_id: Long = 0,
    val name: String = "",
    val cover: String = "",
    val total: Int = 0,
    val description: String = "",
    val mid: Long = 0
)

@kotlinx.serialization.Serializable
data class SeriesItem(
    val meta: SeriesMeta = SeriesMeta(),
    val recent_aids: List<Long> = emptyList()
)

@kotlinx.serialization.Serializable
data class SeriesMeta(
    val series_id: Long = 0,
    val name: String = "",
    val cover: String = "",
    val total: Int = 0,
    val description: String = "",
    val creator: String = "",
    val ctime: Long = 0,
    val mtime: Long = 0,
    val mid: Long = 0
)

// 合集/系列内视频列表响应
@kotlinx.serialization.Serializable
data class SeasonArchivesResponse(
    val code: Int = 0,
    val message: String = "",
    val data: SeasonArchivesData? = null
)

@kotlinx.serialization.Serializable
data class SeasonArchivesData(
    val aids: List<Long> = emptyList(),
    val archives: List<SeasonArchiveItem> = emptyList(),
    val meta: SeasonMeta = SeasonMeta(),
    val page: SeasonPage = SeasonPage()
)

@kotlinx.serialization.Serializable
data class SeasonArchiveItem(
    val aid: Long = 0,
    val bvid: String = "",
    val title: String = "",
    val pic: String = "",
    val duration: Int = 0,
    val pubdate: Long = 0,
    val stat: SeasonArchiveStat = SeasonArchiveStat()
)

@kotlinx.serialization.Serializable
data class SeasonArchiveStat(
    val view: Long = 0,
    val danmaku: Long = 0,
    val reply: Long = 0
)

@kotlinx.serialization.Serializable
data class SeasonPage(
    val page_num: Int = 1,
    val page_size: Int = 30,
    val total: Int = 0
)

// 🔥 系列视频列表响应
@kotlinx.serialization.Serializable
data class SeriesArchivesResponse(
    val code: Int = 0,
    val message: String = "",
    val data: SeriesArchivesData? = null
)

@kotlinx.serialization.Serializable
data class SeriesArchivesData(
    val aids: List<Long> = emptyList(),
    val archives: List<SeriesArchiveItem> = emptyList(),
    val page: SeriesPage = SeriesPage()
)

@kotlinx.serialization.Serializable
data class SeriesArchiveItem(
    val aid: Long = 0,
    val bvid: String = "",
    val title: String = "",
    val pic: String = "",
    val duration: Int = 0,
    val pubdate: Long = 0,
    val stat: SeriesArchiveStat = SeriesArchiveStat()
)

@kotlinx.serialization.Serializable
data class SeriesArchiveStat(
    val view: Long = 0,
    val danmaku: Long = 0,
    val reply: Long = 0
)

@kotlinx.serialization.Serializable
data class SeriesPage(
    val num: Int = 1,
    val size: Int = 30,
    val total: Int = 0
)
