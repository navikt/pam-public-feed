package no.nav.pam.feed.ad

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.url
import io.ktor.client.response.readBytes
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.async


data class FeedAd(val title: String)

fun Routing.feed(
        searchApiHost: String = "https://pam-search-api.nais.oera-q.local",
        clientFactory: () -> HttpClient = { HttpClient(Apache) }
) {
    get("/api/feed") {
        clientFactory().use {
            try {
                val from = call.parameters["from"]?.toInt() ?: 0
                val size = call.parameters["size"]?.toInt() ?: 1
                val requestBody = """{
                "sort": [{"published": "desc"}],
                "query": {
                   "term": {"status": "ACTIVE"}},
                 "_source": {
                   "includes": [  ],
                   "excludes": [ ]
                 },
                 "from": $from,
                 "size": $size
                 }""".trimIndent()

                val searchRequest = async {
                    it.call {
                        method = HttpMethod.Post
                        url("$searchApiHost/ad/_search")
                        body = TextContent(text = requestBody, contentType = ContentType.Application.Json)
                    }.response.readBytes()
                }

                // TODO conversion

                call.respond(searchRequest.await())
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "One of parameters has wrong format")
            }
        }
    }
}