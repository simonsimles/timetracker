package components

import de.simles.timetracker.display
import de.simles.timetracker.firstWorkDayOfWeek
import de.simles.timetracker.lastWorkDayOfWeek
import de.simles.timetracker.models.AbsoluteUnit
import de.simles.timetracker.models.IntervalUnit
import de.simles.timetracker.models.Work
import de.simles.timetracker.models.toTime
import kotlinx.datetime.*
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import react.*
import react.dom.*

val workRowHeader = functionalComponent<RProps> { _ ->
    div(classes = "list-group-item list-group-item-action") {
        div(classes = "row") {
            div(classes = "col") { b { +"Date" } }
            div(classes = "col") { b { +"Project" } }
            div(classes = "col") { b { +"Duration" } }
            div(classes = "col") { b { +"Start" } }
            div(classes = "col") { b { +"End" } }
            div(classes = "col") { b { +"Break" } }
        }
    }
}

external interface WorkRowProps : RProps {
    var work: Work
    var active: Boolean
    var reportActive: (Work) -> Unit
    var projectList: List<String>?
}

val workEditRow = functionalComponent<WorkRowProps> { props ->
    div(classes = "list-group-item list-group-item-action" + (if (props.active) " active" else "")) {
        attrs {
            onClickFunction = { _ -> props.reportActive(props.work) }
        }
        div(classes = "row") {
            div(classes = "col") {
                if (props.active) {
                    input(classes = "form-control") {
                        attrs {
                            type = InputType.date
                            max = props.work.time.date.lastWorkDayOfWeek().toString()
                            min = props.work.time.date.firstWorkDayOfWeek().toString()
                            defaultValue = props.work.time.date.toString()
                            onChangeFunction = { e ->
                                val newUpdatedWork =
                                    props.work.withDate((e.target as HTMLInputElement).value.toLocalDate())
                                props.reportActive(newUpdatedWork)
                            }
                        }
                    }
                } else {
                    +props.work.time.date.display()
                }
            }
            div(classes = "col") {
                if (props.active) {
                    select(classes = "form-select") {
                        props.projectList?.map {
                            option(classes = if (it == props.work.project) "active" else "") {
                                +it
                                attrs {
                                    value = it
                                }
                            }
                        }
                        attrs {
                            onChangeFunction = { e ->
                                val selected = (e.target as HTMLSelectElement).value
                                console.log("Clicked $selected")
                                val newUpdatedWork = props.work.withProject(selected)
                                props.reportActive(newUpdatedWork)
                            }
                        }
                    }
                } else {
                    +props.work.project
                }
            }
            div(classes = "col") {
                if (props.active) {
                    input(classes = "form-control") {
                        attrs {
                            when (props.work.time) {
                                is AbsoluteUnit -> {
                                    defaultValue = (props.work.time as AbsoluteUnit).duration.toString()
                                    onChangeFunction = { e ->
                                        val newUpdatedWork =
                                            props.work.withTime((props.work.time as AbsoluteUnit).withDuration((e.target as HTMLInputElement).value.toTime()))
                                        props.reportActive(newUpdatedWork)
                                    }
                                }
                                is IntervalUnit -> value = props.work.time.getTotalDuration().toString()
                            }
                        }
                    }
                } else {
                    +"${props.work.time.getTotalDuration()}"
                }
            }
            div(classes = "col") {
                if (props.active) {
                    input(classes = "form-control") {
                        attrs {
                            when (props.work.time) {
                                is AbsoluteUnit -> value = ""
                                is IntervalUnit -> {
                                    defaultValue = (props.work.time as IntervalUnit).start.toString()
                                    onChangeFunction = { e ->
                                        val newUpdatedWork =
                                            props.work.withTime((props.work.time as IntervalUnit).withStart((e.target as HTMLInputElement).value.toTime()))
                                        props.reportActive(newUpdatedWork)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    when (props.work.time) {
                        is AbsoluteUnit -> +""
                        is IntervalUnit -> +"${(props.work.time as IntervalUnit).start}"
                    }
                }
            }
            div(classes = "col") {
                if (props.active) {
                    input(classes = "form-control") {
                        attrs {
                            when (props.work.time) {
                                is AbsoluteUnit -> value = ""
                                is IntervalUnit -> {
                                    defaultValue = (props.work.time as IntervalUnit).end.toString()
                                    onChangeFunction = { e ->
                                        val newUpdatedWork =
                                            props.work.withTime((props.work.time as IntervalUnit).withEnd((e.target as HTMLInputElement).value.toTime()))
                                        props.reportActive(newUpdatedWork)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    when (props.work.time) {
                        is AbsoluteUnit -> +""
                        is IntervalUnit -> +"${(props.work.time as IntervalUnit).end}"
                    }
                }
            }
            div(classes = "col") {
                if (props.active) {
                    input(classes = "form-control") {
                        attrs {
                            when (props.work.time) {
                                is AbsoluteUnit -> value = ""
                                is IntervalUnit -> {
                                    defaultValue = (props.work.time as IntervalUnit).pause.toString()
                                    onChangeFunction = { e ->
                                        val newUpdatedWork =
                                            props.work.withTime((props.work.time as IntervalUnit).withPause((e.target as HTMLInputElement).value.toTime()))
                                        props.reportActive(newUpdatedWork)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    when (props.work.time) {
                        is AbsoluteUnit -> +""
                        is IntervalUnit -> +"${(props.work.time as IntervalUnit).pause}"
                    }
                }
            }
        }
    }
}

fun RBuilder.workEditRowHeader(handler: RProps.() -> Unit) = child(workRowHeader) {
    attrs { handler() }
}

fun RBuilder.workEditRow(handler: WorkRowProps.() -> Unit) = child(workEditRow) {
    attrs { handler() }
}
