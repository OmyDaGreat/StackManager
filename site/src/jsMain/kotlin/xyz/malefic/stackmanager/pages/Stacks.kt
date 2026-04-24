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
import kotlinx.coroutines.await
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.stackmanager.util.StackListResponse
import xyz.malefic.stackmanager.util.fetchJson
import xyz.malefic.stackmanager.util.json

@Page
@Composable
fun StacksPage() {
    val ctx = rememberPageContext()
    var stacks by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        error = ""
        try {
            val resp = fetchJson("/api/stacks").await()
            val result = json.decodeFromString<StackListResponse>(resp)
            stacks = result.stacks
        } catch (e: Exception) {
            error = "Failed to load stacks: ${e.message}"
        }
        loading = false
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        Column(Modifier.maxWidth(800.px).padding(24.px)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                H2 { Text("Docker Stacks") }
                Box(Modifier.margin(left = 16.px)) {
                    Button(attrs = {
                        onClick { ctx.router.navigateTo("/stack/new") }
                        style { property("padding", "8px 16px") }
                    }) { Text("+ New Stack") }
                }
                Box(Modifier.margin(left = 8.px)) {
                    Button(attrs = {
                        onClick { ctx.router.navigateTo("/login") }
                        style { property("padding", "8px 16px") }
                    }) { Text("Settings") }
                }
            }

            when {
                loading -> P { Text("Loading...") }
                error.isNotEmpty() -> P { Text("❌ $error") }
                stacks.isEmpty() -> P { Text("No stacks found. Create your first stack!") }
                else -> {
                    stacks.forEach { name ->
                        Box(
                            Modifier.fillMaxWidth().margin(topBottom = 8.px),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.margin(right = 16.px)) {
                                    H3 { Text(name) }
                                }
                                Button(attrs = {
                                    onClick { ctx.router.navigateTo("/stack/$name") }
                                    style { property("padding", "6px 12px") }
                                }) { Text("Manage") }
                            }
                        }
                    }
                }
            }
        }
    }
}
