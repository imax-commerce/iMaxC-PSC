package com.imaxcorp.imaxc.ui.delivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.GeoFireProvider
import com.imaxcorp.imaxc.toastLong
import kotlinx.android.synthetic.main.notification_view.*
import java.text.DecimalFormat

class AcceptActivity : AppCompatActivity() {

    private lateinit var idDocument: String
    private lateinit var mOrder: ClientBooking
    private lateinit var mClientOrder: ClientBookingProvider
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mGeoFireProvider: GeoFireProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_view)

        idDocument = intent.getStringExtra("DOC")
        mClientOrder = ClientBookingProvider()
        mGeoFireProvider = GeoFireProvider("active_drivers")
        getOrder()
        btnAcceptBooking.setOnClickListener { acceptBooking() }
        btnCancelBooking.setOnClickListener { cancelBooking() }
    }

    private fun acceptBooking() {

        mAuthProvider = AuthProvider()
        mClientOrder.updateDetail(idDocument, mapOf("idDriver" to mAuthProvider.getId()))
        mClientOrder.updateStatus(idDocument, mapOf(
            "status" to "accept",
            mAuthProvider.getId() to true
        )).addOnCompleteListener {
            if (it.isComplete && it.isSuccessful){
                mGeoFireProvider.removeBookingActive(idDocument)
                mGeoFireProvider.removeLocation(mAuthProvider.getId())
                Intent(this,
                    MapDriverBookingActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setAction(Intent.ACTION_RUN).putExtra("ID_DOC",idDocument).also { act->
                        startActivity(act)
                    }
            }else{
                toastLong("Ocurrio un error, intentelo mas tarde...")
            }
        }


    }

    private fun cancelBooking() {
        finish()
    }

    private fun getOrder() {
        mClientOrder.getClientBooking(idDocument).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    mOrder = snapshot.getValue(ClientBooking::class.java) as ClientBooking

                    textViewOrigin.text = mOrder.origin?.address
                    textViewDestination.text = mOrder.destination?.address
                    textViewMin.text = mOrder.detail?.time
                    textViewDistance.text = mOrder.detail?.km
                    textViewCounter.visibility = View.GONE
                    et_driver_game.visibility = View.VISIBLE
                    et_driver_game.text = getString(R.string.ganancia_driver,DecimalFormat("0.0").format(mOrder.detail?.price!!*0.75)+"0")
                }
            }

        })
    }
}