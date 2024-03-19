package no.nav.pam.feed.ad

import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.host
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import mu.KotlinLogging
import mu.withLoggingContext
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.format.DateTimeParseException

internal const val MAX_TOTAL_HITS = 5000

private val log = KotlinLogging.logger { }
private val secureLog = KotlinLogging.logger("secureLogger")

fun Route.feed(
        searchApiHost: String,
        httpClient: HttpClient,
        searchMeter: SearchMeter = SearchMeter()
) {
    val url = "$searchApiHost/public-feed/ad/_search"
    log.info("Using search API host: ${searchApiHost}")

    get("/api/v1/ads") {
        val subject = call.principal<JWTPrincipal>()?.payload?.subject ?: "?"
        withLoggingContext("U" to subject) {
            secureLog.info ("Auth subject: $subject")

            searchMeter.searchPerformed(
                    client = subject.substringAfter("@"),
                    searchParameterTypes = *call.parameters.names().toTypedArray())

            try {
                val elasticRequest = ElasticRequestBuilder(call.parameters["size"], call.parameters["page"])
                    .updated(call.parameters["updated"])
                    .published(call.parameters["published"])
                    .uuid(call.parameters["uuid"])
                    .source(call.parameters["source"])
                    .orgnr(call.parameters["orgnr"])
                    .municipal(call.parameters["municipal"])
                    .county(call.parameters["county"])
                    .category(call.parameters["category"])
                    .build()
                val elasticRequestAsJson = elasticRequest.asJson()

                val response = httpClient.post<SearchResponseRoot>(url) {
                    body = TextContent(elasticRequestAsJson, ContentType.Application.Json)
                }
                call.respond(mapResult(response, call.parameters.page, call.parameters.size, call.request.host()))
            } catch (dtpe: DateTimeParseException) {
                log.warn("Datoparsing feilet: ${dtpe.message}")
                call.respond(HttpStatusCode.BadRequest, "Date is badly formatted")
            }
        }
    }
}

fun StatusPages.Configuration.feed() {

    exception<IOException> {
        log.error(it) { "Failed to communicate with backend" }
        call.respond(HttpStatusCode.BadGateway, "Failed to communicate with backend")
    }
    exception<NumberFormatException>  {
        log.error { "Bad numeric parameter value: ${it.message}" }
        call.respond(HttpStatusCode.BadRequest, "Bad numeric parameter value: ${it.message}")
    }
}

private val Parameters.size get() = (this["size"]?.toInt() ?: 20).coerceIn(1 .. 100 )
private val Parameters.page get() = (this["page"]?.toInt() ?: 0).coerceIn(0 until MAX_TOTAL_HITS / this.size)
