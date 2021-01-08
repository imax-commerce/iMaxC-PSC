package com.imaxcorp.imaxc.data

import java.io.Serializable
import java.util.*

class DetailQuery: Serializable {
    var km: String? = null
    var time: String? = null
    var idClient: String? = null
    var idDriver: String? = null
    var create: Date? = null
    var accept: Date? = null
    var start: Date? = null
    var finish: Date? = null
    var cancel: Date? = null
    var price: Double? = 0.0
    var kmRect: Double? = 0.0
    constructor()

}