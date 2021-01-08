package com.imaxcorp.imaxc.providers

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.services.IGoogleApi
import com.imaxcorp.imaxc.services.RetrofitClient
import retrofit2.Call
import java.util.*

class GoogleApiProvider(context: Context) {

    private var context: Context? = null

    init {
        this.context = context
    }

    fun getDirections(originLatLng: LatLng, destineLatLng: LatLng): Call<String>? {
        val baseUrl = "https://maps.googleapis.com"
        val query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&origin=${originLatLng.latitude},${originLatLng.longitude}&" +
                "destination=${destineLatLng.latitude},${destineLatLng.longitude}&departure_time=${Date().time + 60*60*1000}&traffic_model=best_guess&key=${context?.resources?.getString(
                    R.string.google_maps_key)}"

        val retrofit = RetrofitClient().getClientRule(baseUrl)
        return retrofit?.create(IGoogleApi::class.java)?.getDirections("$baseUrl$query")
    }
}