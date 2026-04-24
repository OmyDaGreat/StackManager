package xyz.malefic.stackmanager.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.stackmanager.util.getBaseUrl
import xyz.malefic.stackmanager.util.getToken
import xyz.malefic.stackmanager.util.setBaseUrl
import xyz.malefic.stackmanager.util.setToken

@Page
@Composable
fun LoginPage() {
    val ctx = rememberPageContext()
    var token by remember { mutableStateOf(getToken()) }
    var baseUrl by remember { mutableStateOf(getBaseUrl()) }
    var saved by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.maxWidth(480.px).padding(24.px),
            horizontalAlignment = Alignment.Start,
        ) {
            H2 { Text("Stack Manager — Settings") }

            P { Text("Set the backend bearer token and (optionally) the backend base URL for Tailscale access.") }

            Label { Text("Bearer Token") }
            Input(
                type = InputType.Password,
                attrs = {
                    value(token)
                    onInput { token = it.value }
                    style {
                        property("width", "100%")
                        property("padding", "8px")
                        property("margin-top", "4px")
                        property("margin-bottom", "12px")
                        property("box-sizing", "border-box")
                    }
                },
            )

            Label { Text("Backend Base URL (optional, e.g. http://100.x.y.z:8080)") }
            Input(
                type = InputType.Text,
                attrs = {
                    value(baseUrl)
                    onInput { baseUrl = it.value }
                    style {
                        property("width", "100%")
                        property("padding", "8px")
                        property("margin-top", "4px")
                        property("margin-bottom", "12px")
                        property("box-sizing", "border-box")
                    }
                },
            )

            Button(attrs = {
                onClick {
                    setToken(token)
                    setBaseUrl(baseUrl)
                    saved = true
                }
                style {
                    property("padding", "8px 16px")
                    property("margin-right", "8px")
                }
            }) { Text("Save") }

            Button(attrs = {
                onClick { ctx.router.navigateTo("/stacks") }
                style { property("padding", "8px 16px") }
            }) { Text("Go to Stacks") }

            if (saved) {
                P { Text("✅ Saved! Settings stored in localStorage.") }
            }
        }
    }
}
