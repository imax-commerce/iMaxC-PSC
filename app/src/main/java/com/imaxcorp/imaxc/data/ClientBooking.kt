package com.imaxcorp.imaxc.data

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
@IgnoreExtraProperties
class ClientBooking: Serializable {
    var origin: AddressPoint? = null
    var destination: AddressPoint? = null
    var detail: DetailOrder? = null
    var payment: String? = null
    var status: String? = null
    var description: String? = null
    var indexType: Map<String,Any>? = null
    var typeService: String? = null
    var express: Boolean? = false

    constructor()
}