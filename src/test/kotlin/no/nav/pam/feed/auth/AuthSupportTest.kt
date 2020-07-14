package no.nav.pam.feed.auth

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test

class AuthSupportTest {
    private fun jwtTokenFactory(): JwtTokenFactory {
        return JwtTokenFactory("test@nav", "auth-test", "fooBarBaz")
    }

    @Test
    fun testDenyList() {
        val om = jacksonObjectMapper()
        val json = """
      {
        "test@foo.bar": 0,
        "test@foo.baz": 1579513544000
      }"""
        val denylist: Map<String, Long> = om.readValue(json, object: TypeReference<Map<String, Long>>(){})

        val factory = jwtTokenFactory()
        val hmacVerifier = factory.newHmacJwtVerifier()
        val denylistVerifier = DenylistVerifier(denylist)

        val fooBarToken = factory.newTokenFor("test@foo.bar")
        val decodedFooBar = hmacVerifier.verify(fooBarToken)
        assert(denylistVerifier.isDenied(decodedFooBar))

        val fooBazToken = factory.newTokenFor("test@foo.baz")
        val decodedFooBaz = hmacVerifier.verify(fooBazToken)
        assert(!denylistVerifier.isDenied(decodedFooBaz))
    }
}