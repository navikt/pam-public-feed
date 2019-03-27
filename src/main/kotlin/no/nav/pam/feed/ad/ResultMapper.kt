package no.nav.pam.feed.ad


fun mapResult(root: SearchResponseRoot, page: Int, size: Int): FeedRoot {
    val adList = root.hits.hits.map { element -> mapAd(element.source) }

    return FeedRoot(adList, root.hits.total, page, size)
}

fun mapAd(source: Source): FeedAd {

    val locations = source.locations.map { l -> mapLocation(l) }
    var employer = source.businessName ?: source.properties["employer"]

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
            source.properties["occupation"]
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