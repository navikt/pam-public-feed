package no.nav.pam.feed.auth

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test

class AuthSupportTest {
    private fun jwtTokenFactory(): JwtTokenFactory {
        return JwtTokenFactory("test@nav", "auth-test", "fooBarBaz")
    }

    @Test
    fun testBlackList() {
        val om = jacksonObjectMapper()
        val json = """
      {
        "test@foo.bar": 0,
        "test@foo.baz": 1579513544000
      }"""
        val blacklist: Map<String, Long> = om.readValue(json, object: TypeReference<Map<String, Long>>(){})

        val factory = jwtTokenFactory()
        val hmacVerifier = factory.newHmacJwtVerifier()
        val blacklistVerifier = BlacklistVerifier(blacklist)

        val fooBarToken = factory.newTokenFor("test@foo.bar")
        val decodedFooBar = hmacVerifier.verify(fooBarToken)
        assert(blacklistVerifier.isBlacklisted(decodedFooBar))

        val fooBazToken = factory.newTokenFor("test@foo.baz")
        val decodedFooBaz = hmacVerifier.verify(fooBazToken)
        assert(!blacklistVerifier.isBlacklisted(decodedFooBaz))
    }
}