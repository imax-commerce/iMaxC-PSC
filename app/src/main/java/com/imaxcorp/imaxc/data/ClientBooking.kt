package com.imaxcorp.imaxc.data

import java.io.Serializable

class ClientBooking: Serializable {
    var origin: AddressPoint? = null
    var destination: AddressPoint? = null
    var detail: DetailOrder? = null
    var payment: String? = null
    var status: String? = null
    var description: String? = null

    constructor()
}