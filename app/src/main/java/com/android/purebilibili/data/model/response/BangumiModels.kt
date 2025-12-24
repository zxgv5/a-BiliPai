// 文件路径: data/model/response/BangumiModels.kt
package com.android.purebilibili.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ========== 番剧/影视响应模型 ==========

/**
 * 番剧时间表响应
 */
@Serializable
data class BangumiTimelineResponse(
    val code: Int = 0,
    val message: String = "",
    val result: List<TimelineDay>? = null
)

@Serializable
data class TimelineDay(
    val date: String = "",           // 日期 "2024-12-14"
    @SerialName("date_ts")
    val dateTs: Long = 0,            // 时间戳
    @SerialName("day_of_week")
    val dayOfWeek: Int = 0,          // 星期几 (1-7)
    @SerialName("is_today")
    val isToday: Int = 0,            // 是否是今天
    val episodes: List<TimelineEpisode>? = null
)

@Serializable
data class TimelineEpisode(
    @SerialName("episode_id")
    val episodeId: Long = 0,
    @SerialName("season_id")
    val seasonId: Long = 0,
    val title: String = "",           // 番剧标题
    val cover: String = "",           // 封面
    @SerialName("square_cover")
    val squareCover: String = "",     // 方形封面
    @SerialName("pub_index")
    val pubIndex: String = "",        // 更新集数 "第12话"
    @SerialName("pub_time")
    val pubTime: String = "",         // 发布时间 "22:00"
    @SerialName("pub_ts")
    val pubTs: Long = 0,              // 发布时间戳
    val delay: Int = 0,               // 是否延迟
    @SerialName("delay_reason")
    val delayReason: String = "",     // 延迟原因
    val follow: Int = 0               // 是否追番
)

/**
 * 番剧索引/筛选响应
 */
@Serializable
data class BangumiIndexResponse(
    val code: Int = 0,
    val message: String = "",
    val data: BangumiIndexData? = null
)

@Serializable
data class BangumiIndexData(
    @SerialName("has_next")
    val hasNext: Int = 0,
    val list: List<BangumiItem>? = null,
    val num: Int = 0,                  // 当前页数量
    val size: Int = 0,                 // 每页数量
    val total: Int = 0                 // 总数
)

@Serializable
data class BangumiItem(
    @SerialName("season_id")
    val seasonId: Long = 0,
    @SerialName("media_id")
    val mediaId: Long = 0,
    val title: String = "",
    val cover: String = "",
    val badge: String = "",           // 角标 "会员专享" "独家"
    @SerialName("badge_type")
    val badgeType: Int = 0,
    val score: String = "",           // 评分 "9.8"
    @SerialName("new_ep")
    val newEp: NewEpInfo? = null,
    val order: String = "",           // 播放量/追番数
    @SerialName("order_type")
    val orderType: String = "",       // "追番人数" "播放数"
    @SerialName("season_type")
    val seasonType: Int = 0,          // 1=番剧 2=电影 3=纪录片 4=国创 5=电视剧
    @SerialName("season_type_name")
    val seasonTypeName: String = "",
    val subtitle: String = "",        // 副标题
    val styles: String = ""           // 风格标签
)

@Serializable
data class NewEpInfo(
    val cover: String = "",
    val id: Long = 0,
    @SerialName("index_show")
    val indexShow: String = ""        // "全13话" "更新至第12话"
)

/**
 * 番剧详情响应
 */
@Serializable
data class BangumiDetailResponse(
    val code: Int = 0,
    val message: String = "",
    val result: BangumiDetail? = null
)

