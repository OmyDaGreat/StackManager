package xyz.malefic.stackmanager

import java.io.File
import java.io.IOException

private val STACK_NAME_REGEX = Regex("^[a-z0-9-]+$")

fun isValidStackName(name: String): Boolean = STACK_NAME_REGEX.matches(name)

fun stackDir(name: String): File = File("${Config.COMPOSE_ROOT}/$name")

fun composeFile(name: String): File = File("${Config.COMPOSE_ROOT}/$name/compose.yml")

fun listStacks(): List<String> {
    val root = File(Config.COMPOSE_ROOT)
    if (!root.exists()) return emptyList()
    return root
        .listFiles { f -> f.isDirectory && File(f, "compose.yml").exists() }
        ?.map { it.name }
        ?.sorted()
        ?: emptyList()
}

data class CommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)

class DockerRuntimeUnavailableException(
    message: String,
    cause: Throwable,
) : RuntimeException(message, cause)

fun runDockerCompose(
    stackName: String,
    vararg args: String,
): CommandResult {
    val dir = stackDir(stackName)
    val cmd = listOf(Config.dockerBin, "compose") + args.toList()
    val processBuilder =
        ProcessBuilder(cmd)
            .directory(dir)
            .redirectErrorStream(false)
    Config.dockerHost?.let { processBuilder.environment()["DOCKER_HOST"] = it }
    val process =
        try {
            processBuilder.start()
        } catch (e: IOException) {
            val dockerHostInfo = Config.dockerHost?.let { " and DOCKER_HOST='$it'" } ?: ""
            throw DockerRuntimeUnavailableException(
                "Unable to execute '${cmd.joinToString(" ")}' in '${dir.absolutePath}'. " +
                    "Verify STACKMGR_DOCKER_BIN points to a valid docker CLI binary$dockerHostInfo.",
                e,
            )
        }
    // Capture stdout and stderr on dedicated threads to prevent pipe-buffer
    // deadlocks when either stream produces enough output to fill the OS pipe buffer.
    var stdout = ""
    var stderr = ""
    val stdoutThread = Thread { stdout = process.inputStream.bufferedReader().readText() }
    val stderrThread = Thread { stderr = process.errorStream.bufferedReader().readText() }
    stdoutThread.start()
    stderrThread.start()
    val exitCode = process.waitFor()
    stdoutThread.join()
    stderrThread.join()
    return CommandResult(exitCode, stdout, stderr)
}
