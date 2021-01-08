package com.imaxcorp.imaxc.data

import java.io.Serializable

class HomeQuery: Serializable {

    var origin: AddressPoint? = null
    var destination: AddressPoint? = null
    var detail: DetailQuery? = null
    var payment: String? = null
    var status: String? = null
    var description: String? = null
    var indexType: Map<String,Any>? = null
    var typeService: String? = null

    constructor()

}