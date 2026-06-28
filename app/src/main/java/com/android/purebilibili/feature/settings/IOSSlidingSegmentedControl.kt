package com.android.purebilibili.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.adaptiveSquircleBackground
import com.android.purebilibili.feature.home.components.BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP
import com.android.purebilibili.feature.home.components.BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import top.yukonga.miuix.kmp.blur.Backdrop as MiuixBackdrop
import top.yukonga.miuix.kmp.basic.TabRow as MiuixTabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults as MiuixTabRowDefaults

internal fun resolveMd3SegmentedLabelFontSizeSp(
    optionCount: Int,
    longestLabelLength: Int
): Float {
    return when {
        optionCount >= 5 -> 13f
        optionCount >= 4 && longestLabelLength >= 6 -> 14f
        optionCount >= 4 -> 15f
        longestLabelLength >= 8 -> 14f
        else -> 16f
    }
}

internal data class Md3SegmentedControlColorTokens(
    val outerContainerColor: Color,
    val activeContainerColor: Color,
    val activeContentColor: Color,
    val inactiveContentColor: Color
)

internal enum class IosSlidingSegmentedControlChrome {
    LIQUID_INDICATOR,
    MD3_SEGMENTED
}

internal fun resolveIosSlidingSegmentedControlChrome(
    uiPreset: UiPreset,
    androidNativeLiquidGlassEnabled: Boolean
): IosSlidingSegmentedControlChrome {
    return if (uiPreset == UiPreset.MD3 && !androidNativeLiquidGlassEnabled) {
        IosSlidingSegmentedControlChrome.MD3_SEGMENTED
    } else {
        IosSlidingSegmentedControlChrome.LIQUID_INDICATOR
    }
}

internal fun resolveMd3SegmentedControlColorTokens(
    androidNativeVariant: AndroidNativeVariant,
    materialPrimaryContainer: Color,
    materialOnPrimaryContainer: Color,
    materialSurfaceContainerHigh: Color,
    materialOnSurfaceVariant: Color,
    miuixSecondaryContainer: Color,
    miuixOnSecondaryContainer: Color,
    miuixSurfaceContainerHigh: Color,
    miuixOnSurfaceVariantSummary: Color
): Md3SegmentedControlColorTokens {
    return if (androidNativeVariant == AndroidNativeVariant.MATERIAL3) {
        Md3SegmentedControlColorTokens(
            outerContainerColor = materialSurfaceContainerHigh,
            activeContainerColor = materialPrimaryContainer,
            activeContentColor = materialOnPrimaryContainer,
            inactiveContentColor = materialOnSurfaceVariant
        )
    } else {
        Md3SegmentedControlColorTokens(
            outerContainerColor = miuixSurfaceContainerHigh,
            activeContainerColor = miuixSecondaryContainer,
            activeContentColor = miuixOnSecondaryContainer,
            inactiveContentColor = miuixOnSurfaceVariantSummary
        )
    }
}

@Composable
internal fun <T> IOSSlidingSegmentedSetting(
    title: String,
    options: List<PlaybackSegmentOption<T>>,
    selectedValue: T,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onSelectionChange: (T) -> Unit
) {
    val uiPreset = LocalUiPreset.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = if (uiPreset == UiPreset.MD3) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = AppSurfaceTokens.onSurface()
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppSurfaceTokens.onSurfaceVariantSummary()
            )
        }
        IOSSlidingSegmentedControl(
            options = options,
            selectedValue = selectedValue,
            enabled = enabled,
            onSelectionChange = onSelectionChange
        )
    }
}

