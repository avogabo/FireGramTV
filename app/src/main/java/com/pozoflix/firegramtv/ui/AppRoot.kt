package com.pozoflix.firegramtv.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pozoflix.firegramtv.ui.screens.*

@Composable
fun AppRoot() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = "home"
    ) {
        // --- Pantalla principal ---
        composable("home") {
            HomeScreen(
                onOpenDetail = { id, isTv ->
                    nav.navigate("detail/$id/$isTv")
                },
                onOpenSettings = {
                    nav.navigate("settings")
                }
            )
        }

        // --- Pantalla de ajustes ---
        composable("settings") {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onRefreshIndex = {
                    nav.popBackStack()
                    nav.navigate("home")
                }
            )
        }

        // --- Detalle de contenido ---
        composable(
            "detail/{tmdbId}/{isTv}",
            arguments = listOf(
                navArgument("tmdbId") { type = NavType.LongType },
                navArgument("isTv") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("tmdbId") ?: 0L
            val isTv = backStackEntry.arguments?.getBoolean("isTv") ?: false

            DetailScreen(
                tmdbId = id,
                isTv = isTv,
                onPlay = { playItem ->
                    nav.navigate("player/${playItem.serialized()}")
                },
                onBack = { nav.popBackStack() }
            )
        }

        // --- Reproductor ---
        composable(
            "player/{play}",
            arguments = listOf(
                navArgument("play") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val payload = backStackEntry.arguments?.getString("play") ?: ""
            PlayerScreen(
                serialized = payload,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
