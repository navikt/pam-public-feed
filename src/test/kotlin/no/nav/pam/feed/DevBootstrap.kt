package no.nav.pam.feed

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
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
