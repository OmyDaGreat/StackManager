package xyz.malefic.stackmanager

import java.io.File

private val STACK_NAME_REGEX = Regex("^[a-z0-9-]+$")

fun isValidStackName(name: String): Boolean = STACK_NAME_REGEX.matches(name)

fun stackDir(name: String): File = File("${Config.COMPOSE_ROOT}/$name")

fun composeFile(name: String): File = File("${Config.COMPOSE_ROOT}/$name/compose.yml")

fun listStacks(): List<String> {
    val root = File(Config.COMPOSE_ROOT)
    if (!root.exists()) return emptyList()
    return root.listFiles { f -> f.isDirectory && File(f, "compose.yml").exists() }
        ?.map { it.name }
        ?.sorted()
        ?: emptyList()
}

data class CommandResult(val exitCode: Int, val stdout: String, val stderr: String)

fun runDockerCompose(stackName: String, vararg args: String): CommandResult {
    val dir = stackDir(stackName)
    val cmd = listOf("docker", "compose") + args.toList()
    val process = ProcessBuilder(cmd)
        .directory(dir)
        .redirectErrorStream(false)
        .start()
    val stdout = process.inputStream.bufferedReader().readText()
    val stderr = process.errorStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    return CommandResult(exitCode, stdout, stderr)
}
