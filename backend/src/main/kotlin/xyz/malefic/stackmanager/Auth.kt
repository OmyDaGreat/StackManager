package xyz.malefic.stackmanager

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.UNAUTHORIZED

val bearerAuthFilter = Filter { next: HttpHandler ->
    { req: Request ->
        val auth = req.header("Authorization")
        val token = Config.token
        if (auth == "Bearer $token") {
            next(req)
        } else {
            Response(UNAUTHORIZED).body("""{"error":"Unauthorized"}""")
                .header("Content-Type", "application/json")
        }
    }
}
