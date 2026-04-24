package xyz.malefic.stackmanager

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.core.Method.GET
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main() {
    val cors = ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive)

    // Health check is unauthenticated so load-balancers/monitors can probe it
    val publicRoutes = routes(
        "/api/health" bind GET to { _: Request ->
            Response(OK).body("""{"status":"ok"}""").header("Content-Type", "application/json")
        },
    )

    val protectedRoutes = cors
        .then(bearerAuthFilter)
        .then(stackRoutes())

    val app = cors.then(routes(publicRoutes, protectedRoutes))

    val server = app.asServer(Undertow(Config.port)).start()
    println("StackManager backend started on ${Config.bindHost}:${Config.port}")
    server.block()
}
