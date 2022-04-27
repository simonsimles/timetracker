package components

import csstype.*
import de.simles.timetracker.toYearMonth
import de.simles.timetracker.toYearWeek
import kotlinx.js.jso
import mui.material.Box
import mui.material.Container
import mui.material.FormControlVariant
import mui.material.TextField
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.onChange

external interface DatePickerProps<T> : Props {
    var value: T
    var onUpdateValue: (T) -> Unit
}

fun <R, S : DatePickerProps<R>> getTypedPicker(inputType: InputType, parser: (String) -> R) =
    FC<S> { props ->
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.center
                alignItems = AlignItems.center
            }
            TextField {
                variant = FormControlVariant.standard
                label = ReactNode(inputType.toString().uppercase())
                type = inputType
                defaultValue = props.value.toString()
                onChange = { event ->
                    val newValue = event.target.asDynamic().value.toString()
                    val newObject = parser(newValue)
                    props.onUpdateValue(newObject)
                }
            }
        }
    }

val weekPicker = getTypedPicker(InputType.week, String::toYearWeek)
val monthPicker = getTypedPicker(InputType.month, String::toYearMonth)
