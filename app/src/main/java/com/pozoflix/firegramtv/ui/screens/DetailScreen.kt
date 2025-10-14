package com.pozoflix.firegramtv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pozoflix.firegramtv.db.DbProvider
import com.pozoflix.firegramtv.db.Favorite
import com.pozoflix.firegramtv.model.PlayParams
import com.pozoflix.firegramtv.tmdb.TmdbRepo
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(tmdbId: Long, isTv: Boolean, onPlay: (PlayParams)->Unit, onBack: ()->Unit) {
    val ctx = LocalContext.current
    val repo = remember { TmdbRepo(ctx) }
    val db = remember { DbProvider.get(ctx) }
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var year by remember { mutableStateOf<Int?>(null) }
    var seasons by remember { mutableStateOf(listOf<Int>()) }
    var selectedSeason by remember { mutableStateOf<Int?>(null) }
    var episodes by remember { mutableStateOf(listOf<com.pozoflix.firegramtv.tmdb.EpisodeDto>()) }
    var isFav by remember { mutableStateOf(false) }
    var tmdbKey by remember { mutableStateOf("") }

    fun favKey() = if (isTv) "t:$tmdbId" else "m:$tmdbId"

    LaunchedEffect(Unit) { isFav = db.favoritesDao().isFav(favKey()) }
    LaunchedEffect(Unit) { com.pozoflix.firegramtv.data.SettingsRepo(ctx).tmdbKey.collect { tmdbKey = it ?: "" } }
    LaunchedEffect(tmdbKey) {
        if (tmdbKey.isBlank()) return@LaunchedEffect
        if (isTv) {
            val tv = repo.tv(tmdbId, tmdbKey)
            title = tv.name; year = tv.first_air_date?.take(4)?.toIntOrNull()
            seasons = (1..tv.number_of_seasons).toList()
        } else {
            val m = repo.movie(tmdbId, tmdbKey)
            title = m.title; year = m.release_date?.take(4)?.toIntOrNull()
        }
    }

    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$title ${year?.let { "($it)" } ?: ""} • TMDB:$tmdbId")
            IconButton(onClick = {
                isFav = !isFav
                scope.launch {
                    if (isFav) db.favoritesDao().add(Favorite(favKey(), tmdbId, isTv))
                    else db.favoritesDao().remove(favKey())
                }
            }) { Icon(if (isFav) Icons.Filled.Star else Icons.Outlined.Star, contentDescription = "Fav") }
        }

        if (!isTv) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onPlay(PlayParams(tmdbId, false, title, year, null, null)) }) { Text("Reproducir") }
                OutlinedButton(onClick = onBack) { Text("Volver") }
            }
        } else {
            Text("Temporadas")
            LazyColumn {
                items(seasons) { s ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Temporada $s")
                        Button(onClick = {
                            selectedSeason = s
                            scope.launch { episodes = repo.season(tmdbId, s, tmdbKey).episodes }
                        }) { Text("Ver episodios") }
                    }
                }
            }
            if (selectedSeason != null) {
                Text("Episodios T$selectedSeason")
                LazyColumn {
                    items(episodes) { e ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("E${e.episode_number} • ${e.name}")
                            Button(onClick = { onPlay(PlayParams(tmdbId, true, title, year, selectedSeason, e.episode_number)) }) { Text("Reproducir") }
                        }
                    }
                }
            }
            OutlinedButton(onClick = onBack) { Text("Volver") }
        }
    }
}
