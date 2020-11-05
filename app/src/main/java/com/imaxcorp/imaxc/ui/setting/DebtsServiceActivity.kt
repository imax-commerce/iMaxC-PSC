package com.imaxcorp.imaxc.ui.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.include.MyToolBar

class DebtsServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debts_service)
        MyToolBar().show(this,"Cobranza de Deuda",true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}