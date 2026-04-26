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

private fun normalizedBaseUrl(value: String): String = value.trim().trimEnd('/')

private fun requireBaseUrl(): String {
    val existing = normalizedBaseUrl(getBaseUrl())
    if (existing.isNotEmpty()) return existing

    val entered =
        normalizedBaseUrl(
            window.prompt(
                "Enter your backend server URL (e.g. http://100.x.y.z:8080)",
                "",
            ) ?: "",
        )
    if (entered.isEmpty()) {
        throw IllegalStateException("Backend server URL is required before continuing. Open Settings to configure it.")
    }
    setBaseUrl(entered)
    return entered
}

fun apiUrl(path: String): String {
    val base = requireBaseUrl()
    return "$base$path"
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
    val responseBody = response.text().await()
    val contentType = response.headers.get("Content-Type")?.lowercase() ?: ""

    if (!response.ok) {
        throw IllegalStateException("API request failed (${response.status} ${response.statusText}): $responseBody")
    }

    if (!contentType.contains("application/json")) {
        val isHtml = responseBody.trimStart().startsWith("<")
        val details =
            if (isHtml) {
                "Received HTML instead of JSON. Check your backend base URL in Settings (e.g. http://100.x.y.z:8080)."
            } else {
                "Received non-JSON response (Content-Type: ${if (contentType.isEmpty()) "missing" else contentType})."
            }
        throw IllegalStateException(details)
    }

    return responseBody
}