@Composable
internal fun <T> IOSSlidingSegmentedControl(
    options: List<PlaybackSegmentOption<T>>,
    selectedValue: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    forceLiquidIndicator: Boolean = false,
    height: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP.dp,
    indicatorHeight: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP.dp,
    labelFontSize: TextUnit = 14.sp,
    miuixBackdrop: MiuixBackdrop? = null,
    tapPressRefractionEnabled: Boolean = true,
    onSelectionChange: (T) -> Unit
) {
    if (options.isEmpty()) return
    val uiPreset = LocalUiPreset.current
    val context = LocalContext.current
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val effectiveAndroidNativeLiquidGlassEnabled =
        forceLiquidIndicator || homeSettings.androidNativeLiquidGlassEnabled
    val chrome = remember(uiPreset, effectiveAndroidNativeLiquidGlassEnabled) {
        resolveIosSlidingSegmentedControlChrome(
            uiPreset = uiPreset,
            androidNativeLiquidGlassEnabled = effectiveAndroidNativeLiquidGlassEnabled
        )
    }
    if (chrome == IosSlidingSegmentedControlChrome.MD3_SEGMENTED) {
        Md3SegmentedControl(
            options = options,
            selectedValue = selectedValue,
            modifier = modifier,
            enabled = enabled,
            onSelectionChange = onSelectionChange
        )
        return
    }
    IOSSlidingSegmentedControlImpl(
        options = options,
        selectedValue = selectedValue,
        modifier = modifier,
        enabled = enabled,
        forceLiquidIndicator = forceLiquidIndicator,
        height = height,
        indicatorHeight = indicatorHeight,
        labelFontSize = labelFontSize,
        miuixBackdrop = miuixBackdrop,
        tapPressRefractionEnabled = tapPressRefractionEnabled,
        onSelectionChange = onSelectionChange
    )
}

@Composable
private fun <T> Md3SegmentedControl(
    options: List<PlaybackSegmentOption<T>>,
    selectedValue: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSelectionChange: (T) -> Unit
) {
    val androidNativeVariant = LocalAndroidNativeVariant.current
    val materialColorScheme = MaterialTheme.colorScheme
    val colorTokens = resolveMd3SegmentedControlColorTokens(
        androidNativeVariant = androidNativeVariant,
        materialPrimaryContainer = materialColorScheme.primaryContainer,
        materialOnPrimaryContainer = materialColorScheme.onPrimaryContainer,
        materialSurfaceContainerHigh = materialColorScheme.surfaceContainerHigh,
        materialOnSurfaceVariant = materialColorScheme.onSurfaceVariant,
        miuixSecondaryContainer = AppSurfaceTokens.secondaryContainer(),
        miuixOnSecondaryContainer = AppSurfaceTokens.onSecondaryContainer(),
        miuixSurfaceContainerHigh = AppSurfaceTokens.surfaceContainerHigh(),
        miuixOnSurfaceVariantSummary = AppSurfaceTokens.onSurfaceVariantSummary()
    )
    val uiPreset = LocalUiPreset.current
    val pillCornerRadius = AppShapes.resolveContainerCornerDp(
        level = ContainerLevel.Pill,
        uiPreset = uiPreset,
        androidNativeVariant = androidNativeVariant
    )
    when (resolveMd3SegmentedControlRenderer(androidNativeVariant)) {
        Md3SegmentedControlRenderer.MIUIX_TAB_ROW -> {
            MiuixTabRowSegmentedControl(
                options = options,
                selectedValue = selectedValue,
                enabled = enabled,
                colorTokens = colorTokens,
                pillCornerRadius = pillCornerRadius,
                modifier = modifier,
                onSelectionChange = onSelectionChange
            )
        }
        Md3SegmentedControlRenderer.MATERIAL_SEGMENTED_BUTTONS -> {
            MaterialMd3SegmentedControl(
                options = options,
                selectedValue = selectedValue,
                enabled = enabled,
                colorTokens = colorTokens,
                pillCornerRadius = pillCornerRadius,
                modifier = modifier,
                onSelectionChange = onSelectionChange
            )
        }
    }
}

