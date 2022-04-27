import components.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import pages.Home
import pages.MonthlyReport
import pages.Projects
import pages.WorkList
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.main
import react.dom.render
import react.useState

val scope = MainScope()

val Index = FC<Props> {
    var page by useState(MENU_ITEM_HOME)

    header {
        menu {
            selected = page
            onMenuChange = { newPage -> page = newPage }
        }
    }
    main {
        when (page) {
            MENU_ITEM_HOME -> {
                Home { }
            }
            MENU_ITEM_PROJECTS -> {
                Projects { }
            }
            MENU_ITEM_WORK_LIST -> {
                WorkList { }
            }
            MENU_ITEM_MONTHLY_REPORT -> {
                MonthlyReport { }
            }
            else -> {}
        }
    }
}

fun main() {
    window.onload = {
        render(Index.create(), document.getElementById("root") ?: error("No root"))
    }
}