@Serializable
data class BangumiDetail(
    @SerialName("season_id")
    val seasonId: Long = 0,
    @SerialName("media_id")
    val mediaId: Long = 0,
    val title: String = "",
    val cover: String = "",
    @SerialName("square_cover")
    val squareCover: String = "",
    val evaluate: String = "",        // 简介
    val rating: BangumiRating? = null,
    val stat: BangumiStat? = null,
    @SerialName("new_ep")
    val newEp: NewEpDetail? = null,
    val episodes: List<BangumiEpisode>? = null,
    val seasons: List<SeasonInfo>? = null,      // 关联季度
    val areas: List<AreaInfo>? = null,          // 地区
    val styles: List<String>? = null,           // 🔥🔥 [修复] 风格是字符串数组，不是对象数组
    val actors: String = "",                     // 演员/声优
    val staff: String = "",                      // 制作人员
    @SerialName("season_type")
    val seasonType: Int = 0,
    @SerialName("season_type_name")
    val seasonTypeName: String = "",
    val total: Int = 0,                          // 总集数
    val mode: Int = 0,                           // 2=电影 3=番剧
    val rights: BangumiRights? = null,
    @SerialName("user_status")
    val userStatus: UserStatus? = null
)

@Serializable
data class BangumiRating(
    val score: Float = 0f,
    val count: Int = 0
)

@Serializable
data class BangumiStat(
    val views: Long = 0,              // 播放量
    val danmakus: Long = 0,           // 弹幕数
    val favorites: Long = 0,          // 追番/追剧数
    val coins: Long = 0,
    val likes: Long = 0,
    val reply: Long = 0,              // 评论数
    val share: Long = 0
)

@Serializable
data class NewEpDetail(
    val id: Long = 0,
    val title: String = "",
    val desc: String = "",            // "全13话"
    @SerialName("is_new")
    val isNew: Int = 0
)

@Serializable
data class BangumiEpisode(
    val id: Long = 0,                 // ep_id
    val aid: Long = 0,                // 对应的视频 aid
    val bvid: String = "",
    val cid: Long = 0,
    val title: String = "",           // 集标题 "第1话 开始"
    @SerialName("long_title")
    val longTitle: String = "",       // 长标题
    val cover: String = "",
    val duration: Long = 0,           // 时长（毫秒）
    val badge: String = "",           // "会员" "预告"
    @SerialName("badge_type")
    val badgeType: Int = 0,
    val status: Int = 0,              // 状态
    @SerialName("pub_time")
    val pubTime: Long = 0,
    val skip: EpisodeSkip? = null     // 跳过片头片尾信息
)

@Serializable
data class EpisodeSkip(
    val op: SkipRange? = null,        // 片头
    val ed: SkipRange? = null         // 片尾
)

@Serializable
data class SkipRange(
    val start: Int = 0,
    val end: Int = 0
)

@Serializable
data class SeasonInfo(
    @SerialName("season_id")
    val seasonId: Long = 0,
    @SerialName("season_title")
    val seasonTitle: String = "",     // "第一季" "第二季"
    val title: String = "",
    val cover: String = "",
    val badge: String = "",
    @SerialName("is_new")
    val isNew: Int = 0
)

@Serializable
data class AreaInfo(
    val id: Int = 0,
    val name: String = ""             // "日本" "中国大陆"
)

@Serializable
data class StyleInfo(
    val id: Int = 0,
    val name: String = ""             // "热血" "恋爱"
)

@Serializable
data class BangumiRights(
    @SerialName("allow_download")
    val allowDownload: Int = 0,
    @SerialName("allow_review")
    val allowReview: Int = 0,
    @SerialName("is_preview")
    val isPreview: Int = 0,           // 是否预告/预览
    @SerialName("watch_platform")
    val watchPlatform: Int = 0
)

@Serializable
data class UserStatus(
    val follow: Int = 0,              // 是否追番
    @SerialName("follow_status")
    val followStatus: Int = 0,
    val vip: Int = 0,                 // 是否大会员
    @SerialName("vip_frozen")
    val vipFrozen: Int = 0,
    val progress: WatchProgress? = null
)

@Serializable
data class WatchProgress(
    @SerialName("last_ep_id")
    val lastEpId: Long = 0,
    @SerialName("last_ep_index")
    val lastEpIndex: String = "",
    @SerialName("last_time")
    val lastTime: Long = 0            // 上次观看时间点
)

