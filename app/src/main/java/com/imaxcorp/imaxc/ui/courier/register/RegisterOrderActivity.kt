package com.imaxcorp.imaxc.ui.courier.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ListAgencies
import com.imaxcorp.imaxc.data.MyDestines
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.toastShort
import java.io.IOException

class RegisterOrderActivity : AppCompatActivity() {

    private var idView: Int = 0
    private lateinit var mFormOrderFragment: FormOrderFragment
    private lateinit var mFormPacketFragment: FormPacketFragment
    lateinit var mListAgencies: ArrayList<MyDestines>
    var dataExists: Boolean = false
    var paymentCargo: Boolean = false
    var debtDriver: Boolean = false
    var express: Boolean = false
    lateinit var idDoc: String
    lateinit var idClient: String
    lateinit var store: String
    var status: String = ""
    var phoneAttention = ""
    var nameAttention = ""
    var price: Double = 0.0
    var cargoMount = 0.0
    lateinit var mMapDestine: MutableMap<String, ArrayList<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_order)
        mListAgencies = ArrayList()
        MyToolBar().show(this,"Detalle de Orden",true)
        dataExists = intent.getBooleanExtra("EXISTS",false)
        if (dataExists) {
            idDoc = intent.getStringExtra("DOC")!!
            idClient = intent.getStringExtra("CLIENT")!!
            store = intent.getStringExtra("STORE")!!
            status = intent.getStringExtra("STATUS")!!
            price = intent.getDoubleExtra("PRICE",10.00)
            paymentCargo = intent.getBooleanExtra("CARGO",false)
            debtDriver = intent.getBooleanExtra("DEBT",false)
            cargoMount = intent.getDoubleExtra("MOUNT_CARGO",0.0)
            phoneAttention = intent.getStringExtra("PHONE")!!
            express = intent.getBooleanExtra("EXPRESS",false)
            nameAttention = intent.getStringExtra("NAME_CONTACT")!!
        }
        initDestine()
        mFormOrderFragment = FormOrderFragment()
        supportFragmentManager.beginTransaction().add(R.id.fl_content,mFormOrderFragment,"FOF").addToBackStack(null).commit()

    }

    private  fun initDestine() {
        val gson = Gson()
        val json = loadData("destinos.json")
        val agencias = gson.fromJson(json, ListAgencies::class.java)
        val listAgencies = agencias.list!!

        mMapDestine = HashMap()
        mMapDestine["mList"] = ArrayList<String>()

        for (item in listAgencies){
            if (mMapDestine.containsKey(item.distrito)){
                mMapDestine["${item.distrito}"]?.add(item.agencia!!)
            }else{
                mMapDestine["${item.distrito}"] = ArrayList()
                mMapDestine["${item.distrito}"]?.add(item.agencia!!)
                mMapDestine["mList"]?.add(item.distrito!!)

            }
        }
    }

    private fun loadData(inFile: String):String{

        var tContents = ""
        try {
            val stream = assets.open(inFile)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            tContents = String(buffer)

        } catch (e: IOException){

        }
        return tContents
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