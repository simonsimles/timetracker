package components

import csstype.Color
import csstype.number
import csstype.px
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

const val MENU_ITEM_HOME = "Home"
const val MENU_ITEM_PROJECTS = "Projects"
const val MENU_ITEM_WORK_LIST = "Work List"
const val MENU_ITEM_MONTHLY_REPORT = "Monthly Report"

external interface MenuProps : Props {
    var selected: String
    var onMenuChange: (String) -> Unit
}

val menu = FC<MenuProps> { props ->
    val menuItems = listOf(
        MENU_ITEM_HOME,
        MENU_ITEM_WORK_LIST,
        MENU_ITEM_PROJECTS//,
        //MENU_ITEM_MONTHLY_REPORT
    )
    Box {
        sx { flexGrow = number(1.0) }
        AppBar {
            position = AppBarPosition.static
            Toolbar {
                variant = ToolbarVariant.regular
                Typography {
                    variant = "h6"
                    noWrap
                    component = div
                    +"Timetracker"
                }
                menuItems.map {
                    Typography {
                        noWrap
                        sx {
                            color = Color("white")
                            paddingLeft = 25.px
                        }
                        onClick = { _ -> props.onMenuChange(it) }
                        +it
                    }
                }
            }
        }
    }
}