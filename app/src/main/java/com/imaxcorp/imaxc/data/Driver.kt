package com.imaxcorp.imaxc.data

import java.io.Serializable
import java.util.*

class Driver: Serializable {
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var date: Date? = null
    var dateString: String? = null
    var typeUser: String? = null
    var vehicleBrand: String? = null
    var vehiclePlate: String? = null
    var subsidiary: String? = null
    var available: Boolean = false
    var employee: Boolean = false

    constructor()
}