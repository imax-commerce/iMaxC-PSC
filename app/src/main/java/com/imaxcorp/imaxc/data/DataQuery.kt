package com.imaxcorp.imaxc.data


data class MyPackList(
    var list: ArrayList<PackBooking>? = null
)

data class MyDestines(
    var destine: String,
    var listAgencies: ArrayList<String> = ArrayList()
)


