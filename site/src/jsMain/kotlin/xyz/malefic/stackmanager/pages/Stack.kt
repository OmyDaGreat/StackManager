package xyz.malefic.stackmanager.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Pre
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import xyz.malefic.stackmanager.util.CommandResponse
import xyz.malefic.stackmanager.util.PutStackRequest
import xyz.malefic.stackmanager.util.StackInfo
import xyz.malefic.stackmanager.util.fetchJson
import xyz.malefic.stackmanager.util.json

@Page("/stack/{name}")
@Composable
fun StackPage() {
    val ctx = rememberPageContext()
    val name = ctx.route.params["name"] ?: "new"
    val isNew = name == "new"

    var stackName by remember { mutableStateOf(if (isNew) "" else name) }
    var composeYaml by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(!isNew) }

    LaunchedEffect(name) {
        if (!isNew) {
            loading = true
            try {
                val resp = fetchJson("/api/stacks/$name")
                val info = json.decodeFromString<StackInfo>(resp)
                composeYaml = info.composeYaml
            } catch (e: Exception) {
                status = "Failed to load stack: ${e.message}"
            }
            loading = false
        }
    }

    suspend fun executeStackCommand(
        path: String,
        method: String = "POST",
        body: String? = null,
    ): String =
        try {
            val resp = fetchJson(path, method, body)
            val result = json.decodeFromString<CommandResponse>(resp)
            "Exit ${result.exitCode}\n${result.stdout}${if (result.stderr.isNotBlank()) "\nSTDERR:\n${result.stderr}" else ""}"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        Column(Modifier.maxWidth(900.px).padding(24.px)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                H2 { Text(if (isNew) "New Stack" else "Stack: $name") }
                Box(Modifier.margin(left = 16.px)) {
                    Button(attrs = {
                        onClick { ctx.router.navigateTo("/stacks") }
                        style { property("padding", "6px 12px") }
                    }) { Text("← Back") }
                }
            }

            if (isNew) {
                Label { Text("Stack name (lowercase letters, digits, hyphens only):") }
                org.jetbrains.compose.web.dom.Input(
                    type = InputType.Text,
                    attrs = {
                        value(stackName)
                        onInput { stackName = it.value }
                        style {
                            property("width", "100%")
                            property("padding", "8px")
                            property("margin-bottom", "12px")
                            property("box-sizing", "border-box")
                        }
                    },
                )
            }

            Label { Text("compose.yml") }
            TextArea(
                value = composeYaml,
                attrs = {
                    onInput { composeYaml = it.value }
                    style {
                        property("width", "100%")
                        property("height", "300px")
                        property("font-family", "monospace")
                        property("font-size", "13px")
                        property("padding", "8px")
                        property("box-sizing", "border-box")
                        property("margin-bottom", "12px")
                    }
                },
            )

            Row {
                val effectiveName = if (isNew) stackName else name

                Button(attrs = {
                    onClick {
                        MainScope().launch {
                            if (effectiveName.isBlank()) {
                                status = "Stack name is required"
                                return@launch
                            }
                            if (!effectiveName.matches(Regex("^[a-z0-9-]+$"))) {
                                status = "Invalid stack name (use only lowercase letters, digits, hyphens)"
                                return@launch
                            }
                            val body = json.encodeToString(PutStackRequest(composeYaml))
                            status =
                                try {
                                    fetchJson("/api/stacks/$effectiveName", "PUT", body)
                                    "✅ Saved!"
                                } catch (e: Exception) {
                                    "Error: ${e.message}"
                                }
                            if (isNew) ctx.router.navigateTo("/stack/$effectiveName")
                        }
                    }
                    style {
                        property("padding", "8px 16px")
                        property("margin-right", "8px")
                    }
                }) { Text("Save") }

                if (!isNew) {
                    Button(attrs = {
                        onClick {
                            MainScope().launch {
                                status = "Deploying..."
                                status = executeStackCommand("/api/stacks/$name/deploy")
                            }
                        }
                        style {
                            property("padding", "8px 16px")
                            property("margin-right", "8px")
                        }
                    }) { Text("Deploy") }

                    Button(attrs = {
                        onClick {
                            MainScope().launch {
                                status = "Stopping..."
                                status = executeStackCommand("/api/stacks/$name/stop")
                            }
                        }
                        style {
                            property("padding", "8px 16px")
                            property("margin-right", "8px")
                        }
                    }) { Text("Stop") }

                    Button(attrs = {
                        onClick {
                            MainScope().launch {
                                status = "Pulling..."
                                status = executeStackCommand("/api/stacks/$name/pull")
                            }
                        }
                        style {
                            property("padding", "8px 16px")
                            property("margin-right", "8px")
                        }
                    }) { Text("Pull") }

                    Button(attrs = {
                        onClick {
                            MainScope().launch {
                                logs = "Loading logs..."
                                logs =
                                    try {
                                        val result = json.decodeFromString<CommandResponse>(fetchJson("/api/stacks/$name/logs"))
                                        result.stdout + (if (result.stderr.isNotBlank()) "\nSTDERR:\n${result.stderr}" else "")
                                    } catch (e: Exception) {
                                        "Error: ${e.message}"
                                    }
                            }
                        }
                        style { property("padding", "8px 16px") }
                    }) { Text("View Logs") }
                }
            }

            if (status.isNotBlank()) {
                P { Text(status) }
            }

            if (logs.isNotBlank()) {
                H4 { Text("Logs") }
                Pre(attrs = {
                    style {
                        property("background", "#f4f4f4")
                        property("padding", "12px")
                        property("overflow-x", "auto")
                        property("white-space", "pre-wrap")
                        property("word-break", "break-all")
                        property("max-height", "400px")
                        property("overflow-y", "auto")
                    }
                }) { Text(logs) }
            }
        }
    }
}
