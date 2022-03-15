import de.simles.timetracker.models.Project
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
fun Route.projectRouting() {
    route("/api") {
        route("/project") {
            get {
                Services.projectStorageHandler.getProjects().onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respondText("Could not load projects", status = HttpStatusCode.InternalServerError)
                }
            }
            get("/project/{name}") {
                val name = call.parameters["name"] ?: return@get call.respondText(
                    "Missing project name",
                    status = HttpStatusCode.BadRequest
                )
                Services.projectStorageHandler.getProjects()
                    .onSuccess {
                        it.find { it.name == name }?.let { call.respond(it) } ?: return@get call.respondText(
                            "Unknown project $name",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                    .onFailure {
                        return@get call.respondText(it.message!!, status = HttpStatusCode.InternalServerError)
                    }
            }
            post {
                val project = call.receive<Project>()
                Services.projectStorageHandler.getProjects().onSuccess {
                    if (it.any { it.name == project.name }) {
                        return@post call.respondText(
                            "Project ${project.name} already exists",
                            status = HttpStatusCode.BadRequest
                        )
                    } else {
                        Services.projectStorageHandler.setProjects(
                            it + listOf(project)
                        )
                        return@post call.respond(project)
                    }
                }.onFailure {
                    return@post call.respondText(
                        it.message!!,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            patch {
                val project = call.receive<Project>()
                Services.projectStorageHandler.getProjects().onSuccess {
                    if (it.any { it.name == project.name }) {
                        Services.projectStorageHandler.setProjects(
                            it.filterNot { it.name == project.name } + listOf(project)
                        )
                        return@patch call.respond(project)
                    } else
                        return@patch call.respondText(
                            "Project ${project.name} does not exist",
                            status = HttpStatusCode.BadRequest
                        )
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
fun Application.registerProjectRouting() {
    routing {
        projectRouting()
    }
}
