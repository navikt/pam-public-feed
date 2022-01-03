import io.ktor.http.content.*
import io.ktor.routing.Route
import java.io.File

fun Route.apiDoc() {

    static("swagger") {
        staticRootFolder = File("/swagger")
        resources("/swagger")
        files(".")
        defaultResource("index.html")
        static("api") {
            resources("swagger/api")
            staticBasePackage = "swagger.api"
        }

    }
}
