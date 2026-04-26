plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

val semverTagRegex = Regex("""^v\d+\.\d+\.\d+$""")
val versionFromGitTag = providers.exec {
    commandLine("git", "tag", "-l", "v[0-9]*.[0-9]*.[0-9]*", "--sort=-v:refname")
}.standardOutput.asText.map { tags ->
    tags.lineSequence()
        .map(String::trim)
        .firstOrNull { semverTagRegex.matches(it) }
        ?.removePrefix("v")
        ?: "0.0.0-SNAPSHOT"
}

val repoVersion = providers.gradleProperty("stackmanagerVersion")
    .orElse(providers.environmentVariable("STACKMANAGER_VERSION"))
    .orElse(versionFromGitTag)

allprojects {
    group = "xyz.malefic.stackmanager"
    version = repoVersion.get()
}