/**
 * 番剧播放地址响应
 * 注意：实际 API 响应结构是 result -> video_info -> dash
 */
@Serializable
data class BangumiPlayUrlResponse(
    val code: Int = 0,
    val message: String = "",
    val result: BangumiPlayUrlResult? = null
)

/**
 * 番剧播放响应 result 层
 */
@Serializable
data class BangumiPlayUrlResult(
    @SerialName("video_info")
    val videoInfo: BangumiVideoInfo? = null
)

/**
 * 番剧播放视频信息（包含 DASH 等）
 * 注意：移除了类型不稳定的字段（has_paid, is_preview 等），它们有时返回 Int 有时返回 Boolean
 */
@Serializable
data class BangumiVideoInfo(
    val quality: Int = 0,
    val format: String = "",
    val timelength: Long = 0,
    @SerialName("accept_format")
    val acceptFormat: String = "",
    @SerialName("accept_quality")
    val acceptQuality: List<Int>? = null,
    @SerialName("accept_description")
    val acceptDescription: List<String>? = null,
    @SerialName("video_codecid")
    val videoCodecid: Int = 0,
    // 🔥🔥 关键：durl 和 dash 字段
    val durl: List<Durl>? = null,
    val durls: List<Durl>? = null,  // 某些情况下叫 durls
    val dash: Dash? = null,
    @SerialName("support_formats")
    val supportFormats: List<FormatItem>? = null
    // 🔥🔥 [修复] 移除类型不稳定的字段：has_paid, is_preview, status 等
    // 这些字段有时返回 Int (0/1)，有时返回 Boolean (true/false)，导致解析失败
)

/**
 * 番剧类型枚举
 */
enum class BangumiType(val value: Int, val label: String) {
    ANIME(1, "番剧"),
    MOVIE(2, "电影"),
    DOCUMENTARY(3, "纪录片"),
    GUOCHUANG(4, "国创"),
    TV_SHOW(5, "电视剧"),
    VARIETY(7, "综艺")
}

// ========== 番剧搜索响应 ==========

/**
 * 番剧搜索响应
 */
@Serializable
data class BangumiSearchResponse(
    val code: Int = 0,
    val message: String = "",
    val data: BangumiSearchData? = null
)

@Serializable
data class BangumiSearchData(
    val numPages: Int = 0,
    val numResults: Int = 0,
    val page: Int = 1,
    @SerialName("pagesize")
    val pageSize: Int = 20,
    val result: List<BangumiSearchItem>? = null
)

@Serializable
data class BangumiSearchItem(
    @SerialName("season_id")
    val seasonId: Long = 0,
    @SerialName("media_id")
    val mediaId: Long = 0,
    val title: String = "",              // 标题 (可能包含高亮标签)
    @SerialName("org_title")
    val orgTitle: String = "",           // 原标题
    val cover: String = "",
    val areas: String = "",              // 地区
    val styles: String = "",             // 风格
    @SerialName("cv")
    val cv: String = "",                 // 声优
    val staff: String = "",              // 制作人员
    @SerialName("season_type_name")
    val seasonTypeName: String = "",     // "番剧" "电影" 等
    @SerialName("season_type")
    val seasonType: Int = 0,
    val desc: String = "",               // 简介
    @SerialName("pubtime")
    val pubTime: Long = 0,
    @SerialName("media_score")
    val mediaScore: MediaScore? = null,
    @SerialName("ep_size")
    val epSize: Int = 0,                 // 集数
    @SerialName("is_avid")
    val isAvid: Boolean = false,
    val badges: List<BangumiSearchBadge>? = null,
    @SerialName("goto_url")
    val gotoUrl: String = "",
    @SerialName("index_show")
    val indexShow: String = ""           // "全12话" "更新至第5话"
)

