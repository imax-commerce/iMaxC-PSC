package com.imaxcorp.imaxc.ui.courier.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.imaxcorp.imaxc.Constant
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.Driver
import com.imaxcorp.imaxc.getPreference
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.toastShort

class RegisterOrderActivity : AppCompatActivity() {

    private var idView: Int = 0
    private lateinit var mFormOrderFragment: FormOrderFragment
    private lateinit var mFormPacketFragment: FormPacketFragment
    var dataExists: Boolean = false
    lateinit var idDoc: String
    lateinit var store: String
    lateinit var status: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_order)
        MyToolBar().show(this,"Detalle de Orden",true)
        dataExists = intent.getBooleanExtra("EXISTS",false)
        if (dataExists) {
            idDoc = intent.getStringExtra("DOC")
            store = intent.getStringExtra("STORE")
            status = intent.getStringExtra("STATUS")
        }

        mFormOrderFragment = FormOrderFragment()
        supportFragmentManager.beginTransaction().add(R.id.fl_content,mFormOrderFragment,"FOF").addToBackStack(null).commit()

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun setIdView(i: Int){
        idView = i
    }

    fun getIdView(): Int{
        return idView
    }

    override fun onBackPressed() {
        if (idView==0){
            if (mFormOrderFragment.isVisible){
                finish()
            }
        }else if (idView==1){
            mFormPacketFragment = supportFragmentManager.findFragmentByTag("FPF") as FormPacketFragment
            if (mFormPacketFragment.isVisible){
                mFormPacketFragment.backPressed()
            }
        }
    }

    fun setTitleTool(title: String){
        this.supportActionBar?.title = title
    }

}