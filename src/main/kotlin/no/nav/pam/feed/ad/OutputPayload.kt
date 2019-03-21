package no.nav.pam.feed.ad

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedRoot(val content: List<FeedAd>,
                    val totalElements: Int,
                    val number: Int,
                    val size: Int) {
    val totalPages: Int = totalElements / size
    val first: Boolean = number == 0
    val last: Boolean = number == totalPages
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedAd(val uuid: String,
                  val created: ZonedDateTime,
                  val updated: ZonedDateTime,
                  val published: ZonedDateTime,
                  val expires: ZonedDateTime,
                  val locations: List<FeedLocation>,
                  val title: String,
                  val reference: String,
                  val employer: String,
                  val adtext: String?,
                  val sourceurl: String?,
                  val applicationdue: String?,
                  val engagementtype: String?,
                  val extent: String?,
                  val occupation: String?,
                  val positioncount: Int?,
                  val sector: String?,
                  val industry: String?) {

    var ur: String = System.getenv("arbeidsplassen_url") ?: "default_value"
    val url: String = "/stillinger/stilling/$uuid"
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedLocation(val country: String,
                        val address: String?,
                        val city: String?,
                        val postalCode: String?,
                        val county: String?,
                        val municipal: String?)