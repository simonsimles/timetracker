package components

import csstype.ClassName
import de.simles.timetracker.models.Time
import de.simles.timetracker.models.toTime
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import react.*
import react.dom.*
import react.dom.html.ButtonType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h5
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.textarea

external interface DailyReportEditProps : Props {
    var id: String
    var time: Time
    var comment: String
    var onSave: (String, Time) -> Unit
}

val dailyReportEdit = FC<DailyReportEditProps> { props ->
    val (comment, updateComment) = useState(props.comment)
    val (time, updateTime) = useState(props.time)
    div {
        className = ClassName("modal")
        id = props.id
        div {
            className = ClassName("modal-dialog")
            div {
                className = ClassName("modal-content")
                div {
                    className = ClassName("modal-header")
                    h5 {
                        className = ClassName("modal-title")
                        +"Edit text"
                    }
                    button {
                        type = ButtonType.button
                        className = ClassName("btn-close")
                        /*attrs {
                            attributes.put("data-bs-dismiss", "modal")
                        }*/
                    }
                }
                div {
                    className = ClassName("modal-body")
                    input {
                        className = ClassName("form-control")
                        defaultValue = props.time.toString()
                        onChange = { e ->
                            updateTime(e.target.value.toTime() ?: Time(0, 0))
                        }
                    }
                }
                textarea {
                    className = ClassName("form-control")
                    defaultValue = props.comment
                    onChange = { e ->
                        updateComment(e.target.value)
                    }
                }
            }
            div {
                className = ClassName("modal-footer")
                button {
                    type = ButtonType.button
                    className = ClassName("btn btn-secondary")
                    //attributes.put("data-bs-dismiss", "modal")
                    +"Close"
                }
                button {
                    type = ButtonType.button
                    className = ClassName("btn btn-primary")
                    onClick = { _ -> props.onSave(comment, time) }
                    //attributes.put("data-bs-dismiss", "modal")
                    +"Save"
                }
            }
        }
    }
}
