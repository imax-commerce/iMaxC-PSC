package com.imaxcorp.imaxc.ui.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.imaxcorp.imaxc.Constant
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.Driver
import com.imaxcorp.imaxc.getPreference
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.toastLong
import com.imaxcorp.imaxc.ui.admin.ShippingActivity
import com.imaxcorp.imaxc.ui.courier.register.RegisterOrderActivity
import kotlinx.android.synthetic.main.activity_main_settings.*

class MainSettingsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var userData: Driver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_settings)
        MyToolBar().show(this,"Mis Herramientas",true)

        val stringData = getPreference(Constant.DATA_LOGIN,"USER")
        userData = Gson().fromJson(stringData, Driver::class.java)
        initView()



    }

    private fun initView() {
        cv_debts.setOnClickListener(this)
        cv_history.setOnClickListener(this)
        cv_profit.setOnClickListener(this)
        cv_assigned.setOnClickListener(this)

        when(userData.credential) {
            Constant.CREDENTIAL_CA, Constant.CREDENTIAL_ATS -> {
                cv_profit.visibility = View.GONE
                cv_assigned.visibility = View.VISIBLE
            }
            Constant.CREDENTIAL_ATT, Constant.CREDENTIAL_ROOT -> {
                cv_profit.visibility = View.VISIBLE
                cv_assigned.visibility = View.VISIBLE
            }
            else -> {
                cv_profit.visibility = View.GONE
                cv_assigned.visibility = View.GONE
            }
        }
    }

    override fun onClick(view: View?) {
        when(view){
            cv_debts -> {
                Intent(applicationContext, DebtsServiceActivity::class.java).also {
                    startActivity(it)
                }
            }
            cv_profit -> {
                Intent(applicationContext, AttentionsHistoryActivity::class.java).also {
                    startActivity(it)
                }
            }
            cv_history -> {
                Intent(applicationContext, CareHistoryActivity::class.java).also {
                    startActivity(it)
                }
            }
            cv_assigned -> {
                Intent(applicationContext, ShippingActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}