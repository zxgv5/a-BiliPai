package com.android.purebilibili.feature.home.components

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TopTabLiquidGlassPolicyTest {

    @Test
    fun `liquid segmented control replaces bespoke top tab glass renderer`() {
        assertTrue(
            shouldTopTabUseLiquidSegmentedControl(
                isLiquidGlassEnabled = true,
                skinPlainStyle = false,
                hasSkinStickerIcons = false,
                forceMaterialUnderline = false
            )
        )
        assertFalse(
            shouldTopTabUseLiquidSegmentedControl(
                isLiquidGlassEnabled = false,
                skinPlainStyle = false,
                hasSkinStickerIcons = false,
                forceMaterialUnderline = false
            )
        )
        assertFalse(
            shouldTopTabUseLiquidSegmentedControl(
                isLiquidGlassEnabled = true,
                skinPlainStyle = true,
                hasSkinStickerIcons = false,
                forceMaterialUnderline = false
            )
        )
    }

    @Test
    fun `outer dock shell preserves page backdrop sampling for matched indicator`() {
        assertTrue(shouldTopTabSegmentedControlUsePageBackdrop(hasOuterChromeSurface = true))
        assertTrue(shouldTopTabSegmentedControlUsePageBackdrop(hasOuterChromeSurface = false))
    }

    @Test
    fun `feed scroll suppresses top segmented indicator until direct interaction`() {
        assertTrue(
            shouldSuppressTopTabSegmentedIndicatorDuringFeedScroll(
                isFeedScrollInProgress = true,
                isInteractionActive = false
            )
        )
        assertFalse(
            shouldSuppressTopTabSegmentedIndicatorDuringFeedScroll(
                isFeedScrollInProgress = true,
                isInteractionActive = true
            )
        )
        assertFalse(
            shouldRenderSegmentedControlIndicatorContentBackdrop(
                liquidGlassEnabled = true,
                isFeedScrollInProgress = true,
                isInteractionActive = false
            )
        )
        assertTrue(
            shouldRenderSegmentedControlIndicatorContentBackdrop(
                liquidGlassEnabled = true,
                isFeedScrollInProgress = true,
                isInteractionActive = true
            )
        )
    }

    @Test
    fun `outer dock shell only owns container glass while segmented capture stays matched`() {
        assertFalse(
            shouldTopTabDrawSegmentedContainerShell(
                liquidGlassEnabled = true,
                hasOuterChromeSurface = true
            )
        )
        assertTrue(
            shouldTopTabDrawSegmentedCaptureBackdropEffects(
                liquidGlassEnabled = true,
                hasOuterChromeSurface = true
            )
        )
        assertTrue(
            shouldTopTabDrawSegmentedContainerShell(
                liquidGlassEnabled = true,
                hasOuterChromeSurface = false
            )
        )
        assertTrue(
            shouldTopTabDrawSegmentedCaptureBackdropEffects(
                liquidGlassEnabled = true,
                hasOuterChromeSurface = false
            )
        )
    }

    @Test
    fun `pager swipe drives indicator position override while tab drag keeps local state`() {
        assertEquals(
            1.32f,
            resolveTopTabLiquidIndicatorPosition(
                pagerPosition = 1.32f,
                dragPosition = 0f,
                dragActive = false,
                pagerInteractionActive = true
            )!!,
            0.001f
        )
        assertNull(
            resolveTopTabLiquidIndicatorPosition(
                pagerPosition = 1.32f,
                dragPosition = 0.8f,
                dragActive = true,
                pagerInteractionActive = true
            )
        )
        assertNull(
            resolveTopTabLiquidIndicatorPosition(
                pagerPosition = 1.32f,
                dragPosition = 1.32f,
                dragActive = false,
                pagerInteractionActive = false
            )
        )
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }

    @Test
    fun `top tab liquid segmented row reuses bottom bar segmented control`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt"
        )
        val liquidBlock = source
            .substringAfter("private fun HomeTopTabLiquidSegmentedTabs(")
            .substringBefore("@Composable\nprivate fun LightweightHomeTopTabs(")

        assertTrue(liquidBlock.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(liquidBlock.contains("drawContainerShell = drawContainerShell"))
        assertTrue(liquidBlock.contains("drawCaptureBackdropEffects = drawCaptureBackdropEffects"))
        assertTrue(liquidBlock.contains("indicatorPositionOverride = indicatorPositionOverride"))
        assertTrue(liquidBlock.contains("resolveMd3TopTabLayoutVisibleSlots("))
        assertTrue(liquidBlock.contains("itemCategoryKeys = categoryKeys"))
        assertTrue(liquidBlock.contains("showIcon = showIcon"))
        assertTrue(liquidBlock.contains("showText = showText"))
        assertTrue(liquidBlock.contains("topTabLabelMode = normalizedLabelMode"))
        assertTrue(liquidBlock.contains("shouldTopTabSegmentedControlUsePageBackdrop("))
        assertTrue(liquidBlock.contains("segmentedMiuixBackdrop"))
        assertTrue(liquidBlock.contains("isFeedScrollInProgress = isFeedScrollInProgress"))
        assertTrue(liquidBlock.contains("externalInteractionActive = pagerInteractionActive"))
        assertTrue(
            source.contains("shouldTopTabUseLiquidSegmentedControl(") &&
                source.contains("HomeTopTabLiquidSegmentedTabs(")
        )
    }

    @Test
    fun `segmented control exposes shell and capture switches for top dock deduplication`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarLiquidSegmentedControl.kt"
        )

        assertTrue(source.contains("drawContainerShell: Boolean = true"))
        assertTrue(source.contains("drawCaptureBackdropEffects: Boolean = true"))
        assertTrue(source.contains("if (drawContainerShell) {"))
        assertTrue(source.contains("drawCaptureBackdropEffects &&"))
        assertTrue(source.contains("showIcon: Boolean = false"))
        assertTrue(source.contains("resolveTopTabCategoryIcon("))
    }
}
