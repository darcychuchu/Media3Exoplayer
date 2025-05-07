package com.vlog.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vlog.player.ui.screens.VideoListScreen
import com.vlog.player.ui.screens.VideoPlayerScreen

// 定义导航路由
object AppDestinations {
    const val VIDEO_LIST_ROUTE = "video_list"
    const val VIDEO_PLAYER_ROUTE = "video_player"
    const val VIDEO_ID_KEY = "videoId"
    
    // 构建带参数的路由
    fun videoPlayerRoute(videoId: String): String {
        return "$VIDEO_PLAYER_ROUTE/$videoId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val actions = remember(navController) { AppNavigationActions(navController) }
    
    NavHost(
        navController = navController,
        startDestination = AppDestinations.VIDEO_LIST_ROUTE
    ) {
        // 视频列表页面
        composable(AppDestinations.VIDEO_LIST_ROUTE) {
            VideoListScreen(onVideoClick = { video ->
                actions.navigateToVideoPlayer(video.id)
            })
        }
        
        // 视频播放页面
        composable(
            route = "${AppDestinations.VIDEO_PLAYER_ROUTE}/{${AppDestinations.VIDEO_ID_KEY}}",
            arguments = listOf(
                navArgument(AppDestinations.VIDEO_ID_KEY) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString(AppDestinations.VIDEO_ID_KEY) ?: ""
            VideoPlayerScreen(
                videoId = videoId,
                onBackClick = { actions.navigateBack() }
            )
        }
    }
}

// 导航动作
class AppNavigationActions(private val navController: NavHostController) {
    
    fun navigateToVideoPlayer(videoId: String) {
        navController.navigate(AppDestinations.videoPlayerRoute(videoId))
    }
    
    fun navigateBack() {
        navController.popBackStack()
    }
}