package no.nav.pam.feed

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
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
        clientFactory: () -> HttpClient = {
            HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = JacksonSerializer {
                        registerModule(JavaTimeModule())
                        registerModule(KotlinModule())
                    }
                }
            }
        },
        environment: Environment = Environment()
): ApplicationEngine {
    return embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            jackson {
                /*
                //TODO: only in dev
                configure(SerializationFeature.INDENT_OUTPUT, true)
                setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                })
                */

                registerModule(JavaTimeModule())
                registerModule(KotlinModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            }
        }
        install(DefaultHeaders) {
            header(HttpHeaders.CacheControl, "public, max-age=600")
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