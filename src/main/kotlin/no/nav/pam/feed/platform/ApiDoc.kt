import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.content.staticBasePackage
import io.ktor.request.path
import io.ktor.response.respondRedirect
import io.ktor.routing.Route

fun Route.apiDoc() {

    static("swagger") {

        intercept(ApplicationCallPipeline.Call) {
            call.request.path().takeUnless { it.endsWith("/") }?.also {
                call.respondRedirect("$it/")
                finish()
            }
        }

        static("api") {
            resources("swagger/api")
            staticBasePackage = "swagger.api"
        }
        resources("swagger")
        staticBasePackage = "swagger"
        defaultResource("index.html")
    }
}
