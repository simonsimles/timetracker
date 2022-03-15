package pages

import JsApi
import components.sumOf
import components.textEdit
import components.timeSum
import components.weekPicker
import de.simles.timetracker.YearWeek
import de.simles.timetracker.display
import de.simles.timetracker.lastWorkDayOfWeek
import de.simles.timetracker.models.Comment
import de.simles.timetracker.models.Work
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import react.*
import react.dom.*
import scope

external interface HomeProps : RProps

external interface HomeState : RState {
    var yearWeek: YearWeek
    var work: List<Work>
    var comments: List<Comment>
}

class Home(props: HomeProps) : RComponent<HomeProps, HomeState>(props) {
    init {
        state.apply {
            yearWeek = YearWeek.current()
            work = emptyList()
            comments = emptyList()
        }
        scope.launch {
            updateWorkItems()
            updateComments()
        }
    }

    private suspend fun updateWorkItems() {
        val year = state.yearWeek.year
        val week = state.yearWeek.week
        if (year != null && week != null) {
            val receivedWork = JsApi.getWork(year, week)
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
        val mondayOfWeek = state.yearWeek.mondayInWeek()
        if (mondayOfWeek != null) {
            val retrievedComments =
                listOf(mondayOfWeek, mondayOfWeek.lastWorkDayOfWeek()).map {
                    (it.year to it.monthNumber)
                }.distinct().foldRight(emptyList()) { pair: Pair<Int, Int>, list: List<Comment> ->
                    list + JsApi.getComments(pair.first, pair.second)
                }
            setState { comments = retrievedComments }
        } else {
            setState { comments = emptyList() }
        }
    }

    private fun saveComment(comment: Comment) {
        val job = scope.launch {
            if (state.comments.any { it.date.equals(comment.date) }) {
                JsApi.updateComment(comment)
            } else {
                JsApi.addComment(comment)
            }
        }
        job.invokeOnCompletion {
            scope.launch { updateComments() }
        }
    }

    override fun RBuilder.render() {
        section(classes = "pt-md-5 pb-md-4 text-center") {
            div(classes = "container align-items-center") {
                div(classes = "row") {
                    weekPicker {
                        value = state.yearWeek
                        onUpdateValue = { newYearWeek ->
                            setState {
                                yearWeek = newYearWeek
                            }
                            scope.launch {
                                updateWorkItems()
                                updateComments()
                            }
                        }
                    }
                }

                val projectsInWeek = state.work.map { it.project }.distinct()
                div(classes = "row") {
                    table(classes = "table table-hover") {
                        setProp("id", "weekTable")
                        setProp("data-show-columns", "true")
                        thead {
                            tr {
                                th { +"Date" }
                                th {
                                    +"Comment"
                                }
                                th { +"Total Time" }
                                projectsInWeek.map {
                                    th { +it }
                                }
                            }
                        }
                        tbody {
                            state.work.groupBy { it.time.date }.map { dateToWorkList ->
                                val work = dateToWorkList.value
                                val totalWork = work.sumOf { it.time.getTotalDuration() }
                                val fixedList = listOf(
                                    dateToWorkList.key.display(),
                                    state.comments.find { it.date.equals(dateToWorkList.key) }?.comment ?: "",
                                    "$totalWork / ${totalWork.asDouble().asDynamic().toFixed(2)}"
                                )
                                val projectTimeList = projectsInWeek.map { project ->
                                    work.filter { it.project == project }.sumOf { it.time.getTotalDuration() }
                                }
                                dateToWorkList.key to fixedList + projectTimeList
                            }.sortedBy { it.first }.map {
                                tr {
                                    it.second.mapIndexed { idx, data ->
                                        val date = it.first
                                        if (idx == 1) {
                                            textEdit {
                                                id = date.dayOfWeek.toString()
                                                text = state.comments.find { it.date.equals(date) }?.comment ?: ""
                                                onSave = { s -> saveComment(Comment(date, s)) }
                                            }
                                            td {
                                                +data.toString()
                                                attrs {
                                                    onClickFunction = { _ ->
                                                        document.getElementById(date.dayOfWeek.toString())?.let {
                                                            showModal(it)
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            td { +data.toString() }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                timeSum {
                    work = state.work
                }
            }
        }
    }
}

fun showModal(@Suppress("UNUSED_PARAMETER") e: Element) {
    js("new bootstrap.Modal(e).show()")
}

fun RBuilder.home(handler: HomeProps.() -> Unit): ReactElement {
    return child(Home::class) {
        this.attrs(handler)
    }
}
