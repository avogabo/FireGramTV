package com.pozoflix.firegramtv.telegram

import android.content.Context
import com.pozoflix.firegramtv.data.SettingsRepo
import com.pozoflix.firegramtv.model.PlayParams
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BotPart(val url: String, val name: String, val size: Long, val partIndex: Int, val totalParts: Int)
data class BotMatch(val parts: List<BotPart>)

class BotSearcher(private val context: Context) {
    private val mutex = Mutex()
    private val index = mutableListOf<Pair<String, Pair<String, Long>>>() // (caption -> (url, size))

    private val rexPart = Regex("(?i)(?:part|pt|cd|disc)[\\s._-]*([0-9]+)")
    private val rexEpisode1 = Regex("S(\\d{2})E(\\d{2})", RegexOption.IGNORE_CASE)
    private val rexEpisode2 = Regex("(\\d{2})x(\\d{2})", RegexOption.IGNORE_CASE)

    suspend fun refreshIndex(): Int {
        val repo = SettingsRepo(context)
        val token = repo.botToken.first() ?: return 0
        val client = BotClient(context) { token }
        val last = repo.lastUpdateId.first()
        val (msgs, maxId) = client.getUpdates(last.takeIf { it>0 })
        if (msgs.isEmpty()) return 0
        val added = mutableListOf<Pair<String, Pair<String, Long>>>()
        for (m in msgs) {
            val cap = m.caption ?: continue
            val f = m.file ?: continue
            val url = f.filePath?.let { client.buildFileUrl(it, token) } ?: continue
            added += cap to (url to f.size)
        }
        mutex.withLock { index += added }
        if (maxId != null) repo.setLastUpdateId(maxId + 1)
        return added.size
    }

    private fun baseKey(name: String): String {
        var s = name.lowercase()
        s = s.replace(Regex("[^a-z0-9]+"), " ").trim()
        s = rexPart.replace(s, "")
        return s
    }

    suspend fun findBestMatch(p: PlayParams): BotMatch? = withContext(Dispatchers.Default) {
        mutex.withLock {
            val wantId = "{tmdb-${p.tmdbId}}"
            val candidates = index.filter { (cap, _) ->
                var ok = true
                ok = ok && cap.contains(wantId, true)
                if (p.isTv) {
                    val tag = if (p.season!=null && p.episode!=null) String.format("S%02dE%02d", p.season, p.episode) else ""
                    ok = ok && (cap.contains(tag, true) || rexEpisode2.containsMatchIn(cap))
                } else if (p.year != null) {
                    ok = ok && cap.contains(p.year.toString(), true)
                }
                ok
            }
            if (candidates.isEmpty()) return@withLock null
            val bestName = candidates.first().first
            val group = index.filter { baseKey(it.first) == baseKey(bestName) }
            val parts = group.map { (n, pair) ->
                val (url, size) = pair
                val idx = rexPart.find(n)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                BotPart(url = url, name = n, size = size, partIndex = idx, totalParts = 0)
            }.sortedBy { it.partIndex }
            val total = parts.maxOfOrNull { it.partIndex } ?: 1
            val normalized = parts.map { it.copy(totalParts = total) }
            BotMatch(normalized)
        }
    }
}
