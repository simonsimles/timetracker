package components

import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*

const val MENU_ITEM_HOME = "Home"
const val MENU_ITEM_PROJECTS = "Projects"
const val MENU_ITEM_WORK_LIST = "Work List"
const val MENU_ITEM_MONTHLY_REPORT = "Monthly Report"

external interface MenuProps : RProps {
    var selected: String
    var onMenuChange: (String) -> Unit
}

val menu = functionalComponent<MenuProps> { props ->
    val menuItems = listOf(
        MENU_ITEM_HOME,
        MENU_ITEM_WORK_LIST,
        MENU_ITEM_PROJECTS,
        MENU_ITEM_MONTHLY_REPORT
    )
    nav(classes = "navbar navbar-expand-lg navbar-dark bg-dark") {
        a(classes = "navbar-brand ms-2", href = "#") {
            +"Timetracker"
        }
        button(classes = "navbar-toggler", type = ButtonType.button) {
            println(this.attrs)
            this.setProp("data-toggle", "collapse")
            this.setProp("data-target", "#navbarSupportedContent")
            span(classes = "navbar-toggler-icon") { }
        }
        div("collapse navbar-collapse") {
            this.setProp("id", "navbarSupportedContent")
            ul("navbar-nav mr-auto") {
                menuItems.map {
                    li("nav-item") {
                        a(classes = "nav-link ${if (props.selected == it) "active" else ""}", href = "#") {
                            +it
                            attrs {
                                onClickFunction = { _ ->
                                    props.onMenuChange(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.menu(handler: MenuProps.() -> Unit) = child(menu) {
    attrs {
        handler()
    }
}