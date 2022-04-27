package pages

import JsApi
import components.timeSum
import components.weekPicker
import components.workEditRow
import components.workRowHeader
import csstype.*
import de.simles.timetracker.YearWeek
import de.simles.timetracker.models.AbsoluteUnit
import de.simles.timetracker.models.IntervalUnit
import de.simles.timetracker.models.Time
import de.simles.timetracker.models.Work
import de.simles.timetracker.toTimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.js.jso
import mui.material.*
import mui.material.Size
import mui.system.ResponsiveStyleValue
import mui.system.sx
import react.*
import scope

enum class NewWorkItemType {
    Absolute, Interval
}

@OptIn(ExperimentalCoroutinesApi::class)
val WorkList = FC<Props> {
    var yearWeek: YearWeek by useState(YearWeek.current())
    var work: Map<Long, Work> by useState(emptyMap())
    var activeWork: Long? by useState()
    var projects: List<String>? by useState()
    val newWorkId = Long.MAX_VALUE

    suspend fun updateWorkItems(yearWeekOverride: YearWeek? = null) {
        val year = (yearWeekOverride ?: yearWeek).year
        val week = (yearWeekOverride ?: yearWeek).week
        work = (if (year != null && week != null) {
            val receivedWork = JsApi.getWork(year, week).associateBy { it.id!! }
            receivedWork
        } else {
            emptyMap()
        })
    }
    useEffect(emptyList<String>()) {
        scope.launch { updateWorkItems() }
        scope.launch {
            val p = JsApi.getProjects().map { it.name }
            projects = p
        }

    }

    fun saveActiveWork() {
        val workToSave = work[activeWork]
        val job: Job = if (workToSave?.id?.equals(newWorkId) == true) {
            scope.launch {
                val addedWork =
                    JsApi.newWork(Work(null, workToSave.project, workToSave.time))
                console.log("Posted work with id ${addedWork.id}")
            }
        } else if (workToSave != null) {
            scope.launch {
                val addedWork = JsApi.updateWork(workToSave)
                console.log("Updated work with id ${addedWork.id}")
            }
        } else {
            scope.launch { }
        }
        job.invokeOnCompletion {
            scope.launch { updateWorkItems() }
        }
        activeWork = null
    }

    fun deleteActiveWork() {
        val workToSave = work.get(activeWork)
        scope.launch {
            if (workToSave != null && workToSave.id?.equals(newWorkId) == false) {
                console.log("Deleted ${JsApi.deleteWork(workToSave)}")
            }
        }.invokeOnCompletion {
            scope.launch { updateWorkItems() }
        }
        activeWork = null
    }

    fun addNewWorkRow(newWorkItemType: NewWorkItemType) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timeUnit = when (newWorkItemType) {
            NewWorkItemType.Interval -> IntervalUnit(now.date, now.toTimeUnit(), null, null)
            NewWorkItemType.Absolute -> AbsoluteUnit(now.date, Time(0, 0))
        }
        val newWork = Work(newWorkId, projects?.first() ?: "", timeUnit)
        work = work.plus(newWorkId to newWork)
        activeWork = newWorkId
    }

    Container {
        sx = jso {
            padding = 20.px
            maxWidth = 80.pct
        }
        weekPicker {
            value = yearWeek
            onUpdateValue = { yw -> yearWeek = yw; scope.launch { updateWorkItems(yw) } }
        }
        TableContainer {
            sx = jso {
                maxHeight = 500.px
                paddingTop = 20.px
            }
            Table {
                stickyHeader = true
                size = Size.small
                workRowHeader { }
                work.values.sortedWith(compareBy({ it.time.date }, { it.id })).map {
                    workEditRow {
                        workItem = it
                        active = (activeWork?.equals(it.id)) == true
                        reportActive = {
                            val newWork = it
                            val updatedWork = (work.filter { it.key != newWork.id }).plus(it.id!! to it)
                            work = updatedWork
                            activeWork = it.id
                        }
                        projectList = projects
                        saveActiveWork = { saveActiveWork() }
                    }
                }
            }
        }
        if (activeWork != null) {
            Box {
                sx = jso {
                    paddingTop = 20.px
                    justifyContent = JustifyContent.spaceBetween
                    justifyItems = JustifyItems.flexEnd
                    justifySelf = JustifySelf.selfEnd
                    alignContent = AlignContent.end
                    alignItems = AlignItems.end
                }
                Button {
                    +"Save"
                    variant = ButtonVariant.outlined
                    size = Size.small
                    onClick = { _ -> saveActiveWork() }
                }
                Button {
                    +"Delete"
                    color = ButtonColor.error
                    variant = ButtonVariant.outlined
                    size = Size.small
                    onClick = { _ -> deleteActiveWork() }
                }
                Button {
                    +"Cancel"
                    variant = ButtonVariant.outlined
                    color = ButtonColor.secondary
                    size = Size.small
                    onClick = { _ ->
                        activeWork = null
                        scope.launch { updateWorkItems() }
                    }
                }
            }
        }
        Box {
            sx = jso {
                paddingTop = 10.px
                alignItems = AlignItems.center
                justifySelf = JustifySelf.center
            }
            Button {
                +"Add Interval Time"
                onClick = { _ -> addNewWorkRow(NewWorkItemType.Interval) }
            }
            Button {
                +"Add Absolute Time"
                onClick = { _ -> addNewWorkRow(NewWorkItemType.Absolute) }
            }
        }
        timeSum {
            workItemList = work.values.toList()
        }
    }
}
