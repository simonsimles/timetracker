package pages

import JsApi
import components.sumOf
import components.textEdit
import components.timeSum
import components.weekPicker
import csstype.*
import de.simles.timetracker.YearWeek
import de.simles.timetracker.display
import de.simles.timetracker.lastWorkDayOfWeek
import de.simles.timetracker.models.Comment
import de.simles.timetracker.models.Time
import de.simles.timetracker.models.Work
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.js.jso
import mui.material.*
import mui.system.ResponsiveStyleValue
import mui.system.sx
import react.FC
import react.Props
import react.useEffect
import react.useState
import scope

val timesTableHeader = FC<TimesTableHeaderFooterProps> { props ->
    TableHead {
        TableRow {
            TableCell { +"Date" }
            TableCell { +"Comment" }
            TableCell { +"Total Time" }
            props.projectList.map { TableCell { +it } }
        }
    }
}

external interface TimesTableHeaderFooterProps : Props {
    var projectList: List<String>
    var projectToTimeMap: Map<String, Time>
}

var timesTableFooter = FC<TimesTableHeaderFooterProps> { props ->
    TableFooter {
        TableRow {
            TableCell { +"Total" }
            TableCell { +"" }
            TableCell {
                val totalTime = props.projectToTimeMap.values.sumOf { it }
                +"$totalTime / ${totalTime.asDouble().asDynamic().toFixed(2)}"
            }
            props.projectList.map {
                TableCell { +props.projectToTimeMap.get(it).toString() }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
val Home = FC<Props> {
    var yearWeek by useState(YearWeek.current())
    var workList by useState<List<Work>>(emptyList())
    var comments by useState<List<Comment>>(emptyList())


    suspend fun updateWorkItems(yearWeekToOverride: YearWeek? = null) {
        val year = yearWeekToOverride?.year ?: yearWeek.year
        val week = yearWeekToOverride?.week ?: yearWeek.week
        val receivedWork = JsApi.getWork(year!!, week!!)
        workList = receivedWork
        console.log("Work is updated")
    }

    suspend fun updateComments(yearWeekToOverride: YearWeek? = null) {
        val mondayOfWeek = (yearWeekToOverride ?: yearWeek).mondayInWeek()
        if (mondayOfWeek != null) {
            val retrievedComments =
                listOf(mondayOfWeek, mondayOfWeek.lastWorkDayOfWeek()).map {
                    (it.year to it.monthNumber)
                }.distinct().foldRight(emptyList()) { pair: Pair<Int, Int>, list: List<Comment> ->
                    list + JsApi.getComments(pair.first, pair.second)
                }
            comments = retrievedComments
        } else {
            comments = emptyList()
        }
    }

    useEffect(emptyList<String>()) {
        scope.launch {
            updateWorkItems()
            updateComments()
        }
    }

    fun saveComment(comment: Comment) {
        val job = scope.launch {
            if (comments.any { it.date.equals(comment.date) }) {
                JsApi.updateComment(comment)
            } else {
                JsApi.addComment(comment)
            }
        }
        job.invokeOnCompletion {
            scope.launch { updateComments() }
        }
    }

    Container {
        sx {
            padding = 20.px
            maxWidth = 80.pct
        }

        weekPicker {
            value = yearWeek
            onUpdateValue = { newYearWeek ->
                yearWeek = newYearWeek
                scope.launch {
                    updateWorkItems(newYearWeek)
                    updateComments(newYearWeek)
                }
            }
        }

        val projectsInWeek: List<String> = workList.map { it.project }.distinct()
        TableContainer {
            sx { width = 100.pct }
            Table {
                stickyHeader
                timesTableHeader {
                    projectList = projectsInWeek
                }
                TableBody {
                    workList
                        .groupBy { it.time.date }
                        .map { getRow(it, comments, projectsInWeek) }
                        .sortedBy { it.first }.map {
                            tableRow {
                                date = it.first
                                row = it.second
                                listOfComments = comments
                                saveCommentFunction = { saveComment(it) }
                            }
                        }
                }
                timesTableFooter {
                    projectList = projectsInWeek
                    projectToTimeMap = workList
                        .groupBy { it.project }
                        .mapValues { it.value.sumOf { it.time.getTotalDuration() } }
                }
            }
        }
        timeSum {
            workItemList = workList
        }
    }
}

private fun <T> Iterable<T>.sumOf(selector: (T) -> Time): Time =
    this.fold(Time(0, 0)) { acc, t -> acc + selector(t) }

fun getRow(
    dateToWorkList: Map.Entry<LocalDate, List<Work>>,
    comments: List<Comment>,
    projectsInWeek: List<String>
): Pair<LocalDate, List<Any>> {
    val work: List<Work> = dateToWorkList.value
    val totalWork = work.sumOf { it.time.getTotalDuration() }
    val fixedList = listOf(
        dateToWorkList.key.display(),
        comments.find { it.date.equals(dateToWorkList.key) }?.comment ?: "",
        "$totalWork / ${totalWork.asDouble().asDynamic().toFixed(2)}"
    )
    val projectTimeList = projectsInWeek.map { project ->
        work.filter { it.project == project }.sumOf { it.time.getTotalDuration() }
    }
    return dateToWorkList.key to fixedList + projectTimeList
}

external interface TableRowProperties : Props {
    var date: LocalDate
    var row: List<Any>
    var listOfComments: List<Comment>
    var saveCommentFunction: (Comment) -> Unit
}

val tableRow = FC<TableRowProperties> { props ->
    var dialogOpen: Boolean by useState(false)
    TableRow {
        hover = true
        props.row.mapIndexed { idx, data ->
            if (idx == 1) {
                textEdit {
                    text = props.listOfComments.find { it.date.equals(props.date) }?.comment ?: ""
                    onSave = { s ->
                        props.saveCommentFunction(Comment(props.date, s))
                        dialogOpen = false
                    }
                    onClose = { dialogOpen = false }
                    isOpen = dialogOpen
                }
                TableCell {
                    +data.toString()
                    onClick = { dialogOpen = true }
                }
            } else {
                TableCell { +data.toString() }
            }
        }
    }
}
