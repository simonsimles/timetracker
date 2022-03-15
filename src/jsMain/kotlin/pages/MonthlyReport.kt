package pages

import JsApi
import components.dailyReportEdit
import components.monthPicker
import components.sumOf
import de.simles.timetracker.YearMonth
import de.simles.timetracker.models.Comment
import de.simles.timetracker.models.DailyReportEntry
import de.simles.timetracker.models.Time
import de.simles.timetracker.models.Work
import de.simles.timetracker.weekNumber
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import react.*
import react.dom.*
import scope

external interface MonthlyReportState : RState {
    var yearMonth: YearMonth
    var work: List<Work>
    var comments: List<Comment>
    var dailyReports: List<DailyReportEntry>
}

class MonthlyReport(props: HomeProps) : RComponent<RProps, MonthlyReportState>(props) {

    private fun showModal(@Suppress("UNUSED_PARAMETER") e: Element) {
        js("new bootstrap.Modal(e).show()")
    }

    init {
        state.apply {
            yearMonth = YearMonth.current()
            work = emptyList()
            comments = emptyList()
            dailyReports = emptyList()
        }
        scope.launch { updateItems() }
    }

    private suspend fun updateItems() {
        updateComments()
        updateWorkItems()
        updateDailyReports()
    }

    private suspend fun updateDailyReports() {
        val yearMonth = state.yearMonth
        if (yearMonth.year != null && yearMonth.month != null) {
            val receivedReports = JsApi.getDailyReports(yearMonth.year, yearMonth.month)
            setState { dailyReports = receivedReports }
            console.log("Updated daily reports")
        } else {
            setState { dailyReports = emptyList() }
        }
    }

    private suspend fun updateWorkItems() {
        val yearMonth = state.yearMonth
        if (yearMonth.year != null && yearMonth.month != null) {
            val firstDay = yearMonth.firstDay()
            val lastDay = yearMonth.lastDay()
            val receivedWork = (firstDay.weekNumber()..lastDay.weekNumber()).distinct().flatMap {
                JsApi.getWork(yearMonth.year, it)
            }
            setState {
                work = receivedWork
            }
            console.log("Work is updated")
        } else {
            setState {
                work = emptyList()
            }
            console.log("Work is reset")
        }
    }

    private suspend fun updateComments() {
        val yearMonth = state.yearMonth
        if (yearMonth.year != null && yearMonth.month != null) {
            val retrievedComments = JsApi.getComments(yearMonth.year, yearMonth.month)
            setState { comments = retrievedComments }
        } else {
            setState { comments = emptyList() }
        }
    }

    private fun saveDailyReport(dailyReportEntry: DailyReportEntry) {
        val job = scope.launch {
            if (state.dailyReports.any { it.date.equals(dailyReportEntry.date) }) {
                console.log("Update daily report $dailyReportEntry")
                JsApi.updateDailyReportEntry(dailyReportEntry)
            } else {
                console.log("Add daily report $dailyReportEntry")
                JsApi.addDailyReport(dailyReportEntry)
            }
        }
        job.invokeOnCompletion {
            scope.launch { updateDailyReports() }
        }
    }

    override fun RBuilder.render() {
        section(classes = "pt-md-5 pb-md-4 text-center") {
            div(classes = "container align-items-center") {
                div(classes = "row") {
                    monthPicker {
                        value = state.yearMonth
                        onUpdateValue = { newYearMonth ->
                            setState {
                                yearMonth = newYearMonth
                            }
                            scope.launch { updateItems() }
                        }
                    }
                }

                val projectsInMonth = (state.work.map { it.project } + state.dailyReports.map { it.project }).distinct()
                div(classes = "row") {
                    table(classes = "table table-hover") {
                        setProp("id", "monthTable")
                        setProp("data-show-columns", "true")
                        thead {
                            tr {
                                th { +"Date" }
                                th { +"Total Time" }
                                projectsInMonth.map {
                                    th { +it }
                                }
                            }
                        }
                        tbody {
                            val yearMonth = state.yearMonth
                            val work = state.work
                            val dailyReports = state.dailyReports
                            if (yearMonth.year != null && yearMonth.month != null) {
                                (1..yearMonth.lastDay().dayOfMonth).map {
                                    LocalDate(yearMonth.year, yearMonth.month, it)
                                }.map { date ->
                                    tr {
                                        td {
                                            +date.toString()
                                        }
                                        td {
                                            div {
                                                b {
                                                    val reportsOfDay = dailyReports.filter { it.date == date }
                                                    val recordedTime = work.filter { it.time.date == date }
                                                        .filter { workEntry ->
                                                            reportsOfDay.none { it.project == workEntry.project }
                                                        }
                                                        .sumOf { it.time.getTotalDuration() }
                                                    val reportedTime = reportsOfDay.sumOf { it.duration }
                                                    +("${recordedTime + reportedTime}")
                                                }
                                            }
                                        }
                                        projectsInMonth.map { project ->
                                            td {
                                                div {
                                                    val projectTime = work.filter {
                                                        it.time.date == date && it.project == project
                                                    }.sumOf { it.time.getTotalDuration() }
                                                    if (projectTime.hour > 0 || projectTime.minute > 0) {
                                                        +("Recorded: $projectTime")
                                                    }
                                                }
                                                dailyReports.firstOrNull {
                                                    it.date == date && it.project == project
                                                }?.let {
                                                    dailyReportEdit {
                                                        id = "$date-$project"
                                                        time = it.duration
                                                        comment = it.comment
                                                        onSave = { comment, time ->
                                                            saveDailyReport(
                                                                DailyReportEntry(
                                                                    date, time, comment, project
                                                                )
                                                            )
                                                        }
                                                    }
                                                    div {
                                                        attrs {
                                                            onClickFunction = { _ ->
                                                                document.getElementById("$date-$project")
                                                                    ?.let { showModal(it) }
                                                            }
                                                        }
                                                        +"Reported: "
                                                        b { +"${it.duration}" }
                                                        br { }
                                                        +(it.comment)
                                                    }
                                                } ?: div {
                                                    dailyReportEdit {
                                                        id = "$date-$project"
                                                        time = Time(0, 0)
                                                        comment = ""
                                                        onSave = { comment, time ->
                                                            saveDailyReport(
                                                                DailyReportEntry(
                                                                    date, time, comment, project
                                                                )
                                                            )
                                                        }
                                                    }
                                                    button(classes = "btn btn-light") {
                                                        attrs {
                                                            onClickFunction = { _ ->
                                                                document.getElementById("$date-$project")
                                                                    ?.let { showModal(it) }
                                                            }
                                                        }
                                                        +"Add"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.monthlyReport(handler: RProps.() -> Unit): ReactElement {
    return child(MonthlyReport::class) {
        this.attrs(handler)
    }
}
