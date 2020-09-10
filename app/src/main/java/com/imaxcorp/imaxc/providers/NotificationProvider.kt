package com.imaxcorp.imaxc.providers

import com.imaxcorp.imaxc.data.FCMResponse
import com.imaxcorp.imaxc.data.FCMSend
import com.imaxcorp.imaxc.services.IFCMApi
import com.imaxcorp.imaxc.services.RetrofitClient
import retrofit2.Call

class NotificationProvider {

    private val url: String = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMSend) : Call<FCMResponse>?{
        val retrofit = RetrofitClient().getClientObject(url)
        return retrofit?.create(IFCMApi::class.java)?.send(body)
    }
}