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
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(
            onOpenDetail = { id, isTv -> nav.navigate("detail/$id/$isTv") },
            onOpenSettings = { nav.navigate("settings") }
        ) }
        composable("settings") { SettingsScreen(
            onBack = { nav.popBackStack() },
            onRefreshIndex = { nav.popBackStack(); nav.navigate("home") }
        ) }
        composable("detail/{tmdbId}/{isTv}",
            arguments = listOf(
                navArgument("tmdbId") { type = NavType.LongType },
                navArgument("isTv") { type = NavType.BoolType }
            )) { backStack ->
            val id = backStack.arguments?.getLong("tmdbId") ?: 0L
            val isTv = backStack.arguments?.getBoolean("isTv") ?: false
            DetailScreen(
                tmdbId = id, isTv = isTv,
                onPlay = { p -> nav.navigate("player/${p.serialized()}") },
                onBack = { nav.popBackStack() }
            )
        }
        composable("player/{play}", arguments = listOf(navArgument("play") { type = NavType.StringType })) { bs ->
            val payload = bs.arguments?.getString("play") ?: ""
            PlayerScreen(serialized = payload, onBack = { nav.popBackStack() })
        }
    }
}
