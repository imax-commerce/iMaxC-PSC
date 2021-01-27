package com.imaxcorp.imaxc.ui.admin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ShippingData
import com.imaxcorp.imaxc.oval
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ShippingAssignAdapter(private var list: ArrayList<ShippingData> = ArrayList()) : RecyclerView.Adapter<ShippingAssignAdapter.MyViewHolder>() {

    private lateinit var mContext: Context
    private var currentSelectedPos: Int = -1
    val selectedItems = SparseBooleanArray()
    var onItemClick: ((Int) -> Unit)? = null
    var onItemLongClick: ((Int) -> Unit)? = null

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
            if (item.selected) {
                itemView.background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    setColor(android.graphics.Color.rgb(232, 240, 253))
                }
            }else {
                itemView.background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    setColor(android.graphics.Color.WHITE)
                }
            }
            val hash = item.agency.hashCode()
            icon.text = item.agency.first().toString()
            icon.background = mContext.oval(Color.rgb(hash,hash/2,0))
            agency.text = item.agency.toUpperCase() + " ("+item.destine.toLowerCase()+")"
            destine.text = item.comers+ " - "+item.stand
            detailA.text = "${item.packages} Paquete(s) " + if (item.express == "Express") item.express else ""
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
            if (selectedItems.isNotEmpty()) onItemClick?.invoke(position)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(position)
            return@setOnLongClickListener true
        }
        if (currentSelectedPos == position)  currentSelectedPos = -1
    }

    fun toggleSelection(position: Int) {
        currentSelectedPos = position
        if (selectedItems[position, false]) {
            selectedItems.delete(position)
            list[position].selected = false
        } else {
            selectedItems.put(position, true)
            list[position].selected = true
        }
        notifyItemChanged(position)
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