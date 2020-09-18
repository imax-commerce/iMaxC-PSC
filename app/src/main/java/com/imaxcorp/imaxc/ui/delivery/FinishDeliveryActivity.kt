package com.imaxcorp.imaxc.ui.delivery

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.ImagesProvider
import com.imaxcorp.imaxc.ui.start.LaunchActivity
import kotlinx.android.synthetic.main.activity_finish_delivery.*
import kotlinx.android.synthetic.main.item_payment.*
import java.text.DecimalFormat
import java.util.*

class FinishDeliveryActivity : AppCompatActivity() {

    private lateinit var bitmap: Bitmap
    private lateinit var dialog: Dialog
    private lateinit var mImageProvider: ImagesProvider
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var idDocument: String
    private lateinit var mAuthProvider: AuthProvider
    private var isPayment = false
    private var mPrice = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_delivery)

        idDocument = intent.getStringExtra("ID_DOC")!!
        isPayment = intent.getBooleanExtra("PAYMENT",false)
        mPrice = intent.getDoubleExtra("PRICE",0.0)
        mAuthProvider = AuthProvider()
        dialog = loading("loading...")
        mImageProvider = ImagesProvider("firm_client")
        mClientBookingProvider = ClientBookingProvider()
        btn_clear.setOnClickListener {
            draw_view.clearCanvas()
        }
        if (!isPayment){
            txt_detail_coin.visibility = View.VISIBLE
            text_deuda.text = "Cobrar por el servicio S/ "+DecimalFormat("0.00").format(mPrice)
            txt_detail_coin.setOnClickListener {
                payment()
            }
        }

        btn_confirm.setOnClickListener {
            if (!isPayment){
                toastLong("Recuerda cobrar por el servicio para poder continuar.")
                return@setOnClickListener
            }

            val mDialogAlert = AlertDialog.Builder(this)
            mDialogAlert.setMessage("Recuerda que debe estar la firma del cliente.")
            mDialogAlert.setPositiveButton("Continuar",DialogInterface.OnClickListener { dialogInterface, i ->
                finishOrder()
                dialogInterface.dismiss()
            })
            mDialogAlert.setNegativeButton("Cerrar",DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })
            mDialogAlert.show()
        }
    }


    private fun payment(){
        val mDialogPayment = Dialog(this)
        mDialogPayment.setContentView(R.layout.item_payment)
        mDialogPayment.title.text = getString(R.string.priceCobrar,DecimalFormat("0.00").format(mPrice))
        mDialogPayment.setCancelable(false)
        mDialogPayment.btnCloseDialog.setOnClickListener {
            mDialogPayment.dismiss()
        }
        mDialogPayment.btnCashDialog.setOnClickListener {
            mDialogPayment.dismiss()
            dialog.show()
            val map = mapOf(
                "/$idDocument/detail/paymentType" to "cash",
                "/$idDocument/detail/statusPayment" to true,
                "/$idDocument/detail/paymentDate" to Date(),
                "/$idDocument/detail/paymentOrigin" to "destine"
            )

            mClientBookingProvider.updateRoot(map)
                .addOnCompleteListener {
                    if (it.isSuccessful && it.isComplete){
                        toastLong("Completado... :)")
                        isPayment = true
                        txt_detail_coin.visibility = View.GONE
                        dialog.dismiss()

                    }else{
                        dialog.dismiss()
                    }
                }
                .addOnFailureListener {
                    toastShort("Error... "+it.message)
                }
        }
        mDialogPayment.btnTransferDialog.setOnClickListener {
            mDialogPayment.dismiss()
            dialog.show()
            val map = mapOf(
                "/$idDocument/detail/paymentType" to "transfer",
                "/$idDocument/detail/statusPayment" to true,
                "/$idDocument/detail/paymentDate" to Date(),
                "/$idDocument/detail/paymentOrigin" to "destine"
            )

            mClientBookingProvider.updateRoot(map)
                .addOnCompleteListener {
                    if (it.isSuccessful && it.isComplete){
                        toastLong("Completado... :)")
                        isPayment = true
                        txt_detail_coin.visibility = View.GONE
                        dialog.dismiss()
                    }else{
                        dialog.dismiss()
                    }
                }
                .addOnFailureListener {
                    toastShort("Error... "+it.message)
                }
        }
        mDialogPayment.show()
    }

    private fun finishOrder() {

        dialog.show()
        draw_view.buildDrawingCache()
        bitmap = draw_view.drawingCache
        mImageProvider.saveBitmap(this, bitmap, idDocument)
            ?.addOnCompleteListener{
                if (it.isSuccessful && it.isComplete){
                    Log.d("TAG-->", "Subio imahgen")
                    mImageProvider.getStorage()?.downloadUrl?.addOnCompleteListener {task ->
                        if (task.isComplete && task.isSuccessful){
                            val urlPhoto = task.result.toString()
                            val map =
                                mapOf(
                                    "/${idDocument}/detail/urlFirm" to urlPhoto,
                                    "/${idDocument}/detail/finish" to Date(),
                                    "/${idDocument}/status" to "finish",
                                    "/${idDocument}/${mAuthProvider.getId()}" to false,
                                    "/${idDocument}/indexType/Domicilio" to "finish",
                                    "/${idDocument}/indexType/${mAuthProvider.getId()}/Domicilio" to "finish",
                                    "/${idDocument}/indexType/${mAuthProvider.getId()}/status" to false
                                )

                            mClientBookingProvider.updateRoot(map).addOnFailureListener {error->
                                toastLong("Ocurrio un error. ${error.message}")
                            }.addOnCompleteListener {it2->
                                if (it2.isComplete && it2.isSuccessful){
                                    toastLong("Termino su atención. :) ")
                                    savePreferenceString("CONNECT","CONNECT","free")
                                    Intent(this,
                                        LaunchActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .setAction(Intent.ACTION_RUN).also { act->
                                            startActivity(act)
                                        }
                                    dialog.dismiss()
                                }else{
                                    dialog.dismiss()
                                }
                            }
                        }else{
                            Log.d("TAG-->", "Url: no descargo")
                        }
                    }
                }else{
                    Log.d("TAG-->", "No se Subio imagen")
                }
            }
    }
}