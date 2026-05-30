package xyz.malefic.stackmanager.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.flexGrow
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.stackmanager.components.styles.AppStyles
import xyz.malefic.stackmanager.util.StackListResponse
import xyz.malefic.stackmanager.util.fetchJson
import xyz.malefic.stackmanager.util.json

@Page
@Composable
fun StacksPage() {
    val ctx = rememberPageContext()
    var stacks by remember { mutableStateOf<List<String>>(emptyList()) }
    var pendingStacks by remember { mutableStateOf<List<String>?>(null) }
    var pendingAdded by remember { mutableStateOf(0) }
    var pendingRemoved by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        error = ""
        try {
            val resp = fetchJson("/api/stacks")
            val result = json.decodeFromString<StackListResponse>(resp)
            stacks = result.stacks
            pendingStacks = null
        } catch (e: Exception) {
            error = "Failed to load stacks: ${e.message}"
        }
        loading = false

        // Auto-poll for external changes every 10 seconds
        while (true) {
            delay(10000)
            try {
                val resp = fetchJson("/api/stacks")
                val result = json.decodeFromString<StackListResponse>(resp)
                val fetched = result.stacks
                // Only surface pending changes if the fetched list differs from the displayed list
                if (fetched != stacks) {
                    val added = fetched.filterNot { it in stacks }
                    val removed = stacks.filterNot { it in fetched }
                    pendingAdded = added.size
                    pendingRemoved = removed.size
                    pendingStacks = fetched
                    // clear transient error
                    error = ""
                }
            } catch (e: Exception) {
                // Don't interrupt polling on error; surface last error
                error = "Failed to refresh stacks: ${e.message}"
            }
        }
    }

    Box(Modifier.fillMaxSize().then(AppStyles.pageContentWrap), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .maxWidth(980.px)
                .padding(30.px)
                .then(AppStyles.stacksContainer),
        ) {
            Row(
                Modifier.fillMaxWidth().margin(bottom = 22.px),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                H2(attrs = AppStyles.contentTitle.toAttrs()) { Text("DOCKER STACKS // GRID") }
                Box(Modifier.margin(left = 16.px)) {
                    Button(
                        attrs =
                            AppStyles.actionButton(Color("#162f45")).toAttrs {
                                onClick { ctx.router.navigateTo("/stack/new") }
                            },
                    ) { Text("+ NEW STACK") }
                }
                Box(Modifier.margin(left = 8.px)) {
                    Button(
                        attrs =
                            AppStyles.actionButton(Color("#1f6f3f")).toAttrs {
                                onClick {
                                    MainScope().launch {
                                        loading = true
                                        error = ""
                                        try {
                                            val resp = fetchJson("/api/stacks")
                                            val result = json.decodeFromString<StackListResponse>(resp)
                                            stacks = result.stacks
                                        } catch (e: Exception) {
                                            error = "Failed to load stacks: ${e.message}"
                                        }
                                        loading = false
                                    }
                                }
                            },
                    ) { Text("REFRESH") }
                }
                Box(Modifier.margin(left = 8.px)) {
                    Button(
                        attrs =
                            AppStyles.actionButton(Color("#2f113a")).toAttrs {
                                onClick { ctx.router.navigateTo("/settings") }
                            },
                    ) { Text("SETTINGS") }
                }
            }

            // Pending external changes banner
            if (pendingStacks != null) {
                Row(Modifier.fillMaxWidth().margin(bottom = 12.px), verticalAlignment = Alignment.CenterVertically) {
                    val parts = mutableListOf<String>()
                    if (pendingAdded > 0) parts.add("$pendingAdded new")
                    if (pendingRemoved > 0) parts.add("$pendingRemoved removed")
                    P(attrs = AppStyles.statusText.toAttrs()) { Text(parts.joinToString(", ") + " detected") }
                    Box(Modifier.margin(left = 12.px)) {
                        Button(attrs = AppStyles.compactActionButton(Color("#12364f")).toAttrs {
                            onClick {
                                stacks = pendingStacks!!
                                pendingStacks = null
                                pendingAdded = 0
                                pendingRemoved = 0
                            }
                        }) { Text("APPLY") }
                    }
                    Box(Modifier.margin(left = 8.px)) {
                        Button(attrs = AppStyles.compactActionButton(Color("#6f6f6f")).toAttrs {
                            onClick {
                                pendingStacks = null
                                pendingAdded = 0
                                pendingRemoved = 0
                            }
                        }) { Text("DISMISS") }
                    }
                }
            }

            when {
                loading -> {
                    P(attrs = AppStyles.statusText.toAttrs()) { Text("Loading stack registry...") }
                }

                error.isNotEmpty() -> {
                    P(attrs = AppStyles.statusText.toAttrs()) { Text("❌ $error") }
                }

                stacks.isEmpty() -> {
                    P(attrs = AppStyles.statusText.toAttrs()) { Text("No stacks found. Create your first stack.") }
                }

                else -> {
                    stacks.forEach { name ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .margin(topBottom = 14.px)
                                .padding(16.px, 18.px)
                                .then(AppStyles.stackListItem),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.flexGrow(1).margin(right = 18.px)) {
                                    H3(attrs = Modifier.color(Color("#9fffe2")).toAttrs()) { Text(name.uppercase()) }
                                }
                                Button(
                                    attrs =
                                        AppStyles.compactActionButton(Color("#12364f")).toAttrs {
                                            onClick { ctx.router.navigateTo("/stack/$name") }
                                        },
                                ) { Text("MANAGE") }
                            }
                        }
                    }
                }
            }
        }
    }
}
