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
//            source.created,
//            source.updated,
            source.published,
            source.expires,
            locations,
            source.title,
            source.reference,
            employer ?: "",
            source.properties["adtext"],
            source.properties["sourceurl"],
            source.properties["applicationdue"],
//            source.properties["engagementtype"],
//            source.properties["extent"],
            source.properties["occupation"],
//            source.properties["positioncount"]?.toInt() ?: null,
//            source.properties["sector"],
            source.properties["industry"]
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