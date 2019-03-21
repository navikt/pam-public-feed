package no.nav.pam.feed

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.ssl.SSLContextBuilder

val acceptInsecureSslClientFactory: () -> HttpClient = {
    HttpClient(Apache) {
        engine {
            customizeClient {
                sslContext = SSLContextBuilder.create().loadTrustMaterial(TrustSelfSignedStrategy()).build()
                setSSLHostnameVerifier(NoopHostnameVerifier())
            }

        }
        install(JsonFeature) {
            serializer = JacksonSerializer {
                registerModule(JavaTimeModule())
                registerModule(KotlinModule())
            }
        }
    }
}

val localTestEnvironment: Environment = Environment(
        searchApiHost = "https://pam-search-api.nais.oera-q.local"
)

fun main(args: Array<String>) {

    Bootstrap.start(webApplication(
            clientFactory = acceptInsecureSslClientFactory,
            environment = localTestEnvironment))

}
