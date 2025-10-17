package com.pozoflix.firegramtv.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun PlayerScreen(urls: List<String>) {
    val context = LocalContext.current
    val player = ExoPlayer.Builder(context).build()

    DisposableEffect(Unit) {
        val ds = DefaultDataSource.Factory(context)
        val concat = ConcatenatingMediaSource().apply {
            urls.forEach { u ->
                addMediaSource(
                    ProgressiveMediaSource.Factory(ds).createMediaSource(MediaItem.fromUri(u))
                )
            }
        }
        player.setMediaSource(concat)
        player.prepare()
        player.playWhenReady = true

        onDispose { player.release() }
    }

    AndroidView(factory = { ctx ->
        PlayerView(ctx).apply { this.player = player }
    })
}
