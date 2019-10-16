package no.nav.pam.feed.platform

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat


fun Route.naisApi(
        ready: () -> Boolean = { true },
        alive: () -> Boolean = { true },
        collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
) {
    get("isReady") {
        if (ready())
            call.respondText("Ready")
        else
            call.respondText("Wait, not ready yet!", status = HttpStatusCode.InternalServerError)
    }

    get("isAlive") {
        if (alive())
            call.respondText("I'm alive!")
        else
            call.respondText("Dead!", status = HttpStatusCode.InternalServerError)
    }

    get("prometheus") {
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}