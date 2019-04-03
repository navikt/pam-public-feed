package no.nav.pam.feed.ad

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.host
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.async

const internal val MAX_TOTAL_HITS = 500

fun Routing.feed(

        searchApiHost: String = "https://pam-search-api.nais.oera-q.local",
        clientFactory: () -> HttpClient
) {
    get("/api/v1/ads") {
        clientFactory().use { client ->
            try {
                val size = (call.parameters["size"]?.toInt() ?: 50).let { Math.min(Math.max(it, 1), 100) }
                val page = (call.parameters["page"]?.toInt() ?: 0).let { Math.min(Math.max(it, 0), MAX_TOTAL_HITS / size) }

                val from = page * size
                val requestBody = """{
                "sort": [{"published": "desc"}],
                "query": {
                   "constant_score": {
                     "filter": {
                       "term": {"status": "ACTIVE"}
                     }
                   }
                 },
                 "_source": {
                   "includes": [
                     "uuid",
                     "created",
                     "updated",
                     "published",
                     "expires",
                     "locationList",
                     "title",
                     "businessName",
                     "source",
                     "properties.adtext",
                     "properties.sourceurl",
                     "properties.applicationdue",
                     "properties.employer",
                     "properties.employerdescription",
                     "properties.occupation",
                     "properties.jobtitle",
                     "categoryList"
                     ],
                   "excludes": [ ]
                 },
                 "from": $from,
                 "size": ${Math.min(size, MAX_TOTAL_HITS - from)}
                 }""".trimIndent()

                val searchRequest = async {
                    client.post<SearchResponseRoot> {
                        url("$searchApiHost/public-feed/ad/_search")
                        body = TextContent(text = requestBody, contentType = ContentType.Application.Json)
                    }
                }.await()

                call.respond(mapResult(searchRequest, page, size, call.request.host()))
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "Bad numeric parameter value: ${e.message}")
            }
        }
    }
}