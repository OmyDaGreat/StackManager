package xyz.malefic.stackmanager.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.BoxShadow
import com.varabyte.kobweb.compose.css.BoxSizing
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.boxSizing
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.stackmanager.components.styles.AppStyles
import xyz.malefic.stackmanager.util.getBaseUrl
import xyz.malefic.stackmanager.util.getToken
import xyz.malefic.stackmanager.util.setBaseUrl
import xyz.malefic.stackmanager.util.setToken

@Page("/settings")
@Composable
fun SettingsPage() {
    val ctx = rememberPageContext()
    var token by remember { mutableStateOf(getToken()) }
    var baseUrl by remember { mutableStateOf(getBaseUrl()) }
    var saved by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().then(AppStyles.pageContentWrap), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .maxWidth(720.px)
                .padding(30.px)
                .then(AppStyles.stackContainer),
            horizontalAlignment = Alignment.Start,
        ) {
            H2(attrs = AppStyles.contentTitle.toAttrs()) { Text("CONTROL LINK // SETTINGS") }

            P(
                attrs = AppStyles.statusText.toAttrs(),
            ) { Text("Set the backend bearer token. Base URL is optional and defaults to this same server.") }

            Label(attrs = AppStyles.pageLabel.toAttrs()) { Text("BEARER TOKEN") }
            Input(
                type = InputType.Password,
                attrs =
                    Modifier
                        .width(100.percent)
                        .padding(10.px, 12.px)
                        .margin(top = 4.px, bottom = 20.px)
                        .boxSizing(BoxSizing.BorderBox)
                        .border(1.px, LineStyle.Solid, Color("#3df3ff"))
                        .borderRadius(10.px)
                        .background(rgba(6, 15, 27, .95f))
                        .color(Color("#bafef1"))
                        .toAttrs {
                            value(token)
                            onInput { token = it.value }
                        },
            )

            Label(attrs = AppStyles.pageLabel.toAttrs()) { Text("BACKEND BASE URL OVERRIDE (optional)") }
            Input(
                type = InputType.Text,
                attrs =
                    Modifier
                        .width(100.percent)
                        .padding(10.px, 12.px)
                        .margin(top = 4.px, bottom = 22.px)
                        .boxSizing(BoxSizing.BorderBox)
                        .border(1.px, LineStyle.Solid, Color("#3df3ff"))
                        .borderRadius(10.px)
                        .background(rgba(6, 15, 27, .95f))
                        .color(Color("#bafef1"))
                        .toAttrs {
                            value(baseUrl)
                            onInput { baseUrl = it.value }
                        },
            )

            Row {
                Button(
                    attrs =
                        Modifier
                            .padding(10.px, 18.px)
                            .margin(right = 10.px)
                            .border(1.px, LineStyle.Solid, Color("#00ffc3"))
                            .borderRadius(12.px)
                            .background(Color("#162f45"))
                            .color(Color("#e5fffa"))
                            .fontWeight(700)
                            .boxShadow(BoxShadow.of(0.px, 0.px, 12.px, color = rgba(0, 255, 195, .2f)))
                            .cursor(Cursor.Pointer)
                            .toAttrs {
                                onClick {
                                    setToken(token)
                                    setBaseUrl(baseUrl)
                                    saved = true
                                }
                            },
                ) { Text("SAVE") }

                Button(
                    attrs =
                        Modifier
                            .padding(10.px, 18.px)
                            .border(1.px, LineStyle.Solid, Color("#00ffc3"))
                            .borderRadius(12.px)
                            .background(Color("#2f113a"))
                            .color(Color("#e5fffa"))
                            .fontWeight(700)
                            .boxShadow(BoxShadow.of(0.px, 0.px, 12.px, color = rgba(0, 255, 195, .2f)))
                            .cursor(Cursor.Pointer)
                            .toAttrs {
                                onClick { ctx.router.navigateTo("/stacks") }
                            },
                ) { Text("GO TO STACKS") }
            }

            if (saved) {
                P(
                    attrs =
                        Modifier
                            .margin(top = 18.px)
                            .padding(10.px, 12.px)
                            .borderRadius(10.px)
                            .border(1.px, LineStyle.Solid, Color("#00ffc3"))
                            .background(rgba(8, 40, 35, .62f))
                            .fontWeight(700)
                            .color(Color("#9fffe2"))
                            .toAttrs(),
                ) { Text("✅ SAVED. SETTINGS STORED IN LOCALSTORAGE.") }
            }
        }
    }
}
