package com.pozoflix.firegramtv.telegram

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.FormBody
import org.json.JSONObject

data class BotFile(val fileId: String, val fileUniqueId: String, val filePath: String?, val size: Long)
data class BotMessage(val chatId: Long, val messageId: Long, val caption: String?, val file: BotFile?)

class BotClient(private val context: Context, private val tokenProvider: suspend ()->String) {
    private val http = OkHttpClient()

    private suspend fun api(method: String, body: FormBody.Builder.()->Unit): JSONObject = withContext(Dispatchers.IO) {
        val token = tokenProvider()
        val form = FormBody.Builder().apply(body).build()
        val req = Request.Builder()
            .url("https://api.telegram.org/bot$token/$method")
            .post(form)
            .build()
        http.newCall(req).execute().use { resp ->
            val txt = resp.body?.string() ?: "{}"
            JSONObject(txt)
        }
    }

    suspend fun getUpdates(offset: Long?): Pair<List<BotMessage>, Long?> {
        val res = api("getUpdates") {
            add("timeout", "0")
            if (offset != null && offset > 0) add("offset", offset.toString())
            add("allowed_updates[]", "channel_post")
            add("allowed_updates[]", "message")
        }
        val out = mutableListOf<BotMessage>()
        if (!res.optBoolean("ok")) return out to null
        val arr = res.optJSONArray("result") ?: return out to null
        var maxId: Long? = null
        for (i in 0 until arr.length()) {
            val upd = arr.getJSONObject(i)
            val updateId = upd.optLong("update_id")
            if (maxId == null || updateId > maxId!!) maxId = updateId
            val msg = (upd.optJSONObject("channel_post") ?: upd.optJSONObject("message")) ?: continue
            val chat = msg.optJSONObject("chat") ?: continue
            val chatId = chat.optLong("id")
            val messageId = msg.optLong("message_id")
            val caption = if (msg.has("caption")) msg.optString("caption") else msg.optString("text", null)
            var file: BotFile? = null
            val video = msg.optJSONObject("video")
            val doc = msg.optJSONObject("document")
            val fileObj = video ?: doc
            if (fileObj != null) {
                val fileId = fileObj.optString("file_id")
                val fileUniqueId = fileObj.optString("file_unique_id")
                val size = fileObj.optLong("file_size", 0L)
                val path = getFilePath(fileId)
                file = BotFile(fileId, fileUniqueId, path, size)
            }
            out += BotMessage(chatId, messageId, caption, file)
        }
        return out to maxId
    }

    private suspend fun getFilePath(fileId: String): String? {
        val res = api("getFile") { add("file_id", fileId) }
        if (!res.optBoolean("ok")) return null
        val result = res.optJSONObject("result") ?: return null
        return result.optString("file_path", null)
    }

    fun buildFileUrl(filePath: String, token: String): String =
        "https://api.telegram.org/file/bot${token}/${filePath}"
}
