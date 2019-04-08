package no.nav.pam.feed

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

class ApiTest {

    private val httpClient = HttpClient(Apache)

    companion object TestServices {

        private val mockSearchApi = MockEngine {
            when (url.fullPath) {
                "/public-feed/ad/_search" -> {
                    MockHttpResponse(
                            call,
                            HttpStatusCode.OK,
                            javaClass.getResourceAsStream("/search.sample/result.json").toByteReadChannel(),
                            headersOf("Content-Type" to listOf("application/json; charset=utf-8"))
                    )
                }
                else -> {
                    error("Unhandled ${url.fullPath}")
                }
            }
        }
        private val mockSearchApiClient = HttpClient(mockSearchApi) {
            install(JsonFeature) {
                serializer = JacksonSerializer {
                    registerModule(JavaTimeModule())
                    registerModule(KotlinModule())
                }
            }
        }

        val randomPort = ServerSocket(0).use { it.localPort }
        val webapp = webApplication(randomPort, { mockSearchApiClient },
                Environment(searchApiHost = "http://mocked-service", auth = AuthConfig(secret = "test-secret")))
        val appUrl = "http://localhost:${randomPort}"

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

    @Test
    fun testNewApiToken() {
        runBlocking<HttpResponse> {
            httpClient.submitForm<HttpResponse>(TestServices.appUrl + "/public-feed/internal/newApiToken",
                    Parameters.build { append("subject", "test@test") })
        }.also { assertTrue(it.status.isSuccess()) }
    }

    @Test
    fun testNoApiToken() {
        runBlocking<HttpResponse> {
            httpClient.get(TestServices.appUrl + "/public-feed/api/v1/ads")
        }.also { assertEquals(401, it.status.value) }
    }

    @Test
    fun testFeedWithApiToken() {
        val tokenValue = obtainApiTokenValue()

        runBlocking {
            httpClient.get<HttpResponse>(TestServices.appUrl + "/public-feed/api/v1/ads") {
                header("Authorization", "Bearer ${tokenValue}")
            }
        }.also { assertTrue(it.status.isSuccess())}

    }

    @Test
    fun testFeedNoAccessWithBadToken() {
        val badTokenValue = obtainApiTokenValue().replace("A", "B")

        runBlocking {
            httpClient.get<HttpResponse>(TestServices.appUrl + "/public-feed/api/v1/ads") {
                header("Authorization", "Bearer ${badTokenValue}")
            }
        }.also { assertEquals(401, it.status.value)}
    }

    private fun obtainApiTokenValue(): String {
        val response = runBlocking<HttpResponse> {
            httpClient.submitForm(TestServices.appUrl + "/public-feed/internal/newApiToken",
                    Parameters.build { append("subject", "test@test") })
        }
        return runBlocking { response.readText(Charsets.UTF_8) }.let { text ->
            text.lines().first { line -> line.startsWith("Authorization:") }.removePrefix("Authorization: Bearer ")
        }
    }

}