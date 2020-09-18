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
    var paymentCargo: Boolean = false
    var debtDriver: Boolean = false
    lateinit var idDoc: String
    lateinit var idClient: String
    lateinit var store: String
    var status: String = ""
    var phoneAttention = ""
    var price: Double = 0.0
    var cargoMount = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_order)
        MyToolBar().show(this,"Detalle de Orden",true)
        dataExists = intent.getBooleanExtra("EXISTS",false)
        if (dataExists) {
            idDoc = intent.getStringExtra("DOC")
            idClient = intent.getStringExtra("CLIENT")
            store = intent.getStringExtra("STORE")
            status = intent.getStringExtra("STATUS")
            price = intent.getDoubleExtra("PRICE",10.00)
            paymentCargo = intent.getBooleanExtra("CARGO",false)
            debtDriver = intent.getBooleanExtra("DEBT",false)
            cargoMount = intent.getDoubleExtra("MOUNT_CARGO",0.0)
            phoneAttention = intent.getStringExtra("PHONE")
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