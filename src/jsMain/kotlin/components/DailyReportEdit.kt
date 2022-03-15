package components

import de.simles.timetracker.models.Time
import de.simles.timetracker.models.toTime
import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import react.*
import react.dom.*

external interface DailyReportEditProps : RProps {
    var id: String
    var time: Time
    var comment: String
    var onSave: (String, Time) -> Unit
}

val dailyReportEdit = functionalComponent<DailyReportEditProps> { props ->
    val (comment, updateComment) = useState(props.comment)
    val (time, updateTime) = useState(props.time)
    div(classes = "modal") {
        attrs {
            id = props.id
        }
        div(classes = "modal-dialog") {
            div(classes = "modal-content") {
                div(classes = "modal-header") {
                    h5(classes = "modal-title") {
                        +"Edit text"
                    }
                    button(type = ButtonType.button, classes = "btn-close") {
                        attrs {
                            attributes.put("data-bs-dismiss", "modal")
                        }
                    }
                }
                div(classes = "modal-body") {
                    input(classes = "form-control") {
                        attrs {
                            defaultValue = props.time.toString()
                            onChangeFunction = { e ->
                                updateTime((e.target as HTMLInputElement).value.toTime())
                            }
                        }
                    }
                    textarea(classes = "form-control") {
                        attrs {
                            defaultValue = props.comment
                            onChangeFunction = { e ->
                                updateComment((e.target as HTMLTextAreaElement).value)
                            }
                        }
                    }
                }
                div(classes = "modal-footer") {
                    button {
                        attrs {
                            type = ButtonType.button
                            classes = setOf("btn", "btn-secondary")
                            attributes.put("data-bs-dismiss", "modal")
                        }
                        +"Close"
                    }
                    button {
                        attrs {
                            type = ButtonType.button
                            classes = setOf("btn", "btn-primary")
                            onClickFunction = { _ -> props.onSave(comment, time) }
                            attributes.put("data-bs-dismiss", "modal")
                        }
                        +"Save"
                    }
                }
            }
        }
    }
}

fun RBuilder.dailyReportEdit(handler: DailyReportEditProps.() -> Unit): ReactElement {
    return child(dailyReportEdit) {
        this.attrs(handler)
    }
}
