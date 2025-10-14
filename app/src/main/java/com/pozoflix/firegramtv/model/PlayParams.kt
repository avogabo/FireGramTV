package com.pozoflix.firegramtv.model

import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class PlayParams(
    val tmdbId: Long,
    val isTv: Boolean,
    val title: String,
    val year: Int?,
    val season: Int?,
    val episode: Int?
) {
    fun serialized(): String {
        fun enc(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8)
        val parts = listOf(
            tmdbId.toString(), isTv.toString(), enc(title),
            year?.toString() ?: "", season?.toString() ?: "", episode?.toString() ?: ""
        )
        return parts.joinToString("|")
    }
    companion object {
        fun deserialized(s: String): PlayParams {
            fun dec(x: String) = URLDecoder.decode(x, StandardCharsets.UTF_8)
            val p = s.split("|")
            return PlayParams(
                tmdbId = p.getOrNull(0)?.toLongOrNull() ?: 0,
                isTv = p.getOrNull(1)?.toBooleanStrictOrNull() ?: false,
                title = dec(p.getOrNull(2) ?: ""),
                year = p.getOrNull(3)?.toIntOrNull(),
                season = p.getOrNull(4)?.toIntOrNull(),
                episode = p.getOrNull(5)?.toIntOrNull()
            )
        }
    }
}
