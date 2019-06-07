package no.nav.pam.feed

import apiDoc
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import no.nav.pam.feed.Bootstrap.start
import no.nav.pam.feed.ad.feed
import no.nav.pam.feed.auth.JwtTokenFactory
import no.nav.pam.feed.auth.tokenManagementApi
import no.nav.pam.feed.platform.naisApi

private val log = KotlinLogging.logger { }
private val defaultClientFactory :  () -> HttpClient = {
    HttpClient(Apache) {
        install(JsonFeature) {
            serializer = jacksonSerializer
        }
    }
}

fun main() {

    start(searchApi())
}


fun searchApi(
        port: Int = 9021,
        clientFactory: () -> HttpClient = defaultClientFactory,
        environment: Environment = Environment()
): ApplicationEngine {

    val tokenFactory = JwtTokenFactory(environment.auth.issuer, environment.auth.audience, environment.auth.secret)
    if (environment.auth.optional) {
        log.warn("API authentication requirement disabled")
    }
    log.info("Token secret key has ${environment.auth.secret.length} characters")

    return embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            jackson {
                registerModules(JavaTimeModule(), KotlinModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            }
        }
        install(DefaultHeaders) {
            header(HttpHeaders.CacheControl, "public, max-age=300")
            header(HttpHeaders.Server, "ktor")
        }
        install(Authentication) {
            jwt {
                verifier(tokenFactory.newHmacJwtVerifier())
                this.realm = "${environment.auth.audience}, contact ${environment.auth.contact} for access"
                validate { credential ->
                    if (credential.payload.audience.contains(environment.auth.audience))
                        JWTPrincipal(credential.payload)
                    else
                        null
                }
            }
        }
        install(CORS) {
            allowCredentials = true
            header("Authorization")
            anyHost()
        }
        install(StatusPages) {
            feed()
        }
        routing {

            route (environment.contextPath) {

                apiDoc()

                route("/internal") {
                    naisApi()
                    tokenManagementApi(tokenFactory)
                }

                authenticate(optional = environment.auth.optional) {
                    feed(httpClient = clientFactory(), searchApiHost = environment.searchApiHost)
                }
            }
        }
    }
}


object Bootstrap {

    fun start(webApplication: ApplicationEngine) {
        log.debug("Starting web application")
        webApplication.start(wait = true)
    }

}

internal val jacksonSerializer = JacksonSerializer {
    registerModule(JavaTimeModule())
    registerModule(KotlinModule())
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

}