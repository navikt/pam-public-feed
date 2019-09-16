package no.nav.pam.feed.ad

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import mu.KotlinLogging

internal class ElasticRequest(
        pageSize: Int,
        currentPage: Int,
        valueFilters: List<ValueParam> = listOf(),
        locationValueFilters: List<ValueParam> = listOf(),
        dateFilters: List<DateParam> = listOf()) {

    private val defaultValue: ValueParam = "ACTIVE".parseAsValueFilter("status", true).get()

    private val size = pageSize
    private val from = currentPage * pageSize

    private val terms: List<Term> = listOf(
            valueFilters.map { it.asTerm() } + defaultValue.asTerm(),
            dateFilters.map { it.asTerm() })
            .flatten().filterNotNull()
    private val negatedTerms: List<Term> = valueFilters.mapNotNull { it.asNegatedTerm() }
    private val locationTerms: List<Term> = listOf(
            locationValueFilters.map{ it.asTerm() })
            .flatten().filterNotNull()

    internal fun asJson() = objectMapper.writeValueAsString(SearchRequest(
            query = Query(bool = BoolQuery(must_not = negatedTerms, filter = terms,
                    must = if (locationTerms.isEmpty()) emptyList()
                            else listOf(Nested(NestedQuery(query = Query(bool = BoolQuery(filter = locationTerms))))))),
            from = from,
            size = size))
            .apply { log.debug { this } }

}

private val log = KotlinLogging.logger { }

val objectMapper = ObjectMapper().apply {
    registerModule(SimpleModule()
            .addSerializer(RangeSerializer())
            .addSerializer(ValueSerializer())
            .addSerializer(SortSerializer()))
}

private fun ValueParam.asTerm(): Term? = if (!isNegated) Value(name, value()) else null
private fun ValueParam.asNegatedTerm(): Term? = if(isNegated) Value(name, value()) else null

interface Term

private fun DateParam.asTerm(): Term = Range(
        name = name,
        startOperator = if(startInclusive) "gte" else "gt",
        start = start(stringConverter),
        endOperator = if(endInclusive) "lte" else "lt",
        end = end(stringConverter)
)

private data class SearchRequest(
        val sort: List<Sort> = listOf(Sort()),
        val query: Query,
        val _source: QuerySource = QuerySource(),
        val from: Int,
        val size: Int
)

private data class Sort(val field: String = "published", val direction: String = "desc")
private data class QuerySource(val includes: List<String> = defaultFields, val excludes: List<String> = emptyList())
private data class Query(val bool: BoolQuery = BoolQuery())
private data class BoolQuery(val must_not: List<Term> = emptyList(), val filter: List<Term> = emptyList(),
                             val must: List<Nested> = emptyList())
private data class Nested(val nested: NestedQuery)
private data class NestedQuery(val path: String = "locationList", val query: Query)
private data class Range(val name: String, val startOperator: String, val endOperator: String, val start: String, val end: String): Term
private data class Value(val name: String, val value: String): Term

private class RangeSerializer: JsonSerializer<Range>() {
    override fun handledType() = Range::class.java

    override fun serialize(range: Range?, jgen: JsonGenerator?, provider: SerializerProvider?) {
        jgen?.writeStartObject()
            jgen?.writeObjectFieldStart("range")
                jgen?.writeObjectFieldStart(range?.name)
                    jgen?.writeStringField(range?.startOperator, range?.start)
                    jgen?.writeStringField(range?.endOperator, range?.end)
                jgen?.writeEndObject()
            jgen?.writeEndObject()
        jgen?.writeEndObject()
    }
}
private class ValueSerializer: JsonSerializer<Value>() {
    override fun handledType() = Value::class.java

    override fun serialize(value: Value?, jgen: JsonGenerator?, provider: SerializerProvider?) {
        jgen?.writeStartObject()
            jgen?.writeObjectFieldStart("term")
                jgen?.writeStringField(value?.name, value?.value)
            jgen?.writeEndObject()
        jgen?.writeEndObject()
    }
}
private class SortSerializer: JsonSerializer<Sort>() {
    override fun handledType() = Sort::class.java

    override fun serialize(sort: Sort?, jgen: JsonGenerator?, provider: SerializerProvider?) {
        jgen?.writeStartObject()
        jgen?.writeStringField(sort?.field, sort?.direction)
        jgen?.writeEndObject()
    }
}

private val defaultFields = listOf(
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
        "properties.positioncount",
        "properties.sector",
        "properties.starttime",
        "occupationList",
        "employer"
)