package no.nav.pam.feed

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.*
import kotlinx.coroutines.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.pam.feed.ad.FeedRoot
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

class ApiTest {

    private val httpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = jacksonSerializer
        }
    }

    companion object TestServices {

        private val mockSearchApi = MockEngine { request ->
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
        private val mockSearchApiClient = HttpClient(mockSearchApi) {
            install(JsonFeature) {
                serializer = jacksonSerializer
            }
        }

        private val randomPort = ServerSocket(0).use { it.localPort }
        val webapp = searchApi(randomPort, { mockSearchApiClient },
                Environment(searchApiHost = "http://mocked-service",
                        auth = AuthConfig(secret = "test-secret",
                        denylistJson = """
                            {
                                "foo@bar.no": 0
                            }
                        """)))
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
        runBlocking {
            httpClient.submitForm<HttpResponse>(appUrl + "/public-feed/internal/newApiToken",
                    Parameters.build { append("subject", "test@test") })
        }.also { assertTrue(it.status.isSuccess()) }
    }

    @Test
    fun testFeedWithValidApiToken() {
        val tokenValue = obtainApiTokenValue()

        runBlocking {
            httpClient.get<HttpResponse>(appUrl + "/public-feed/api/v1/ads") {
                header("Authorization", "Bearer ${tokenValue}")
            }
        }.also { assertTrue(it.status.isSuccess())}
    }

    @Test
    fun testFeedWithValidTokenFutureExpiry() {
        val futureExpiryToken = obtainApiTokenValue(expires = LocalDateTime.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE))

        runBlocking {
            httpClient.get<HttpResponse>(appUrl + "/public-feed/api/v1/ads") {
                header("Authorization", "Bearer ${futureExpiryToken}")
            }
        }.also { assertTrue(it.status.isSuccess()) }
    }

    @Test
    fun testFeedWithExpiredToken() {
        val expiredToken = obtainApiTokenValue(expires = "2018-01-01")

        runBlocking {
            httpClient.get<HttpResponse>(appUrl + "/public-feed/api/v1/ads") {
                header("Authorization", "Bearer ${expiredToken}")
            }
        }.also { assertEquals(401, it.status.value) }
    }

    @Test
    fun testNoApiToken() {
        runBlocking {
            httpClient.get<HttpResponse>(appUrl + "/public-feed/api/v1/ads")
        }.also { assertEquals(401, it.status.value) }
    }

    @Test
    fun testFeedNoAccessWithBadToken() {
        val badTokenValue = obtainApiTokenValue().replace("A", "B")
        runBlocking {
            httpClient.get<HttpResponse>(appUrl + "/public-feed/api/v1/ads") {
                header("Authorization", "Bearer ${badTokenValue}")
            }
        }.also { assertEquals(401, it.status.value) }
    }

    @Test
    fun testFeedWithRevokedApiToken() {
        val tokenValue = obtainApiTokenValue("foo@bar.no")

        runBlocking {
            httpClient.get<HttpResponse>(appUrl + "/public-feed/api/v1/ads") {
                header("Authorization", "Bearer ${tokenValue}")
            }
        }.also { assertEquals(401, it.status.value)}
    }

    @Test
    fun paging_Size10Page0() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=10&page=0") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(10, feed.pageSize)
        assertEquals(0, feed.pageNumber)
        assertEquals(34, feed.totalPages)
        assertEquals(332, feed.totalElements)
        assertTrue(feed.first)
        assertFalse(feed.last)
    }

    @Test
    fun paging_Size10Page1() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=10&page=1") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(10, feed.pageSize)
        assertEquals(1, feed.pageNumber)
        assertEquals(34, feed.totalPages)
        assertEquals(332, feed.totalElements)
        assertFalse(feed.first)
        assertFalse(feed.last)
    }

    @Test
    fun paging_Size10Page32() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=10&page=32") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(10, feed.pageSize)
        assertEquals(32, feed.pageNumber)
        assertEquals(34, feed.totalPages)
        assertEquals(332, feed.totalElements)
        assertFalse(feed.first)
        assertFalse(feed.last)
    }

    @Test
    fun paging_Size10Page33() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=10&page=33") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(10, feed.pageSize)
        assertEquals(33, feed.pageNumber)
        assertEquals(34, feed.totalPages)
        assertEquals(332, feed.totalElements)
        assertFalse(feed.first)
        assertTrue(feed.last)
    }

    @Test
    fun paging_SizeNegativePage0() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=-10&page=0") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(1, feed.pageSize)
    }

    @Test
    fun paging_PageNegative() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?page=-10") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(0, feed.pageNumber)
    }

    @Test
    fun paging_Size1() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=1") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(1, feed.pageSize)
        assertEquals(0, feed.pageNumber)
        assertEquals(332, feed.totalPages)
        assertEquals(332, feed.totalElements)
        assertTrue(feed.first)
        assertFalse(feed.last)
    }

    @Test
    fun location_Size1() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=1&municipal=OSLO&county=OSLO") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(1, feed.pageSize)
        assertEquals(0, feed.pageNumber)
        assertTrue(feed.first)
        assertFalse(feed.last)
    }

    @Test
    fun paging_Size1Page331() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=1&page=331") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(1, feed.pageSize)
        assertEquals(331, feed.pageNumber)
        assertEquals(332, feed.totalPages)
        assertEquals(332, feed.totalElements)
        assertFalse(feed.first)
        assertTrue(feed.last)
    }

    @Test
    fun paging_Size100Page100() {
        val feed: FeedRoot = runBlocking {
            httpClient.get<FeedRoot>(appUrl + "/public-feed/api/v1/ads?size=100&page=100") {
                header("Authorization", "Bearer ${obtainApiTokenValue()}")
            }
        }

        assertEquals(100, feed.pageSize)
        assertEquals(49, feed.pageNumber)
        assertEquals(4, feed.totalPages)
        assertEquals(332, feed.totalElements)
        assertFalse(feed.first)
        assertTrue(feed.last)
    }

    private fun obtainApiTokenValue(subject: String = "test@test", expires: String? = null): String = runBlocking {
        httpClient.submitForm<HttpResponse>(appUrl + "/public-feed/internal/newApiToken",
                Parameters.build {
                    append("subject", subject)
                    expires?.also { append("expires", it) }
                })
                .readText(Charsets.UTF_8).let { text ->
                    text.lines().first { line -> line.startsWith("Authorization:") }
                            .removePrefix("Authorization: Bearer ")
                }
    }


}
