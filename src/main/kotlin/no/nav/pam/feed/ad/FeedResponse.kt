package no.nav.pam.feed.ad

data class FeedPage(val total: Int, val ads: ArrayList<FeedAd>)

data class FeedAd(val uuid: String,
                  val url: String,
                  val created: String,
                  val updated: String,
                  val published: String,
                  val expires: String,
                  val locations: ArrayList<FeedLocation>,
                  val title: String,
                  val source: String,
                  val medium: String,
                  val source_id: String,
                  val businessName: String,
                  val adtext: String,
                  val sourceurl: String?,
                  val applicationdue: String?,
                  val engagementtype: String?,
                  val extent: String?,
                  val occupation: String?,
                  val positioncount: Int?,
                  val sector: String?,
                  val industry: String?)

data class FeedLocation(val country: String,
                        val address: String?,
                        val city: String?,
                        val postalCode: String?,
                        val county: String?,
                        val municipal: String?)