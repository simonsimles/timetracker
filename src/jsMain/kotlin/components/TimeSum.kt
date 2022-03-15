package components

import JsApi
import de.simles.timetracker.models.Time
import de.simles.timetracker.models.Work
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import react.*
import react.dom.b
import react.dom.div
import react.dom.hr
import scope

external interface TimeSum : RProps {
    var work: List<Work>?
}

fun <T> Iterable<T>.sumOf(selector: (T) -> Time): Time {
    var sum = Time(0, 0)
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

@ExperimentalCoroutinesApi
val timeSum = functionalComponent<TimeSum> { props ->
    val (categoryToProjectMap, setCategoryToProjectMap) = useState<Map<String, List<String>>?>(null)
    if (categoryToProjectMap == null) {
        scope.launch {
            val categoryMap =
                JsApi.getProjects()
                    .associate { it.name to it.category }
                    .map { listOf(it.value, it.key) }
                    .groupBy { it[0] }
                    .map { it.key to it.value.flatten() }
                    .toMap()
            console.log("Project map is set")
            setCategoryToProjectMap(categoryMap)
        }
    }
    categoryToProjectMap?.toList()?.map {
        div(classes = "row") {
            div(classes = "col") {
                val projectsForCategory = it.second
                val sum = props.work?.filter { projectsForCategory.contains(it.project) }
                    ?.sumOf { it.time.getTotalDuration() } ?: Time(0, 0)
                b {
                    +"${it.first}: "
                }
                +"$sum / ${sum.asDouble().asDynamic().toFixed(2)}"
            }
        }
    }
    div(classes = "row justify-content-start") {
        div(classes = "col") {
            hr { }
            b {
                +"Total time: "
            }
            val totalDuration: Time = props.work?.sumOf { it.time.getTotalDuration() } ?: Time(0, 0)
            +"${totalDuration} / ${totalDuration.asDouble().asDynamic().toFixed(2)}"
        }
    }
}

fun RBuilder.timeSum(handler: TimeSum.() -> Unit): ReactElement {
    return child(timeSum) {
        this.attrs(handler)
    }
}
