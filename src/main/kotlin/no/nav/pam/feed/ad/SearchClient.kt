package no.nav.pam.feed.ad

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.response.readBytes
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
            val searchRequest = async { it.call("$searchApiHost/ad/_search").response.readBytes() }

            // TODO conversion
            
            call.respond(searchRequest.await())
        }

    }
}