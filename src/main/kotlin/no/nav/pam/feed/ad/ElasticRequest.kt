package no.nav.pam.feed.ad

import mu.KotlinLogging

internal class ElasticRequest(pageSize: Int, currentPage: Int, vararg filters: Filter) {

    private val filters = MutableList(1) { Filter("status", "ACTIVE") } + filters

    private val size = pageSize
    private val from = currentPage * pageSize
    private val terms get() = filters.map { it.asTerm() }.joinToString(",\n")

    internal val body get() =
        """{
                "sort": [{"updated": "desc"}],
                "query": {
                   "bool": {
                     "filter": [
                       $terms
                     ]
                   }
                 },
                 "_source": {
                   "includes": [
                     "uuid",
                     "created",
                     "updated",
                     "published",
                     "expires",
                     "locationList",
                     "title",
                     "businessName",
                     "source",
                     "properties.adtext",
                     "properties.sourceurl",
                     "properties.applicationdue",
                     "properties.employer",
                     "properties.employerdescription",
                     "properties.employerhomepage",
                     "properties.engagementtype",
                     "properties.extent",
                     "properties.jobtitle",
                     "properties.occupation",
                     "properties.positioncount",
                     "properties.sector",
                     "properties.starttime",
                     "categoryList",
                     "employer"
                     ],
                   "excludes": [ ]
                 },
                 "from": $from,
                 "size": ${minOf(size, MAX_TOTAL_HITS - from)}
                 }""".trimIndent()
                .apply { log.debug { this } }


}

internal typealias Filter = Pair<String, String>

private fun Filter.asTerm() = """{ "term": {"$first":"$second"} }"""
private val log = KotlinLogging.logger { }
