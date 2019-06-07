package no.nav.pam.feed.ad

import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.TextContent
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.host
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.filter
import io.ktor.util.flattenForEach
import mu.KotlinLogging
import java.io.IOException

internal const val MAX_TOTAL_HITS = 5000

private val log = KotlinLogging.logger { }

fun Route.feed(searchApiHost: String, httpClient: HttpClient) {
    log.info("Using search API host: ${searchApiHost}")

    get("/api/v1/ads") {

        val req = ElasticRequest(call.parameters.size, call.parameters.page, *call.parameters.filters())

        val response = httpClient.post<SearchResponseRoot> {
            url("$searchApiHost/public-feed/ad/_search")
            body = TextContent(text = req.body, contentType = ContentType.Application.Json)
        }

        log.debug { "Auth subject: ${call.principal<JWTPrincipal>()?.payload?.subject}" }

        call.respond(mapResult(response, call.parameters.page, call.parameters.size, call.request.host()))

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
private val Parameters.page get() = (this["page"]?.toInt() ?: 0).coerceIn(0 .. MAX_TOTAL_HITS / this.size)
private val validFilters = listOf("uuid", "source", "updated")
private fun Parameters.filters() = mutableListOf<Filter>()
        .apply { this@filters.filter { key, _ -> key in validFilters }
                .flattenForEach { key, value -> add(Filter(key, value)) } }
        .let { it.toTypedArray() }