package xyz.malefic.stackmanager.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Background
import com.varabyte.kobweb.compose.css.BackgroundImage
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.css.functions.LinearGradient
import com.varabyte.kobweb.compose.css.functions.linearGradient
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderBottom
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.display
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.flexGrow
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.letterSpacing
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.minWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.right
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.translateY
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.fa.FaBarsStaggered
import com.varabyte.kobweb.silk.components.icons.fa.FaGear
import com.varabyte.kobweb.silk.components.icons.fa.FaInfo
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.selectors.hover
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.s
import org.jetbrains.compose.web.css.textDecoration
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.stackmanager.util.Pages
import com.varabyte.kobweb.compose.ui.graphics.Color as Kolor

val NavBarStyle =
    CssStyle.base {
        Modifier
            .fillMaxWidth()
            .height(74.px)
            .background(
                Background.of(
                    BackgroundImage.of(
                        linearGradient(
                            LinearGradient.Direction.ToRight,
                        ) {
                            add(Color("#060a13"), 0.percent)
                            add(Color("#0b1829"), 48.percent)
                            add(Color("#11081c"), 100.percent)
                        },
                    ),
                ),
            ).boxShadow(0.px, 0.px, 20.px, color = Kolor.rgba(0f, 255f, 195f, .25f))
            .borderBottom(1.px, LineStyle.Solid, Color("#00ffc3"))
    }

val NavItemStyle =
    Modifier
        .padding(10.px, 18.px)
        .margin(0.px, 4.px)
        .borderRadius(10.px)
        .background(Color("#111d31"))
        .border(1.px, LineStyle.Solid, Color("#3df3ff"))
        .boxShadow(0.px, 0.px, 10.px, color = Kolor.rgba(0f, 255f, 195f, .2f))
        .styleModifier {
            textDecoration("none")
        }.letterSpacing(.03.em)
        .color(Color("#dbfff7"))
        .fontSize(13.px)
        .fontWeight(700)
        .transition(Transition.all(0.2.s))
        .whiteSpace(WhiteSpace.NoWrap)

val NavItemHoverStyle =
    CssStyle {
        base {
            NavItemStyle
        }

        hover {
            Modifier
                .background(Color("#172a45"))
                .color(Color("#88ffe2"))
                .translateY((-2).px)
        }
    }

val ActiveNavItemStyle =
    CssStyle.base {
        NavItemStyle
            .background(Color("#2b1137"))
            .color(Color("#ff9ef0"))
            .fontWeight(700)
    }

val DropdownStyle =
    CssStyle.base {
        Modifier
            .position(Position.Relative)
            .display(DisplayStyle.InlineBlock)
    }

val DropdownContentStyle =
    CssStyle.base {
        Modifier
            .position(Position.Absolute)
            .top(100.percent)
            .right(0.px)
            .background(Color("#0b1628"))
            .minWidth(180.px)
            .boxShadow(0.px, 0.px, 14.px, color = Kolor.rgba(0f, 255f, 195f, .18f))
            .borderRadius(10.px)
            .border(1.px, LineStyle.Solid, Color("#3df3ff"))
            .zIndex(1000)
            .padding(8.px, 0.px)
    }

val DropdownItemStyle =
    Modifier
        .display(DisplayStyle.Block)
        .padding(10.px, 16.px)
        .styleModifier {
            textDecoration("none")
        }.color(Color("#c9fff1"))
        .fontSize(14.px)
        .transition(Transition.of("background-color", .15.s))
        .whiteSpace(WhiteSpace.NoWrap)

val DropdownItemHoverStyle =
    CssStyle {
        base {
            DropdownItemStyle
        }

        hover {
            Modifier.background(Color("#12283f"))
        }
    }

val DropdownButtonStyle =
    Modifier
        .padding(12.px, 16.px)
        .margin(0.px, 4.px)
        .borderRadius(10.px)
        .background(Color("#1b0f2c"))
        .border(1.px, LineStyle.Solid, Color("#00ffc3"))
        .boxShadow(0.px, 0.px, 10.px, color = Kolor.rgba(0f, 255f, 195f, .2f))
        .color(Color("#d8fff6"))
        .fontSize(13.px)
        .fontWeight(700)
        .cursor(Cursor.Pointer)
        .transition(Transition.all(.2.s))
        .whiteSpace(WhiteSpace.NoWrap)
        .letterSpacing(.03.em)

val DropdownButtonHoverStyle =
    CssStyle {
        base {
            DropdownButtonStyle
        }

        hover {
            Modifier
                .background(Color("#28153e"))
                .border {
                    color(Color("#3df3ff"))
                }.translateY((-2).px)
        }
    }

@Layout
@Composable
fun NavBarLayout(content: @Composable () -> Unit) {
    val ctx = rememberPageContext()
    val currentRoute = ctx.route.path

    Column(Modifier.fillMaxSize()) {
        Box(
            NavBarStyle.toModifier(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .maxWidth(1200.px)
                    .padding(0.px, 20.px),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.flexGrow(1)) {
                    Box(
                        Modifier
                            .padding(10.px, 14.px)
                            .borderRadius(10.px)
                            .display(DisplayStyle.InlineBlock)
                            .background(Color("#101e32"))
                            .border(1.px, LineStyle.Solid, Color("#00ffc3"))
                            .letterSpacing(.08.em)
                            .fontSize(14.px)
                            .color(Color("#8effdd"))
                            .fontWeight(700),
                    ) {
                        Text("STACK MANAGER")
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pages.entries.forEach { page ->
                        val isActive = page.isCurrentPage(currentRoute)
                        val pageRoute = page.route

                        Link(
                            path = pageRoute,
                            modifier =
                                if (isActive) {
                                    ActiveNavItemStyle.toModifier()
                                } else {
                                    NavItemHoverStyle.toModifier()
                                },
                        ) {
                            when (page) {
                                Pages.STACKS -> FaBarsStaggered()
                                Pages.SETTINGS -> FaGear()
                                Pages.ABOUT -> FaInfo()
                            }
                        }
                    }
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            content()
        }
    }
}

private fun Pages.isCurrentPage(currentRoute: String): Boolean =
    when (this) {
        Pages.STACKS -> currentRoute == route || currentRoute == "" || currentRoute == "/"
        else -> currentRoute == route
    }
