package com.android.purebilibili.feature.space

import com.android.purebilibili.data.model.response.ArchiveMajor
import com.android.purebilibili.data.model.response.ArchiveStat
import com.android.purebilibili.data.model.response.ArticleMajor
import com.android.purebilibili.data.model.response.DrawItem
import com.android.purebilibili.data.model.response.DrawMajor
import com.android.purebilibili.data.model.response.DynamicAuthorModule
import com.android.purebilibili.data.model.response.DynamicContentModule
import com.android.purebilibili.data.model.response.DynamicDesc
import com.android.purebilibili.data.model.response.DynamicItem
import com.android.purebilibili.data.model.response.DynamicMajor
import com.android.purebilibili.data.model.response.DynamicModules
import com.android.purebilibili.data.model.response.DynamicStatModule
import com.android.purebilibili.data.model.response.EmojiInfo
import com.android.purebilibili.data.model.response.OpusMajor
import com.android.purebilibili.data.model.response.OpusPic
import com.android.purebilibili.data.model.response.OpusSummary
import com.android.purebilibili.data.model.response.RichTextNode
import com.android.purebilibili.data.model.response.SpaceDynamicContent
import com.android.purebilibili.data.model.response.SpaceDynamicDesc
import com.android.purebilibili.data.model.response.SpaceDynamicItem
import com.android.purebilibili.data.model.response.SpaceDynamicMajor
import com.android.purebilibili.data.model.response.SpaceDynamicOpusSummary
import com.android.purebilibili.data.model.response.SpaceDynamicRichText
import com.android.purebilibili.data.model.response.StatItem

enum class SpaceDynamicPresentationState {
    LOADING,
    CONTENT,
    EMPTY,
    ERROR
}

internal fun shouldRequestInitialSpaceDynamicLoad(
    hasLoadedOnce: Boolean,
    isLoading: Boolean
): Boolean {
    return !hasLoadedOnce && !isLoading
}

fun resolveSpaceDynamicPresentationState(
    itemCount: Int,
    isLoading: Boolean,
    hasLoadedOnce: Boolean,
    lastLoadFailed: Boolean
): SpaceDynamicPresentationState {
    if (itemCount > 0) return SpaceDynamicPresentationState.CONTENT
    if (isLoading || !hasLoadedOnce) return SpaceDynamicPresentationState.LOADING
    if (lastLoadFailed) return SpaceDynamicPresentationState.ERROR
    return SpaceDynamicPresentationState.EMPTY
}

internal fun filterSpaceDynamicItemsByQuery(
    items: List<SpaceDynamicItem>,
    query: String
): List<SpaceDynamicItem> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return items

    return items.filter { item ->
        resolveSpaceDynamicSearchText(item).contains(normalizedQuery, ignoreCase = true)
    }
}

/** Extra feed pages to pull when local search has no hit yet. */
internal const val SPACE_DYNAMIC_SEARCH_PREFETCH_PAGE_LIMIT = 5

internal const val SPACE_DYNAMIC_SEARCH_DEBOUNCE_MS = 300L

/**
 * Keep fetching more space dynamics while the query has no local matches.
 * Stops when a match appears, the feed ends, or [maxPages] pages were pulled this search.
 */
internal fun shouldPrefetchMoreSpaceDynamicsForSearch(
    query: String,
    matchCount: Int,
    hasMore: Boolean,
    pagesFetchedForSearch: Int,
    maxPages: Int = SPACE_DYNAMIC_SEARCH_PREFETCH_PAGE_LIMIT
): Boolean {
    if (query.trim().isEmpty()) return false
    if (matchCount > 0) return false
    if (!hasMore) return false
    if (pagesFetchedForSearch >= maxPages) return false
    return true
}

internal fun mergeSpaceDynamicPages(
    existing: List<SpaceDynamicItem>,
    incoming: List<SpaceDynamicItem>
): List<SpaceDynamicItem> {
    if (incoming.isEmpty()) return existing
    if (existing.isEmpty()) return incoming
    val seen = existing.mapTo(HashSet(existing.size + incoming.size)) { it.id_str }
    val merged = existing.toMutableList()
    incoming.forEach { item ->
        if (item.id_str.isNotBlank() && seen.add(item.id_str)) {
            merged += item
        }
    }
    return merged
}

/**
 * Build searchable plain text for a space dynamic.
 * Bilibili space feeds often leave [SpaceDynamicDesc.text] empty and put body only in
 * rich_text_nodes, or only put content on the reposted [SpaceDynamicItem.orig].
 */
internal fun resolveSpaceDynamicSearchText(item: SpaceDynamicItem): String {
    return buildString {
        appendSpaceDynamicContentSearchText(item.modules.module_dynamic)
        item.orig?.let { orig ->
            append('\n')
            appendSpaceDynamicContentSearchText(orig.modules.module_dynamic)
            orig.modules.module_author?.name?.takeIf { it.isNotBlank() }?.let {
                append('\n')
                append(it)
            }
        }
        item.modules.module_author?.name?.takeIf { it.isNotBlank() }?.let {
            append('\n')
            append(it)
        }
    }
}

private fun StringBuilder.appendSpaceDynamicContentSearchText(content: SpaceDynamicContent?) {
    if (content == null) return
    appendSpaceDynamicDescSearchText(content.desc)
    val major = content.major ?: return
    major.archive?.let { archive ->
        appendLineIfNotBlank(archive.title)
        appendLineIfNotBlank(archive.desc)
        appendLineIfNotBlank(archive.bvid)
    }
    major.opus?.let { opus ->
        appendLineIfNotBlank(opus.title)
        appendSpaceDynamicOpusSummarySearchText(opus.summary)
    }
    major.article?.let { article ->
        appendLineIfNotBlank(article.title)
        appendLineIfNotBlank(article.desc)
        appendLineIfNotBlank(article.label)
    }
}

