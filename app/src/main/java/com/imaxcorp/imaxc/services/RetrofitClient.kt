package com.imaxcorp.imaxc.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitClient {
    private var retrofit: Retrofit? = null
    private val BASE_DCM_API = "https://fcm.googleapis.com/"

    fun getClientRule(url: String): Retrofit? {
        if (retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }
        return retrofit
    }

    fun getClientObject(url: String): Retrofit? {
        if (retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }

    fun getApiFCM() : Retrofit? {
        if (retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_DCM_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }
}