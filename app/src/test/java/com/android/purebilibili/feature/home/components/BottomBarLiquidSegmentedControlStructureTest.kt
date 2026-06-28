package com.android.purebilibili.feature.home.components

import java.io.File
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BottomBarLiquidSegmentedControlStructureTest {

    @Test
    fun `segmented indicator keeps slot width so content remains centered`() {
        val width = resolveSegmentedControlIndicatorWidthDp(
            slotWidthDp = 60f,
            indicatorHeightDp = 56f,
            itemCount = 5
        )

        assertEquals(60f, width)
    }

    @Test
    fun `segmented indicator reduces height for cramped slots to stay capsule shaped`() {
        assertEquals(
            37.5f,
            resolveSegmentedControlIndicatorHeightDp(
                slotWidthDp = 60f,
                indicatorHeightDp = 56f,
            )
        )
    }

    @Test
    fun `segmented indicator keeps full height for already wide home slots`() {
        assertEquals(
            56f,
            resolveSegmentedControlIndicatorHeightDp(
                slotWidthDp = 128f,
                indicatorHeightDp = 56f,
            )
        )
    }

    @Test
    fun `segmented indicator offset follows slot position without clamping dead zone`() {
        assertEquals(
            4f,
            resolveSegmentedControlIndicatorOffsetDp(
                position = 0f,
                slotWidthDp = 60f,
                contentPaddingDp = 4f,
            )
        )
        assertEquals(
            34f,
            resolveSegmentedControlIndicatorOffsetDp(
                position = 0.5f,
                slotWidthDp = 60f,
                contentPaddingDp = 4f,
            )
        )
        assertEquals(
            244f,
            resolveSegmentedControlIndicatorOffsetDp(
                position = 4f,
                slotWidthDp = 60f,
                contentPaddingDp = 4f,
            )
        )
    }

    @Test
    fun `segmented control only follows continuous drag when touch starts on indicator`() {
        assertTrue(
            shouldFollowSegmentedControlIndicatorDrag(
                pointerX = 132f,
                indicatorPosition = 2f,
                itemWidthPx = 64f
            )
        )
        assertFalse(
            shouldFollowSegmentedControlIndicatorDrag(
                pointerX = 80f,
                indicatorPosition = 2f,
                itemWidthPx = 64f
            )
        )
        assertFalse(
            shouldFollowSegmentedControlIndicatorDrag(
                pointerX = 196.1f,
                indicatorPosition = 2f,
                itemWidthPx = 64f
            )
        )
    }

    @Test
    fun `segmented control sweep release resolves label without requiring indicator follow`() {
        assertEquals(
            0,
            resolveSegmentedControlSweepSelectionIndex(
                pointerX = -12f,
                itemWidthPx = 64f,
                itemCount = 4
            )
        )
        assertEquals(
            1,
            resolveSegmentedControlSweepSelectionIndex(
                pointerX = 82f,
                itemWidthPx = 64f,
                itemCount = 4
            )
        )
        assertEquals(
            3,
            resolveSegmentedControlSweepSelectionIndex(
                pointerX = 260f,
                itemWidthPx = 64f,
                itemCount = 4
            )
        )
    }

    @Test
    fun `inline segmented control uses same neutral idle indicator color as dock`() {
        val themeColor = androidx.compose.ui.graphics.Color(0xFF00A1D6)
        val inlineIndicator = resolveSegmentedControlIndicatorIdleSurfaceColor(
            isDarkTheme = false
        )
        val dockedIndicator = resolveSegmentedControlIndicatorIdleSurfaceColor(
            isDarkTheme = false
        )

        assertEquals(
            resolveBottomBarIdleIndicatorSurfaceColor(darkTheme = false).red,
            inlineIndicator.red,
            0.001f
        )
        assertEquals(
            resolveBottomBarIdleIndicatorSurfaceColor(darkTheme = false).red,
            dockedIndicator.red,
            0.001f
        )
        assertFalse(
            resolveAndroidNativeIndicatorColor(themeColor = themeColor, darkTheme = false).red ==
                inlineIndicator.red
        )
        assertTrue(shouldRenderSegmentedControlHiddenCaptureLayer(liquidGlassEnabled = true))
        assertFalse(shouldRenderSegmentedControlHiddenCaptureLayer(liquidGlassEnabled = false))
        assertTrue(shouldRenderSegmentedControlExportCapture(
            liquidGlassEnabled = true,
            hasExternalBackdrop = false
        ))
        assertTrue(shouldRenderSegmentedControlExportCapture(
            liquidGlassEnabled = true,
            hasExternalBackdrop = true
        ))
        assertFalse(shouldApplySegmentedControlExportThemeTint(hasExternalBackdrop = false))
        assertTrue(shouldApplySegmentedControlExportThemeTint(hasExternalBackdrop = true))
        assertFalse(
            shouldApplySegmentedControlCaptureBackdropEffects(
                hasExternalBackdrop = false,
                drawCaptureBackdropEffects = true,
                liquidGlassEnabled = true
            )
        )
        assertTrue(
            shouldApplySegmentedControlCaptureBackdropEffects(
                hasExternalBackdrop = true,
                drawCaptureBackdropEffects = true,
                liquidGlassEnabled = true
            )
        )
    }

    @Test
    fun `feed scroll suppresses segmented indicator backdrop until direct interaction`() {
        assertFalse(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = true,
                motionProgress = 0.5f,
                hasExternalBackdrop = false,
                isFeedScrollInProgress = true,
                isInteractionActive = false
            )
        )
        assertTrue(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = true,
                motionProgress = 0.5f,
                hasExternalBackdrop = false,
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
    }

    @Test
    fun `segmented indicator only samples hidden tab backdrop while sliding without external backdrop`() {
        assertFalse(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = true,
                motionProgress = 0f,
                hasExternalBackdrop = false
            )
        )
        assertTrue(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = true,
                motionProgress = 0.01f,
                hasExternalBackdrop = false
            )
        )
        assertTrue(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = true,
                motionProgress = 0f,
                hasExternalBackdrop = true
            )
        )
        assertFalse(
            shouldDrawSegmentedControlIndicatorBackdrop(
                liquidGlassEnabled = false,
                motionProgress = 1f,
                hasExternalBackdrop = true
            )
        )
    }

    @Test
    fun `android native inline segmented control keeps liquid pill when global glass is enabled`() {
        assertEquals(
            SegmentedControlChromeStyle.LIQUID_PILL,
            resolveSegmentedControlChromeStyle(
                uiPreset = UiPreset.MD3,
                androidNativeLiquidGlassEnabled = true,
                preferInlineContentStyle = true
            )
        )
    }

    @Test
    fun `android native inline segmented control still uses underline when global glass is disabled`() {
        assertEquals(
            SegmentedControlChromeStyle.ANDROID_NATIVE_UNDERLINE,
            resolveSegmentedControlChromeStyle(
                uiPreset = UiPreset.MD3,
                androidNativeLiquidGlassEnabled = false,
                preferInlineContentStyle = true
            )
        )
    }

    @Test
    fun `android native chrome segmented control keeps liquid pill when global glass is enabled`() {
        assertEquals(
            SegmentedControlChromeStyle.LIQUID_PILL,
            resolveSegmentedControlChromeStyle(
                uiPreset = UiPreset.MD3,
                androidNativeLiquidGlassEnabled = true,
                preferInlineContentStyle = false
            )
        )
    }

    @Test
    fun `segmented control keeps sliding glass by default with opt out flag`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarLiquidSegmentedControl.kt"
        )

        assertTrue(source.contains("BottomBarMotionProfile.ANDROID_NATIVE_FLOATING"))
        assertFalse(source.contains("BottomBarMotionProfile.IOS_FLOATING"))
        assertTrue(source.contains("resolveBottomBarRefractionMotionProfile("))
        assertTrue(source.contains(".kernelSuMiuixFloatingDockSurface("))
        assertTrue(source.contains("blurRadius = androidNativeTuning.shellBlurRadiusDp.dp"))
        assertTrue(source.contains("miuixBlur(4.dp.toPx(), 4.dp.toPx())"))
        assertFalse(source.contains("blur(8.dp.toPx())"))
        assertFalse(source.contains(".border("))
        assertTrue(source.contains("BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP = 58"))
        assertTrue(source.contains("BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP = 56"))
        assertTrue(source.contains("dragState.dragOffset / itemWidthPx"))
        assertTrue(source.contains("resolveBottomBarItemMotionVisual("))
        assertTrue(source.contains("rememberMiuixCombinedBackdrop("))
        assertTrue(source.contains("miuixDrawBackdrop("))
        assertTrue(source.contains("resolveBottomBarBackdropPresetProgress("))
        assertTrue(source.contains("resolveBottomBarBackdropPresetCaptureLens("))
        assertTrue(source.contains("resolveBottomBarBackdropPresetIndicatorLens("))
        assertTrue(source.contains("resolveBottomBarLiquidGlassHighlightAlpha("))
        assertFalse(source.contains("Highlight.Default.copy(alpha = captureHighlightAlpha)"))
        assertFalse(source.contains("Shadow(alpha = indicatorGlowAlpha)"))
        assertTrue(source.contains("rememberBottomBarIndicatorDragScaleProgress("))
        assertTrue(source.contains("KernelSuMiuixBottomBarIndicatorLayer("))
        assertTrue(source.contains("indicatorLayerScaleProgress = indicatorLayerScaleProgress"))
        assertFalse(source.contains("dragScaleProgress = maxOf(motionProgress, tapPressProgress)"))
        assertFalse(source.contains("val indicatorScale = lerp(1f, 78f / 56f, motionProgress)"))
        assertFalse(source.contains("velocity = dragState.velocity / 10f"))
        assertFalse(source.contains("resolveIosFloatingBottomIndicatorColor("))
        assertFalse(source.contains("resolveIosFloatingBottomIndicatorTintAlpha("))
        assertFalse(source.contains("resolveLiquidSegmentedIndicatorColor("))
        assertTrue(source.contains("liquidGlassEffectsEnabled: Boolean = true"))
        assertTrue(source.contains("liquidGlassRequestedEnabled: Boolean? = null"))
        assertTrue(source.contains("dragSelectionEnabled: Boolean = true"))
        assertFalse(source.contains("shellBackdrop"))
        assertTrue(source.contains("val tabsBackdrop = rememberMiuixLayerBackdrop()"))
        assertTrue(source.contains(".miuixLayerBackdrop(tabsBackdrop)"))
        assertTrue(source.contains("val exportTintColor = resolveAndroidNativeExportTintColor("))
        assertTrue(source.contains(".graphicsLayer(colorFilter = ColorFilter.tint(exportTintColor))"))
        assertTrue(source.contains("val hasExternalBackdrop = miuixBackdrop != null"))
        assertTrue(source.contains("!shouldRenderIndicatorContentBackdrop -> null"))
        assertTrue(source.contains("hasExternalBackdrop ->"))
        assertTrue(source.contains("liquidGlassEnabled -> tabsBackdrop"))
        assertTrue(source.contains("rememberMiuixCombinedBackdrop(miuixBackdrop, tabsBackdrop)"))
        assertTrue(source.contains("shouldRenderSegmentedControlIndicatorContentBackdrop("))
        assertTrue(source.contains("externalInteractionActive: Boolean = false"))
        assertTrue(source.contains("isFeedScrollInProgress: Boolean = false"))
        assertTrue(source.contains("shouldRenderSegmentedControlHiddenCaptureLayer("))
        assertTrue(source.contains("shouldApplySegmentedControlExportThemeTint("))
        assertTrue(source.contains("shouldApplySegmentedControlCaptureBackdropEffects("))
        assertTrue(source.contains("resolveSegmentedControlIndicatorIdleSurfaceColor("))
        assertTrue(source.contains("background(containerColor, containerShape)"))
        assertTrue(source.contains("shouldDrawSegmentedControlIndicatorBackdrop("))
        assertFalse(source.contains("val contentBackdrop = if (backdrop != null) combinedBackdrop else null"))
        assertFalse(source.contains("if (liquidGlassEnabled && contentBackdrop != null)"))
        assertFalse(source.contains("val useIndicatorBackdrop = liquidGlassEnabled && indicatorVisualPolicy.shouldRefract"))
        assertFalse(source.contains("LiquidIndicator("))
        assertFalse(source.contains("backdrop = indicatorBackdrop"))
        assertTrue(source.contains("chromaticAberration = 0.5f"))
        assertTrue(source.contains("miuixBackdrop: MiuixBackdrop? = null"))
        assertTrue(source.contains("getHomeSettings("))
        assertTrue(source.contains("val requestedLiquidGlassEnabled = liquidGlassRequestedEnabled"))
        assertTrue(source.contains("?: homeSettings.isBottomBarLiquidGlassEnabled"))
        assertTrue(source.contains("resolveEffectiveLiquidGlassEnabled("))
        assertTrue(source.contains("resolveSegmentedControlChromeStyle("))
        assertTrue(source.contains("AndroidNativeUnderlinedSegmentedControl("))
        assertTrue(source.contains("SegmentedControlChromeStyle.ANDROID_NATIVE_UNDERLINE"))
        assertTrue(source.contains("onIndicatorPositionChanged?.invoke(safeSelectedIndex.toFloat())"))
        assertTrue(source.contains(".widthIn(min = 28.dp, max = 56.dp)"))
        assertTrue(source.contains("if (enabled && itemCount > 1 && dragSelectionEnabled)"))
        assertTrue(source.contains("Modifier.segmentedControlSelectionGesture("))
        assertTrue(source.contains("change.consume()"))
        assertTrue(source.contains("shouldFollowIndicatorFrom = { downX ->"))
        assertTrue(source.contains("shouldFollowSegmentedControlIndicatorDrag("))
        assertTrue(source.contains("onSweepSelected = { index ->"))
        assertTrue(source.contains("resolveSegmentedControlSweepSelectionIndex("))
        assertTrue(source.contains("notifyIndexChanged = true"))
        assertTrue(source.contains("settleIndex = null"))
        assertTrue(source.contains("onPressChanged = dragState::setPressed"))
        assertFalse(source.contains("indicatorEffectProgress"))
        assertFalse(source.contains("backdrop = if (shouldRefractContent)"))
        assertFalse(source.contains("backdrop = shellBackdrop"))
        assertFalse(source.contains(".clip(containerShape)"))
        assertFalse(source.contains(".clip(indicatorShape)"))
        assertTrue(source.contains("resolveSegmentedControlIndicatorWidthDp("))
        assertTrue(source.contains("resolveSegmentedControlIndicatorHeightDp("))
        assertTrue(source.contains("resolveSegmentedControlIndicatorOffsetDp("))
        assertTrue(source.contains("shouldDrawSegmentedControlIndicatorBackdrop("))
        assertTrue(source.contains("val indicatorShape = resolveSharedBottomBarCapsuleShape()"))
        assertTrue(source.contains("val containerShape = indicatorShape"))
        assertTrue(source.contains("shellShape = indicatorShape"))
        assertTrue(source.contains("indicatorTranslationXPx = with(density) { indicatorOffset.toPx() }"))
        assertTrue(source.contains("indicatorWidth = indicatorWidth"))
        assertTrue(source.contains("indicatorHeight = resolvedIndicatorHeight"))
        assertTrue(source.contains("indicatorPanelOffsetPx = panelOffsetPx"))
        assertFalse(source.contains("indicatorSettleReboundTransform = clickPulseTransform"))
        assertFalse(source.contains("scaleX = indicatorTransform.scaleX"))
        assertFalse(source.contains("scaleY = indicatorTransform.scaleY"))
        assertFalse(source.contains("containerWidthDp = maxWidth.value"))
        val indicatorIndex = source.indexOf("KernelSuMiuixBottomBarIndicatorLayer(")
        val visibleLabelsIndex = source.indexOf(
            "selectionEmphasis = refractionMotionProfile.visibleSelectionEmphasis",
            startIndex = indicatorIndex
        )
        assertTrue(indicatorIndex >= 0)
        assertTrue(visibleLabelsIndex > indicatorIndex)
        assertFalse(source.contains("val indicatorPolicy = remember(itemCount)"))
        assertFalse(source.contains("resolveBottomBarIndicatorPolicy(itemCount = itemCount)"))
        assertTrue(source.contains("motionSpec.refraction.panelOffsetMaxDp.dp.toPx()"))
        assertFalse(source.contains("exportPanelOffsetPx"))
        assertTrue(source.contains("indicatorPanelOffsetPx = panelOffsetPx"))
        assertFalse(source.contains("visiblePanelOffsetPx"))
        assertFalse(source.contains("indicatorWidthMultiplier = 1f"))
        assertFalse(source.contains("height: Dp = 42.dp"))
        assertFalse(source.contains("indicatorHeight: Dp = 34.dp"))
        assertFalse(source.contains("indicatorMaxWidth = segmentWidth"))
        assertFalse(source.contains("maxWidthToItemRatio = 1f"))
        assertFalse(source.contains("indicatorWidthMultiplier = 0.92f"))
        assertFalse(source.contains("maxScale = 1.06f"))
        assertFalse(source.contains(".offset(x = segmentWidth * dragState.value)"))
    }

    @Test
    fun `common list and dynamic tabs pass page backdrop into segmented control`() {
        val commonList = loadSource("app/src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt")
        val dynamicScreen = loadSource("app/src/main/java/com/android/purebilibili/feature/dynamic/DynamicScreen.kt")
        val dynamicTopBar = loadSource("app/src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicTopBar.kt")
        val iosSegmented = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/IOSSlidingSegmentedControl.kt")

        assertTrue(commonList.contains("val commonListChromeBackdrop = rememberMiuixLayerBackdrop()"))
        assertTrue(commonList.contains(".miuixLayerBackdrop(commonListChromeBackdrop)"))
        assertTrue(commonList.contains("miuixBackdrop = commonListChromeBackdrop"))
        assertTrue(dynamicScreen.contains("val dynamicChromeBackdrop = rememberMiuixLayerBackdrop()"))
        assertTrue(dynamicScreen.contains(".miuixLayerBackdrop(dynamicChromeBackdrop)"))
        assertTrue(dynamicScreen.contains("miuixBackdrop = dynamicChromeBackdrop"))
        assertTrue(dynamicTopBar.contains("miuixBackdrop: MiuixBackdrop? = null"))
        assertTrue(dynamicTopBar.contains("miuixBackdrop = miuixBackdrop"))
        assertTrue(iosSegmented.contains("miuixBackdrop: MiuixBackdrop? = null"))
        assertTrue(iosSegmented.contains("miuixBackdrop = miuixBackdrop"))
    }

    @Test
    fun `segmented control does not attach drag gesture when drag selection is disabled`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarLiquidSegmentedControl.kt"
        )

        assertTrue(
            source.contains("if (enabled && itemCount > 1 && dragSelectionEnabled)"),
            "Scrollable contribution tabs disable drag selection, so the liquid indicator must not attach a competing horizontal drag gesture"
        )
    }

    @Test
    fun `global video dynamic and live segmented surfaces share android native fallback`() {
        val paths = listOf(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/CommentSortFilterBar.kt",
            "app/src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt",
            "app/src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicTopBar.kt",
            "app/src/main/java/com/android/purebilibili/feature/live/LiveListScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/live/LiveAreaScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/live/LivePlayerScreen.kt"
        )

        paths.forEach { path ->
            assertTrue(
                loadSource(path).contains("BottomBarLiquidSegmentedControl("),
                "$path should keep using BottomBarLiquidSegmentedControl so the global Android native fallback applies"
            )
        }
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
}
