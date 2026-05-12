package xyz.malefic.stackmanager.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.BoxShadow
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.lineHeight
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.stackmanager.components.styles.AppStyles

@Page
@Composable
fun AboutPage() {
    Box(Modifier.fillMaxSize().then(AppStyles.pageContentWrap), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .maxWidth(920.px)
                .padding(32.px)
                .borderRadius(18.px)
                .border(2.px, LineStyle.Solid, Color("#00ffc3"))
                .background(rgba(8, 12, 22, .9f))
                .boxShadow(BoxShadow.of(0.px, 0.px, 24.px, color = Color("#00ffc3")))
                .lineHeight(1.8)
                .fontSize(1.1.cssRem)
                .color(Color("#bafef1")),
        ) {
            Span {
                Text("STACK MANAGER // RETRO OPS CONSOLE // built by ")
                Link("https://github.com/OmyDaGreat", "MALEFIC")
                Text(". Manage Docker Compose stacks on your server over Tailscale.")
            }
        }
    }
}
