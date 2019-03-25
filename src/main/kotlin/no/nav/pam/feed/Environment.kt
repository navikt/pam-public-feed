package no.nav.pam.feed

data class Environment (
    val searchApiHost: String = "http://pam-search-api.default",
    var arbeidsplassenHost: String = getEnvVar("arbeidsplassen_url", "test")
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
        System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")