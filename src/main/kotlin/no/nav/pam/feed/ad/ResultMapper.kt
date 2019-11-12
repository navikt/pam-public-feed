package no.nav.pam.feed.ad


fun mapResult(root: SearchResponseRoot, page: Int, size: Int, host: String?): FeedRoot {
    val adList = root.hits.hits.map { element -> mapAd(element.source, host) }
    val total = root.hits.total.value.takeIf { x -> x in 0..MAX_TOTAL_HITS } ?: MAX_TOTAL_HITS

    return FeedRoot(adList, total, page, size)
}

fun mapAd(source: Source, host: String?): FeedAd {
    val link = "https://$host/stillinger/stilling/${source.uuid}"

    return FeedAd(
            uuid = source.uuid,
            published = source.published,
            expires = source.expires,
            updated = source.updated,
            workLocations = source.locationList.map { l -> mapLocation(l) },
            title = source.title,
            description = source.properties["adtext"],
            sourceurl = source.properties["sourceurl"],
            source = source.source,
            applicationDue = source.properties["applicationdue"],
            occupationCategories = source.occupationList.map { FeedOccupation(it.level1, it.level2) },
            jobtitle = source.properties["jobtitle"],
            link = link,
            employer = mapEmployer(source),
            engagementtype = source.properties["engagementtype"],
            extent = source.properties["extent"],
            starttime = source.properties["starttime"],
            positioncount = source.properties["positioncount"],
            sector = source.properties["sector"]
    )
}

fun mapEmployer(source: Source): FeedEmployer {
    return FeedEmployer(
            source.businessName ?: source.employer.let { e -> e?.name ?: "" },
            source.employer.let { e -> e?.orgnr },
            source.properties["employerdescription"],
            source.properties["employerhomepage"]
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
