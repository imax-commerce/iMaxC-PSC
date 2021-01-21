package com.imaxcorp.imaxc.services

import com.imaxcorp.imaxc.data.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface IMaxApi {
    @Headers("Content-Type: application/json")
    @POST("api/order")
    fun getMyOrder(@Body data: DataOrder ) : Call<ResponseOrder>

    @Headers("Content-Type: application/json")
    @POST("api/debt")
    fun getMyDebt(@Body data: DataDebt) : Call<ResponseDebt>

    @Headers("Content-Type: application/json")
    @POST("api/shipping/assigned")
    fun getExpressOrder(@Body data: DataDebt) : Call<ResponseShipping>

    @Headers("Content-Type: application/json")
    @POST("api/shipping/list")
    fun getShippingOrder(@Body data: DataDebt) : Call<ResponseShipping>

}