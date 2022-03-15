import de.simles.timetracker.models.Comment
import de.simles.timetracker.models.Project
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
fun Route.commentRouting() {
    route("/api") {
        route("/comment") {
            get("/{year}/{month}") {
                val year = call.parameters["year"]?.toInt() ?: return@get call.respondText(
                    "Could not retrieve year for comment",
                    status = HttpStatusCode.BadRequest
                )
                val month = call.parameters["month"]?.toInt() ?: return@get call.respondText(
                    "Could not retrieve month for comment",
                    status = HttpStatusCode.BadRequest
                )
                Services.commentStorageHandler.getMonth(year, month)
                    .onSuccess { call.respond(it) }
                    .onFailure {
                        return@get call.respondText(it.message!!, status = HttpStatusCode.InternalServerError)
                    }
            }
            post {
                val comment = call.receive<Comment>()
                Services.commentStorageHandler.getMonth(comment.date.year, comment.date.monthNumber).onSuccess {
                    if (it.any { it.date.equals(comment.date) }) {
                        return@post call.respondText(
                            "Comment for ${comment.date} already exists, use PATCH",
                            status = HttpStatusCode.BadRequest
                        )
                    } else {
                        Services.commentStorageHandler.setMonth(
                            comment.date.year, comment.date.monthNumber,
                            it + listOf(comment)
                        )
                        return@post call.respond(comment)
                    }
                }.onFailure {
                    return@post call.respondText(
                        it.message!!,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            patch {
                val comment = call.receive<Comment>()
                Services.commentStorageHandler.getMonth(comment.date.year, comment.date.monthNumber).onSuccess {
                    if (it.any { it.date.equals(comment.date) }) {
                        Services.commentStorageHandler.setMonth(
                            comment.date.year, comment.date.monthNumber,
                            it.filterNot { it.date.equals(comment.date) } + listOf(comment)
                        )
                        return@patch call.respond(comment)
                    } else {
                        return@patch call.respondText(
                            "Comment for date ${comment.date} does not exist",
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
fun Application.registerCommentRouting() {
    routing {
        commentRouting()
    }
}
