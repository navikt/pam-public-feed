package no.nav.pam.feed.ad

import io.prometheus.client.Counter

class SearchMeter {

    companion object {

        private val clientCounter = Counter.build("public_feed_searches_total", "Antall søk mot pam-public-feed")
                .labelNames("client")
                .register()

        private val termsCounter = Counter.build("public_feed_search_terms_total", "Type søk mot pam-public-feed.")
                .labelNames("parameter")
                .register()

    }

    fun searchPerformed(client: String, vararg searchParameterTypes: String) {
        clientCounter.labels(client).inc()

        searchParameterTypes.forEach { termsCounter.labels(it).inc() }

    }

}
