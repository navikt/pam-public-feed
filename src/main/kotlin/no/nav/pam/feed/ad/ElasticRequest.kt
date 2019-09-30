package no.nav.pam.feed.ad

import mu.KotlinLogging
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.NestedQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder

class ElasticRequest(
        private val pageSize: Int,
        private val currentPage: Int,
        valueFilters: List<ValueParam> = listOf(),
        val locationValueFilters: List<ValueParam> = listOf(),
        val dateFilters: List<DateParam> = listOf()) {

    private val defaultValue: ValueParam = "ACTIVE".parseAsValueFilter("status", true)!!
    private val valueFilters = valueFilters + defaultValue

    fun asJson(): String {

        val queryBuilder = QueryBuilders.boolQuery()
                .must(locationQuery)
                .apply { filter().addAll(filterQueries) }
                .apply { mustNot().addAll(mustNotQueries) }

        val request = SearchSourceBuilder()
                .sort("published", SortOrder.DESC)
                .query(queryBuilder)
                .fetchSource(sourceIncludes, emptyArray())
                .size(pageSize)
                .from(currentPage * pageSize)
                .toString()

        return request.apply { log.debug { this } }
    }

    private val valueQueries = this.valueFilters.filter { !it.isNegated }.map { it.toTermQuery() }
    private val dateQueries = dateFilters.map { it.toRangeQuery() }
    private val filterQueries = dateQueries + valueQueries
    private val mustNotQueries = valueFilters.filter { it.isNegated }.map { it.toTermQuery() }
    private val locationFilterQueries = locationValueFilters.map { it.toTermQuery() }

    private val locationQuery = NestedQueryBuilder(
            "locationList",
            QueryBuilders.boolQuery().apply { filter().addAll(locationFilterQueries) },
            ScoreMode.Avg)

}

private val log = KotlinLogging.logger { }

private val sourceIncludes = arrayOf(
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

fun ValueParam.toTermQuery() = QueryBuilders.termQuery(this.name, this.value())

fun DateParam.toRangeQuery(): RangeQueryBuilder {
    val range = QueryBuilders.rangeQuery(this.name)
    if (startInclusive) range.gte(start(stringConverter)) else range.gt(start(stringConverter))
    if (endInclusive) range.lte(end(stringConverter)) else range.lt(end(stringConverter))
    return range
}