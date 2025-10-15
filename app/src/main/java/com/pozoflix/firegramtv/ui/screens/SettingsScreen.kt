package com.pozoflix.firegramtv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.key

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onRefreshIndex: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        // Campo de usuario
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botones
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back")
            }
            Button(onClick = onRefreshIndex, modifier = Modifier.weight(1f)) {
                Text("Refresh Index")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de canales
        val channels = listOf(
            Channel(1, "General"),
            Channel(2, "Sports"),
            Channel(3, "Movies")
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(channels, key = { it.chatId }) { channel ->
                ChannelRow(channel)
            }
        }

        // Ejemplo correcto de key con IntArray
        val myIntArray = intArrayOf(1, 2, 3)
        key(myIntArray.contentHashCode()) {
            myIntArray.forEach { v ->
                Text(text = "Valor: $v")
            }
        }
    }
}

data class Channel(val chatId: Int, val name: String)

@Composable
fun ChannelRow(channel: Channel) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = channel.name)
    }
}
