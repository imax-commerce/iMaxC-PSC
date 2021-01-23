package com.imaxcorp.imaxc.ui.admin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.AgencyData
import com.imaxcorp.imaxc.services.ClickListener
import java.util.*
import kotlin.collections.ArrayList

class AgencyAdapter(private var list: ArrayList<AgencyData> = ArrayList()) : RecyclerView.Adapter<AgencyAdapter.MyViewHolder>() {

    private lateinit var mContext: Context
    private var listener: ClickListener? = null

    fun setListener(onclickListener: ClickListener){
        this.listener = onclickListener
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val agency = view.findViewById<TextView>(R.id.text_agency)
        private val address = view.findViewById<TextView>(R.id.text_address)
        private val schedule = view.findViewById<TextView>(R.id.text_schedule)
        private val phone = view.findViewById<TextView>(R.id.text_phone)

        @SuppressLint("SetTextI18n")
        fun bind(item: AgencyData){
            agency.text = item.agencia?.toUpperCase(Locale.ROOT)
            address.text = item.direccion?.toLowerCase(Locale.ROOT)
            schedule.text = "Cierre: ${item.cierre}"
            phone.text = if (item.telefonos != null)item.telefonos.toString()else "Sin Número"
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mContext = parent.context
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_agency,parent,false)
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

    fun getItem(position: Int): AgencyData {
        return list[position]
    }

    fun getList() : ArrayList<AgencyData> {
        return list
    }

    fun deleteItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setList(list: ArrayList<AgencyData>) {
        this.list = list
        notifyDataSetChanged()
    }

}