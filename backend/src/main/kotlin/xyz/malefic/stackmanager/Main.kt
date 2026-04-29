package xyz.malefic.stackmanager

import io.undertow.Undertow as RawUndertow
import io.undertow.server.handlers.BlockingHandler as UndertowBlockingHandler
import java.io.File
import java.net.BindException
import java.net.URLConnection
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.HEAD
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
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
 * This variant lets the caller request a specific listener address so the API
 * can be kept off public interfaces when that address exists.
 *
 * @param port TCP port to listen on.
 * @param host Requested network interface address to bind (e.g. `127.0.0.1` or a Tailscale IP).
 */
class BoundUndertow(
    private val port: Int,
    private val host: String,
) : ServerConfig {
    override fun toServer(http: HttpHandler): Http4kServer =
        object : Http4kServer {
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
    val publicRoutes =
        routes(
            "/api/health" bind GET to { _: Request ->
                Response(OK).body("""{"status":"ok"}""").header("Content-Type", "application/json")
            },
        )

    val protectedRoutes =
        cors
            .then(bearerAuthFilter)
            .then(stackRoutes())

    val apiApp = cors.then(routes(publicRoutes, protectedRoutes))
    val webRoot = File(Config.webRoot)

    val app: HttpHandler =
        { req ->
            if (req.uri.path.startsWith("/api/")) {
                apiApp(req)
            } else {
                serveFrontend(req, webRoot)
            }
        }

    val server = startServer(app, Config.port, Config.bindHost)
    println("StackManager backend+frontend started on port ${Config.port} (web root: ${webRoot.absolutePath})")
    server.block()
}

private fun startServer(
    app: HttpHandler,
    port: Int,
    bindHost: String,
): Http4kServer {
    fun startOn(host: String) = app.asServer(BoundUndertow(port, host)).start()

    return try {
        startOn(bindHost)
    } catch (e: Exception) {
        if (bindHost != "0.0.0.0" && causesBindAddressError(e)) {
            System.err.println(
                "WARN: failed to bind to '$bindHost' (${rootCauseMessage(e)}); retrying on 0.0.0.0",
            )
            startOn("0.0.0.0")
        } else {
            throw e
        }
    }
}

private fun causesBindAddressError(t: Throwable): Boolean =
    generateSequence(t) { it.cause }.any { it is BindException } &&
        generateSequence(t) { it.cause }.any {
            it.message?.contains("requested address", ignoreCase = true) == true
        }

private fun rootCauseMessage(t: Throwable): String =
    generateSequence(t) { it.cause }.lastOrNull()?.message ?: t.message.orEmpty()

private fun serveFrontend(
    req: Request,
    webRoot: File,
): Response {
    if (req.method != GET && req.method != HEAD) return Response(NOT_FOUND)

    val path = req.uri.path.removePrefix("/")
    val requested = if (path.isBlank()) "index.html" else path

    val directFile = resolveStaticFile(webRoot, requested)
    if (directFile != null) return fileResponse(req, directFile)

    if (!requested.contains(".")) {
        val indexFile = resolveStaticFile(webRoot, "index.html")
        if (indexFile != null) return fileResponse(req, indexFile)
    }

    return Response(NOT_FOUND)
}

private fun resolveStaticFile(
    webRoot: File,
    relativePath: String,
): File? {
    val canonicalRoot = webRoot.canonicalFile
    val canonicalCandidate = File(canonicalRoot, relativePath).canonicalFile
    if (canonicalCandidate.path != canonicalRoot.path &&
        !canonicalCandidate.path.startsWith(canonicalRoot.path + File.separator)
    ) {
        return null
    }
    return canonicalCandidate.takeIf { it.isFile }
}

private fun fileResponse(
    req: Request,
    file: File,
): Response {
    val contentType = URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"
    val response = Response(OK).header("Content-Type", contentType)
    return if (req.method == HEAD) response else response.body(file.inputStream(), file.length())
}
