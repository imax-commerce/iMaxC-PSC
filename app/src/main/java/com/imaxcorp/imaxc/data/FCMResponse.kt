package com.imaxcorp.imaxc.data

import com.google.gson.annotations.SerializedName

class FCMResponse {
    @SerializedName("multicast_id")
    var multicast_id: Long? = 0.toLong()
    @SerializedName("success")
    var success: Int? = 2
    @SerializedName("failure")
    var failure: Int? = 2
    @SerializedName("canonical_ids")
    var canonical_ids: Int? = 2
    @SerializedName("results")
    var results: List<Result>? = null
}

class Result {
    @SerializedName("message_id")
    var message_id: String? = null
    @SerializedName("error")
    var error: String? = null
}