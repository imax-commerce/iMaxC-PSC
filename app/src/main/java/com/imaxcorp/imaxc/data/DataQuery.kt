package com.imaxcorp.imaxc.data

import java.util.*
import kotlin.collections.ArrayList


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

data class ResponseShipping(
    var code: Int? = 0,
    var success: Boolean? = false,
    var data: ArrayList<ShippingData>? = null
)
data class ListShipping(
    var list: ArrayList<PackBooking>? = null
)

class MyBooking: ClientBooking() {
    var shipping: ListShipping? = null
}

data class MyDate(
    var date: Int? = 0,
    var day: Int? = 0,
    var hours: Int? = 0,
    var minutes: Int? = 0,
    var month: Int? = 0,
    var time: Long? = 0,
    var timezoneOffset: Int? = 0,
    var year: Int? = 0
)
data class ShippingData(
    var create: MyDate? = null,
    var assign: MyDate? = null,
    var packages: Int = 0,
    var status: String = "",
    var stand: String = "",
    var comers: String = "",
    var idOrder: String = "",
    var idPost: String = "",
    var agency: String = "",
    var destine: String = "",
    var domicile: Boolean = false,
    var cargo: Boolean = false,
    var guia:Boolean = false,
    var shippingCost: Int = 0
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


