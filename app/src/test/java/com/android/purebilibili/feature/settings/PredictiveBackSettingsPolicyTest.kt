package com.android.purebilibili.feature.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PredictiveBackSettingsPolicyTest {

    @Test
    fun styleOptions_includeAllSupportedStyles() {
        val values = resolvePredictiveBackStyleOptions().map { it.value }.toSet()
        assertEquals(setOf("scale", "aosp", "classic", "default"), values)
    }

    @Test
    fun styleLabel_resolvesKnownValues() {
        assertEquals("卡片缩放", resolvePredictiveBackStyleLabel("scale"))
        assertEquals("系统跨页", resolvePredictiveBackStyleLabel("aosp"))
        assertEquals("经典滑出", resolvePredictiveBackStyleLabel("classic"))
        assertEquals("系统默认", resolvePredictiveBackStyleLabel("default"))
    }

    @Test
    fun styleLabel_fallsBackForUnknownValue() {
        assertEquals("卡片缩放", resolvePredictiveBackStyleLabel("unknown"))
    }
}