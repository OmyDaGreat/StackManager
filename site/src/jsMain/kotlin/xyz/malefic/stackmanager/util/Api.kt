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

@Serializable
data class ErrorResponse(
    val error: String,
    val code: String? = null,
)

val json = Json { ignoreUnknownKeys = true }

fun getToken(): String = localStorage.getItem("stackmgr_token") ?: ""

fun setToken(token: String) = localStorage.setItem("stackmgr_token", token)

private fun normalizedBaseUrl(value: String?): String = value?.trim()?.trimEnd('/').orEmpty()

fun getBaseUrl(): String {
    val stored = normalizedBaseUrl(localStorage.getItem("stackmgr_base_url"))
    return if (stored.isNotEmpty()) stored else window.location.origin.trimEnd('/')
}

fun setBaseUrl(url: String) {
    val normalized = normalizedBaseUrl(url)
    if (normalized.isEmpty()) {
        localStorage.removeItem("stackmgr_base_url")
    } else {
        localStorage.setItem("stackmgr_base_url", normalized)
    }
}

fun apiUrl(path: String): String {
    val base = getBaseUrl()
    return if (path.startsWith("/")) "$base$path" else "$base/$path"
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
    // Check if token is missing and redirect to login for API calls (not during login page itself)
    if (getToken().isEmpty() && !url.contains("/api/health") && !url.contains("/api/kobweb-status")) {
        window.location.href = "/login"
        // Return early to prevent further execution
        return ""
    }

    val init =
        RequestInit(
            method = method,
            headers = authHeaders(),
            body = body,
        )
    val response = window.fetch(apiUrl(url), init).await()
    val responseBody = response.text().await()
    val contentType = response.headers.get("Content-Type")?.lowercase() ?: ""

    // Check for 401 or UNAUTHORIZED error code and redirect to login
    if (response.status.toInt() == 401) {
        window.location.href = "/login"
        return ""
    }

    // Try to parse error response to check for UNAUTHORIZED code
    if (!response.ok && contentType.contains("application/json")) {
        try {
            val errorResp = json.decodeFromString<ErrorResponse>(responseBody)
            if (errorResp.code == "UNAUTHORIZED") {
                window.location.href = "/login"
                return ""
            }
        } catch (e: Exception) {
            // Not a valid error response, continue with normal error handling
        }
    }

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
