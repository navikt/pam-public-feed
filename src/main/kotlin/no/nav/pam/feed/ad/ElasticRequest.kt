package no.nav.pam.feed.ad

import mu.KotlinLogging

internal class ElasticRequest(
        pageSize: Int,
        currentPage: Int,
        valueFilters: List<ValueParam> = listOf(),
        dateFilters: List<DateParam> = listOf()) {

    private val defaultValue: ValueParam = "ACTIVE".parseAsValueFilter("status", true).get()

    private val size = pageSize
    private val from = currentPage * pageSize

    private val terms: String
    private val negatedTerms: String

    init {
        val valueTerms = (valueFilters + defaultValue).map { it.asTerm() }.filter { it.isNotBlank() }.joinToString(",\n")
        val rangeTerms = dateFilters.map { it.asTerm() }.joinToString(",\n")
        terms = listOf(valueTerms, rangeTerms).filter { it.isNotBlank() }.joinToString(",\n")
        negatedTerms = valueFilters.map { it.asNegatedTerm() }.filter { it.isNotBlank() }.joinToString(",\n")
    }

    internal val body get() =
        """{
                "sort": [{"updated": "desc"}],
                "query": {
                   "bool": {
                     "must_not": [
                       $negatedTerms
                     ],
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

fun ValueParam.asTerm() = if (!isNegated) """{ "term": {"${name}":"${value()}"} }""" else ""
fun ValueParam.asNegatedTerm() = if(isNegated) """{ "term": {"${name}":"${value()}"} }""" else ""

fun DateParam.asTerm(): String {
    val startOperator = if(startInclusive()) "gte" else "gt"
    val endOperator = if(endInclusive()) "lte" else "lt"
    return """{
        |"range":
          |{"${name()}": {
            |"${startOperator}" : "${start(Converter.ToString)}",
            |"${endOperator}" : "${end(Converter.ToString)}"
            |}
          |}
        |}""".trimMargin()
}

private val log = KotlinLogging.logger { }
