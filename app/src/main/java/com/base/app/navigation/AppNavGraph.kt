package com.base.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.base.app.core.navigation.core.LocalNavController
import com.base.app.core.navigation.core.applyDestination
import com.base.app.core.navigation.destination.Destination
import com.base.app.feature.main.presentation.language.LanguageScreenProvider
import com.base.app.feature.main.presentation.main.MainScreenProvider
import com.base.app.feature.main.presentation.postdetail.PostDetailScreenProvider

private const val NAV_ANIM_DURATION = 300
private const val NAV_ANIM_OFFSET_FRACTION = 0.1f

/**
 * Nơi đăng ký mọi destination của app: mỗi màn là một `ScreenProvider` (nằm cạnh màn trong
 * `feature/<name>/presentation/<screen>/`). Thêm màn = thêm một dòng `applyDestination`.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Destination = MainScreenProvider.Main(),
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { (it * NAV_ANIM_OFFSET_FRACTION).toInt() },
                    animationSpec = tween(NAV_ANIM_DURATION)
                ) + fadeIn(animationSpec = tween(NAV_ANIM_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -(it * NAV_ANIM_OFFSET_FRACTION).toInt() },
                    animationSpec = tween(NAV_ANIM_DURATION)
                ) + fadeOut(animationSpec = tween(NAV_ANIM_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -(it * NAV_ANIM_OFFSET_FRACTION).toInt() },
                    animationSpec = tween(NAV_ANIM_DURATION)
                ) + fadeIn(animationSpec = tween(NAV_ANIM_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { (it * NAV_ANIM_OFFSET_FRACTION).toInt() },
                    animationSpec = tween(NAV_ANIM_DURATION)
                ) + fadeOut(animationSpec = tween(NAV_ANIM_DURATION))
            }
        ) {
            applyDestination(MainScreenProvider())
            applyDestination(PostDetailScreenProvider())
            applyDestination(LanguageScreenProvider())
        }
    }
}
