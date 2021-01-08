package com.imaxcorp.imaxc.providers

import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GeoFireProvider(reference: String) {


    private var mGeoFire: GeoFire
    private lateinit var geoQuery: GeoQuery

    init {
        val dbReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(reference)
        mGeoFire = GeoFire(dbReference)
    }

    fun saveLocation(idDriver: String, latLng: LatLng) {
        mGeoFire.setLocation(idDriver, GeoLocation(latLng.latitude,latLng.longitude))
    }

    fun removeLocation(idDriver: String) {
        mGeoFire.removeLocation(idDriver)
    }

    fun getActiveOrder(latLng: LatLng) : GeoQuery {
        geoQuery = GeoFire(FirebaseDatabase.getInstance().reference.child("orders_actives")).queryAtLocation(GeoLocation(latLng.latitude,latLng.longitude),10.0)
        geoQuery.removeAllListeners()
        return geoQuery
    }

    fun removeBookingActive(idDocument: String) {
        val bookingActive = GeoFire(FirebaseDatabase.getInstance().reference.child("orders_actives"))
        bookingActive.removeLocation(idDocument)
    }

    fun removeQuery(){
        geoQuery.removeAllListeners()
    }
}