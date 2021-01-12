package com.imaxcorp.imaxc.data


data class MyPackList(
    var list: ArrayList<PackBooking>? = null
)

data class MyDestines(
    var destine: String,
    var listAgencies: ArrayList<String> = ArrayList()
)
// data consult My attentions Order
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
    var cs: Double? = 0.0,
    var express: Boolean = false,
    var packet: Int = 0
)

// data consult My Debt Pending
data class DataDebt(
    var id: String? = ""
)

data class ResponseDebt(
    var code: Int? = 0,
    var success: Boolean? = false,
    var data: ArrayList<ItemDebt>
)

data class ItemDebt(
    var id: String? = "",
    var cc: String? = "",
    var st: String? = "",
    var cs: Double? = 0.0,
    var ds: String? = ""
)


