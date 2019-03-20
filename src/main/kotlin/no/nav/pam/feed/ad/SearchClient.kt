package no.nav.pam.feed.ad

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.async

fun Routing.feed(

        searchApiHost: String = "https://pam-search-api.nais.oera-q.local",
        clientFactory: () -> HttpClient = {
            HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = JacksonSerializer {
                        registerModule(JavaTimeModule())
                        registerModule(KotlinModule())
                    }
                }
            }
        }
) {
    get("/api/feed") {
        clientFactory().use { it ->
            try {
                val from = call.parameters["from"]?.toInt() ?: 0
                val size = call.parameters["size"]?.toInt() ?: 100
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
                     "source",
                     "medium",
                     "reference",
                     "businessName",
                     "properties.adtext",
                     "properties.sourceurl",
                     "properties.applicationdue",
                     "properties.engagementtype",
                     "properties.extent",
                     "properties.occupation",
                     "properties.positioncount",
                     "properties.sector",
                     "properties.industry",
                     "properties.employer"
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

                call.respond(mapResult(searchRequest))
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "One of parameters has wrong format")
            }
        }
    }
}