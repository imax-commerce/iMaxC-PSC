package com.imaxcorp.imaxc.ui.delivery

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.data.ListShipping
import com.imaxcorp.imaxc.data.MyBooking
import com.imaxcorp.imaxc.data.PackBooking
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.loading
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.ClientProvider
import com.imaxcorp.imaxc.toastShort

class DetailExpressActivity : AppCompatActivity() {
    private var docId: String? = null
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mDialog: Dialog
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_express)

        MyToolBar().show(this,"Detalles para el envio",true)
        mDialog = loading(null)
        docId = intent.getStringExtra("DOCUMENT")
        mAuthProvider = AuthProvider()
        mClientBookingProvider = ClientBookingProvider()

        initListen()

    }

    private fun initListen() {
        docId?.let {
            Log.d("DOCUMENT",it)
            mDialog.show()
            valueEventListener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (::mDialog.isInitialized && mDialog.isShowing) mDialog.dismiss()
                    var data = snapshot.getValue(MyBooking::class.java)!!

                    toastShort(data.shipping?.list?.size.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    if (::mDialog.isInitialized && mDialog.isShowing) mDialog.dismiss()
                    toastShort("Fail Cancel")
                }

            }
            mClientBookingProvider.getClientBooking(it).addValueEventListener(valueEventListener)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        docId?.let { mClientBookingProvider.getClientBooking(it).removeEventListener(valueEventListener) }
        super.onDestroy()
    }
}