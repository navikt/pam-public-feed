package no.nav.pam.feed.ad

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
    }
}

