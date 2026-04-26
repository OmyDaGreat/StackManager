package xyz.malefic.stackmanager.util

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

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

val json = Json { ignoreUnknownKeys = true }

fun getToken(): String = localStorage.getItem("stackmgr_token") ?: ""

fun setToken(token: String) = localStorage.setItem("stackmgr_token", token)

fun getBaseUrl(): String = localStorage.getItem("stackmgr_base_url") ?: ""

fun setBaseUrl(url: String) = localStorage.setItem("stackmgr_base_url", url)

fun apiUrl(path: String): String {
    val base = getBaseUrl().trimEnd('/')
    return if (base.isEmpty()) path else "$base$path"
}

fun authHeaders(): Headers {
    val h = Headers()
    h.append("Authorization", "Bearer ${getToken()}")
    h.append("Content-Type", "application/json")
    return h
}

suspend fun fetchJson(
    url: String,
    method: String = "GET",
    body: String? = null,
): String {
    val init =
        RequestInit(
            method = method,
            headers = authHeaders(),
            body = body,
        )
    val response = window.fetch(apiUrl(url), init).await()
    return response.text().await()
}
