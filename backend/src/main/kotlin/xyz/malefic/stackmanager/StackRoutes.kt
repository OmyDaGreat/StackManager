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
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
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

private fun composeCommandResponse(
    stackName: String,
    vararg args: String,
): Response =
    try {
        val result = runDockerCompose(stackName, *args)
        jsonResponse(OK, json.encodeToString(CommandResponse(result.exitCode, result.stdout, result.stderr)))
    } catch (e: DockerRuntimeUnavailableException) {
        jsonResponse(
            SERVICE_UNAVAILABLE,
            json.encodeToString(ErrorResponse(e.message ?: "docker runtime unavailable")),
        )
    }

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
            composeCommandResponse(name, "up", "-d")
        },
        "/api/stacks/{name}/stop" bind POST to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            if (!composeFile(name).exists()) return@to jsonResponse(NOT_FOUND, json.encodeToString(ErrorResponse("stack not found")))
            composeCommandResponse(name, "down")
        },
        "/api/stacks/{name}/pull" bind POST to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            if (!composeFile(name).exists()) return@to jsonResponse(NOT_FOUND, json.encodeToString(ErrorResponse("stack not found")))
            composeCommandResponse(name, "pull")
        },
        "/api/stacks/{name}/logs" bind GET to { req: Request ->
            val name = req.path("name") ?: return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("missing name")))
            if (!isValidStackName(name)) return@to jsonResponse(BAD_REQUEST, json.encodeToString(ErrorResponse("invalid stack name")))
            if (!composeFile(name).exists()) return@to jsonResponse(NOT_FOUND, json.encodeToString(ErrorResponse("stack not found")))
            val tail = req.query("tail") ?: "100"
            composeCommandResponse(name, "logs", "--tail", tail, "--no-color")
        },
    )
