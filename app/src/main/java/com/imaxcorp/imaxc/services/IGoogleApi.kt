package com.imaxcorp.imaxc.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface IGoogleApi {
    @GET
    fun getDirections(@Url url: String) : Call<String>
}