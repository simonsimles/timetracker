import de.simles.timetracker.Api
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.window

object JsApi : Api() {

    override fun getClient(): HttpClient {
        return HttpClient {
            install(JsonFeature) {
                KotlinxSerializer()
            }
            defaultRequest {
                host = window.location.hostname
                port = window.location.port.toInt()
                contentType(ContentType.Application.Json)
            }
        }
    }

}