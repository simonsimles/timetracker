import de.simles.timetracker.models.Work
import de.simles.timetracker.weekNumber
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import java.util.*

@ExperimentalSerializationApi
fun Route.modelRouting() {
    route("/api") {
        route("/work") {
            get {
                call.respond(Services.workStorageHandler.getYears())
            }
            post {
                val receivedWork = call.receive<Work>()
                Services.projectStorageHandler.getProjects().onSuccess {
                    val projects = it
                    if (projects.none { it.name == receivedWork.project })
                        return@post call.respondText(
                            "Project ${receivedWork.project} unknown",
                            status = HttpStatusCode.BadRequest
                        )
                    val work = Work(Clock.System.now().epochSeconds, receivedWork.project, receivedWork.time)
                    val year = work.time.date.year
                    val weekNumber = work.time.date.weekNumber()
                    Services.workStorageHandler.getWork(year, weekNumber).onSuccess {
                        Services.workStorageHandler.setWork(year, weekNumber, it + listOf(work))
                    }.onFailure {
                        return@post call.respondText(
                            "Work couldn't be read",
                            status = HttpStatusCode.InternalServerError
                        )
                    }
                    call.respond(work)
                }.onFailure {
                    return@post call.respondText(
                        "Projects couldn't be read",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            route("/{year}") {
                get {
                    val year = call.parameters["year"]?.toInt() ?: return@get call.respondText(
                        "Missing year", status = HttpStatusCode.BadRequest
                    )
                    call.respond(Services.workStorageHandler.getWeeks(year))
                }
                route("/{week}") {
                    get {
                        val year = call.parameters["year"]?.toInt() ?: return@get call.respondText(
                            "Missing year", status = HttpStatusCode.BadRequest
                        )
                        val week = call.parameters["week"]?.toInt() ?: return@get call.respondText(
                            "Missing week",
                            status = HttpStatusCode.BadRequest
                        )
                        Services.workStorageHandler.getWork(year, week).onSuccess {
                            call.respond(it)
                        }.onFailure {
                            call.respondText(it.message!!, status = HttpStatusCode.InternalServerError)
                        }
                    }
                    route("/{id}") {
                        get {
                            val year = call.parameters["year"]?.toInt() ?: return@get call.respondText(
                                "Missing year", status = HttpStatusCode.BadRequest
                            )
                            val week = call.parameters["week"]?.toInt() ?: return@get call.respondText(
                                "Missing week",
                                status = HttpStatusCode.BadRequest
                            )
                            val id = call.parameters["id"]?.toLong() ?: return@get call.respondText(
                                "Missing week",
                                status = HttpStatusCode.BadRequest
                            )
                            Services.workStorageHandler.getWork(year, week).onSuccess {
                                val work = it.find { it.id == id } ?: return@get call.respondText(
                                    "Id unknown",
                                    status = HttpStatusCode.BadRequest
                                )
                                call.respond(work)
                            }.onFailure {
                                call.respondText(it.message!!, status = HttpStatusCode.InternalServerError)
                            }
                        }
                        patch {
                            val year = call.parameters["year"]?.toInt() ?: return@patch call.respondText(
                                "Missing year", status = HttpStatusCode.BadRequest
                            )
                            val week = call.parameters["week"]?.toInt() ?: return@patch call.respondText(
                                "Missing week",
                                status = HttpStatusCode.BadRequest
                            )
                            val id = call.parameters["id"]?.toLong() ?: return@patch call.respondText(
                                "Missing week",
                                status = HttpStatusCode.BadRequest
                            )
                            val work = call.receive<Work>()
                            Services.workStorageHandler.getWork(year, week).onSuccess {
                                if (it.any { it.id == id }) {
                                    Services.workStorageHandler.setWork(
                                        year,
                                        week,
                                        it.filterNot { it.id == id } + listOf(work))
                                    call.respond(work)
                                } else {
                                    return@patch call.respondText(
                                        "Id unknown",
                                        status = HttpStatusCode.BadRequest
                                    )
                                }
                            }.onFailure {
                                call.respondText(it.message!!, status = HttpStatusCode.InternalServerError)
                            }
                        }
                        delete {
                            val year = call.parameters["year"]?.toInt() ?: return@delete call.respondText(
                                "Missing year", status = HttpStatusCode.BadRequest
                            )
                            val week = call.parameters["week"]?.toInt() ?: return@delete call.respondText(
                                "Missing week",
                                status = HttpStatusCode.BadRequest
                            )
                            val id = call.parameters["id"]?.toLong() ?: return@delete call.respondText(
                                "Missing week",
                                status = HttpStatusCode.BadRequest
                            )
                            Services.workStorageHandler.getWork(year, week).onSuccess {
                                if (it.any { it.id == id }) {
                                    Services.workStorageHandler.setWork(
                                        year,
                                        week,
                                        it.filterNot { it.id == id })
                                    call.respond(id)
                                } else {
                                    return@delete call.respondText(
                                        "Id unknown",
                                        status = HttpStatusCode.BadRequest
                                    )
                                }
                            }.onFailure {
                                call.respondText(it.message!!, status = HttpStatusCode.InternalServerError)
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalSerializationApi
fun Application.registerModelRouting() {
    routing {
        modelRouting()
    }
}
