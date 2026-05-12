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
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Pre
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import xyz.malefic.stackmanager.components.styles.AppStyles
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

    Box(Modifier.fillMaxSize().then(AppStyles.pageContentWrap), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .maxWidth(980.px)
                .padding(30.px)
                .then(AppStyles.stackContainer),
        ) {
            Row(Modifier.fillMaxWidth().margin(bottom = 22.px), verticalAlignment = Alignment.CenterVertically) {
                H2(attrs = AppStyles.contentTitle.toAttrs()) { Text(if (isNew) "NEW STACK // DRAFT" else "STACK // ${name.uppercase()}") }
                Box(Modifier.margin(left = 16.px)) {
                    Button(
                        attrs =
                            AppStyles.compactActionButton(Color("#12364f")).toAttrs {
                                onClick { ctx.router.navigateTo("/stacks") }
                            },
                    ) { Text("← BACK") }
                }
            }

            if (isNew) {
                Label(attrs = AppStyles.pageLabel.toAttrs()) { Text("STACK NAME (lowercase letters, digits, hyphens only):") }
                org.jetbrains.compose.web.dom.Input(
                    type = InputType.Text,
                    attrs =
                        AppStyles.stackNameInput.toAttrs {
                            value(stackName)
                            onInput { stackName = it.value }
                        },
                )
            }

            Label(attrs = AppStyles.pageLabel.toAttrs()) { Text("COMPOSE.YML") }
            TextArea(
                value = composeYaml,
                attrs =
                    AppStyles.composeTextArea.toAttrs {
                        onInput { composeYaml = it.value }
                    },
            )

            Row(
                Modifier
                    .margin(bottom = 12.px)
                    .styleModifier { property("flex-wrap", "wrap") },
            ) {
                val effectiveName = if (isNew) stackName else name

                Button(
                    attrs =
                        AppStyles.actionButton(Color("#162f45"), withRightMargin = true).toAttrs {
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
                        },
                ) { Text("Save") }

                if (!isNew) {
                    Button(
                        attrs =
                            AppStyles.actionButton(Color("#103526"), withRightMargin = true).toAttrs {
                                onClick {
                                    MainScope().launch {
                                        status = "Deploying..."
                                        status = executeStackCommand("/api/stacks/$name/deploy")
                                    }
                                }
                            },
                    ) { Text("Deploy") }

                    Button(
                        attrs =
                            AppStyles.actionButton(Color("#37210f"), withRightMargin = true).toAttrs {
                                onClick {
                                    MainScope().launch {
                                        status = "Stopping..."
                                        status = executeStackCommand("/api/stacks/$name/stop")
                                    }
                                }
                            },
                    ) { Text("Stop") }

                    Button(
                        attrs =
                            AppStyles.actionButton(Color("#302f0e"), withRightMargin = true).toAttrs {
                                onClick {
                                    MainScope().launch {
                                        status = "Pulling..."
                                        status = executeStackCommand("/api/stacks/$name/pull")
                                    }
                                }
                            },
                    ) { Text("Pull") }

                    Button(
                        attrs =
                            AppStyles.actionButton(Color("#2f113a")).toAttrs {
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
                            },
                    ) { Text("VIEW LOGS") }
                }
            }

            if (status.isNotBlank()) {
                P(attrs = AppStyles.statusText.toAttrs()) { Text(status) }
            }

            if (logs.isNotBlank()) {
                H4(attrs = AppStyles.pageLabel.toAttrs()) { Text("LOGS") }
                Pre(attrs = AppStyles.logsOutput.toAttrs()) { Text(logs) }
            }
        }
    }
}
