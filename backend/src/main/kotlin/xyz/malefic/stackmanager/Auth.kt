package xyz.malefic.stackmanager

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.UNAUTHORIZED

val bearerAuthFilter =
    Filter { next: HttpHandler ->
        { req: Request ->
            val auth = req.header("Authorization") ?: ""
            val expected = "Bearer ${Config.token}"
            if (constantTimeEquals(auth, expected)) {
                next(req)
            } else {
                Response(UNAUTHORIZED)
                    .body("""{"error":"Unauthorized","code":"UNAUTHORIZED"}""")
                    .header("Content-Type", "application/json")
            }
        }
    }

/** Constant-time string comparison to prevent timing attacks. */
private fun constantTimeEquals(
    a: String,
    b: String,
): Boolean {
    val aBytes = a.toByteArray()
    val bBytes = b.toByteArray()
    val maxLen = maxOf(aBytes.size, bBytes.size)
    var diff = aBytes.size xor bBytes.size
    for (i in 0 until maxLen) {
        val aByte = if (i < aBytes.size) aBytes[i].toInt() else 0
        val bByte = if (i < bBytes.size) bBytes[i].toInt() else 0
        diff = diff or (aByte xor bByte)
    }
    return diff == 0
}
