package com.imaxcorp.imaxc.ui.setting.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ItemDebt
import com.imaxcorp.imaxc.services.OnClickListener
import com.imaxcorp.imaxc.toastShort
import com.imaxcorp.imaxc.ui.setting.DebtsDetailActivity
import kotlinx.android.synthetic.main.item_debt.view.*
import java.text.DecimalFormat

class MyDebtAdapter(private val list: ArrayList<ItemDebt>, private val mContext: Context): RecyclerView.Adapter<MyDebtAdapter.MyViewPagerViewHolder>() {

    private var listener: OnClickListener? = null

    fun setListener(onClickListener: OnClickListener){
        this.listener = onClickListener
    }

    inner class MyViewPagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ds = view.findViewById<TextView>(R.id.textDebtDate)
        private val cc = view.findViewById<TextView>(R.id.textDebtCC)
        private val cs = view.findViewById<TextView>(R.id.textDebtMont)
        private val bc = view.findViewById<TextView>(R.id.btnDebtCharge)
        private val bd = view.findViewById<TextView>(R.id.btnDebtDetail)

        @SuppressLint("SetTextI18n")
        fun bind(item: ItemDebt) {
            ds.text = item.ds
            cc.text = item.cc+" "+item.st
            cs.text = DecimalFormat("S/ 0.00").format(item.cs)
            bd.setOnClickListener {
                mContext.startActivity(
                    Intent(mContext,DebtsDetailActivity::class.java).putExtra("DOC",item.id)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewPagerViewHolder {
        return MyViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_debt,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewPagerViewHolder, position: Int) {
        holder.bind(list[position])
        holder.itemView.btnDebtCharge.setOnClickListener(View.OnClickListener {
            if (listener != null){
                listener!!.onClickEvent(list[position].id!!,position,"${list[position].cc} ${list[position].st}")
            }
        })

    }
}