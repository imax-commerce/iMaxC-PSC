package com.imaxcorp.imaxc.ui

import android.app.Dialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.loading
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.ImagesProvider
import kotlinx.android.synthetic.main.activity_finish_delivery.*
import java.util.*

class FinishDeliveryActivity : AppCompatActivity() {

    private lateinit var bitmap: Bitmap
    private lateinit var dialog: Dialog
    private lateinit var mImageProvider: ImagesProvider
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var idDocument: String
    private lateinit var mAuthProvider: AuthProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_delivery)

        idDocument = intent.getStringExtra("ID_DOC")!!
        mAuthProvider = AuthProvider()
        dialog = loading("cargando...")
        mImageProvider = ImagesProvider("firm_client")
        mClientBookingProvider = ClientBookingProvider()
        btn_clear.setOnClickListener {
            draw_view.clearCanvas()
        }

        btn_confirm.setOnClickListener {
            dialog.show()
            draw_view.buildDrawingCache()
            bitmap = draw_view.drawingCache
            mImageProvider.saveBitmap(this, bitmap, idDocument)
                ?.addOnCompleteListener{
                    if (it.isSuccessful && it.isComplete){
                        Log.d("TAG-->", "Subio imahgen")
                        mImageProvider.getStorage()?.downloadUrl?.addOnCompleteListener {task ->
                            if (task.isComplete && task.isSuccessful){
                                Log.d("TAG-->", "Url: ${task.result}")
                                val urlPhoto = task.result.toString()
                                val map =
                                    mapOf(
                                        "urlFirm" to urlPhoto,
                                        "finish" to Date()
                                    )
                                mClientBookingProvider.updateStatus(idDocument,mapOf("status" to "finish", mAuthProvider.getId() to false))
                                mClientBookingProvider.updateDetail(idDocument,map).addOnSuccessListener {
                                    finish()
                                }
                            }else{
                                Log.d("TAG-->", "Url: no descargo")
                            }
                        }
                    }else{
                        Log.d("TAG-->", "No Subio imahgen")
                    }
                }

        }
    }
}