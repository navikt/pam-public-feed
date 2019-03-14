package no.nav.pam.feed.ad

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.url
import io.ktor.client.response.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.async


fun Routing.feed(
        searchApiHost: String = "https://pam-search-api.nais.oera-q.local",
        clientFactory: () -> HttpClient = { HttpClient(Apache) }
) {
    get("/api/feed") {
        clientFactory().use {
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
                     "properties.industry"
                     ],
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
                    }.response.readText()
                }

                val gson = Gson()
                val jsonObject = gson.fromJson(searchRequest.await(), JsonObject::class.java)
                val feedPage = mapJsonObjectToFeedPage(jsonObject)

                call.respond(feedPage)
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, "One of parameters has wrong format")
            }
        }
    }
}