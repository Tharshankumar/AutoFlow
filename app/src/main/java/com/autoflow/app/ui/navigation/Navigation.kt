package com.autoflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autoflow.app.ui.screens.HomeScreen
import com.autoflow.app.ui.screens.RoutineBuilderScreen
import com.autoflow.app.ui.screens.RoutineDetailScreen
import com.autoflow.app.ui.viewmodel.RoutineViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object RoutineBuilder : Screen("routine_builder")
    data object RoutineDetail : Screen("routine_detail/{routineId}") {
        fun createRoute(routineId: Long) = "routine_detail/$routineId"
    }
}

@Composable
fun AutoFlowNavigation() {
    val navController = rememberNavController()
    val viewModel: RoutineViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onCreateRoutine = {
                    navController.navigate(Screen.RoutineBuilder.route)
                },
                onRoutineClick = { routineId ->
                    navController.navigate(Screen.RoutineDetail.createRoute(routineId))
                }
            )
        }

        composable(Screen.RoutineBuilder.route) {
            RoutineBuilderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RoutineDetail.route,
            arguments = listOf(
                navArgument("routineId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: return@composable
            RoutineDetailScreen(
                viewModel = viewModel,
                routineId = routineId,
                onBack = { navController.popBackStack() },
                onDelete = { navController.popBackStack() }
            )
        }
    }
}
