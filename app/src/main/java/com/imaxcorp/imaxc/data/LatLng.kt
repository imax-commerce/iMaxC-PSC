package com.imaxcorp.imaxc.data

import java.io.Serializable

class LatLng(): Serializable {
    var latitude:Double = 0.0
    var longitude:Double  = 0.0

    constructor(latitude: Double, longitude: Double) : this() {
        this.latitude = latitude
        this.longitude = longitude
    }
}