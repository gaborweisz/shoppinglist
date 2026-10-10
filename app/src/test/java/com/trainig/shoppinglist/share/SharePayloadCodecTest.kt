package com.trainig.shoppinglist.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharePayloadCodecTest {
    private val payload = SharePayload(
        listId = "list-1",
        version = 7,
        items = listOf(
            SharedItem("r1", "Tej", "2 l", "zsíros", "Tejtermék"),
            SharedItem("r2", "Brot", null, null, "")
        )
    )

    @Test
    fun `link round trips through encode and decode`() {
        val link = SharePayloadCodec.toLink(payload)

        assertEquals(payload, SharePayloadCodec.fromText(link))
    }

    @Test
    fun `link is found inside a chat message`() {
        val message = "🛒 My Shopping List\n☐ Tej\n\nOpen in the Shopping List app:\n" +
            SharePayloadCodec.toLink(payload) + "\n"

        assertEquals(payload, SharePayloadCodec.fromText(message))
    }

    @Test
    fun `text without link or with broken data returns null`() {
        assertNull(SharePayloadCodec.fromText("just some text"))
        assertNull(SharePayloadCodec.fromText(null))
        assertNull(SharePayloadCodec.fromText("shoppinglist://share?d=abc"))
    }

    @Test
    fun `truncated link returns null`() {
        val link = SharePayloadCodec.toLink(payload)

        assertNull(SharePayloadCodec.fromText(link.dropLast(10)))
    }
}
