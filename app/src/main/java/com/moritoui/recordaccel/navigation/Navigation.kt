package com.moritoui.recordaccel.navigation

import MainScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moritoui.recordaccel.ui.DetailScreen

sealed class Screen(
    val route: String
) {
    object MainScreen : Screen("mainScreen")
    object DetailScreen : Screen("detailScreen")
}

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        modifier = Modifier,
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(
                viewModel = hiltViewModel(),
                popUp = { navController.navigate(Screen.DetailScreen.route) }
            )
        }
        composable(Screen.DetailScreen.route) {
            DetailScreen(
                viewModel = hiltViewModel()
            )
        }
    }
}
