package no.nav.pam.feed.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import io.ktor.application.call
import io.ktor.auth.HttpAuthHeader
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import mu.KotlinLogging
import java.util.*

private val log = KotlinLogging.logger { }

class JwtTokenFactory(issuer: String, audience: String, secret: String) {
    val issuer = issuer
    val audience = audience
    val algorithm = Algorithm.HMAC256(secret)

    fun newHmacJwtVerifier(): JWTVerifier =
            JWT.require(algorithm) // signature
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()

    fun newTokenFor(subject: String): String =
            JWT.create()
                    .withSubject(subject)
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .withIssuedAt(Date())
                    .sign(algorithm)

}

fun Route.tokenManagementApi (tokenFactory: JwtTokenFactory) {

    get("apiTokenInfo") {
        val authHeader = call.request.parseAuthorizationHeader()
        if (authHeader != null && authHeader is HttpAuthHeader.Single && authHeader.authScheme == "Bearer") {
            try {
                val decoded = JWT.decode(authHeader.blob)
                val valid = try { tokenFactory.newHmacJwtVerifier().verify(decoded); "OK" } catch (e: Exception) { "FAIL: ${e.message}" }
                call.respondText("""

                    Token information:
                    Algorithm:    ${decoded.algorithm}
                    Subject:      ${decoded.subject}
                    Issuer:       ${decoded.issuer}
                    Issued at:    ${decoded.issuedAt}
                    Verification: ${valid}

                """.trimIndent())
            } catch (e: JWTDecodeException) {
                call.respondText("Unable to decode JWT token: ${e.message}\n")
            }
        } else {
            call.respondText("Authorization header not present or type is unsupported. Supply token in Authorization header.\n")
        }
    }

    post("newApiToken") {
        try {
            val subject = call.receiveParameters().get("subject")
            if (subject == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing form parameter 'subject'\n")
                return@post
            }

            val newToken = tokenFactory.newTokenFor(subject)
            log.info("New token created for subject '$subject': $newToken")
            call.respondText("For subject: ${subject}\nAuthorization: Bearer ${newToken}\n")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Bad form data\n")
        }
    }

}
