package components

import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.InputType
import react.dom.onChange
import react.useState

external interface TextEditProps : Props {
    var isOpen: Boolean
    var text: String
    var onSave: (String) -> Unit
    var onClose: () -> Unit
}

val textEdit = FC<TextEditProps> { props ->
    var text by useState(props.text)
    Dialog {
        open = props.isOpen
        onClose = { _, _ -> props.onClose() }
        fullWidth = true
        maxWidth = "sm"

        DialogTitle {
            +"Edit Text"
        }
        DialogContent {
            DialogContentText { +"Edit or add a comment" }
            TextField {
                autoFocus = true
                margin = FormControlMargin.dense
                label = ReactNode("Comment")
                type = InputType.text
                fullWidth = true
                variant = FormControlVariant.standard
                multiline = true
                minRows = 4
                value = text
                onChange = { e -> text = e.target.asDynamic().value.toString() }
            }
        }
        DialogActions {
            Button {
                onClick = { props.onClose() }
                +"Close"
            }
            Button {
                onClick = { props.onSave(text) }
                +"Save"
            }
        }

    }
}