@Serializable
data class MediaScore(
    val score: Float = 0f,
    @SerialName("user_count")
    val userCount: Int = 0
)

@Serializable
data class BangumiSearchBadge(
    val text: String = "",
    @SerialName("text_color")
    val textColor: String = "",
    @SerialName("text_color_night")
    val textColorNight: String = "",
    @SerialName("bg_color")
    val bgColor: String = "",
    @SerialName("bg_color_night")
    val bgColorNight: String = "",
    @SerialName("border_color")
    val borderColor: String = "",
    @SerialName("border_color_night")
    val borderColorNight: String = "",
    @SerialName("bg_style")
    val bgStyle: Int = 0
)

// ========== 我的追番列表响应 ==========

/**
 * 我的追番列表响应
 */
@Serializable
data class MyFollowBangumiResponse(
    val code: Int = 0,
    val message: String = "",
    val data: MyFollowBangumiData? = null
)

@Serializable
data class MyFollowBangumiData(
    val total: Int = 0,
    val pn: Int = 1,
    val ps: Int = 30,
    val list: List<FollowBangumiItem>? = null
)

@Serializable
data class FollowBangumiItem(
    @SerialName("season_id")
    val seasonId: Long = 0,
    @SerialName("media_id")
    val mediaId: Long = 0,
    val title: String = "",
    val cover: String = "",
    @SerialName("square_cover")
    val squareCover: String = "",
    val evaluate: String = "",           // 简介
    val areas: List<AreaInfo>? = null,
    @SerialName("season_type_name")
    val seasonTypeName: String = "",
    @SerialName("season_type")
    val seasonType: Int = 0,
    val badge: String = "",              // "会员" "独家"
    @SerialName("badge_type")
    val badgeType: Int = 0,
    @SerialName("new_ep")
    val newEp: NewEpInfo? = null,
    val progress: String = "",           // 观看进度文案 "看到第5话"
    @SerialName("is_finish")
    val isFinish: Int = 0,               // 是否完结
    @SerialName("follow_status")
    val followStatus: Int = 0,           // 追番状态
    val total: Int = 0,                  // 总集数
    @SerialName("first_ep")
    val firstEp: Long = 0,               // 第一集 epId
    val url: String = ""
)

// ========== 筛选条件 ==========

/**
 * 番剧筛选条件
 */
data class BangumiFilter(
    val year: String = "-1",             // 年份，-1=全部
    val area: Int = -1,                  // 地区，-1=全部
    val styleId: Int = -1,               // 风格，-1=全部
    val isFinish: Int = -1,              // 状态，-1=全部, 0=连载, 1=完结
    val seasonStatus: Int = -1,          // 付费类型，-1=全部
    val order: Int = 2                   // 排序，2=播放量, 0=更新时间
) {
    companion object {
        val ORDER_OPTIONS = listOf(
            0 to "更新时间",
            2 to "播放数量",
            4 to "追番人数",
            3 to "最高评分"
        )
        
        val AREA_OPTIONS = listOf(
            -1 to "全部地区",
            1 to "中国大陆",
            2 to "日本",
            3 to "美国",
            4 to "英国",
            5 to "其他"
        )
        
        val STATUS_OPTIONS = listOf(
            -1 to "全部状态",
            0 to "连载中",
            1 to "已完结"
        )
        
        val YEAR_OPTIONS = listOf(
            "-1" to "全部年份",
            "2025" to "2025",
            "2024" to "2024",
            "2023" to "2023",
            "2022" to "2022",
            "2021" to "2021",
            "2020" to "2020",
            "2019" to "2019",
            "2018" to "2018",
            "2017" to "2017",
            "2016" to "2016",
            "2015" to "2015",
            "2010-2014" to "2010-2014",
            "2005-2009" to "2005-2009",
            "2000-2004" to "2000-2004",
            "90年代" to "90年代",
            "80年代" to "80年代",
            "更早" to "更早"
        )
    }
}
