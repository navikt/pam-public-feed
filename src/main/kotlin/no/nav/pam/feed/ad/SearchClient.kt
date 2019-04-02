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

fun Routing.feed(

        searchApiHost: String = "https://pam-search-api.nais.oera-q.local",
        clientFactory: () -> HttpClient
) {
    get("/api/v1/ads") {
        clientFactory().use { it ->
            try {
                val page = call.parameters["page"]?.toInt() ?: 0
                val size = call.parameters["size"]?.takeIf { x -> x.toInt() in 1..100 }?.toInt() ?: 100
                val from = page * size
                val requestBody = """{
                "sort": [{"published": "desc"}],
                "query": {
                   "term": {"status": "ACTIVE"}},
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
                 "size": $size
                 }""".trimIndent()

                val searchRequest = async {
                    it.post<SearchResponseRoot> {
                        url("$searchApiHost/ad/_search")
                        body = TextContent(text = requestBody, contentType = ContentType.Application.Json)
                    }
                }.await()

                call.respond(mapResult(searchRequest, page, size, call.request.host()))
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "One of the parameters has wrong format")
            }
        }
    }
}