package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class AnimationSettingsScreenStructureTest {

    @Test
    fun animationSettingsScreen_exposesPredictiveBackToggleAndStylePicker() {
        val source = animationSettingsSource()

        assertTrue(source.contains("预测性返回"))
        assertTrue(source.contains("setPredictiveBackEnabled"))
        assertTrue(source.contains("setPredictiveBackAnimationStyle"))
        assertTrue(source.contains("resolvePredictiveBackStyleOptions"))
        assertTrue(source.contains("SettingsIconRole.PREDICTIVE_BACK"))
    }

    private fun animationSettingsSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
        ).first { it.exists() }.readText()
    }
}