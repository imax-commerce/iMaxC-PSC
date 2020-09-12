package com.imaxcorp.imaxc.ui.courier.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imaxcorp.imaxc.R

class PendingFragment : Fragment() {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mOrderPendingAdapter: OrderPendingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById(R.id.item_pending)
        val ll = LinearLayoutManager(context)
        mRecyclerView.layoutManager = ll
        mOrderPendingAdapter = OrderPendingAdapter(
            (context as CourierActivity).optionsPending,
            this.requireActivity()
        )
        mRecyclerView.adapter = mOrderPendingAdapter
        mOrderPendingAdapter.startListening()
    }
}