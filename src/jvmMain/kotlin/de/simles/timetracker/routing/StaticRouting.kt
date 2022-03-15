import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import kotlinx.html.*

fun HTML.index() {
    head {
        title("Timetracker")
        script {
            src = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
            integrity = "sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
            attributes.put("crossorigin", "anonymous")
        }
        link {
            href = "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css"
            rel = "stylesheet"
            integrity = "sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC"
            attributes.put("crossorigin", "anonymous")
        }
        script(src = "/static/Timetracker.js") {}
        link {
            rel = "icon"
            href = "/static/favicon.ico"
        }
    }
    body(classes = "d-flex flex-column min-vh-100") {
        div {
            id = "root"
        }
        footer(classes = "footer mt-auto py-3 bg-light text-center") {
            div(classes = "container") {
                span(classes = "text-muted") {
                    +"© Simon Lessenich, 2021"
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