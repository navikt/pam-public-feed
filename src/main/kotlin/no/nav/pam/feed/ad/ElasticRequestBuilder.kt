package no.nav.pam.feed.ad

class ElasticRequestBuilder(
        pageSize: String?,
        currentPage: String?) {

    private val pageSize: Int = (pageSize?.toInt() ?: 20).coerceIn(1 .. 100 )
    private val currentPage: Int


    private var updated: DateParam? = null
    private var published: DateParam? = null

    private var uuid: ValueParam? = null
    private var source: ValueParam? = null
    private var orgnr: ValueParam? = null

    private var municipal: ValueParam? = null
    private var county: ValueParam? = null

    init {
        this.currentPage = (currentPage?.toInt() ?: 0).coerceIn(0 .. MAX_TOTAL_HITS / this.pageSize)
    }

    internal fun municipal(municipal: String?) = apply { this.municipal =  municipal?.parseAsLocationValueFilter("municipal", true) }

    internal fun county(county: String?) = apply { this.county = county?.parseAsLocationValueFilter("county", true) }

    internal fun uuid(uuid: String?) = apply { this.uuid = uuid?.parseAsValueFilter("uuid", true) }

    internal fun source(source: String?) = apply { this.source = source?.parseAsValueFilter("source", true) }

    internal fun orgnr(orgnr: String?) = apply { this.orgnr = orgnr?.parseAsValueFilter("orgnr", true ) }

    internal fun updated(updated: String?) = apply { this.updated = updated?.parseAsDateFilter("updated", true) }

    internal fun published(published: String?) = apply { this.published = published?.parseAsDateFilter("published", true) }

    internal fun build() = ElasticRequest(
            pageSize = pageSize,
            currentPage = currentPage,
            valueFilters = listOfNotNull(uuid, source, orgnr),
            locationValueFilters = listOfNotNull(municipal, county),
            dateFilters = listOfNotNull(updated, published))

}