private fun StringBuilder.appendSpaceDynamicDescSearchText(desc: SpaceDynamicDesc?) {
    if (desc == null) return
    appendLineIfNotBlank(desc.text)
    desc.rich_text_nodes.forEach { node ->
        appendLineIfNotBlank(node.text)
        appendLineIfNotBlank(node.orig_text)
    }
}

private fun StringBuilder.appendSpaceDynamicOpusSummarySearchText(summary: SpaceDynamicOpusSummary?) {
    if (summary == null) return
    appendLineIfNotBlank(summary.text)
    summary.rich_text_nodes.forEach { node ->
        appendLineIfNotBlank(node.text)
        appendLineIfNotBlank(node.orig_text)
    }
}

private fun StringBuilder.appendLineIfNotBlank(value: String?) {
    val normalized = value?.trim().orEmpty()
    if (normalized.isEmpty()) return
    if (isNotEmpty()) append('\n')
    append(normalized)
}

internal fun resolveSpaceDynamicCardItems(items: List<SpaceDynamicItem>): List<DynamicItem> {
    return items.map(::resolveSpaceDynamicCardItem)
}

private fun SpaceDynamicRichText.toDynamicRichTextNode(): RichTextNode {
    return RichTextNode(
        type = type,
        text = text,
        emoji = emoji?.let { emoji ->
            EmojiInfo(
                icon_url = emoji.icon_url,
                size = emoji.size,
                text = emoji.text
            )
        },
        jump_url = jump_url,
        rid = rid
    )
}

private fun SpaceDynamicDesc.toDynamicDesc(): DynamicDesc {
    return DynamicDesc(
        text = text,
        rich_text_nodes = rich_text_nodes.map { it.toDynamicRichTextNode() }
    )
}

private fun resolveSpaceDynamicArticleFallbackDesc(major: SpaceDynamicMajor?): DynamicDesc? {
    val article = major?.article ?: return null
    val text = listOf(article.title.trim(), article.desc.trim())
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(separator = "\n")
    return text.takeIf { it.isNotBlank() }?.let { DynamicDesc(text = it) }
}

private fun resolveSpaceDynamicContentDesc(content: SpaceDynamicContent): DynamicDesc? {
    val mappedDesc = content.desc?.toDynamicDesc()
    if (mappedDesc != null && (mappedDesc.text.isNotBlank() || mappedDesc.rich_text_nodes.isNotEmpty())) {
        return mappedDesc
    }
    return resolveSpaceDynamicArticleFallbackDesc(content.major)
}

internal fun resolveSpaceDynamicCardItem(item: SpaceDynamicItem): DynamicItem {
    return DynamicItem(
        id_str = item.id_str,
        type = item.type,
        visible = item.visible,
        basic = item.basic,
        modules = DynamicModules(
            module_author = item.modules.module_author?.let { author ->
                DynamicAuthorModule(
                    mid = author.mid,
                    name = author.name,
                    face = author.face,
                    pub_time = author.pub_time,
                    pub_ts = author.pub_ts
                )
            },
            module_dynamic = item.modules.module_dynamic?.let { content ->
                DynamicContentModule(
                    desc = resolveSpaceDynamicContentDesc(content),
                    major = content.major?.let { major ->
                        DynamicMajor(
                            type = major.type,
                            archive = major.archive?.let { archive ->
                                ArchiveMajor(
                                    aid = archive.aid,
                                    bvid = archive.bvid,
                                    title = archive.title,
                                    cover = archive.cover,
                                    desc = archive.desc,
                                    duration_text = archive.duration_text,
                                    stat = ArchiveStat(
                                        play = archive.stat.play,
                                        danmaku = archive.stat.danmaku
                                    ),
                                    badge = archive.badge,
                                    isChargingArc = archive.isChargingArc,
                                    elecArcType = archive.elecArcType,
                                    isUgcpay = archive.isUgcpay,
                                    ugcPay = archive.ugcPay,
                                    ugcPayPreview = archive.ugcPayPreview
                                )
                            },
                            draw = major.draw?.let { draw ->
                                DrawMajor(
                                    id = draw.id,
                                    items = draw.items.map { drawItem ->
                                        DrawItem(
                                            src = drawItem.src,
                                            width = drawItem.width,
                                            height = drawItem.height
                                        )
                                    }
                                )
                            },
                            opus = major.opus?.let { opus ->
                                OpusMajor(
                                    summary = opus.summary?.let { summary ->
                                        OpusSummary(
                                            text = summary.text,
                                            rich_text_nodes = summary.rich_text_nodes.map { it.toDynamicRichTextNode() }
                                        )
                                    },
                                    pics = opus.pics.map { pic ->
                                        OpusPic(
                                            url = pic.src,
                                            width = pic.width,
                                            height = pic.height
                                        )
                                    },
                                    title = opus.title
                                )
                            },
                            article = major.article?.let { article ->
                                ArticleMajor(
                                    id = article.id,
                                    title = article.title,
                                    desc = article.desc,
                                    covers = article.covers,
                                    jump_url = article.jump_url,
                                    label = article.label
                                )
                            }
                        )
                    }
                )
            },
            module_more = item.modules.module_more,
            module_stat = item.modules.module_stat?.let { stat ->
                DynamicStatModule(
                    comment = StatItem(
                        count = stat.comment.count,
                        forbidden = stat.comment.forbidden
                    ),
                    forward = StatItem(
                        count = stat.forward.count,
                        forbidden = stat.forward.forbidden
                    ),
                    like = StatItem(
                        count = stat.like.count,
                        forbidden = stat.like.forbidden
                    )
                )
            }
        )
    )
}
