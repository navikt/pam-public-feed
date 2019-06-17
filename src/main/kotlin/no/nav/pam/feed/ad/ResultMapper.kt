package no.nav.pam.feed.ad


fun mapResult(root: SearchResponseRoot, page: Int, size: Int, host: String?): FeedRoot {
    val adList = root.hits.hits.map { element -> mapAd(element.source, host) }
    val total = root.hits.total.takeIf { x -> x in 0..MAX_TOTAL_HITS } ?: MAX_TOTAL_HITS

    return FeedRoot(adList, total, page, size)
}

fun mapAd(source: Source, host: String?): FeedAd {
    val locations = source.locations.map { l -> mapLocation(l) }
    val link = "https://$host/stillinger/stilling/${source.uuid}"

    return FeedAd(
            uuid = source.uuid,
            published = source.published,
            expires = source.expires,
            updated = source.updated,
            workLocations = locations,
            title = source.title,
            description = source.properties["adtext"],
            source = source.properties["sourceurl"],
            origin = source.source,
            applicationDue = source.properties["applicationdue"],
            occupations = populateOccupations(source),
            link = link,
            employer = mapEmployer(source),
            engagementtype = source.properties["engagementtype"],
            extent = source.properties["extent"],
            starttime = source.properties["starttime"],
            positioncount = source.properties["positioncount"],
            employerhomepage = source.properties["employerhomepage"],
            sector = source.properties["sector"]
    )
}

fun mapEmployer(source: Source): FeedEmployer {
    return FeedEmployer(
            source.businessName ?: source.employer.let { e -> e?.name ?: "" },
            source.employer.let { e -> e?.orgnr },
            source.properties["employerdescription"]
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