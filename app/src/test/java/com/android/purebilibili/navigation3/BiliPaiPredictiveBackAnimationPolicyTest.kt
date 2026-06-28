package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation3.predictiveback.BiliPaiAospCrossActivityPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.BiliPaiClassicPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.BiliPaiDefaultPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.BiliPaiDisabledPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationStyle
import com.android.purebilibili.navigation3.predictiveback.BiliPaiScalePredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.BiliPaiSharedElementPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.resolveBiliPaiPredictiveBackAnimationHandler
import kotlin.test.Test
import kotlin.test.assertTrue

class BiliPaiPredictiveBackAnimationPolicyTest {

    @Test
    fun sharedElementRoute_usesSharedElementHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT,
            style = BiliPaiPredictiveBackAnimationStyle.AOSP,
        )
        assertTrue(handler is BiliPaiSharedElementPredictiveBackAnimation)
    }

    @Test
    fun classicCardRoute_usesScaleHandlerByDefault() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.CLASSIC_CARD,
        )
        assertTrue(handler is BiliPaiScalePredictiveBackAnimation)
    }

    @Test
    fun classicCardRoute_aospStyle_usesAospHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.CLASSIC_CARD,
            style = BiliPaiPredictiveBackAnimationStyle.AOSP,
        )
        assertTrue(handler is BiliPaiAospCrossActivityPredictiveBackAnimation)
    }

    @Test
    fun classicCardRoute_classicStyle_usesClassicHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.CLASSIC_CARD,
            style = BiliPaiPredictiveBackAnimationStyle.CLASSIC,
        )
        assertTrue(handler is BiliPaiClassicPredictiveBackAnimation)
    }

    @Test
    fun disabledClassicCard_usesDisabledHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.CLASSIC_CARD,
            predictiveBackEnabled = false,
        )
        assertTrue(handler is BiliPaiDisabledPredictiveBackAnimation)
    }

    @Test
    fun disabledSharedElementRoute_keepsSharedElementHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT,
            predictiveBackEnabled = false,
        )
        assertTrue(handler is BiliPaiSharedElementPredictiveBackAnimation)
    }

    @Test
    fun fallbackRoute_usesDefaultHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.FALLBACK,
        )
        assertTrue(handler is BiliPaiDefaultPredictiveBackAnimation)
    }
}