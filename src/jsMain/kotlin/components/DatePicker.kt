package components

import de.simles.timetracker.YearMonth
import de.simles.timetracker.YearWeek
import de.simles.timetracker.toYearMonth
import de.simles.timetracker.toYearWeek
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*

external interface DatePickerProps<T> : RProps {
    var value: T
    var onUpdateValue: (T) -> Unit
}

fun <R, S : DatePickerProps<R>> getTypedPicker(inputType: InputType, parser: (String) -> R) =
    functionalComponent<S> { props ->
        div(classes = "row justify-content-md-center mb-3") {
            div(classes = "col-md-auto") {
                label(classes = "form-label") {
                    setProp("for", "inputGroupSelectDate")
                    +inputType.name.uppercase()
                }
                input(classes = "form-control") {
                    attrs {
                        type = inputType
                        id = "inputGroupSelectDate"
                        defaultValue = props.value.toString()
                        onChangeFunction = { event ->
                            val newValue = (event.target as HTMLInputElement).value
                            val newObject = parser(newValue)
                            props.onUpdateValue(newObject)
                        }
                    }
                }
            }
        }
    }


val weekPicker = getTypedPicker(InputType.week, String::toYearWeek)
val monthPicker = getTypedPicker(InputType.month, String::toYearMonth)

fun RBuilder.monthPicker(handler: DatePickerProps<YearMonth>.() -> Unit): ReactElement {
    return child(monthPicker) {
        this.attrs(handler)
    }
}

fun RBuilder.weekPicker(handler: DatePickerProps<YearWeek>.() -> Unit): ReactElement {
    return child(weekPicker) {
        this.attrs(handler)
    }
}
