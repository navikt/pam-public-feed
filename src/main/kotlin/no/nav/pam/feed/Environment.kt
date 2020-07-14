package no.nav.pam.feed

data class AuthConfig (
        val optional: Boolean = false,
        val issuer: String = "nav.no",
        val audience: String = "feed-api-v1",
        val contact: String = "support@arbeidsplassen.nav.no",
        val secret: String = getEnvVar("AUTH_SECRET"),
        val blacklistJson: String = getEnvVar("BLACKLIST_JSON", "{}")
)

data class Environment (
        val searchApiHost: String = "http://pam-search-api.default",
        val contextPath: String = "/public-feed",
        val indentJson: Boolean = false,
        val auth: AuthConfig = AuthConfig()
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
        System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")