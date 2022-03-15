import de.simles.timetracker.storage.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json


object Services {
    lateinit var projectStorageHandler: ProjectStorageHandler
    lateinit var workStorageHandler: WorkStorageHandler
    lateinit var commentStorageHandler: CommentStorageHandler
    lateinit var dailyReportStorageHandler: DailyReportStorageHandler
}

@ExperimentalSerializationApi
fun main(args: Array<String>) {
    Services.workStorageHandler = FileBasedWorkStorage
    Services.projectStorageHandler = FileBasedProjectStorage
    Services.commentStorageHandler = FileBasedCommentStorage
    Services.dailyReportStorageHandler = FileBasedDailyReportsStorage
    io.ktor.server.netty.EngineMain.main(args)
}

@ExperimentalSerializationApi
fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            useArrayPolymorphism = false
        })
    }
    registerModelRouting()
    registerStaticRouting()
    registerProjectRouting()
    registerCommentRouting()
    registerDailyReportRouting()
}