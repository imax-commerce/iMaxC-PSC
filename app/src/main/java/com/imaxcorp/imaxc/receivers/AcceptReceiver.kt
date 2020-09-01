package com.imaxcorp.imaxc.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.GeoFireProvider
import com.imaxcorp.imaxc.toastLong
import com.imaxcorp.imaxc.ui.MapDriverBookingActivity

class AcceptReceiver : BroadcastReceiver() {

    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mGeoFireProvider: GeoFireProvider
    private lateinit var mAuthProvider: AuthProvider
    override fun onReceive(context: Context?, intent: Intent?) {

        mAuthProvider = AuthProvider()
        val idClient = intent?.getStringExtra("idClient")

        idClient?.let {document->
            mGeoFireProvider = GeoFireProvider("active_drivers")
            mGeoFireProvider.removeLocation(mAuthProvider.getId())
            mClientBookingProvider = ClientBookingProvider()
            mClientBookingProvider.getStatus(document).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(2)
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){
                        if (snapshot.value.toString()=="create"){
                            mClientBookingProvider.updateDetail(document, mapOf("idDriver" to mAuthProvider.getId()))
                            mClientBookingProvider.updateStatus(document, mapOf(
                                "status" to "accept",
                                mAuthProvider.getId() to true
                            )
                            )
                            Intent(context, MapDriverBookingActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .setAction(Intent.ACTION_RUN).putExtra("ID_DOC",idClient).also {
                                    context?.startActivity(it)
                                }
                        }
                        else{
                            context?.toastLong("La solicitud ya no esta disponible.")
                        }
                    }

                }

            })


        }
    }

}