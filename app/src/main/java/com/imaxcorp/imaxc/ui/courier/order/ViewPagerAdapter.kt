package com.imaxcorp.imaxc.ui.courier.order

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.imaxcorp.imaxc.ui.courier.order.OrderFragment
import com.imaxcorp.imaxc.ui.courier.order.PendingFragment

class ViewPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                OrderFragment()
            }
            1 -> {
                PendingFragment()
            }
            else -> OrderFragment()
        }
    }


}