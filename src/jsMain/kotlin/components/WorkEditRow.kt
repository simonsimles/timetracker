package components

import csstype.AlignItems
import de.simles.timetracker.display
import de.simles.timetracker.firstWorkDayOfWeek
import de.simles.timetracker.lastWorkDayOfWeek
import de.simles.timetracker.models.*
import kotlinx.datetime.toLocalDate
import kotlinx.js.jso
import mui.icons.material.AlarmOff
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.InputType
import react.dom.onChange
import react.key

val workRowHeader = FC<Props> {
    TableHead {
        TableRow {
            TableCell { +"Date" }
            TableCell { +"Project" }
            TableCell { +"Duration" }
            TableCell { +"Start" }
            TableCell { +"End" }
            TableCell { +"Break" }
        }
    }
}

external interface WorkRowProps : Props {
    var workItem: Work
    var active: Boolean
    var reportActive: (Work) -> Unit
    var projectList: List<String>?
    var saveActiveWork: () -> Unit
}

fun toStringOnlyOnIntervalTimeUnit(time: TimeUnit, propertyGetter: (IntervalUnit) -> Time?) = when (time) {
    is AbsoluteUnit -> ""
    is IntervalUnit -> "${propertyGetter(time)}"
}

external interface MinMax : InputBaseComponentProps {
    var min: String
    var max: String
}

fun minMax(minValue: String, maxValue: String): MinMax = jso {
    min = minValue
    max = maxValue
}

val workEditRow = FC<WorkRowProps> { props ->
    if (!props.active) {
        TableRow {
            hover = true
            onClick = { _ -> props.reportActive(props.workItem) }
            TableCell { +props.workItem.time.date.display() }
            TableCell { +props.workItem.project }
            TableCell { +"${props.workItem.time.getTotalDuration()}" }
            TableCell {
                +toStringOnlyOnIntervalTimeUnit(props.workItem.time) { it.start }
            }
            TableCell {
                +toStringOnlyOnIntervalTimeUnit(props.workItem.time) { it.end }
            }
            TableCell {
                +toStringOnlyOnIntervalTimeUnit(props.workItem.time) { it.pause }
            }
        }
    } else {
        rowEditor {
            projectList = props.projectList
            workItem = props.workItem
            reportActive = props.reportActive
            active = props.active
        }
    }
}

val rowEditor = FC<WorkRowProps> { props ->
    TableRow {
        selected = true
        TableCell {
            TextField {
                variant = FormControlVariant.standard
                label = ReactNode("date")
                type = InputType.date
                value = props.workItem.time.date.toString()
                onChange = { e ->
                    val newUpdatedWork =
                        props.workItem.withDate(e.target.asDynamic().value.toString().toLocalDate())
                    props.reportActive(newUpdatedWork)
                }
                inputProps = minMax(
                    props.workItem.time.date.firstWorkDayOfWeek().toString(),
                    props.workItem.time.date.lastWorkDayOfWeek().toString()
                )
            }
        }
        TableCell {
            TextField {
                variant = FormControlVariant.standard
                label = ReactNode("project")
                onChange = { e ->
                    val newUpdatedWork = props.workItem.withProject(e.target.asDynamic().value)
                    props.reportActive(newUpdatedWork)
                }
                select = true
                value = props.workItem.project
                props.projectList?.map {
                    MenuItem {
                        key = it
                        value = it
                        +it
                    }
                }
            }
        }
        TableCell {
            if (props.workItem.time is AbsoluteUnit) {
                TextField {
                    variant = FormControlVariant.standard
                    value = (props.workItem.time as AbsoluteUnit).duration.toString()
                    onChange = { e ->
                        val newUpdatedWork =
                            props.workItem.withTime(
                                (props.workItem.time as AbsoluteUnit).withDuration(
                                    e.target.asDynamic().value.toString().toTime() ?: Time(0, 0)
                                )
                            )
                        props.reportActive(newUpdatedWork)
                    }
                }
            } else {
                +props.workItem.time.getTotalDuration().toString()
            }
        }
        fun getIntervalUnitTextField(type: IntervalFieldType) = TableCell {
            sx = jso {
                alignItems = AlignItems.baseline
            }
            TextField {
                variant = FormControlVariant.standard
                label = ReactNode(type.toString().lowercase())
                value = type.get(props.workItem.time as IntervalUnit).toString()
                onChange = { e ->
                    val newUpdatedWork =
                        props.workItem.withTime(
                            type.set(
                                props.workItem.time as IntervalUnit,
                                e.target.asDynamic().value.toString().toTime()
                            )
                        )
                    props.reportActive(newUpdatedWork)
                }
            }
            if (type == IntervalFieldType.END && type.get(props.workItem.time as IntervalUnit) == null) {
                IconButton {
                    size = Size.small
                    AlarmOff {
                        fontSize = SvgIconSize.small
                    }
                    onClick = {
                        props.reportActive(
                            props.workItem.withTime(
                                (props.workItem.time as IntervalUnit).withEnd(Time.now())
                            )
                        )
                    }
                }
            }
        }
        if (props.workItem.time is IntervalUnit) {
            getIntervalUnitTextField(IntervalFieldType.START)
        } else {
            TableCell { +"" }
        }
        if (props.workItem.time is IntervalUnit) {
            getIntervalUnitTextField(IntervalFieldType.END)
        } else {
            TableCell { +"" }
        }
        if (props.workItem.time is IntervalUnit) {
            getIntervalUnitTextField(IntervalFieldType.PAUSE)
        } else {
            TableCell { +"" }
        }
    }
}

enum class IntervalFieldType(val get: (IntervalUnit) -> Time?, val set: (IntervalUnit, Time?) -> IntervalUnit) {
    START({ it.start }, { intervalUnit, time -> intervalUnit.withStart(time ?: Time.now()) }),
    END({ it.end }, { intervalUnit, time -> intervalUnit.withEnd(time) }),
    PAUSE({ it.pause }, { intervalUnit, time -> intervalUnit.withPause(time) })
}