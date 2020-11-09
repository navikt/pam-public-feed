package no.nav.pam.feed.ad

import mu.KotlinLogging
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.NestedQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder

class ElasticRequest(
        private val pageSize: Int,
        private val currentPage: Int,
        valueFilters: List<ValueParam> = listOf(),
        matchFilters: List<ValueParam> = listOf(),
        val locationValueFilters: List<ValueParam> = listOf(),
        val dateFilters: List<DateParam> = listOf()) {

    private val onlyActiveFilter: ValueParam = "ACTIVE".parseAsValueFilter("status")!!
    private val notFinnFilter: ValueParam = "!FINN".parseAsValueFilter("source")!!

    private val valueFilters = valueFilters + onlyActiveFilter + notFinnFilter

    fun asJson(): String {

        val queryBuilder = QueryBuilders.boolQuery()
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

    private val valueQueries = this.valueFilters.filter { !it.isNegated }.flatMap { it.toTermQueries() }
    private val dateQueries = dateFilters.map { it.toRangeQuery() }
    private val matchQueries = matchFilters.flatMap { it.toMatchQueries() }
    private val filterQueries = dateQueries + valueQueries + matchQueries + LocationQuery(locationValueFilters).filter()
    private val mustNotQueries = this.valueFilters.filter { it.isNegated }.flatMap { it.toTermQueries() }

}


private class LocationQuery(val filters: List<ValueParam> = listOf()) {

    internal fun filter(): List<QueryBuilder> {
        if(filters.isEmpty()) return emptyList()

        // This gets ugly... first groups all values by name - so we get diffent lists for municipalities and counties
        // then convert the contents to of each list to term queries
        // the package all term queries into a bool query - so we have one bool query for minicipalities and one for counties
        // Finally aggregate them into different filters ofthe locationFilter - so we get an AND-relation between the different groups (municipalities and counties)
        val locationQuery = filters.groupBy { it.name } // group by name (type)
                .mapValues { it.value.flatMap { it.toTermQueries() } } // transform to a list of term queries
                .mapValues { QueryBuilders.boolQuery().apply { should().addAll( it.value ) } } // convert term query list to a bool query with each element as should
                .values.fold(QueryBuilders.boolQuery()) { acc, it -> acc.filter(it)} // Fold the queries created in the last step into a wrapping bool filter query

        return listOf(NestedQueryBuilder(
                "locationList",
                locationQuery,
                ScoreMode.Avg))
    }

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

private fun ValueParam.toTermQueries() = values().map { QueryBuilders.termQuery(this.name, it) }
private fun ValueParam.toMatchQueries() = values().map { QueryBuilders.matchQuery(this.name, it) }

private fun DateParam.toRangeQuery(): RangeQueryBuilder {
    val range = QueryBuilders.rangeQuery(this.name)
    if (startInclusive) range.gte(start(stringConverter)) else range.gt(start(stringConverter))
    if (endInclusive) range.lte(end(stringConverter)) else range.lt(end(stringConverter))
    return range
}