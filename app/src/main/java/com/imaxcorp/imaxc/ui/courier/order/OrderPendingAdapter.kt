package com.imaxcorp.imaxc.ui.courier.order

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.oval
import com.imaxcorp.imaxc.ui.courier.register.RegisterOrderActivity
import kotlinx.android.synthetic.main.item_order.view.*
import java.text.SimpleDateFormat
import java.util.*

class OrderPendingAdapter (private val options: FirebaseRecyclerOptions<ClientBooking>, private val mContext: Context) :
    FirebaseRecyclerAdapter<ClientBooking, OrderPendingAdapter.ViewHolder>(options){

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
        val hash =  model.status!!.hashCode()
        val time = model.detail?.create
        time?.let {
            holder.itemView.txt_ccomer.text = SimpleDateFormat("dd-MM-yy", Locale.US).format(time)
            holder.itemView.txt_paquetes.text = SimpleDateFormat("HH:mm", Locale.US).format(time)
        }
        holder.itemView.txt_agencia.text = model.description!!.replace("|"," ")
        holder.itemView.txt_icon.text = model.status?.first().toString()
        holder.itemView.txt_icon.background = mContext.oval(
            Color.rgb(
                hash,hash/2,0
            )
        )
        holder.itemView.txt_flete.text = SimpleDateFormat("HH:mm", Locale.US).format(model.detail?.accept)
        holder.itemView.txt_guia.text = model.detail?.km
        holder.itemView.txt_destino.text = model.origin?.street+" "+model.origin?.feature
        holder.itemView.setOnClickListener {
            mContext.startActivity(
                Intent(mContext,RegisterOrderActivity::class.java).putExtra("EXISTS",true)
                    .putExtra("DOC",idDoc)
                    .putExtra("STORE",model.description)
                    .putExtra("STATUS",model.status)
            )
        }
    }

    override fun getItemCount(): Int {
        (mContext as CourierActivity).updateBadge(super.getItemCount(),2)
        return super.getItemCount()
    }

}