package xyz.malefic.stackmanager.components.styles

import com.varabyte.kobweb.compose.css.BoxShadow
import com.varabyte.kobweb.compose.css.BoxSizing
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.css.WordBreak
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.boxSizing
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxHeight
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.letterSpacing
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.wordBreak
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba

object AppStyles {
    val pageContentWrap =
        Modifier
            .padding(topBottom = 26.px, leftRight = 20.px)

    val stackContainer =
        Modifier
            .borderRadius(18.px)
            .border(2.px, LineStyle.Solid, Color("#00ffc3"))
            .background(rgba(8, 12, 22, .9f))
            .boxShadow(BoxShadow.of(0.px, 0.px, 24.px, color = Color("#00ffc3")))

    val stacksContainer =
        Modifier
            .borderRadius(18.px)
            .border(2.px, LineStyle.Solid, Color("#00ffc3"))
            .background(rgba(8, 12, 22, .9f))
            .boxShadow(BoxShadow.of(0.px, 0.px, 24.px, color = Color("#00ffc3")))

    val stackListItem =
        Modifier
            .borderRadius(14.px)
            .border(1.px, LineStyle.Solid, Color("#3df3ff"))
            .background(rgba(14, 19, 34, .88f))
            .boxShadow(BoxShadow.of(0.px, 0.px, 12.px, color = rgba(0, 255, 195, .18f)))

    val contentTitle =
        Modifier
            .color(Color("#8bffe0"))
            .fontFamily("\"VT323\", \"Share Tech Mono\", \"JetBrains Mono\", monospace")
            .fontSize(34.px)
            .letterSpacing(2.px)

    private val actionButtonBase =
        Modifier
            .border(1.px, LineStyle.Solid, Color("#00ffc3"))
            .color(Color("#e5fffa"))
            .fontWeight(700)
            .fontFamily("\"Share Tech Mono\", \"JetBrains Mono\", monospace")
            .boxShadow(BoxShadow.of(0.px, 0.px, 12.px, color = rgba(0, 255, 195, .2f)))
            .cursor(Cursor.Pointer)

    fun actionButton(
        background: CSSColorValue,
        withRightMargin: Boolean = false,
    ): Modifier {
        var modifier =
            actionButtonBase
                .padding(10.px, 16.px)
                .borderRadius(10.px)
                .background(background)
        if (withRightMargin) {
            modifier = modifier.margin(right = 10.px)
        }
        return modifier
    }

    fun compactActionButton(background: CSSColorValue): Modifier =
        Modifier
            .padding(8.px, 14.px)
            .border(1.px, LineStyle.Solid, Color("#00ffc3"))
            .borderRadius(10.px)
            .background(background)
            .color(Color("#e5fffa"))
            .fontWeight(700)
            .fontFamily("\"Share Tech Mono\", \"JetBrains Mono\", monospace")
            .boxShadow(BoxShadow.of(0.px, 0.px, 10.px, color = rgba(0, 255, 195, .2f)))
            .cursor(Cursor.Pointer)

    val stackNameInput =
        Modifier
            .width(100.percent)
            .padding(10.px, 12.px)
            .margin(bottom = 18.px)
            .boxSizing(BoxSizing.BorderBox)
            .border(1.px, LineStyle.Solid, Color("#3df3ff"))
            .borderRadius(10.px)
            .background(rgba(6, 15, 27, .95f))
            .color(Color("#bafef1"))
            .fontFamily("\"Share Tech Mono\", \"JetBrains Mono\", monospace")

    val composeTextArea =
        Modifier
            .width(100.percent)
            .height(300.px)
            .padding(12.px)
            .margin(bottom = 20.px)
            .boxSizing(BoxSizing.BorderBox)
            .border(1.px, LineStyle.Solid, Color("#3df3ff"))
            .borderRadius(12.px)
            .background(rgba(5, 14, 28, .95f))
            .color(Color("#b8ffe9"))
            .fontSize(13.px)
            .fontFamily("\"JetBrains Mono\", \"Fira Code\", \"Share Tech Mono\", monospace")

    val pageLabel =
        Modifier
            .color(Color("#89ffd6"))
            .fontFamily("\"Share Tech Mono\", \"JetBrains Mono\", monospace")
            .fontWeight(700)
            .margin(bottom = 8.px)

    val statusText =
        Modifier
            .margin(top = 10.px)
            .padding(10.px, 12.px)
            .borderRadius(10.px)
            .border(1.px, LineStyle.Solid, Color("#ff47d4"))
            .background(rgba(30, 10, 36, .72f))
            .color(Color("#ffd4f6"))
            .fontFamily("\"Share Tech Mono\", \"JetBrains Mono\", monospace")

    val logsOutput =
        Modifier
            .background(rgba(7, 12, 24, .95f))
            .padding(14.px)
            .border(1.px, LineStyle.Solid, Color("#3df3ff"))
            .borderRadius(12.px)
            .overflow(Overflow.Auto, Overflow.Auto)
            .whiteSpace(WhiteSpace.PreWrap)
            .wordBreak(WordBreak.BreakAll)
            .maxHeight(400.px)
            .fontFamily("\"JetBrains Mono\", \"Fira Code\", monospace")
            .color(Color("#b8ffe9"))
}
