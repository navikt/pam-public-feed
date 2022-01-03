package no.nav.pam.feed

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.util.cio.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.pam.feed.ad.FeedRoot
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.server.engine.*

private val log = KotlinLogging.logger {}

class ApiIntegrationTest {
    private val httpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = jacksonSerializer
        }
    }

    companion object TestServices {

        private val searchApi = MockEngine { request ->
            when (request.url.fullPath) {
                "/public-feed/ad/_search" -> {
                    respond(
                            javaClass.getResourceAsStream("/search.sample/result.json").toByteReadChannel(),
                            HttpStatusCode.OK,
                            headersOf("Content-Type" to listOf("application/json; charset=utf-8"))
                    )
                }
                else -> {
                    error("Unhandled ${request.url.fullPath}")
                }
            }
        }
        private val mockSearchApiClient = HttpClient(searchApi) {
            install(JsonFeature) {
                serializer = jacksonSerializer
            }
        }

        private val randomPort = ServerSocket(0).use { it.localPort }
        private val webapp = searchApi(randomPort, { mockSearchApiClient },
                Environment(searchApiHost = "http://mocked-service", auth = AuthConfig(secret = "test-secret")))
        val appUrl = "http://localhost:$randomPort"

        @BeforeAll
        @JvmStatic
        internal fun startServices() {
            webapp.start()
            log.debug("Started test services")
        }

        @AfterAll
        @JvmStatic
        internal fun stopServices() {
            webapp.stop(0, 0, TimeUnit.SECONDS)
            log.debug("Stopped test services")
        }
    }

    //@Test
    fun `that elastic query with paging is wellformed`() {
        runBlocking { `call with parameters`("size=20&page=50") }
        val elasticJson = JSONObject(String(runBlocking { searchApi.requestHistory.last().body.toByteArray() }))

        assertThat(elasticJson.query("/from")).isEqualTo(1000);
        assertThat(elasticJson.query("/size")).isEqualTo(20);
    }

    //@Test
    fun `that elastic query with single source is wellformed`() {
        runBlocking { `call with parameters`("source=Stillingsregistrering") }
        val elasticJson = JSONObject(String(runBlocking { searchApi.requestHistory.last().body.toByteArray() }))

        assertThat(elasticJson.query("/query/bool/filter/0/term/source/value")).isEqualTo("Stillingsregistrering")
    }

    //@Test
    fun `that elastic query with multiple params is wellformed`() {
        runBlocking { `call with parameters`("category=Utdanning&source=Stillingsregistrering") }
        val elasticJson = JSONObject(String(runBlocking { searchApi.requestHistory.last().body.toByteArray() }))

        assertThat(elasticJson.query("/query/bool/filter/0/term/source/value")).isEqualTo("Stillingsregistrering")
        assertThat(elasticJson.query("/query/bool/filter/2/match/category_no/query")).isEqualTo("Utdanning")
    }

    //@Test
    fun `that elastic query with single excluded source is wellformed`() {
        runBlocking { `call with parameters`("source=!Stillingsregistrering") }
        val elasticJson = JSONObject(String(runBlocking { searchApi.requestHistory.last().body.toByteArray() }))

        assertThat(elasticJson.query("/query/bool/must_not/0/term/source/value")).isEqualTo("Stillingsregistrering")

    }

    //@Test
    fun `that elastic query with multiple excluded sources source is wellformed`() {
        runBlocking { `call with parameters`("source=!Stillingsregistrering,XML_STILLING") }
        val elasticJson = JSONObject(String(runBlocking { searchApi.requestHistory.last().body.toByteArray() }))

        assertThat(elasticJson.query("/query/bool/must_not/0/term/source/value")).isEqualTo("Stillingsregistrering")
        assertThat(elasticJson.query("/query/bool/must_not/1/term/source/value")).isEqualTo("XML_STILLING")
    }

    //@Test
    fun `that elastic query always includes active`() {
        runBlocking { `call with parameters`("") }
        val elasticJson = JSONObject(String(runBlocking { searchApi.requestHistory.last().body.toByteArray() }))

        assertThat(elasticJson.query("/query/bool/filter/0/term/status/value")).isEqualTo("ACTIVE")
    }

    //@Test
    fun `that elastic query always excludes finn ads`() {
        runBlocking { `call with parameters`("") }
        val elasticJson = JSONObject(String(runBlocking { searchApi.requestHistory.last().body.toByteArray() }))

        assertThat(elasticJson.query("/query/bool/must_not/0/term/source/value")).isEqualTo("FINN")
    }

    suspend fun `call with parameters`(queryParams: String) {
        httpClient.get<FeedRoot>("$appUrl/public-feed/api/v1/ads?$queryParams") {
            header("Authorization", "Bearer ${obtainApiTokenValue()}")
        }
    }

    private fun obtainApiTokenValue(subject: String = "test@test"): String = runBlocking {
        httpClient.submitForm<HttpResponse>(appUrl + "/public-feed/internal/newApiToken",
                Parameters.build { append("subject", subject) })
            .readText(Charsets.UTF_8).let { text ->
                    text.lines().first { line -> line.startsWith("Authorization:") }
                            .removePrefix("Authorization: Bearer ")
                }
    }

}
