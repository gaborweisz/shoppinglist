package com.trainig.shoppinglist.share

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.Deflater
import java.util.zip.Inflater

data class SharedItem(
    val remoteId: String,
    val name: String,
    val quantity: String? = null,
    val note: String? = null,
    val category: String = ""
)

data class SharePayload(
    val listId: String,
    val version: Int,
    val items: List<SharedItem>
)

/**
 * Encodes a [SharePayload] into a `shoppinglist://share?d=...` link that can travel inside a chat
 * message, and finds and decodes such a link in any received text.
 */
object SharePayloadCodec {
    private const val FORMAT_VERSION = 1
    private const val LINK_PREFIX = "shoppinglist://share?d="
    private val LINK_REGEX = Regex("""shoppinglist://share\?d=([A-Za-z0-9_-]+)""")

    fun toLink(payload: SharePayload): String = LINK_PREFIX + encode(payload)

    /** Returns the payload of the first share link in [text], or null if there is no valid one. */
    fun fromText(text: String?): SharePayload? {
        val data = text?.let { LINK_REGEX.find(it) }?.groupValues?.get(1) ?: return null
        return runCatching { decode(data) }.getOrNull()
    }

    fun encode(payload: SharePayload): String {
        val items = JSONArray()
        payload.items.forEach { item ->
            items.put(
                JSONObject()
                    .put("r", item.remoteId)
                    .put("n", item.name)
                    .putOpt("q", item.quantity)
                    .putOpt("o", item.note)
                    .put("c", item.category)
            )
        }
        val json = JSONObject()
            .put("f", FORMAT_VERSION)
            .put("id", payload.listId)
            .put("v", payload.version)
            .put("items", items)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(deflate(json.toString().toByteArray(Charsets.UTF_8)))
    }

    fun decode(data: String): SharePayload {
        val json = JSONObject(String(inflate(Base64.getUrlDecoder().decode(data)), Charsets.UTF_8))
        require(json.getInt("f") == FORMAT_VERSION) { "Unsupported share format" }
        val items = json.getJSONArray("items")
        return SharePayload(
            listId = json.getString("id"),
            version = json.getInt("v"),
            items = (0 until items.length()).map { i ->
                val item = items.getJSONObject(i)
                SharedItem(
                    remoteId = item.getString("r"),
                    name = item.getString("n"),
                    quantity = item.optStringOrNull("q"),
                    note = item.optStringOrNull("o"),
                    category = item.optString("c", "")
                )
            }
        )
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null

    private fun deflate(bytes: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(bytes)
        deflater.finish()
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            out.write(buffer, 0, deflater.deflate(buffer))
        }
        deflater.end()
        return out.toByteArray()
    }

    private fun inflate(bytes: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(bytes)
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                throw IllegalArgumentException("Truncated share data")
            }
            out.write(buffer, 0, count)
        }
        inflater.end()
        return out.toByteArray()
    }
}
