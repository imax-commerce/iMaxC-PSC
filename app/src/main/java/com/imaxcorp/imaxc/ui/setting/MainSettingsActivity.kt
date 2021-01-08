package com.imaxcorp.imaxc.ui.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.toastLong
import com.imaxcorp.imaxc.ui.courier.register.RegisterOrderActivity
import kotlinx.android.synthetic.main.activity_main_settings.*

class MainSettingsActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_settings)
        MyToolBar().show(this,"Mis Herramientas",true)
        initView()



    }

    private fun initView() {
        cv_debts.setOnClickListener(this)
        cv_history.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view){
            cv_debts -> {
                Intent(applicationContext, DebtsServiceActivity::class.java).also {
                    startActivity(it)
                }
            }

            cv_history -> {
                Intent(applicationContext, AttentionsHistoryActivity::class.java).also {
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