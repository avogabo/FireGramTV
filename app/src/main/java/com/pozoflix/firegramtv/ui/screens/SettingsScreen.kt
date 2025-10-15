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

// 🔽 imports para TV/DPAD
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.KeyboardOptions

@Composable
fun SettingsScreen(onBack: () -> Unit, onRefreshIndex: () -> Unit) {
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

    // 🔧 Focus management para TV
    val fm = LocalFocusManager.current
    val frTmdb = remember { FocusRequester() }
    val frBot = remember { FocusRequester() }
    val frSave = remember { FocusRequester() }
    val frBack = remember { FocusRequester() }
    val frUpdate = remember { FocusRequester() }

    // Campos de nuevo canal
    val frNewName = remember { FocusRequester() }
    val frNewId = remember { FocusRequester() }
    val frNewType = remember { FocusRequester() }
    val frAddChannel = remember { FocusRequester() }

    fun Modifier.tvFocusable(): Modifier = this
        .focusRequester(frTmdb) // se cambia luego por el FR que toque
        .focusable()
        .onPreviewKeyEvent { ev ->
            if (ev.type == KeyEventType.KeyDown) {
                when (ev.key) {
                    Key.DirectionDown -> { fm.moveFocus(FocusDirection.Down); true }
                    Key.DirectionUp -> { fm.moveFocus(FocusDirection.Up); true }
                    Key.Tab -> { fm.moveFocus(FocusDirection.Down); true }
                    else -> false
                }
            } else false
        }

    Column(
        Modifier
            .padding(24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ajustes (BOT)", style = MaterialTheme.typography.headlineSmall)

        // TMDB
        OutlinedTextField(
            value = tmdb,
            onValueChange = { tmdb = it },
            label = { Text("TMDB API Key") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(frTmdb)
                .focusable()
                .onPreviewKeyEvent { ev ->
                    if (ev.type == KeyEventType.KeyDown) {
                        when (ev.key) {
                            Key.DirectionDown, Key.Tab -> { fm.moveFocus(FocusDirection.Down); true }
                            Key.DirectionUp -> { fm.moveFocus(FocusDirection.Up); true }
                            else -> false
                        }
                    } else false
                }
        )

        // Bot token
        OutlinedTextField(
            value = botToken,
            onValueChange = { botToken = it },
            label = { Text("Bot Token (BotFather)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(frBot)
                .focusable()
                .onPreviewKeyEvent { ev ->
                    if (ev.type == KeyEventType.KeyDown) {
                        when (ev.key) {
                            Key.DirectionDown, Key.Tab -> { fm.moveFocus(FocusDirection.Down); true }
                            Key.DirectionUp -> { fm.moveFocus(FocusDirection.Up); true }
                            else -> false
                        }
                    } else false
                }
        )

        // Botonera principal
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        repo.saveTmdbKey(tmdb)
                        repo.saveBotToken(botToken)
                    }
                },
                modifier = Modifier
                    .focusRequester(frSave)
                    .focusable()
                    .onPreviewKeyEvent { ev ->
                        if (ev.type == KeyEventType.KeyDown && ev.key == Key.DirectionDown) {
                            fm.moveFocus(FocusDirection.Down); true
                        } else false
                    }
            ) { Text("Guardar") }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .focusRequester(frBack)
                    .focusable()
            ) { Text("Volver") }

            Button(
                onClick = {
                    scope.launch {
                        val added = BotSearcher(ctx).refreshIndex()
                        lastAdd = added
                        onRefreshIndex()
                    }
                },
                modifier = Modifier
                    .focusRequester(frUpdate)
                    .focusable()
            ) { Text("Actualizar índice (Bot)") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            Text("Actualizar índice al iniciar")
            Switch(
                checked = autoRefresh,
                onCheckedChange = { v ->
                    autoRefresh = v
                    scope.launch { repo.setAutoRefresh(v) }
                }
            )
        }

        if (lastAdd > 0) Text("Añadidos $lastAdd elementos nuevos.")

        Spacer(Modifier.height(8.dp))
        Text("Canales de Telegram")

        var newName by remember { mutableStateOf("") }
        var newChatId by remember { mutableStateOf("") }
        var newType by remember { mutableStateOf("MIXED") }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(frNewName)
                    .focusable()
                    .onPreviewKeyEvent { ev ->
                        if (ev.type == KeyEventType.KeyDown) {
                            when (ev.key) {
                                Key.DirectionDown, Key.Tab -> { fm.moveFocus(FocusDirection.Down); true }
                                Key.DirectionUp -> { fm.moveFocus(FocusDirection.Up); true }
                                else -> false
                            }
                        } else false
                    }
            )
            OutlinedTextField(
                value = newChatId,
                onValueChange = { newChatId = it },
                label = { Text("chat_id") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(frNewId)
                    .focusable()
                    .onPreviewKeyEvent { ev ->
                        if (ev.type == KeyEventType.KeyDown) {
                            when (ev.key) {
                                Key.DirectionDown, Key.Tab -> { fm.moveFocus(FocusDirection.Down); true }
                                Key.DirectionUp -> { fm.moveFocus(FocusDirection.Up); true }
                                else -> false
                            }
                        } else false
                    }
            )
            OutlinedTextField(
                value = newType,
                onValueChange = { newType = it },
                label = { Text("Tipo (MOVIE/TV/MIXED)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(frNewType)
                    .focusable()
                    .onPreviewKeyEvent { ev ->
                        if (ev.type == KeyEventType.KeyDown) {
                            when (ev.key) {
                                Key.DirectionDown, Key.Tab -> { fm.moveFocus(FocusDirection.Down); true }
                                Key.DirectionUp -> { fm.moveFocus(FocusDirection.Up); true }
                                else -> false
                            }
                        } else false
                    }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    newChatId.toLongOrNull()?.let { id ->
                        val list = channels + ChannelCfg(newName.ifBlank { id.toString() }, id, newType.uppercase())
                        scope.launch { repo.saveChannels(list) }
                        newName = ""; newChatId = ""; newType = "MIXED"
                    }
                },
                modifier = Modifier
                    .focusRequester(frAddChannel)
                    .focusable()
            ) { Text("Añadir canal") }

            if (channels.isNotEmpty()) {
                Button(onClick = { scope.launch { repo.saveChannels(emptyList()) } }) { Text("Vaciar lista") }
            }
        }

        LazyColumn {
            items(channels) { ch ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${ch.name} • ${ch.chatId} • ${ch.type}")
                }
            }
        }

        Text("Añade el bot como ADMIN en esos canales para que reciba posts nuevos.")
    }

    // 🔰 Al entrar en la pantalla, damos el foco al primer campo
    LaunchedEffect(Unit) { frTmdb.requestFocus() }
}
