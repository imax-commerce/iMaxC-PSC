package com.imaxcorp.imaxc.ui.courier.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.include.MyToolBar

class RegisterOrderActivity : AppCompatActivity() {

    private var idView: Int = 0
    private lateinit var mFormOrderFragment: FormOrderFragment
    private lateinit var mFormPacketFragment: FormPacketFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_order)
        MyToolBar().show(this,"Registro Orden",true)
        mFormOrderFragment = FormOrderFragment()
        mFormPacketFragment = FormPacketFragment()
        supportFragmentManager.beginTransaction().add(R.id.fl_content,mFormOrderFragment,"FOF").addToBackStack(null).commit()

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}