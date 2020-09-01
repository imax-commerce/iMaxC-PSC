package com.imaxcorp.imaxc.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking

class ItemAdapter(private val options: FirebaseRecyclerOptions<ClientBooking>, private val mContext: Context) : FirebaseRecyclerAdapter<ClientBooking, ItemAdapter.ViewHolder>(options) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        LayoutInflater.from(parent.context).run {
            return ViewHolder(inflate(R.layout.item_pending, parent,false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ClientBooking) {
        val idDoc = getRef(position).key
        holder.itemView.setOnClickListener {
            Intent(mContext, MapDriverBookingActivity::class.java).putExtra("ID_DOC", idDoc).also{
                mContext.startActivity(it)
            }
        }
    }
}