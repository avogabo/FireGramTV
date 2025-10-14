package com.pozoflix.firegramtv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pozoflix.firegramtv.data.ChannelCfg
import com.pozoflix.firegramtv.data.SettingsRepo
import com.pozoflix.firegramtv.telegram.BotSearcher
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: ()->Unit, onRefreshIndex: ()->Unit) {
    val ctx = LocalContext.current
    val repo = remember { SettingsRepo(ctx) }
    val scope = rememberCoroutineScope()

    var tmdb by remember { mutableStateOf("") }
    var botToken by remember { mutableStateOf("") }
    var channels by remember { mutableStateOf(listOf<ChannelCfg>()) }
    var lastAdd by remember { mutableStateOf(0) }
    var autoRefresh by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { repo.tmdbKey.collect { tmdb = it ?: "" } }
    LaunchedEffect(Unit) { repo.botToken.collect { botToken = it ?: "" } }
    LaunchedEffect(Unit) { repo.channels.collect { channels = it } }
    LaunchedEffect(Unit) { repo.autoRefresh.collect { autoRefresh = it } }

    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ajustes (BOT)", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(tmdb, { tmdb = it }, label = { Text("TMDB API Key") })
        OutlinedTextField(botToken, { botToken = it }, label = { Text("Bot Token (BotFather)") })

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { scope.launch { repo.saveTmdbKey(tmdb); repo.saveBotToken(botToken) } }) { Text("Guardar") }
            OutlinedButton(onClick = onBack) { Text("Volver") }
            Button(onClick = {
                scope.launch {
                    val added = BotSearcher(ctx).refreshIndex()
                    lastAdd = added
                    onRefreshIndex()
                }
            }) { Text("Actualizar índice (Bot)") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            Text("Actualizar índice al iniciar")
            Switch(checked = autoRefresh, onCheckedChange = { v ->
                autoRefresh = v
                scope.launch { repo.setAutoRefresh(v) }
            })
        }

        if (lastAdd > 0) Text("Añadidos $lastAdd elementos nuevos.")

        Spacer(Modifier.height(8.dp))
        Text("Canales de Telegram")

        var newName by remember { mutableStateOf("") }
        var newChatId by remember { mutableStateOf("") }
        var newType by remember { mutableStateOf("MIXED") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(newName, { newName = it }, label = { Text("Nombre") }, modifier = Modifier.weight(1f))
            OutlinedTextField(newChatId, { newChatId = it }, label = { Text("chat_id") }, modifier = Modifier.weight(1f))
            OutlinedTextField(newType, { newType = it }, label = { Text("Tipo (MOVIE/TV/MIXED)") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                newChatId.toLongOrNull()?.let { id ->
                    val list = channels + ChannelCfg(newName.ifBlank { id.toString() }, id, newType.uppercase())
                    scope.launch { repo.saveChannels(list) }
                    newName = ""; newChatId = ""; newType = "MIXED"
                }
            }) { Text("Añadir canal") }
            if (channels.isNotEmpty()) {
                Button(onClick = { scope.launch { repo.saveChannels(emptyList()) } }) { Text("Vaciar lista") }
            }
        }
        LazyColumn { items(channels) { ch -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${ch.name} • ${ch.chatId} • ${ch.type}") } } }

        Text("Añade el bot como ADMIN en esos canales para que reciba posts nuevos.")
    }
}
