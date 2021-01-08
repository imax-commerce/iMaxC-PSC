package com.imaxcorp.imaxc.ui.setting

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.DataOrder
import com.imaxcorp.imaxc.data.ItemOrder
import com.imaxcorp.imaxc.data.ResponseOrder
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.loading
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.IMaxProvider
import com.imaxcorp.imaxc.toastShort
import com.imaxcorp.imaxc.ui.setting.adapter.MyHistoryAdapter
import kotlinx.android.synthetic.main.activity_attentions_history.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class AttentionsHistoryActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var mDateSelectListener: DatePickerDialog.OnDateSetListener
    private var isForDate = true
    private var isStart = true
    private var maxDateInMilliSeconds: Long = 0
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mIMaxProvider: IMaxProvider
    private lateinit var mDialog: Dialog
    private var montTotal: Double = 0.0
    private lateinit var adapter: MyHistoryAdapter
    private var itemList: ArrayList<ItemOrder> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attentions_history)
        MyToolBar().show(this,"Mis Atenciones",true)
        mAuthProvider = AuthProvider()
        mIMaxProvider = IMaxProvider(this)
        mDialog = loading(null)
        adapter = MyHistoryAdapter(itemList,this@AttentionsHistoryActivity)
        itemOrderRV.setHasFixedSize(true)
        val ll = LinearLayoutManager(this@AttentionsHistoryActivity)
        ll.orientation = LinearLayoutManager.VERTICAL
        itemOrderRV.layoutManager = ll
        itemOrderRV.adapter = adapter

        initView()
        mDateSelectListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            val monthCore = month+1
            val date = DecimalFormat("00").format(day)+"/"+DecimalFormat("00").format(monthCore)+"/"+year.toString()
            if (isStart) {
                dateStart.setText(date)
                val minCal = Calendar.getInstance()
                minCal.set(year,month,day)
                maxDateInMilliSeconds = minCal.timeInMillis
                if (!isForDate) {
                    dateEnd.isEnabled = true
                    dateEnd.setText("")
                }
            }else
                dateEnd.setText(date)
        }

    }

    private fun initView() {
        checkDate.setOnClickListener(this)
        checkPeriod.setOnClickListener(this)
        btnShear.setOnClickListener(this)
        dateStart.setOnClickListener(this)
        dateEnd.setOnClickListener(this)
        val dateNow = Date()
        val date = Date(dateNow.year,dateNow.month,dateNow.day+1)
        val mBody = DataOrder()
        mBody.start = date.time
        //mBody.id = "OTwwSRUk3KWRc0eaNtA9OJVm0G23"
        mBody.id = mAuthProvider.getId()
        getRetrofit(mBody)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onClick(view: View?) {
        when(view) {
            checkDate -> {
                if (checkDate.isChecked){
                    dateStart.hint = "Fecha"
                    dateStart.setText("")
                    dateEnd.visibility = View.INVISIBLE
                    dateEnd.isEnabled = false
                    dateEnd.setText("")
                    isForDate = true
                }

            }
            checkPeriod -> {
                if (checkPeriod.isChecked){
                    dateStart.hint = "Fecha Inicio"
                    dateStart.setText("")
                    dateEnd.visibility = View.VISIBLE
                    dateEnd.isEnabled = false
                    isForDate = false
                }
            }
            dateStart -> {
                isStart = true
                selectDate()
            }
            dateEnd -> {
                isStart = false
                selectDate()
            }
            btnShear -> {
                if (isForDate){
                    val dateTextStart = dateStart.text.toString().trim()
                    if (dateTextStart.isEmpty()) {
                        toastShort("Ingrese la fecha requerida")
                        return
                    }
                    getOrderAttention(dateTextStart)
                }else{
                    val dateTextStart = dateStart.text.toString().trim()
                    val dateTextEnd = dateEnd.text.toString().trim()
                    if (dateTextEnd.isEmpty() || dateTextStart.isEmpty()) {
                        toastShort("Ingrese las Fechas requeridas")
                        return
                    }
                    getOrderAttention(dateTextStart,dateTextEnd)
                }
            }
        }
    }

    private fun selectDate() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        cal.set(year,month,day)

        val dataSelect = DatePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            mDateSelectListener,
            year,month,day
        )
        val minDateInMilliSeconds = cal.timeInMillis

        if (isForDate){
            dataSelect.datePicker.maxDate = minDateInMilliSeconds
        }else{
            if (isStart){
                dataSelect.datePicker.maxDate = minDateInMilliSeconds
            }else{
                dataSelect.datePicker.minDate = maxDateInMilliSeconds
            }
        }

        dataSelect.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dataSelect.show()
    }

    private fun getOrderAttention(initDate: String, endDate: String? = null) {
        val data = DataOrder()
        //data.id = "OTwwSRUk3KWRc0eaNtA9OJVm0G23" //mAuthProvider.getId()
        data.id = mAuthProvider.getId()
        val auxStart = initDate.split("/")
        val myDateStart = Date("${auxStart[2]}/${auxStart[1]}/${auxStart[0]}")
        data.start = myDateStart.time
        endDate?.let {
            val auxEnd = it.split("/")
            val myDateEnd = Date("${auxEnd[2]}/${auxEnd[1]}/${auxEnd[0]}")
            data.end = myDateEnd.time
        }
        getRetrofit(data)
    }

    private fun getRetrofit(data: DataOrder) {
        mDialog.show()
        mIMaxProvider.getHistoryOrder(data)?.enqueue(object : Callback<ResponseOrder> {
            override fun onFailure(call: Call<ResponseOrder>, t: Throwable) {
                mDialog.dismiss()
                toastShort("error. ${t.message}")
            }

            override fun onResponse(call: Call<ResponseOrder>, response: Response<ResponseOrder>) {
                if (response.body() != null) {
                    if (response.body()?.success!!){

                        val myData = response.body()?.data
                        myData?.let {
                            itemList.clear()
                            itemList.addAll(it.filterNotNull())
                            adapter.notifyDataSetChanged()
                            montTotal = 0.0
                            for (item in it){
                                montTotal += item.cs!!
                            }
                            textTotalS.text = DecimalFormat("S/ 0.00").format(montTotal)
                            textTotalG.text = DecimalFormat("S/ 0.00").format(montTotal*0.15)
                        }

                        mDialog.dismiss()
                    }else{
                        toastShort("Error ${response.body()?.code}")
                        mDialog.dismiss()
                    }
                }else{
                    toastShort("Ocurrio un error. intetelo mas tarde...")
                    mDialog.dismiss()
                }
            }

        })
    }

}