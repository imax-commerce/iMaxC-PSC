package com.imaxcorp.imaxc.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.database.*
import com.imaxcorp.imaxc.data.HomeQuery
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.DriverProvider
import com.imaxcorp.imaxc.providers.GeoFireProvider
import com.imaxcorp.imaxc.savePreferenceString
import com.imaxcorp.imaxc.toastLong
import com.imaxcorp.imaxc.toastShort
import com.imaxcorp.imaxc.ui.delivery.MapDriverBookingActivity
import java.util.*

class AcceptReceiver : BroadcastReceiver() {

    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mGeoFireProvider: GeoFireProvider
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mDriverProvider: DriverProvider
    override fun onReceive(context: Context?, intent: Intent?) {

        mDriverProvider = DriverProvider()
        mAuthProvider = AuthProvider()
        val idClient = intent?.getStringExtra("idClient")

        idClient?.let {document->
            mGeoFireProvider = GeoFireProvider("active_drivers")
            mGeoFireProvider.removeLocation(mAuthProvider.getId())
            mClientBookingProvider = ClientBookingProvider()
            val postRef = mClientBookingProvider.getClientBooking(document)

            postRef.runTransaction(object: Transaction.Handler {
                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error!=null){
                        val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.cancel(2)
                        context.toastLong("error "+error.message)
                    }else{
                        val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.cancel(2)
                        context.toastShort("Success :)")
                        mGeoFireProvider.removeLocation(mAuthProvider.getId())
                        mGeoFireProvider.removeBookingActive(document)
                        mClientBookingProvider.getRootRef(mapOf(
                            "ClientBooking/$document/${mAuthProvider.getId()}" to true,
                            "ClientBooking/$document/indexType/${mAuthProvider.getId()}/Domicilio" to "accept",
                            "ClientBooking/$document/indexType/${mAuthProvider.getId()}/status" to true,
                            "Users/Drivers/${mAuthProvider.getId()}/online" to "working",
                            "Driver_order/${mAuthProvider.getId()}/$document/active" to true

                        )).addOnCompleteListener {
                            Intent(context, MapDriverBookingActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .setAction(Intent.ACTION_RUN).putExtra("ID_DOC",document).also {
                                    context.startActivity(it)
                                }
                        }
                    }
                }

                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val p = currentData.getValue(HomeQuery::class.java)
                        ?: return Transaction.success(currentData)

                    if (p.status=="create"){
                        p.detail?.accept = Date()
                        p.detail?.idDriver = mAuthProvider.getId()
                        p.indexType = mapOf(
                            "Domicilio" to "accept",
                            mAuthProvider.getId() to mapOf(
                                "Domicilio" to true,
                                "status" to true
                            )
                        )
                        p.status = "accept"
                        p.indexType = mapOf(
                            "Domicilio" to "accept"
                        )
                    }

                    currentData.value = p
                    return Transaction.success(currentData)
                }
            })

        }
    }

}