package com.imaxcorp.imaxc.ui.courier.order

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.ui.courier.register.RegisterOrderActivity
import kotlinx.android.synthetic.main.dialog_free.*
import kotlinx.android.synthetic.main.item_order.view.*
import java.text.SimpleDateFormat
import java.util.*

class OrderFreeAdapter (private val options: FirebaseRecyclerOptions<ClientBooking>, private val mContext: Context) :
    FirebaseRecyclerAdapter<ClientBooking, OrderFreeAdapter.ViewHolder>(options){

    private var mAuthProvider: AuthProvider = AuthProvider()
    private var mClientBookingProvider: ClientBookingProvider = ClientBookingProvider()
    private var loadDialog: Dialog = mContext.loading("Escribiendo...")

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        LayoutInflater.from(parent.context).run {
            return ViewHolder(
                inflate(R.layout.item_order, parent, false)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ClientBooking) {
        val idDoc = getRef(position).key
        val hash = model.description!!.hashCode()
        val time = model.detail?.create
        holder.itemView.txt_agencia.text = "Solicitud Disponible"
        time?.let {
            holder.itemView.txt_ccomer.text = SimpleDateFormat("dd-MM-yy", Locale.US).format(time)
            holder.itemView.txt_guia.text = SimpleDateFormat("HH:mm", Locale.US).format(time)
        }
        holder.itemView.txt_flete.text = model.detail?.km
        holder.itemView.txt_icon.text = model.description?.first().toString()
        holder.itemView.txt_icon.background =
            mContext.oval(
                Color.rgb(
                    hash,
                    hash / 2,
                    0
                )
            )
        holder.itemView.txt_destino.visibility = View.GONE
        holder.itemView.txt_paquetes.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val mDialog = Dialog(mContext)
            mDialog.setContentView(R.layout.dialog_free)
            mDialog.setCancelable(false)
            mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mDialog.dialog_name_id.text = model.description!!.replace("|"," ")
            mDialog.dialog_phone_id.text = SimpleDateFormat("HH:mm", Locale.US).format(model.detail?.create)
            mDialog.dialog_reference_id.text = SimpleDateFormat("dd/MM/yy", Locale.US).format(model.detail?.create)
            if (model.detail?.km=="Sin Carreta")
                mDialog.dialog_img_contact.setImageResource(R.drawable.icon_sc)
            else
                mDialog.dialog_img_contact.setImageResource(R.drawable.icon_cc)
            mDialog.dialog_btn_ok.setOnClickListener {
                loadDialog.show()
                acceptBooking(mClientBookingProvider.getClientBooking(idDoc!!))
                mDialog.dismiss()
            }

            mDialog.dialog_btn_cancel.setOnClickListener {
                mDialog.dismiss()
            }
            mDialog.show()
        }



    }

    override fun getItemCount(): Int {
        (mContext as CourierActivity).updateBadge(super.getItemCount(),1)
        return super.getItemCount()
    }

    private fun acceptBooking(postRef: DatabaseReference) {

            postRef.runTransaction(object : Transaction.Handler {
                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error!=null){
                        mContext.toastShort("Error "+error.message)
                    }else{
                        mContext.toastShort("Aceptaste una solicitud!! :)")
                    }
                    loadDialog.dismiss()
                }

                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val p = currentData.getValue(ClientBooking::class.java)
                        ?: return Transaction.success(currentData)

                    if (p.status=="create"){
                        p.detail?.accept = Date()
                        p.detail?.idDriver = mAuthProvider.getId()
                        p.indexType = mapOf(
                            "Agencia" to "accept",
                            mAuthProvider.getId() to mapOf(
                                "status" to true,
                                "Agencia" to "accept"
                            )
                        )
                        p.status = "accept"
                    }

                    currentData.value = p
                    return Transaction.success(currentData)
                }

            })
    }
}
