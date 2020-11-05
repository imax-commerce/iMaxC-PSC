package com.imaxcorp.imaxc.data


data class MyPackList(
    var list: ArrayList<PackBooking>? = null
)

data class MyDestines(
    var destine: String,
    var listAgencies: ArrayList<String> = ArrayList()
)

data class DataOrder(
    var id: String? = "",
    var start: Long? = 0,
    var end: Long? = 0
)

data class ResponseOrder(
    var code: Int? = 0,
    var success: Boolean? = false,
    var data: ArrayList<ItemOrder>
)

data class ItemOrder(
    var id: String? = "",
    var cc: String? = "",
    var st: String? = "",
    var cs: Double? = 0.0
)


