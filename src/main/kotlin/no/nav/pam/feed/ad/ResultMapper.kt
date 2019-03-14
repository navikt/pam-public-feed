package no.nav.pam.feed.ad

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

fun mapJsonObjectToFeedPage(jsonObject: JsonObject): FeedPage {
    val hits = jsonObject.getAsJsonObject("hits")
    val total = hits.getAsJsonPrimitive("total").asInt
    val hitsArray = hits.getAsJsonArray("hits")

    var feedAdList = arrayListOf<FeedAd>()
    for (h in hitsArray) {
        feedAdList.add(mapHitToFeedAd(h.asJsonObject))
    }

    return FeedPage(total, feedAdList)
}

fun mapHitToFeedAd(jsonObject: JsonObject): FeedAd {
    val source = jsonObject.getAsJsonObject("_source")
    val locationList = source.getAsJsonArray("locationList")
    val properties = source.getAsJsonObject("properties")

    return FeedAd(
            source.get("uuid").asString,
            "/stillinger/stilling/" + source.get("uuid").asString,
            source.get("created").asString,
            source.get("updated").asString,
            source.get("published").asString,
            source.get("expires").asString,
            mapLocationList(locationList),
            source.get("title").asString,
            source.get("source").asString,
            source.get("medium").asString,
            source.get("reference").asString,
            source.get("businessName").asString,
            properties.get("adtext").asString,
            fetchNullablePrimitive(properties, "sourceurl")?.asString,
            fetchNullablePrimitive(properties, "applicationdue")?.asString,
            fetchNullablePrimitive(properties, "engagementtype")?.asString,
            fetchNullablePrimitive(properties, "extent")?.asString,
            fetchNullablePrimitive(properties, "occupation")?.asString,
            fetchNullablePrimitive(properties, "positioncount")?.asInt,
            fetchNullablePrimitive(properties, "sector")?.asString,
            fetchNullablePrimitive(properties, "industry")?.asString
    )
}

fun mapLocationList(locationListArray: JsonArray): ArrayList<FeedLocation> {
    val locationList = arrayListOf<FeedLocation>()

    for (element in locationListArray) {
        val location = element.asJsonObject

        locationList.add(FeedLocation(
                location.get("country").asString,
                fetchNullablePrimitive(location, "address")?.asString,
                fetchNullablePrimitive(location, "city")?.asString,
                fetchNullablePrimitive(location, "postalCode")?.asString,
                fetchNullablePrimitive(location, "county")?.asString,
                fetchNullablePrimitive(location, "municipal")?.asString
        ))
    }

    return locationList
}

fun fetchNullablePrimitive(jsonObject: JsonObject, name: String): JsonPrimitive? {
    return if (fetchNullableElement(jsonObject, name)?.isJsonPrimitive == true)
        fetchNullableElement(jsonObject, name)?.asJsonPrimitive else null
}

fun fetchNullableElement(jsonObject: JsonObject, name: String): JsonElement? {
    return if (jsonObject.has(name)) jsonObject.get(name) else null
}