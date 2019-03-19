package no.nav.pam.feed.ad

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchResponseRoot(val hits: Hits)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hits(val total: Int, val hits: ArrayList<Hit>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hit(@JsonProperty("_source") val source: Source)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Source(val uuid: String,
                  val created: String,
                  val updated: String,
                  val published: String,
                  val expires: String,
                  val title: String,
                  val source: String,
                  val medium: String,
                  val reference: String,
                  val businessName: String?,
                  @JsonProperty("locationList") val locations: List<Location>,
                  val properties: Map<String, String>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Location(val country: String,
                    val address: String?,
                    val city: String?,
                    val postalCode: String?,
                    val county: String?,
                    val municipal: String?)