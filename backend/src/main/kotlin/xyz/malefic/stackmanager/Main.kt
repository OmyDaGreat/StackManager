package xyz.malefic.stackmanager

import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main() {
    val app = ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive)
        .then(bearerAuthFilter)
        .then(stackRoutes())

    val server = app.asServer(Undertow(Config.port)).start()
    println("StackManager backend started on ${Config.bindHost}:${Config.port}")
    server.block()
}
