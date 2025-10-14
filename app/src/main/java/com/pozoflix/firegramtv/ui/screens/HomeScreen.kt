package com.pozoflix.firegramtv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pozoflix.firegramtv.data.SettingsRepo
import com.pozoflix.firegramtv.db.DbProvider
import com.pozoflix.firegramtv.db.MediaProgress
import com.pozoflix.firegramtv.db.Favorite
import com.pozoflix.firegramtv.tmdb.TmdbRepo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HomeScreen(onOpenDetail: (Long, Boolean) -> Unit, onOpenSettings: ()->Unit) {
    val ctx = LocalContext.current
    val tmdb = remember { TmdbRepo(ctx) }
    val settings = remember { SettingsRepo(ctx) }
    val scope = rememberCoroutineScope()

    var tmdbKey by remember { mutableStateOf<String?>(null) }
    var movies by remember { mutableStateOf(emptyList<com.pozoflix.firegramtv.tmdb.MovieDto>()) }
    var tv by remember { mutableStateOf(emptyList<com.pozoflix.firegramtv.tmdb.TvDto>()) }
    var recent by remember { mutableStateOf(emptyList<MediaProgress>()) }
    var favs by remember { mutableStateOf(emptyList<Favorite>()) }
    var halloween by remember { mutableStateOf(emptyList<com.pozoflix.firegramtv.tmdb.MovieDto>()) }
    var christmas by remember { mutableStateOf(emptyList<com.pozoflix.firegramtv.tmdb.MovieDto>()) }
    var summer by remember { mutableStateOf(emptyList<com.pozoflix.firegramtv.tmdb.MovieDto>()) }
    var autoRefresh by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        settings.tmdbKey.collect { key ->
            tmdbKey = key
            if (!key.isNullOrBlank()) {
                scope.launch { movies = tmdb.trendingMovies(key!!) }
                scope.launch { tv = tmdb.trendingTv(key!!) }
                val today = LocalDate.now()
                if (today.month.value == 10) scope.launch { halloween = tmdb.halloween(key!!) }
                if (today.month.value == 12) scope.launch { christmas = tmdb.christmas(key!!) }
                if (today.month.value in 7..8) scope.launch { summer = tmdb.summerBlockbusters(key!!) }
            }
        }
    }
    LaunchedEffect(Unit) { settings.autoRefresh.collect { autoRefresh = it } }
    LaunchedEffect(autoRefresh) {
        if (autoRefresh) {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.pozoflix.firegramtv.telegram.BotSearcher(ctx).refreshIndex()
                }
            } catch (_: Exception) { }
        }
    }
    LaunchedEffect(Unit) { DbProvider.get(ctx).progressDao().recent(10).collectLatest { recent = it } }
    LaunchedEffect(Unit) { DbProvider.get(ctx).favoritesDao().list(30).collectLatest { favs = it } }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onOpenSettings) { Text("Ajustes") }
        }

        if (favs.isNotEmpty()) {
            Text("⭐ Favoritos")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(favs) { f ->
                    OutlinedButton(onClick = { onOpenDetail(f.tmdbId, f.isTv) }) { Text((if (f.isTv) "Serie" else "Película") + " · TMDB " + f.tmdbId) }
                }
            }
        }

        Text("Siguiendo")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(recent) { p ->
                Card(Modifier.size(220.dp, 110.dp)) {
                    Column(Modifier.padding(8.dp)) {
                        Text(p.key)
                        Text("Pos: ${p.positionMs/1000}s")
                    }
                }
            }
        }

        if (halloween.isNotEmpty()) {
            Text("Especial Halloween 🎃")
            LazyRow { items(halloween) { m -> Button(onClick = { onOpenDetail(m.id, false) }) { Text(m.title) } } }
        }
        if (christmas.isNotEmpty()) {
            Text("Cine de Navidad 🎄")
            LazyRow { items(christmas) { m -> Button(onClick = { onOpenDetail(m.id, false) }) { Text(m.title) } } }
        }
        if (summer.isNotEmpty()) {
            Text("Blockbusters de verano ☀️")
            LazyRow { items(summer) { m -> Button(onClick = { onOpenDetail(m.id, false) }) { Text(m.title) } } }
        }

        Text("Películas populares")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(movies) { m ->
                Card(Modifier.size(240.dp, 140.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(m.title, modifier = Modifier.padding(8.dp)) }
                }
            }
        }

        Text("Series populares")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tv) { s ->
                Card(Modifier.size(240.dp, 140.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(s.name, modifier = Modifier.padding(8.dp)) }
                }
            }
        }
    }
}
