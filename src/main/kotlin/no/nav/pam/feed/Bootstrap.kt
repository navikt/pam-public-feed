package no.nav.pam.feed

import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import no.nav.pam.feed.Bootstrap.start
import no.nav.pam.feed.ad.feed
import no.nav.pam.feed.platform.naisApi

fun main(args: Array<String>) {


    start(webApplication())

}

fun webApplication(
        port: Int = 9021,
        clientFactory: () -> HttpClient = { HttpClient(Apache) },
        environment: Environment = Environment()
        ): ApplicationEngine {
    return embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            jackson(

            )
        }
        routing {
            naisApi()
            feed(clientFactory = clientFactory, searchApiHost = environment.searchApiHost)
        }
    }
}

object Bootstrap {

    private val log = KotlinLogging.logger { }

    fun start(webApplication: ApplicationEngine) {
        log.debug("Starting weg application")
        webApplication.start(wait = true)
    }
}