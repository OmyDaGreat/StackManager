package xyz.malefic.stackmanager.util

enum class Pages(
    val value: String,
    val route: String,
) {
    STACKS("Stacks", "/stacks"),
    SETTINGS("Settings", "/settings"),
    ABOUT("About", "/about"),
}
