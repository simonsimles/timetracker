import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import kotlinx.html.*

fun HTML.index() {
    head {
        title("Timetracker")
        script(src = "/static/Timetracker.js") {}
        link {
            rel = "icon"
            href = "/static/favicon.ico"
        }
    }
    body {
        div {
            id = "root"
        }
        footer {
            style = "position: fixed;" +
                    "left: 0;" +
                    "bottom: 0;" +
                    "height: 50px;" +
                    "width: 100%;" +
                    "background-color: #F0EDED;" +
                    "color: rgb(25, 118, 210);" +
                    "text-align: center;" +
                    "vertical-align: middle"
            div {
                h3 {
                    +"© Simon Lessenich, 2022"
                }
            }
        }
    }
}

fun Route.staticRouting() {
    get("/") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
    }
    static("/static") {
        resources()
    }
}

fun Application.registerStaticRouting() {
    routing {
        staticRouting()
    }
}