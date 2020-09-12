package com.imaxcorp.imaxc.ui.courier.register

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.PackBooking
import com.imaxcorp.imaxc.oval
import kotlinx.android.synthetic.main.item_order.view.*

class OrderAdapter(var listOrder: ArrayList<PackBooking>, private val mContext: Context): RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        LayoutInflater.from(parent.context).run {
            return ViewHolder(
                inflate(R.layout.item_order, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return listOrder.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hash = listOrder[position].agency.hashCode()
        holder.itemView.txt_agencia.text = listOrder[position].agency
        holder.itemView.txt_destino.text = listOrder[position].destine
        holder.itemView.txt_icon.text = listOrder[position].agency?.first().toString()
        holder.itemView.txt_icon.background = mContext.oval(
            Color.rgb(
                hash, hash/2, 0
            )
        )

        holder.itemView.txt_ccomer.text = if(listOrder[position].domicile) "Domicilio" else "Normal"
        holder.itemView.txt_flete.text = if (listOrder[position].cargo) "Pagar Flete" else "Contraentrega"
        holder.itemView.txt_paquetes.text = if (listOrder[position].gui) "Con Guia" else "Sin Guia"
        holder.itemView.txt_guia.text = listOrder[position].packages.toString() + " Paquetes"

    }
}