package components

import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLTextAreaElement
import react.*
import react.dom.*

external interface TextEditProps : RProps {
    var id: String
    var text: String
    var onSave: (String) -> Unit
}

val textEdit = functionalComponent<TextEditProps> { props ->
    val (text, updateText) = useState(props.text)
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
                    textarea(classes = "form-control") {
                        attrs {
                            defaultValue = props.text
                            onChangeFunction = { e ->
                                updateText((e.target as HTMLTextAreaElement).value)
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
                            onClickFunction = { _ -> props.onSave(text) }
                            attributes.put("data-bs-dismiss", "modal")
                        }
                        +"Save"
                    }
                }
            }
        }
    }
}

fun RBuilder.textEdit(handler: TextEditProps.() -> Unit): ReactElement {
    return child(textEdit) {
        this.attrs(handler)
    }
}
