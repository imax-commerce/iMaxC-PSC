package com.imaxcorp.imaxc.ui.courier.order

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.imaxcorp.imaxc.Constant
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.deletePreference
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.notificacion
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.TokenProvider
import com.imaxcorp.imaxc.ui.MessageActivity
import com.imaxcorp.imaxc.ui.courier.register.RegisterOrderActivity
import com.imaxcorp.imaxc.ui.setting.MainSettingsActivity
import com.imaxcorp.imaxc.ui.start.LoginActivity
import kotlinx.android.synthetic.main.activity_courier.*

class CourierActivity : AppCompatActivity() {

    private val adapter by lazy {
        ViewPagerAdapter(this)
    }
    lateinit var optionsFree: FirebaseRecyclerOptions<ClientBooking>
    lateinit var optionsPending: FirebaseRecyclerOptions<ClientBooking>
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var badgeFree: BadgeDrawable
    private lateinit var badgePending: BadgeDrawable
    private lateinit var postListener: ChildEventListener
    private lateinit var mTokenProvider: TokenProvider
    var isNotification = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier)
        MyToolBar().show(this,"Solicitudes",false)

        mClientBookingProvider = ClientBookingProvider()
        mAuthProvider = AuthProvider()
        mTokenProvider = TokenProvider()
        generateToken()
        isNotification = false
        pager.adapter = adapter
        val tabLayoutMediator = TabLayoutMediator(tab_layout,pager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                when(position + 1){
                    1 -> {
                        tab.text = "Disponibles"
                        //tab.setIcon(R.drawable.ic_beach_access_black_24dp)
                        badgeFree = tab.orCreateBadge
                        badgeFree.backgroundColor = ContextCompat.getColor(applicationContext, R.color.colorBlue)
                        badgeFree.maxCharacterCount = 2

                    }
                    2 -> {
                        tab.text = "Pendientes"
                        //tab.setIcon(R.drawable.ic_bookmark_black_24dp)
                        badgePending = tab.orCreateBadge
                        badgePending.backgroundColor = ContextCompat.getColor(applicationContext, R.color.colorBlue)
                        badgePending.maxCharacterCount = 2

                    }
                }
            })
        tabLayoutMediator.attach()

        getBooking()

        fabAdd.setOnClickListener {
            Intent(applicationContext, RegisterOrderActivity::class.java).also {
                startActivity(it)
            }
        }
    }
    private fun generateToken(){
        mTokenProvider.create(mAuthProvider.getId())

    }

    private fun getBooking() {
        val query = mClientBookingProvider.getBookingFree()
        optionsFree = FirebaseRecyclerOptions.Builder<ClientBooking>()
            .setQuery(query,ClientBooking::class.java)
            .build()
        val q = mClientBookingProvider.getBookingPending(mAuthProvider.getId())
        optionsPending = FirebaseRecyclerOptions.Builder<ClientBooking>()
            .setQuery(q,ClientBooking::class.java)
            .build()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.collector_bar_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menuProfile -> {
                val mDialogAlert = AlertDialog.Builder(this)
                mDialogAlert.setCancelable(false)
                mDialogAlert.setTitle("Cerrar sesión")
                mDialogAlert.setMessage("Confirme para continuar")
                mDialogAlert.setPositiveButton("CERRAR", DialogInterface.OnClickListener { dialog, _ ->
                    deletePreference(Constant.DATA_LOGIN)
                    mAuthProvider.logOut()
                    startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                })
                mDialogAlert.setNegativeButton("CANCELAR",DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                })
                mDialogAlert.show()
            }
            R.id.menuConfig -> {
                Intent(applicationContext, MainSettingsActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateBadge(i: Int, item: Int){
        if (i==0){
            when(item){
                1-> badgeFree.isVisible = false
                2-> badgePending.isVisible = false
            }

        }else{
            when(item){
                1-> {
                    badgeFree.number = i
                    badgeFree.isVisible = true
                }
                2-> {
                    badgePending.number = i
                    badgePending.isVisible = true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isNotification = true
    }

}