// 文件路径: data/model/response/ResponseModels.kt
// 1. 强制压制 InternalSerializationApi 报错
@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.android.purebilibili.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReplyResponse(
    val code: Int = 0,
    val message: String = "",
    val data: ReplyData? = null
)

@Serializable
data class ReplyData(
    // 🔥 WBI API 使用 cursor
    val cursor: ReplyCursor = ReplyCursor(),
    // 🔥 旧版 API 使用 page
    val page: ReplyPage = ReplyPage(),
    // 🔥 普通评论列表
    val replies: List<ReplyItem>? = emptyList(),
    // 🔥🔥 [新增] 置顶评论列表 (WBI API)
    @SerialName("top_replies")
    val topReplies: List<ReplyItem>? = null,
    // 🔥🔥 [新增] 热门评论列表
    val hots: List<ReplyItem>? = null,
    // 🔥🔥 [新增] UP主信息（包含 UP 置顶评论）
    val upper: ReplyUpper? = null
) {
    // 🔥 统一获取总评论数
    fun getAllCount(): Int = if (cursor.allCount > 0) cursor.allCount else page.count
    // 🔥 统一获取是否结束
    fun getIsEnd(currentPage: Int, currentSize: Int): Boolean {
        return if (cursor.allCount > 0) {
            cursor.isEnd
        } else {
            // 旧版 API 没有 isEnd，用页数判断
            currentSize >= page.count || page.count == 0
        }
    }
    // 🔥🔥 [新增] 获取置顶评论（WBI 和旧版 API 兼容）
    fun collectTopReplies(): List<ReplyItem> {
        val result = mutableListOf<ReplyItem>()
        // 添加 UP 置顶
        upper?.top?.let { result.add(it) }
        // 添加其他置顶
        topReplies?.let { result.addAll(it) }
        return result.distinctBy { it.rpid }
    }
}

// 🔥🔥 [新增] UP 主信息
@Serializable
data class ReplyUpper(
    val mid: Long = 0,
    // UP 主置顶评论
    val top: ReplyItem? = null
)

// 🔥 WBI API 的游标信息
@Serializable
data class ReplyCursor(
    @SerialName("all_count") val allCount: Int = 0,
    @SerialName("is_end") val isEnd: Boolean = false,
    val next: Int = 0
)

// 🔥 旧版 API 的分页信息
@Serializable
data class ReplyPage(
    val num: Int = 0,      // 当前页码
    val size: Int = 0,     // 每页数量
    val count: Int = 0,    // 总评论数
    val acount: Int = 0    // 总计评论条数（包含回复）
)

@Serializable
data class ReplyItem(
    val rpid: Long = 0,
    val oid: Long = 0,
    val mid: Long = 0,
    val count: Int = 0,
    val rcount: Int = 0,
    val like: Int = 0,
    val ctime: Long = 0,

    // 🔥🔥 核心修复：给对象类型加上默认值 = ReplyMember()
    // 遇到被删除用户或特殊评论时，member 字段可能缺失或为 null，不加默认值会导致整个列表解析崩溃
    val member: ReplyMember = ReplyMember(),
    val content: ReplyContent = ReplyContent(),

    val replies: List<ReplyItem>? = null,
    
    // 🔥 UP主操作信息（UP觉得很赞/UP回复了）
    @SerialName("up_action")
    val upAction: ReplyUpAction? = null
)

// 🔥 UP主操作信息
@Serializable
data class ReplyUpAction(
    val like: Boolean = false,  // UP主觉得很赞
    val reply: Boolean = false  // UP主回复了
)

@Serializable
data class ReplyMember(
    val mid: String = "0",
    val uname: String = "未知用户",
    val avatar: String = "",

    @SerialName("level_info")
    val levelInfo: ReplyLevelInfo = ReplyLevelInfo(),

    val vip: ReplyVipInfo? = null
)

@Serializable
data class ReplyLevelInfo(
    @SerialName("current_level")
    val currentLevel: Int = 0
)

@Serializable
data class ReplyVipInfo(
    val vipType: Int = 0,
    val vipStatus: Int = 0
)

@Serializable
data class ReplyContent(
    val message: String = "",
    val device: String? = "",
    val emote: Map<String, ReplyEmote>? = null,
    // 🔥 评论图片
    val pictures: List<ReplyPicture>? = null
)

// 🔥 评论图片
@Serializable
data class ReplyPicture(
    @SerialName("img_src") val imgSrc: String = "",
    @SerialName("img_width") val imgWidth: Int = 0,
    @SerialName("img_height") val imgHeight: Int = 0,
    @SerialName("img_size") val imgSize: Float = 0f
)

@Serializable
data class ReplyEmote(
    val id: Long = 0,
    val text: String = "",
    val url: String = ""
)