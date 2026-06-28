package com.android.purebilibili.feature.settings

internal fun resolvePredictiveBackStyleOptions(): List<PlaybackSegmentOption<String>> {
    return listOf(
        PlaybackSegmentOption("scale", "卡片缩放"),
        PlaybackSegmentOption("aosp", "系统跨页"),
        PlaybackSegmentOption("classic", "经典滑出"),
        PlaybackSegmentOption("default", "系统默认"),
    )
}

internal fun resolvePredictiveBackStyleLabel(storageValue: String): String {
    return resolvePredictiveBackStyleOptions()
        .firstOrNull { it.value == storageValue }
        ?.label
        ?: "卡片缩放"
}