package com.imaxcorp.imaxc.ui.delivery.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ShippingData
import com.imaxcorp.imaxc.oval
import com.imaxcorp.imaxc.services.ClickListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ShippingAdapter(private var list: ArrayList<ShippingData> = ArrayList()) : RecyclerView.Adapter<ShippingAdapter.MyViewHolder>() {

    private lateinit var mContext: Context
    private var listener: ClickListener? = null

    fun setListener(onclickListener: ClickListener){
        this.listener = onclickListener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon = view.findViewById<TextView>(R.id.txt_icon)
        private val agency = view.findViewById<TextView>(R.id.txt_agencia)
        private val destine = view.findViewById<TextView>(R.id.txt_destino)
        private val detailA = view.findViewById<TextView>(R.id.txt_guia)
        private val detailB = view.findViewById<TextView>(R.id.txt_ccomer)
        private val detailC = view.findViewById<TextView>(R.id.txt_paquetes)
        private val detailD = view.findViewById<TextView>(R.id.txt_flete)

        @SuppressLint("SetTextI18n")
        fun bind(item: ShippingData){
            val hash = item.agency.hashCode()
            icon.text = item.agency.first().toString()
            icon.background = mContext.oval(Color.rgb(hash,hash/2,0))
            agency.text = item.agency
            destine.text = item.destine
            detailA.text = "${item.packages} Paquete(s)"
            detailB.text = SimpleDateFormat("dd/MM HH:mm", Locale.US).format(item.create?.time)
            detailC.text = (if(item.guia) "C/G " else "S/G ") + (if(item.cargo) "Pagar Flete" else "Contraentrega")
            detailD.text = if (item.domicile) "Entrega a Domicilio" else "Entrega en Agencia"

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mContext = parent.context
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_order,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener {
            if (listener!=null) listener!!.clickEvent(position)
        }
    }

    fun getItem(position: Int): ShippingData {
        return list[position]
    }

    fun getList() : ArrayList<ShippingData> {
        return list
    }

    fun deleteItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setList(list: ArrayList<ShippingData>) {
        this.list = list
        notifyDataSetChanged()
    }


}