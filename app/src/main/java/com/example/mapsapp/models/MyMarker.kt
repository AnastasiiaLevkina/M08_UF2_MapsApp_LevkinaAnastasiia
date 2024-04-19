package com.example.mapsapp.models

import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.LatLng

data class MyMarker(
    var userId: String?,
    var markerId: String? = null,
    var position: LatLng,
    var title: String,
    var snippet: String,
    var color: Float,
    var photo: String? = null,
) {
    constructor(userId: String?, pos: LatLng, title: String, snippet: String, color: Float): this(
        userId,null, pos, title, snippet, color, null
    )
    constructor(): this(null, null, LatLng(0.0, 0.0), "Unnamed marker",
        "No description", HUE_RED, null)
    constructor(
                userId: String?,
                lat: Double,
                long: Double,
                title: String,
                snippet: String,
                color: Float,
                photo: String?): this(userId, null, LatLng(lat, long), title, snippet, color, photo)
}