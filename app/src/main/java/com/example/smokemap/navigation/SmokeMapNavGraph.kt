package com.example.smokemap.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smokemap.ui.detail.SpotDetailScreen
import com.example.smokemap.ui.favorites.FavoritesScreen
import com.example.smokemap.ui.map.MapScreen

@Composable
fun SmokeMapNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MAP) {
        composable(Screen.MAP) {
            MapScreen(
                onSpotClick = { spotId ->
                    navController.navigate(Screen.spotDetail(spotId))
                },
                onFavoritesClick = {
                    navController.navigate(Screen.FAVORITES)
                }
            )
        }

        composable(
            route = Screen.SPOT_DETAIL,
            arguments = listOf(navArgument("spotId") { type = NavType.StringType })
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getString("spotId") ?: return@composable
            SpotDetailScreen(
                spotId = spotId,
                onBack = { navController.popBackStack() }
            )
        }

composable(Screen.FAVORITES) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onSpotClick = { spotId ->
                    navController.navigate(Screen.spotDetail(spotId))
                }
            )
        }
    }
}
