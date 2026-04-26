plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("xyz.malefic.stackmanager.MainKt")
}

dependencies {
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.undertow)
    implementation(libs.http4k.format.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.slf4j.simple)
}
