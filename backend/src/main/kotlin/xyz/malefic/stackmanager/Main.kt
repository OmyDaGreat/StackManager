package xyz.malefic.stackmanager

import io.undertow.Undertow as RawUndertow
import io.undertow.server.handlers.BlockingHandler as UndertowBlockingHandler
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Http4kUndertowHttpHandler
import org.http4k.server.ServerConfig
import org.http4k.server.asServer

/**
 * Custom [ServerConfig] that binds Undertow to a specific [host] and [port].
 *
 * The standard http4k [org.http4k.server.Undertow] always binds to `0.0.0.0`.
 * This variant restricts the listener to [host] so the API is only reachable on
 * the configured interface (e.g. a Tailscale IP), never on the public NIC.
 *
 * @param port TCP port to listen on.
 * @param host Network interface address to bind (e.g. `127.0.0.1` or a Tailscale IP).
 */
class BoundUndertow(private val port: Int, private val host: String) : ServerConfig {
    override fun toServer(http: HttpHandler): Http4kServer = object : Http4kServer {
        private val server =
            RawUndertow
                .builder()
                .addHttpListener(port, host, UndertowBlockingHandler(Http4kUndertowHttpHandler(http)))
                .build()

        override fun start() = apply { server.start() }

        override fun stop() = apply { server.stop() }

        override fun port() = port
    }
}

fun main() {
    val cors = ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive)

    // Health check is unauthenticated so load-balancers/monitors can probe it
    val publicRoutes = routes(
        "/api/health" bind GET to { _: Request ->
            Response(OK).body("""{"status":"ok"}""").header("Content-Type", "application/json")
        },
    )

    val protectedRoutes =
        cors
            .then(bearerAuthFilter)
            .then(stackRoutes())

    val app = cors.then(routes(publicRoutes, protectedRoutes))

    val server = app.asServer(BoundUndertow(Config.port, Config.bindHost)).start()
    println("StackManager backend started on ${Config.bindHost}:${Config.port}")
    server.block()
}
