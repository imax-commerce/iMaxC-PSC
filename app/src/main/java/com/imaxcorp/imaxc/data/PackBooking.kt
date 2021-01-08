package com.imaxcorp.imaxc.data

import java.io.Serializable
import java.util.*

class PackBooking: Serializable {

    var create: Date? = null
    var accept: Date? = null
    var receive: Date? = null
    var assign: Date? = null
    var shipping: Date? = null
    var packages: Int = 0
    var status: String = "create"
    var stand: String = ""
    var comers: String = ""
    var urlPhoto: String = ""
    var idAccept: String = ""
    var idOrder: String = ""
    var idPost: String = ""
    var agency: String = ""
    var destine: String = ""
    var shippingCost: Double = 0.0
    var domicile: Boolean = false
    var cargo: Boolean = false
    var guia:Boolean = false
    var indexController: MutableMap<String?,Any?>? = null

    constructor()
}