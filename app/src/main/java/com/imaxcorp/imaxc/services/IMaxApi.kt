package com.imaxcorp.imaxc.services

import com.imaxcorp.imaxc.data.DataDebt
import com.imaxcorp.imaxc.data.DataOrder
import com.imaxcorp.imaxc.data.ResponseDebt
import com.imaxcorp.imaxc.data.ResponseOrder
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IMaxApi {
    @Headers("Content-Type: application/json")
    @POST("api/order")
    fun getMyOrder(@Body data: DataOrder ) : Call<ResponseOrder>

    @Headers("Content-Type: application/json")
    @POST("api/debt")
    fun getMyDebt(@Body data: DataDebt) : Call<ResponseDebt>

}