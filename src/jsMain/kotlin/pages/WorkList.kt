package pages

import JsApi
import components.timeSum
import components.weekPicker
import components.workEditRow
import components.workEditRowHeader
import de.simles.timetracker.YearWeek
import de.simles.timetracker.models.AbsoluteUnit
import de.simles.timetracker.models.IntervalUnit
import de.simles.timetracker.models.Time
import de.simles.timetracker.models.Work
import de.simles.timetracker.toTimeUnit
import de.simles.timetracker.toYearWeek
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.attrs
import react.dom.button
import react.dom.div
import react.dom.section
import scope

external interface WorkListState : RState {
    var yearWeek: YearWeek
    var work: Map<Long, Work>?
    var activeWork: Long?
    var projects: List<String>?
}

class WorkList : RComponent<RProps, WorkListState>() {
    private val newWorkId = -1L

    private enum class NewWorkItemType {
        Absolute, Interval
    }

    init {
        state.apply {
            yearWeek = YearWeek.current()
        }
        scope.launch { updateWorkItems() }
        scope.launch {
            val p = JsApi.getProjects().map { it.name }
            setState { projects = p }
        }
    }

    private suspend fun updateWorkItems() {
        val year = state.yearWeek.year
        val week = state.yearWeek.week
        if (year != null && week != null) {
            val receivedWork = JsApi.getWork(year, week).associateBy { it.id!! }
            setState {
                work = receivedWork
            }
        } else {
            setState {
                work = null
            }
        }
    }

    private fun saveActiveWork() {
        val workToSave = state.work?.get(state.activeWork)
        val job: Job
        if (workToSave?.id?.equals(newWorkId) == true) {
            job = scope.launch {
                val addedWork =
                    JsApi.newWork(Work(null, workToSave.project, workToSave.time))
                console.log("Posted work with id ${addedWork.id}")
            }
        } else if (workToSave != null) {
            job = scope.launch {
                val addedWork = JsApi.updateWork(workToSave)
                console.log("Updated work with id ${addedWork.id}")
            }
        } else {
            job = scope.launch { }
        }
        job.invokeOnCompletion {
            scope.launch { updateWorkItems() }
        }
        setState { activeWork = null }
    }

    private fun deleteActiveWork() {
        val workToSave = state.work?.get(state.activeWork)
        scope.launch {
            if (workToSave != null && workToSave.id?.equals(newWorkId) == false) {
                console.log("Deleted ${JsApi.deleteWork(workToSave)}")
            }
        }.invokeOnCompletion {
            scope.launch { updateWorkItems() }
        }
        setState { activeWork = null }
    }

    private fun addNewWorkRow(newWorkItemType: NewWorkItemType) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timeUnit = when (newWorkItemType) {
            NewWorkItemType.Interval -> IntervalUnit(now.date, now.toTimeUnit(), null, null)
            NewWorkItemType.Absolute -> AbsoluteUnit(now.date, Time(0, 0))
        }
        val newWork = Work(newWorkId, state.projects?.first() ?: "", timeUnit)
        val workList = state.work
        setState {
            work = workList?.plus(newWorkId to newWork) ?: mapOf(-1L to newWork)
            activeWork = newWorkId
        }
    }

    override fun RBuilder.render() {
        section(classes = "pt-md-5 pb-md-4 text-center") {
            div(classes = "container align-items-center") {
                div(classes = "row") {
                    weekPicker {
                        value = state.yearWeek
                        onUpdateValue = { yw -> setState { yearWeek = yw }; scope.launch { updateWorkItems() } }
                    }
                }
                div(classes = "row") {
                    div(classes = "list-group") {
                        workEditRowHeader { }
                        state.work?.values?.sortedWith(compareBy({ it.time.date }, { it.id }))?.map {
                            workEditRow {
                                work = it
                                active = (state.activeWork?.equals(it.id)) == true
                                reportActive = {
                                    val newWork = it
                                    val updatedWork = (state.work?.filter { it.key != newWork.id })?.plus(it.id!! to it)
                                    setState {
                                        work = updatedWork
                                        activeWork = it.id
                                    }
                                }
                                projectList = state.projects
                            }
                        }
                    }
                }
                if (state.activeWork != null) {
                    div(classes = "row pt-md-3") {
                        div(classes = "col") {
                            button(type = ButtonType.button, classes = "btn btn-primary") {
                                +"Save"
                                attrs {
                                    onClickFunction = { _ -> saveActiveWork() }
                                }
                            }
                        }
                        div(classes = "col") {
                            button(type = ButtonType.button, classes = "btn btn-danger") {
                                +"Delete"
                                attrs {
                                    onClickFunction = { _ -> deleteActiveWork() }
                                }
                            }
                        }
                        div(classes = "col") {
                            button(type = ButtonType.button, classes = "btn btn-secondary") {
                                +"Cancel"
                                attrs {
                                    onClickFunction = { _ ->
                                        setState { activeWork = null }
                                        scope.launch { updateWorkItems() }
                                    }
                                }
                            }
                        }
                    }
                }
                div(classes = "row pt-md-4") {
                    div(classes = "col") {
                        button(classes = "btn btn-success") {
                            +"Add Interval Time"
                            attrs {
                                type = ButtonType.button
                                onClickFunction = { _ -> addNewWorkRow(NewWorkItemType.Interval) }
                            }
                        }
                    }
                    div(classes = "col") {
                        button(classes = "btn btn-success") {
                            +"Add Absolute Time"
                            attrs {
                                type = ButtonType.button
                                onClickFunction = { _ -> addNewWorkRow(NewWorkItemType.Absolute) }
                            }
                        }
                    }
                }
                timeSum {
                    work = state.work?.values?.toList()
                }
            }
        }
    }
}

fun RBuilder.worklist(handler: RProps.() -> Unit): ReactElement {
    return child(WorkList::class) {
        this.attrs(handler)
    }
}