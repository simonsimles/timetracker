import de.simles.timetracker.models.DailyReportEntry
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
fun Route.dailyReportRouting() {
    route("/api") {
        route("/dailyReport") {
            get("/{year}/{month}") {
                val year = call.parameters["year"]?.toInt() ?: return@get call.respondText(
                    "Could not retrieve year for dailyReports",
                    status = HttpStatusCode.BadRequest
                )
                val month = call.parameters["month"]?.toInt() ?: return@get call.respondText(
                    "Could not retrieve month for dailyReports",
                    status = HttpStatusCode.BadRequest
                )
                Services.dailyReportStorageHandler.getDailyReports(year, month)
                    .onSuccess { call.respond(it) }
                    .onFailure {
                        return@get call.respondText(it.message!!, status = HttpStatusCode.InternalServerError)
                    }
            }
            post {
                val newDailyReportEntry = call.receive<DailyReportEntry>()
                Services.dailyReportStorageHandler.getDailyReports(
                    newDailyReportEntry.date.year,
                    newDailyReportEntry.date.monthNumber
                ).onSuccess {
                    if (it.any { it.date == newDailyReportEntry.date }) {
                        return@post call.respondText(
                            "DailyReportEntry for ${newDailyReportEntry.date} already exists, use PATCH",
                            status = HttpStatusCode.BadRequest
                        )
                    } else {
                        Services.dailyReportStorageHandler.setDailyReports(
                            newDailyReportEntry.date.year, newDailyReportEntry.date.monthNumber,
                            it + listOf(newDailyReportEntry)
                        )
                        return@post call.respond(newDailyReportEntry)
                    }
                }.onFailure {
                    return@post call.respondText(
                        it.message!!,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            patch {
                val updatedDailyReportEntry = call.receive<DailyReportEntry>()
                Services.dailyReportStorageHandler.getDailyReports(
                    updatedDailyReportEntry.date.year,
                    updatedDailyReportEntry.date.monthNumber
                ).onSuccess {
                    if (it.any { it.date == updatedDailyReportEntry.date }) {
                        Services.dailyReportStorageHandler.setDailyReports(
                            updatedDailyReportEntry.date.year, updatedDailyReportEntry.date.monthNumber,
                            it.filterNot { it.date == updatedDailyReportEntry.date } + listOf(
                                updatedDailyReportEntry
                            )
                        )
                        return@patch call.respond(updatedDailyReportEntry)
                    } else {
                        return@patch call.respondText(
                            "DailyReportEntry for date ${updatedDailyReportEntry.date} does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }.onFailure {
                    return@patch call.respondText(
                        it.message!!,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }
}

@ExperimentalSerializationApi
fun Application.registerDailyReportRouting() {
    routing {
        dailyReportRouting()
    }
}
