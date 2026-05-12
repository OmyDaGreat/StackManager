package xyz.malefic.stackmanager

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.style.common.SmoothColorStyle
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.Color
import com.varabyte.kobweb.compose.ui.styleModifier

@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {
    SilkApp {
        Surface(
            SmoothColorStyle
                .toModifier()
                .minHeight(100.vh)
                .color(Color("#bafef1"))
                .styleModifier {
                    property(
                        "background",
                        "linear-gradient(150deg, #060a13 0%, #0b1829 46%, #130925 100%)",
                    )
                    property(
                        "background-image",
                        "repeating-linear-gradient(0deg, rgba(0, 255, 195, 0.08) 0, rgba(0, 255, 195, 0.08) 1px, transparent 1px, transparent 3px), " +
                            "radial-gradient(circle at 12% 18%, rgba(0, 255, 195, 0.16) 0, transparent 24%), " +
                            "radial-gradient(circle at 80% 12%, rgba(61, 243, 255, 0.17) 0, transparent 20%), " +
                            "radial-gradient(circle at 34% 84%, rgba(255, 71, 212, 0.14) 0, transparent 26%)",
                    )
                    property("font-family", "\"Share Tech Mono\", \"JetBrains Mono\", \"Lucida Console\", monospace")
                },
        ) {
            content()
        }
    }
}
