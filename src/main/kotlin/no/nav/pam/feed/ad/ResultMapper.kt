package no.nav.pam.feed.ad


fun mapResult(root: SearchResponseRoot, page: Int, size: Int, host: String?): FeedRoot {
    val adList = root.hits.hits.map { element -> mapAd(element.source, host) }
    val total = root.hits.total.takeIf { x -> x in 0..MAX_TOTAL_HITS } ?: MAX_TOTAL_HITS

    return FeedRoot(adList, total, page, size)
}

fun mapAd(source: Source, host: String?): FeedAd {

    val locations = source.locations.map { l -> mapLocation(l) }
    var employer = source.businessName ?: source.properties["employer"]
    var link = "https://$host/stillinger/stilling/${source.uuid}"

    return FeedAd(
            source.uuid,
            source.published,
            source.expires,
            locations,
            source.title,
            employer ?: "",
            source.properties["employerdescription"],
            source.properties["adtext"],
            source.properties["sourceurl"],
            source.properties["applicationdue"],
            populateOccupations(source),
            link
    )
}

fun mapLocation(sourceLocation: Location): FeedLocation {
    return FeedLocation(
            sourceLocation.country,
            sourceLocation.address,
            sourceLocation.city,
            sourceLocation.postalCode,
            sourceLocation.county,
            sourceLocation.municipal
    )
}

fun populateOccupations(source: Source): List<String> {
    val occupations = arrayListOf<String>()

    if (source.source.toUpperCase() == "DIR") {
        for (category in source.categories) {
            occupations.add(category.name)
        }
    } else {
        if (source.properties["occupation"] != null) {
            occupations.addAll(source.properties["occupation"]!!.split(";"))
        } else if (source.properties["jobtitle"] != null) {
            occupations.add(source.properties["jobtitle"]!!)
        }
    }

    return occupations
}