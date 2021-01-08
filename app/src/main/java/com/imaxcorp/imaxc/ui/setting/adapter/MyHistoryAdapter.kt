package com.imaxcorp.imaxc.ui.setting.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ItemOrder
import java.text.DecimalFormat

class MyHistoryAdapter(private val list: ArrayList<ItemOrder>, private val mContext: Context): RecyclerView.Adapter<MyHistoryAdapter.MyViewPagerViewHolder>() {

    inner class MyViewPagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cc = view.findViewById<TextView>(R.id.textCC)
        private val cs = view.findViewById<TextView>(R.id.textCS)
        private val gs = view.findViewById<TextView>(R.id.textGS)

        @SuppressLint("SetTextI18n")
        fun bind(item: ItemOrder) {
            cc.text = item.cc+" "+item.st
            cs.text = DecimalFormat("S/ 0.00").format(item.cs)
            gs.text = DecimalFormat("S/ 0.00").format(0.15* item.cs!!)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewPagerViewHolder {
        return MyViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_attend,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewPagerViewHolder, position: Int) {
        holder.bind(list[position])
    }
}