@Composable
private fun <T> MiuixTabRowSegmentedControl(
    options: List<PlaybackSegmentOption<T>>,
    selectedValue: T,
    enabled: Boolean,
    colorTokens: Md3SegmentedControlColorTokens,
    pillCornerRadius: Dp,
    modifier: Modifier = Modifier,
    onSelectionChange: (T) -> Unit
) {
    val selectedIndex = resolveSelectionIndex(options = options, selectedValue = selectedValue)
    val tabRowColors = resolveMiuixSegmentedTabRowColors(colorTokens)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .adaptiveSquircleBackground(
                color = colorTokens.outerContainerColor,
                cornerRadius = pillCornerRadius
            )
            .padding(4.dp)
    ) {
        MiuixTabRow(
            tabs = options.map { it.label },
            selectedTabIndex = selectedIndex,
            onTabSelected = { index ->
                if (!enabled) return@MiuixTabRow
                options.getOrNull(index)?.let { option ->
                    onSelectionChange(option.value)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = MiuixTabRowDefaults.tabRowColors(
                backgroundColor = tabRowColors.backgroundColor,
                contentColor = tabRowColors.contentColor,
                selectedBackgroundColor = tabRowColors.selectedBackgroundColor,
                selectedContentColor = tabRowColors.selectedContentColor
            ),
            height = 40.dp,
            cornerRadius = pillCornerRadius,
            itemSpacing = 4.dp
        )
    }
}

@Composable
private fun <T> MaterialMd3SegmentedControl(
    options: List<PlaybackSegmentOption<T>>,
    selectedValue: T,
    enabled: Boolean,
    colorTokens: Md3SegmentedControlColorTokens,
    pillCornerRadius: Dp,
    modifier: Modifier = Modifier,
    onSelectionChange: (T) -> Unit
) {
    val longestLabelLength = remember(options) {
        options.maxOfOrNull { it.label.length } ?: 0
    }
    val labelFontSize = remember(options.size, longestLabelLength) {
        resolveMd3SegmentedLabelFontSizeSp(
            optionCount = options.size,
            longestLabelLength = longestLabelLength
        ).sp
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .adaptiveSquircleBackground(
                color = colorTokens.outerContainerColor,
                cornerRadius = pillCornerRadius
            )
            .padding(4.dp)
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option.value == selectedValue,
                    onClick = { onSelectionChange(option.value) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = colorTokens.activeContainerColor,
                        activeContentColor = colorTokens.activeContentColor,
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = colorTokens.inactiveContentColor,
                        disabledActiveContainerColor = colorTokens.activeContainerColor.copy(alpha = 0.35f),
                        disabledActiveContentColor = colorTokens.activeContentColor.copy(alpha = 0.55f),
                        disabledInactiveContainerColor = Color.Transparent,
                        disabledInactiveContentColor = colorTokens.inactiveContentColor.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.weight(1f),
                    icon = {}
                ) {
                    Text(
                        text = option.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = labelFontSize),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> IOSSlidingSegmentedControlImpl(
    options: List<PlaybackSegmentOption<T>>,
    selectedValue: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    forceLiquidIndicator: Boolean = false,
    height: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP.dp,
    indicatorHeight: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP.dp,
    labelFontSize: TextUnit = 14.sp,
    miuixBackdrop: MiuixBackdrop? = null,
    tapPressRefractionEnabled: Boolean = true,
    onSelectionChange: (T) -> Unit
) {
    val selectedIndex = resolveSelectionIndex(options = options, selectedValue = selectedValue)
    BottomBarLiquidSegmentedControl(
        items = options.map { it.label },
        selectedIndex = selectedIndex,
        onSelected = { index ->
            options.getOrNull(index)?.let { option ->
                onSelectionChange(option.value)
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        height = height,
        indicatorHeight = indicatorHeight,
        labelFontSize = labelFontSize,
        miuixBackdrop = miuixBackdrop,
        forceLiquidChrome = forceLiquidIndicator,
        liquidGlassRequestedEnabled = if (forceLiquidIndicator) true else null,
        tapPressRefractionEnabled = tapPressRefractionEnabled
    )
}
