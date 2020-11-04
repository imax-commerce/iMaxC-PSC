package com.imaxcorp.imaxc.ui.setting

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.include.MyToolBar
import kotlinx.android.synthetic.main.activity_attentions_history.*
import java.text.DecimalFormat
import java.util.*

class AttentionsHistoryActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var mDateSelectListener: DatePickerDialog.OnDateSetListener
    private var start = true
    private var maxDateInMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attentions_history)
        MyToolBar().show(this,"Mis Atenciones",true)
        initView()
        mDateSelectListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            val monthCore = month+1
            val date = DecimalFormat("00").format(day)+"/"+DecimalFormat("00").format(monthCore)+"/"+year.toString().substring(2)
            if (start) {
                dateStart.setText(date)
                val minCal = Calendar.getInstance()
                minCal.set(year,month,day)
                maxDateInMilliSeconds = minCal.timeInMillis
                dateEnd.isClickable = true
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
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onClick(view: View?) {
        when(view) {
            checkDate -> {
                if (checkDate.isChecked){
                    checkPeriod.isChecked = false
                    dateEnd.visibility = View.INVISIBLE
                    dateStart.hint = "Fecha"
                    dateEnd.setText("")
                    dateEnd.isClickable = false
                }

            }
            checkPeriod -> {
                if (checkPeriod.isChecked){
                    checkDate.isChecked = false
                    dateEnd.visibility = View.VISIBLE
                    dateStart.hint = "Fecha Inicio"
                }
            }
            dateStart -> {
                start = true
                selectDate()
            }
            dateEnd -> {
                start = false
                selectDate()
            }
            btnShear -> {

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

        if (start){
            dataSelect.datePicker.minDate = maxDateInMilliSeconds
        }else{
            dataSelect.datePicker.maxDate = minDateInMilliSeconds
        }
        dataSelect.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dataSelect.show()
    }

}