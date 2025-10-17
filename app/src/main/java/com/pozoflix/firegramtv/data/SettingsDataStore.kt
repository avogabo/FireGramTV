package com.pozoflix.firegramtv.data

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "firegram_prefs")


@Serializable
data class ChannelCfg(val name: String, val chatId: Long, val type: String = "MIXED")

object Keys {
    val TMDB = stringPreferencesKey("tmdb_key")
    val BOT_TOKEN = stringPreferencesKey("bot_token")
    val CHANNELS = stringPreferencesKey("channels_json")
    val LAST_UPDATE_ID = longPreferencesKey("bot_last_update_id")
    val AUTO_REFRESH = booleanPreferencesKey("auto_refresh_on_start")
}

class SettingsRepo(private val ctx: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    val tmdbKey: Flow<String?> = ctx.dataStore.data.map { it[Keys.TMDB] }
    val botToken: Flow<String?> = ctx.dataStore.data.map { it[Keys.BOT_TOKEN] }
    val channels: Flow<List<ChannelCfg>> = ctx.dataStore.data.map {
        it[Keys.CHANNELS]?.let { s -> runCatching { json.decodeFromString<List<ChannelCfg>>(s) }.getOrElse { emptyList() } } ?: emptyList()
    }
    val lastUpdateId: Flow<Long> = ctx.dataStore.data.map { it[Keys.LAST_UPDATE_ID] ?: 0L }
    val autoRefresh: Flow<Boolean> = ctx.dataStore.data.map { it[Keys.AUTO_REFRESH] ?: true }

    suspend fun saveTmdbKey(v: String) = ctx.dataStore.edit { it[Keys.TMDB] = v }
    suspend fun saveBotToken(v: String) = ctx.dataStore.edit { it[Keys.BOT_TOKEN] = v }
    suspend fun saveChannels(list: List<ChannelCfg>) = ctx.dataStore.edit { it[Keys.CHANNELS] = json.encodeToString(list) }
    suspend fun setLastUpdateId(v: Long) = ctx.dataStore.edit { it[Keys.LAST_UPDATE_ID] = v }
    suspend fun setAutoRefresh(v: Boolean) = ctx.dataStore.edit { it[Keys.AUTO_REFRESH] = v }
}
