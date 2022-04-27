package components

import JsApi
import csstype.AlignItems
import csstype.JustifyContent
import csstype.TextAlign
import csstype.px
import de.simles.timetracker.models.Time
import de.simles.timetracker.models.Work
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.hr
import react.dom.html.ReactHTML.span
import scope

external interface TimeSum : Props {
    var workItemList: List<Work>
}

fun <T> Iterable<T>.sumOf(selector: (T) -> Time): Time =
    this.fold(Time(0, 0)) { acc, t -> acc + selector(t) }


suspend fun getCategoryMap(): Map<String, List<String>> = JsApi.getProjects()
    .groupBy { it.category }
    .mapValues { it.value.map { it.name } }

@ExperimentalCoroutinesApi
val timeSum = FC<TimeSum> { props ->
    var categoryToProjectMap by useState<Map<String, List<String>>>(emptyMap())

    useEffect(emptyList<String>()) {
        scope.launch {
            categoryToProjectMap = getCategoryMap()
        }
    }

    categoryToProjectMap.toList().map {
        List {
            dense = true
            sx {
                paddingTop = 5.px
                justifyContent = JustifyContent.center
                alignItems = AlignItems.center
            }
            ListItem {
                val projectsForCategory = it.second
                val sum = props.workItemList.filter { projectsForCategory.contains(it.project) }
                    .sumOf { it.time.getTotalDuration() }
                ListItemText {
                    b { +"${it.first}: " }
                    +"$sum / ${sum.asDouble().asDynamic().toFixed(2)}"
                }
            }
        }
    }
    Box {
        sx {
            paddingTop = 20.px
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
        }
        Typography {
            align = TypographyAlign.inherit
            sx {
                textAlign = TextAlign.center
            }
            b { +"Total time: " }
            val totalDuration: Time = props.workItemList.sumOf { it.time.getTotalDuration() }
            +"${totalDuration}"
            span { +" / " }
            +"${totalDuration.asDouble().asDynamic().toFixed(2)}"
        }
    }
}

