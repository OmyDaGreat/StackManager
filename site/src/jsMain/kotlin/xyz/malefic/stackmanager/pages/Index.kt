package xyz.malefic.stackmanager.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext

@Page
@Composable
fun HomePage() {
    val ctx = rememberPageContext()
    ctx.router.navigateTo("/stacks")
}
