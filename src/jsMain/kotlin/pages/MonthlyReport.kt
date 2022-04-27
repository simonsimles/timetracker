package pages

import JsApi
import components.dailyReportEdit
import components.monthPicker
import components.sumOf
import csstype.ClassName
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
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.section
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import scope

val MonthlyReport = FC<Props> {

    var yearMonth by useState(YearMonth.current())
    var work by useState<List<Work>>(emptyList())
    var comments by useState<List<Comment>>(emptyList())
    var dailyReports by useState<List<DailyReportEntry>>(emptyList())

    suspend fun updateDailyReports() {
        if (yearMonth.year != null && yearMonth.month != null) {
            val receivedReports = JsApi.getDailyReports(yearMonth.year!!, yearMonth.month!!)
            dailyReports = receivedReports
            console.log("Updated daily reports")
        } else {
            dailyReports = emptyList()
        }
    }

    suspend fun updateWorkItems() {
        if (yearMonth.year != null && yearMonth.month != null) {
            val firstDay = yearMonth.firstDay()
            val lastDay = yearMonth.lastDay()
            val receivedWork = (firstDay.weekNumber()..lastDay.weekNumber()).distinct().flatMap {
                JsApi.getWork(yearMonth.year!!, it)
            }
            work = receivedWork
            console.log("Work is updated")
        } else {
            work = emptyList()
            console.log("Work is reset")
        }
    }

    suspend fun updateComments() {
        if (yearMonth.year != null && yearMonth.month != null) {
            val retrievedComments = JsApi.getComments(yearMonth.year!!, yearMonth.month!!)
            comments = retrievedComments
        } else {
            comments = emptyList()
        }
    }

    suspend fun updateItems() {
        updateComments()
        updateWorkItems()
        updateDailyReports()
    }

    useEffect(emptyList<String>()) {
        scope.launch { updateItems() }
    }

    fun saveDailyReport(dailyReportEntry: DailyReportEntry) {
        val job = scope.launch {
            if (dailyReports.any { it.date.equals(dailyReportEntry.date) }) {
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

    section {
        className = ClassName("pt-md-5 pb-md-4 text-center")
        div {
            className = ClassName("container align-items-center")
            div {
                className = ClassName("row")
                monthPicker {
                    value = yearMonth
                    onUpdateValue = { newYearMonth ->
                        yearMonth = newYearMonth
                        scope.launch { updateItems() }
                    }
                }
            }

            val projectsInMonth = (work.map { it.project } + dailyReports.map { it.project }).distinct()
            div {
                className = ClassName("row")
                table {
                    className = ClassName("table table-hover")
                    id = "monthTable"
                    //setProp("data-show-columns", "true")
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
                        if (yearMonth.year != null && yearMonth.month != null) {
                            (1..yearMonth.lastDay().dayOfMonth).map {
                                LocalDate(yearMonth.year!!, yearMonth.month!!, it)
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
                                                    onClick = { _ ->
                                                        document.getElementById("$date-$project")
                                                            ?.let {  }
                                                    }
                                                }
                                                +"Reported: "
                                                b { +"${it.duration}" }
                                                br { }
                                                +(it.comment)
                                            }
                                                ?: div {
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
                                                    button {
                                                        className = ClassName("btn btn-light")
                                                        onClick = { _ ->
                                                            document.getElementById("$date-$project")
                                                                ?.let { }
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
