package no.nav.pam.feed.ad

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.ZonedDateTime

data class FeedRoot(val content: List<FeedAd>,
                    val totalElements: Int,
                    val pageNumber: Int,
                    val pageSize: Int) {
    val totalPages: Int = totalElements / pageSize
    val first: Boolean = pageNumber == 0
    val last: Boolean = pageNumber == totalPages
    val sort: String = "published:desc"
}

//@JsonInclude(JsonInclude.Include.NON_NULL)
data class FeedAd(val uuid: String,
                  val published: ZonedDateTime,
                  val expires: ZonedDateTime,
                  val workLocations: List<FeedLocation>,
                  val title: String,
                  val employer: String,
                  val employerDescription: String?,
                  val description: String?,
                  val source: String?,
                  val applicationDue: String?,
                  val occupations: List<String>,
                  val link: String)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FeedLocation(val country: String,
                        val address: String?,
                        val city: String?,
                        val postalCode: String?,
                        val county: String?,
                        val municipal: String?)