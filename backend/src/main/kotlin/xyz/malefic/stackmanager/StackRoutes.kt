package xyz.malefic.stackmanager

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

@Serializable
data class StackInfo(
    val name: String,
    val composeYaml: String,
)

@Serializable
data class PutStackRequest(
    val composeYaml: String,
)

@Serializable
data class CommandResponse(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)

@Serializable
data class StackListResponse(
    val stacks: List<String>,
)

@Serializable
data class ErrorResponse(
    val error: String,
)

private val json = Json { prettyPrint = false }

private fun jsonResponse(
    status: org.http4k.core.Status,
    body: String,
) = Response(status).body(body).header("Content-Type", "application/json")

fun stackRoutes() =
    routes(
        "/api/stacks" bind GET to { _: Request ->
            val stacks = listStacks()
            jsonResponse(OK, json.encodeToString(StackListResponse(stacks)))
        },
        "/api/stacks/{name}" bind GET to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            val file = composeFile(name)
            if (!file.exists()) return@to jsonResponse(NOT_FOUND, json.encodeToString(ErrorResponse("stack not found")))
            val yaml = file.readText()
            jsonResponse(OK, json.encodeToString(StackInfo(name, yaml)))
        },
        "/api/stacks/{name}" bind PUT to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            val body =
                runCatching { json.decodeFromString<PutStackRequest>(req.bodyString()) }.getOrNull()
                    ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid request body")))
            val dir = stackDir(name)
            dir.mkdirs()
            composeFile(name).writeText(body.composeYaml)
            jsonResponse(OK, """{"status":"saved"}""")
        },
        "/api/stacks/{name}/deploy" bind POST to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            if (!composeFile(name).exists()) return@to jsonResponse(NOT_FOUND, json.encodeToString(ErrorResponse("stack not found")))
            val result = runDockerCompose(name, "up", "-d")
            jsonResponse(OK, json.encodeToString(CommandResponse(result.exitCode, result.stdout, result.stderr)))
        },
        "/api/stacks/{name}/stop" bind POST to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            if (!composeFile(name).exists()) return@to jsonResponse(NOT_FOUND, json.encodeToString(ErrorResponse("stack not found")))
            val result = runDockerCompose(name, "down")
            jsonResponse(OK, json.encodeToString(CommandResponse(result.exitCode, result.stdout, result.stderr)))
        },
        "/api/stacks/{name}/pull" bind POST to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            if (!composeFile(name).exists()) return@to jsonResponse(NOT_FOUND, json.encodeToString(ErrorResponse("stack not found")))
            val result = runDockerCompose(name, "pull")
            jsonResponse(OK, json.encodeToString(CommandResponse(result.exitCode, result.stdout, result.stderr)))
        },
        "/api/stacks/{name}/logs" bind GET to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            if (!composeFile(name).exists()) return@to jsonResponse(NOT_FOUND, json.encodeToString(ErrorResponse("stack not found")))
            val tail = req.query("tail") ?: "100"
            val result = runDockerCompose(name, "logs", "--tail", tail, "--no-color")
            jsonResponse(OK, json.encodeToString(CommandResponse(result.exitCode, result.stdout, result.stderr)))
        },
    )
