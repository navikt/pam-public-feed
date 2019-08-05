package no.nav.pam.feed.ad

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class SearchResponseRoot(val hits: Hits)

data class Hits(val total: Int, val hits: ArrayList<Hit>)

data class Hit(@JsonProperty("_source") val source: Source)

data class Source(
        val uuid: String,
        val created: ZonedDateTime,
        val updated: ZonedDateTime,
        val published: ZonedDateTime,
        val expires: ZonedDateTime,
        val title: String,
        val businessName: String?,
        val source: String,
        val employer: Employer?,
        val locationList: List<Location>,
        val occupationList: List<Occupation>,
        val properties: Map<String, String>)

data class Location(
        val country: String?,
        val address: String?,
        val city: String?,
        val postalCode: String?,
        val county: String?,
        val municipal: String?)

data class Occupation(val level1: String,
                      val level2: String)

data class Employer(
        val name: String,
        val orgnr: String?,
        val parentOrgnr: String?)
