package com.imaxcorp.imaxc.ui.courier

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import kotlinx.android.synthetic.main.activity_courier.*

class CourierActivity : AppCompatActivity() {

    private val adapter by lazy {
        ViewPagerAdapter(this)
    }

    private var mPendingFragment: PendingFragment = PendingFragment()
    private var mOrderFragment: OrderFragment = OrderFragment()
    lateinit var mFreeList: ArrayList<ClientBooking>
    lateinit var mPendingList: ArrayList<ClientBooking>

    private lateinit var mClientBookingProvider: ClientBookingProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier)
        MyToolBar().show(this,"Solicitudes",false)
        mClientBookingProvider = ClientBookingProvider()
        mFreeList = ArrayList()
        mPendingList = ArrayList()
        pager.adapter = adapter
        val tabLayoutMediator = TabLayoutMediator(tab_layout,pager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                when(position + 1){
                    1 -> {
                        tab.text = "Disponibles"
                        //tab.setIcon(R.drawable.ic_beach_access_black_24dp)
                        val badge: BadgeDrawable = tab.orCreateBadge
                        badge.backgroundColor = ContextCompat.getColor(applicationContext, R.color.colorBlue)
                        badge.number = 1
                        badge.maxCharacterCount = 2
                        badge.isVisible = true
                    }
                    2 -> {
                        tab.text = "Pendientes"
                        //tab.setIcon(R.drawable.ic_bookmark_black_24dp)
                        val badge:BadgeDrawable = tab.orCreateBadge
                        badge.backgroundColor = ContextCompat.getColor(applicationContext, R.color.colorBlue)
                        badge.number = 10
                        badge.maxCharacterCount = 2
                        badge.isVisible = true
                    }
                }
            })
        tabLayoutMediator.attach()
        getBookingFree()
    }

    private fun getBookingFree() {
        val query = mClientBookingProvider.getBookingFree()
        val options = FirebaseRecyclerOptions.Builder<ClientBooking>()
            .setQuery(query,ClientBooking::class.java)
            .build()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.collector_bar_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menuProfile -> {
            }
            R.id.menuConfig -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}