package no.nav.pam.feed.ad

import java.time.ZonedDateTime

data class FeedRoot(
        val content: List<FeedAd>,
        val totalElements: Int,
        val pageNumber: Int = 0,
        val pageSize: Int) {
    val totalPages: Int = totalElements / pageSize + if (totalElements % pageSize > 0) 1 else 0
    val first: Boolean = pageNumber == 0
    val last: Boolean = pageNumber >= totalPages - 1
    val sort: String = "published:desc"
}

data class FeedAd(
        val uuid: String,
        val published: ZonedDateTime,
        val expires: ZonedDateTime,
        val workLocations: List<FeedLocation>,
        val title: String,
        val description: String?,
        val source: String?,
        val applicationDue: String?,
        val occupations: List<String>,
        val link: String,
        val employer: FeedEmployer,
        val engagementtype: String?,
        val extent: String?,
        val starttime: String?,
        val positioncount: String?,
        val employerhomepage: String?,
        val sector: String?)

data class FeedLocation(
        val country: String,
        val address: String?,
        val city: String?,
        val postalCode: String?,
        val county: String?,
        val municipal: String?)

data class FeedEmployer(
        val name: String,
        val orgnr: String?,
        val description: String?)