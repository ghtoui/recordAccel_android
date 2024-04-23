package com.moritoui.recordaccel.navigation

import MainScreen
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moritoui.recordaccel.ui.DetailScreen

sealed class Screen(
    val route: String,
) {
    object MainScreen : Screen("mainScreen")
    object DetailScreen : Screen("detailScreen")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.MainScreen.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth / 2 }, animationSpec = tween()) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth / 2 }, animationSpec = tween()) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth / 2 }, animationSpec = tween()) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth / 2 }, animationSpec = tween()) },
    ) {
        composable(
            Screen.MainScreen.route,
        ) {
            MainScreen(
                viewModel = hiltViewModel(),
                popUp = {
                    navController.navigate(Screen.DetailScreen.route)
                },
            )
        }
        composable(
            Screen.DetailScreen.route,
        ) {
            DetailScreen(
                viewModel = hiltViewModel(),
            )
        }
    }
}
