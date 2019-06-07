package no.nav.pam.feed

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
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
            serializer = jacksonSerializer
        }
    }
}

val localTestEnvironment: Environment = Environment(
        searchApiHost =  getEnvVar("SEARCH_API_HOST", "http://localhost:9025"),
        indentJson = true,
        auth = AuthConfig(optional = true, secret = getEnvVar("AUTH_SECRET", "dev-key"))
)

fun main() {

    Bootstrap.start(searchApi(
            clientFactory = acceptInsecureSslClientFactory,
            environment = localTestEnvironment))

}
