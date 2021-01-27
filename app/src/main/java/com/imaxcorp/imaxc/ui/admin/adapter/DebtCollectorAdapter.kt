package com.imaxcorp.imaxc.ui.admin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.AgencyData
import com.imaxcorp.imaxc.data.ItemDebt
import com.imaxcorp.imaxc.services.OnClickListener
import com.imaxcorp.imaxc.toastShort
import com.imaxcorp.imaxc.ui.setting.DebtsDetailActivity
import kotlinx.android.synthetic.main.item_debt.view.*
import java.text.DecimalFormat

class DebtCollectorAdapter(private var list: ArrayList<ItemDebt> = ArrayList()): RecyclerView.Adapter<DebtCollectorAdapter.MyViewPagerViewHolder>() {

    private var listener: OnClickListener? = null
    private lateinit var mContext: Context

    private var currentSelectedPos: Int = -1
    val selectedItems = SparseBooleanArray()
    var onItemClick: ((Int) -> Unit)? = null
    var onItemLongClick: ((Int) -> Unit)? = null

    fun setListener(onClickListener: OnClickListener){
        this.listener = onClickListener
    }

    inner class MyViewPagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<TextView>(R.id.textDebtDate)
        private val cc = view.findViewById<TextView>(R.id.textDebtCC)
        private val cs = view.findViewById<TextView>(R.id.textDebtMont)
        private val cargo = view.findViewById<TextView>(R.id.textDebtService)

        @SuppressLint("SetTextI18n")
        fun bind(item: ItemDebt) {
            if (item.selected){
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

            name.text = item.driver + " "+item.ds
            cc.text = item.cc+" "+item.st
            cs.text = DecimalFormat("S/ 0.00").format(item.cs)
            cargo.text = if (item.cargo) "Flete "+ DecimalFormat("S/ 0.00").format(item.cost)
            else "Contraentrega"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewPagerViewHolder {
        mContext = parent.context
        return MyViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_debt,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewPagerViewHolder, position: Int) {
        holder.bind(list[position])

        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(position)
            return@setOnLongClickListener  true
        }

        holder.itemView.setOnClickListener {
            if (selectedItems.isNotEmpty()) onItemClick?.invoke(position)
            else listener!!.onClickEvent(list[position].id,position,"${list[position].cc} ${list[position].st} ${DecimalFormat("S/ 0.00").format(list[position].cs)}")
        }

        if (currentSelectedPos == position) currentSelectedPos = -1

        holder.itemView.btnDebtDetail.setOnClickListener {
            mContext.startActivity(
                Intent(mContext,DebtsDetailActivity::class.java).putExtra("DOC",list[position].id)
            )
        }
    }

    fun toggleSelection(position: Int){
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

    fun getItem(position: Int): ItemDebt {
        return list[position]
    }

    fun getList() : ArrayList<ItemDebt> {
        return list
    }

    fun deleteItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setList(list: ArrayList<ItemDebt>) {
        this.list = list
        notifyDataSetChanged()
    }
}