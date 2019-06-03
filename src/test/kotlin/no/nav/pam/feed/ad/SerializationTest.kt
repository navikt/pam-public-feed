package no.nav.pam.feed.ad

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class SerializationTest {

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun init() {
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(KotlinModule())
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    @Test
    fun shouldDeserializeCorrectly() {
        val stream = SerializationTest::class.java.getResourceAsStream("/search.sample/result.json")
        val root = objectMapper.readValue<SearchResponseRoot>(stream)

        val resultList = root.hits.hits.map { x -> x.source }
        val sample = resultList.find { x -> x.uuid == "82063742-1623-499e-8743-615e8ff39d41" }

        assertEquals(3, resultList.size)
        assertNotNull(sample)
        assertEquals("Ulvik herad", sample!!.businessName)
        assertEquals("979821506", sample.employer!!.orgnr)
        assertEquals("979692900", sample.employer!!.parentOrgnr)
        assertEquals("TEST COMPANY NAME", sample.employer!!.name)
    }

    @Test
    fun shouldMapCorrectly(){
        val stream = SerializationTest::class.java.getResourceAsStream("/search.sample/result.json")
        val root = objectMapper.readValue<SearchResponseRoot>(stream)
        val resultList = root.hits.hits.map { x -> x.source }
        val sample = resultList.find { x -> x.uuid == "82063742-1623-499e-8743-615e8ff39d41" }

        val feedAd = mapAd(sample!!, "localhost")
        assertNotNull(feedAd)
        assertEquals("Ulvik herad", feedAd.employer.name)
        assertEquals("979821506", feedAd.employer.orgnr)
        assertEquals(1, feedAd.workLocations.size)
        assertEquals("HORDALAND", feedAd.workLocations[0].county)
    }
}

