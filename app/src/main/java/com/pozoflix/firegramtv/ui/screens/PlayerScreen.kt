package com.pozoflix.firegramtv.ui.screens

import android.media.MediaCodecList
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.pozoflix.firegramtv.db.DbProvider
import com.pozoflix.firegramtv.db.MediaProgress
import com.pozoflix.firegramtv.model.PlayParams
import com.pozoflix.firegramtv.telegram.BotSearcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PlayerScreen(serialized: String, onBack: ()->Unit) {
    val params = remember { PlayParams.deserialized(serialized) }
    val ctx = LocalContext.current
    val exo = remember { ExoPlayer.Builder(ctx).build() }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }

    fun progressKey(): String = if (params.isTv) "t:${params.tmdbId}:${params.season}x${params.episode}" else "m:${params.tmdbId}"

    BackHandler { exo.stop(); exo.release(); onBack() }

    LaunchedEffect(Unit) {
        val searcher = BotSearcher(ctx)
        val match = withContext(Dispatchers.IO) { searcher.refreshIndex(); searcher.findBestMatch(params) }
        if (match == null) { error = "No se encontró el archivo en el índice del bot. Usa 'Actualizar índice (Bot)' en Ajustes justo después de publicar o reenviar los posts."; return@LaunchedEffect }
        val dsFactory = DefaultDataSource.Factory(ctx)
        val concat = ConcatenatingMediaSource()
        match.parts.forEach { part ->
            val mediaItem = MediaItem.fromUri(Uri.parse(part.url))
            val mediaSource = ProgressiveMediaSource.Factory(dsFactory).createMediaSource(mediaItem)
            concat.addMediaSource(mediaSource)
        }
        exo.setMediaSource(concat); exo.prepare()
        val p = withContext(Dispatchers.IO) { DbProvider.get(ctx).progressDao().find(progressKey()) }
        if (p != null && p.positionMs > 10_000) exo.seekTo(p.positionMs)
        exo.playWhenReady = true
    }

    DisposableEffect(Unit) {
        val job = scope.launch {
            while (true) {
                kotlinx.coroutines.delay(2000)
                val pos = exo.currentPosition; val dur = exo.duration
                if (dur > 0) withContext(Dispatchers.IO) {
                    DbProvider.get(ctx).progressDao().upsert(MediaProgress(progressKey(), pos, dur, System.currentTimeMillis()))
                }
            }
        }
        onDispose { job.cancel() }
    }

    if (error != null) {
        AlertDialog(onDismissRequest = onBack, confirmButton = {}, title = { Text("No se pudo reproducir") }, text = { Text(error!!) })
    }

    Box(Modifier.fillMaxSize()) { /* Player view */ }
}

@Composable
fun supportsAv1(): Boolean = try {
    MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.any { info ->
        !info.isEncoder && info.supportedTypes.any { it.equals("video/av01", true) }
    }
} catch (e: Exception) { false }
