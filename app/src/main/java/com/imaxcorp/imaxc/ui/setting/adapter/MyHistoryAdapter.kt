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

class MyHistoryAdapter(private val list: ArrayList<ItemOrder>,
                       private val mContext: Context,
                       private val isProfit: Boolean = false):
    RecyclerView.Adapter<MyHistoryAdapter.MyViewPagerViewHolder>() {

    val commers = mContext.resources.getStringArray(R.array.commerces)

    inner class MyViewPagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cc = view.findViewById<TextView>(R.id.textCC)
        private val cs = view.findViewById<TextView>(R.id.textCS)
        private val gs = view.findViewById<TextView>(R.id.textGS)

        @SuppressLint("SetTextI18n")
        fun bind(item: ItemOrder) {
            cc.text = item.cc+" "+item.st + if(item.express)" - Express" else ""
            cs.visibility = View.GONE
            gs.text = if (isProfit) DecimalFormat("S/ 0.00").format(profit(item)) else DecimalFormat("S/ 0.00").format(item.cs)
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

    fun profit(item: ItemOrder): Double {

        return if (item.cs!!>50)
            7.5
        else when(item.cc!!.trim()){
            //bellota bellota II, Udampe
            commers[0],commers[1],commers[12] -> {
                    (if (item.express) item.cs!!-item.packet*5 else item.cs!!)*0.19
            }
            //Malvinas plaza, Via mix
            commers[3],commers[15] -> {
                (if (item.express) item.cs!!-item.packet*5 else item.cs!!)*0.15
            }
            //malvitec
            commers[4] -> {
                (if (item.express) item.cs!!-item.packet*5 else item.cs!!)*0.105
            }
            //Mesa Redonda, Progreso, Progreso II
            commers[5],commers[9],commers[10] -> {
                (if (item.express) item.cs!!-item.packet*5 else item.cs!!)*0.12
            }
            //Nuevo Centro Paruro, Polvos Azules
            commers[7],commers[11] -> {
                (if (item.express) item.cs!!-item.packet*5 else item.cs!!)*0.18
            }
            //Nicolini
            commers[6] -> {
                (if (item.express) item.cs!!-item.packet*5 else item.cs!!)*0.2
            }
            //Otros
            else -> {
                (if (item.express) item.cs!!-item.packet*5 else item.cs!!)*0.17
            }
        }
    }



}
