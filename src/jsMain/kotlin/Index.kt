import components.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.html.role
import pages.home
import pages.monthlyReport
import pages.projects
import pages.worklist
import react.*
import react.dom.attrs
import react.dom.header
import react.dom.main
import react.dom.render

external interface IndexState : RState {
    var page: String
}

val scope = MainScope()

class Index : RComponent<RProps, IndexState>() {
    init {
        state.apply {
            page = MENU_ITEM_HOME
        }
    }

    override fun RBuilder.render() {
        header {
            menu {
                selected = state.page
                onMenuChange = { newPage ->
                    setState {
                        page = newPage
                    }
                }
            }
        }
        main(classes = "flex-shrink-0") {
            attrs {
                role = "main"
            }
            when (state.page) {
                MENU_ITEM_HOME -> {
                    home { }
                }
                MENU_ITEM_PROJECTS -> {
                    projects { }
                }
                MENU_ITEM_WORK_LIST -> {
                    worklist { }
                }
                MENU_ITEM_MONTHLY_REPORT -> {
                    monthlyReport {  }
                }
            }
        }
    }
}

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            child(Index::class) {

            }
        }
    }
}
