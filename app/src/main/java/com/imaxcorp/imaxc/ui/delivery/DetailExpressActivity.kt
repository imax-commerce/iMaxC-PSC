package com.imaxcorp.imaxc.ui.delivery

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.data.MyBooking
import com.imaxcorp.imaxc.include.FileUtil
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.ImagesProvider
import kotlinx.android.synthetic.main.activity_detail_express.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class DetailExpressActivity : AppCompatActivity(),View.OnClickListener {
    private var docId: String? = null
    private var index: Int = 0
    private var paymetSend: Double = 0.0
    private var isCargo: Boolean = false
    private var mFileImage: File? = null
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mImagesProvider: ImagesProvider
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mDialog: Dialog
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var myBooking: MyBooking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_express)

        MyToolBar().show(this,"Detalles para el envio",true)
        mDialog = loading(null)
        docId = intent.getStringExtra("DOCUMENT")
        index = intent.getIntExtra("INDICE",0)
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
                    myBooking = snapshot.getValue(MyBooking::class.java)!!
                    if (!::mImagesProvider.isInitialized)
                        mImagesProvider = ImagesProvider("client_voucher/"+myBooking.detail?.idClient)

                    initView()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (::mDialog.isInitialized && mDialog.isShowing) mDialog.dismiss()
                    toastShort("Fail Cancel")
                }

            }
            mClientBookingProvider.getClientBooking(it).addValueEventListener(valueEventListener)
            btn_payment.setOnClickListener(this)
            btn_save.setOnClickListener(this)
            camera_action.setOnClickListener(this)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        if (::myBooking.isInitialized) {

            text_origin.text = myBooking.description?.replace("|","  ")
            text_packet.text = "${myBooking.shipping?.list?.get(index)?.packages} Paquete(s)"
            text_guia.text = if(myBooking.shipping?.list?.get(index)?.guia!!) "Tiene guia" else "No tiene guia"
            text_deliver.text = if(myBooking.shipping?.list?.get(index)?.domicile!!) "Envío a domicilio" else "Envío a oficina"
            text_phone_contact.text  = myBooking.shipping?.list?.get(index)?.destine
            text_payment.text = if(myBooking.shipping?.list?.get(index)?.cargo!!) {
                et_cost_payment.visibility = View.VISIBLE
                btn_payment.text = getString(R.string.btn_cargo)
                "Paga en origen"
            } else {
                et_cost_payment.visibility = View.GONE
                btn_payment.text = getString(R.string.btn_payment)
                "Paga en destino"
            }
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

    override fun onClick(v: View?) {
        when(v) {
            btn_payment->{
                when(btn_payment.text){
                    getString(R.string.btn_payment) -> {
                        et_cost_payment.visibility = View.VISIBLE
                        btn_payment.text = getString(R.string.btn_cargo)
                    }
                    getString(R.string.btn_cargo) -> {
                        et_cost_payment.visibility = View.GONE
                        et_cost_payment.setText("")
                        paymetSend = 0.0
                        btn_payment.text = getString(R.string.btn_payment)
                    }
                }
            }
            camera_action -> openGallery()
            btn_save -> uploadImage()
        }
    }

    private fun uploadImage() {

        if (mFileImage == null){
            toastShort("No se adjunto guia")
            return
        }
        if (et_cost_payment.isVisible && TextUtils.isEmpty(et_cost_payment.text.trim())){
            toastShort("Ingrese el costo de envío")
            return
        }else{
            paymetSend = if (TextUtils.isEmpty(et_cost_payment.text.trim())) 0.0 else et_cost_payment.text.toString().trim().toDouble()
        }


        if (::mImagesProvider.isInitialized){
            mDialog.show()
            mImagesProvider.saveImage(this,mFileImage!!,"${docId}-${index}")?.addOnProgressListener {

            }?.addOnCompleteListener {
                if (it.isSuccessful && it.isComplete){
                    mImagesProvider.getStorage()?.downloadUrl?.addOnCompleteListener { url ->
                        if (url.isComplete && url.isSuccessful) uploadData(url.result.toString())
                        else uploadData()
                    }
                }
            }
        }


    }

    private fun uploadData(path: String? = null){

        val data = mapOf(
            "ClientBooking/$docId/shipping/list/$index/status" to "finish",
            "ClientBooking/$docId/shipping/list/$index/shippingCost" to paymetSend,
            "ClientBooking/$docId/shipping/list/$index/shipping" to Date(),
            "ClientBooking/$docId/shipping/list/$index/idOrder" to docId,
            "ClientBooking/$docId/shipping/list/$index/idPost" to mAuthProvider.getId(),
            "ClientBooking/$docId/shipping/list/$index/urlPhoto" to path,
            "ClientBooking/$docId/status" to if(myBooking.detail?.envoy?.plus(1)!! < myBooking.detail?.numberShipping!!) myBooking.status else "finish",
            "ClientBooking/$docId/detail/envoy" to (myBooking.detail?.envoy?.plus(1)),
            "Assigned/${mAuthProvider.getId()}/$docId/$index" to null
        )

        mClientBookingProvider.getRootRef(data).addOnCompleteListener {
            if (it.isSuccessful && it.isComplete){
                toastShort("Success")
                setResult(Activity.RESULT_OK)
                finish()
            }else {
                if (mDialog.isShowing) mDialog.dismiss()
            }
        }.addOnFailureListener {
            toastShort("Error!! "+it.message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            Constant.GALLERY_RESULT -> {
                if (resultCode == Activity.RESULT_OK){
                    try {
                        mFileImage = FileUtil.from(this,data!!.data)
                        Glide.with(this).load(mFileImage).into(iv_voucher)

                    } catch (e: Exception){
                        Log.d("ERROR-->:","${e.message}")
                    }
                }
            }
        }
    }
}