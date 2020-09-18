package com.imaxcorp.imaxc.ui.delivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.database.*
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.data.HomeQuery
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.GeoFireProvider
import kotlinx.android.synthetic.main.notification_view.*
import java.text.DecimalFormat
import java.util.*

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
        btnCancelBooking.text = "Cerrar"
        getOrder()
        btnAcceptBooking.setOnClickListener {
            getPreference("CONNECT","CONNECT")?.let {
                if (it=="free"){
                    acceptBooking(mClientOrder.getClientBooking(idDocument))
                }else{
                    toastLong("aun tienes atenciones sin concluir. Finalizelos para continuar")
                }
            }

        }
        btnCancelBooking.setOnClickListener { cancelBooking() }
    }

    private fun acceptBooking(postRef: DatabaseReference) {
        mAuthProvider = AuthProvider()

        postRef.runTransaction(object: Transaction.Handler {
            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error!=null){
                    toastLong("error "+error.message)
                }else{
                    toastShort("Success :)")
                    mGeoFireProvider.removeBookingActive(idDocument)
                    mGeoFireProvider.removeLocation(mAuthProvider.getId())
                    mClientOrder.updateRoot(mapOf(
                        "/$idDocument/${mAuthProvider.getId()}" to true,
                        "/$idDocument/indexType/${mAuthProvider.getId()}/Domicilio" to "accept",
                        "/$idDocument/indexType/${mAuthProvider.getId()}/status" to true
                    ))
                    savePreferenceString("CONNECT","CONNECT","working")
                    Intent(this@AcceptActivity,
                        MapDriverBookingActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .setAction(Intent.ACTION_RUN).putExtra("ID_DOC",idDocument).also { act->
                            startActivity(act)
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