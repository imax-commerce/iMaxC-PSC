package com.imaxcorp.imaxc.services

import com.imaxcorp.imaxc.data.FCMResponse
import com.imaxcorp.imaxc.data.FCMSend
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMApi {
    @Headers("Content-Type: application/json", "Authorization: key=AAAABvrXG7s:APA91bH7b6-7I9BGpw92A2FmSfLgRlLGp04ZyeUi0_y6r4JFuILOsr0VJYFp-YdhqUA1s6EZowHSgtJUQExZMP50Ne68-dT3cEHzyqX6Es3sVpTS0WR2U9urVpmcv6qq6Xm1TcRYYAsq")
    @POST("fcm/send")
    fun send(@Body data: FCMSend) : Call<FCMResponse>
}