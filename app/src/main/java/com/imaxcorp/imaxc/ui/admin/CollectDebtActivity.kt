package com.imaxcorp.imaxc.ui.admin

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.DataDebt
import com.imaxcorp.imaxc.data.ItemDebt
import com.imaxcorp.imaxc.data.ResponseDebt
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.loading
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.IMaxProvider
import com.imaxcorp.imaxc.services.OnClickListener
import com.imaxcorp.imaxc.toastShort
import com.imaxcorp.imaxc.ui.setting.adapter.MyDebtAdapter
import kotlinx.android.synthetic.main.activity_debts_service.*
import kotlinx.android.synthetic.main.item_payment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class CollectDebtActivity: AppCompatActivity() {
    private lateinit var mIMaxProvider: IMaxProvider
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mDialog: Dialog
    private var montTotal: Double = 0.0
    private lateinit var adapter: MyDebtAdapter
    private var itemList: ArrayList<ItemDebt> = ArrayList()


    private val listener = object : OnClickListener {
        override fun onClickEvent(id: String, position: Int, title: String) {

            val mDialogPayment = Dialog(this@CollectDebtActivity)
            mDialogPayment.setContentView(R.layout.item_payment)
            mDialogPayment.title.text = title
            mDialogPayment.btnCloseDialog.setOnClickListener {
                mDialogPayment.dismiss()
            }
            mDialogPayment.btnCashDialog.setOnClickListener {
                mDialogPayment.dismiss()
                mDialog.show()
                val updates = mapOf(
                    "/$id/payment" to "cash",
                    "/$id/detail/debtService" to false,
                    "/$id/indexType/${mAuthProvider.getId()}/debtService" to false
                )
                mClientBookingProvider.updateRoot(updates)
                    .addOnCompleteListener {
                        if (it.isComplete && it.isSuccessful){
                            toastShort("Operación Completada")
                            montTotal -= itemList[position].cs!!
                            itemList.removeAt(position)
                            adapter.notifyDataSetChanged()
                            textDebtTotal.text = DecimalFormat("S/ 0.00").format(montTotal)
                        }
                        mDialog.dismiss()
                    }
                    .addOnFailureListener {
                        toastShort("Error!! "+it.message)
                    }
            }

            mDialogPayment.btnTransferDialog.setOnClickListener {
                mDialogPayment.dismiss()
                mDialog.show()
                val updates = mapOf(
                    "/$id/payment" to "transfer",
                    "/$id/detail/debtService" to false,
                    "/$id/indexType/${mAuthProvider.getId()}/debtService" to false
                )
                mClientBookingProvider.updateRoot(updates)
                    .addOnCompleteListener {
                        if (it.isComplete && it.isSuccessful){
                            toastShort("Operación Completada")
                            montTotal -= itemList[position].cs!!
                            itemList.removeAt(position)
                            adapter.notifyDataSetChanged()
                            textDebtTotal.text = DecimalFormat("S/ 0.00").format(montTotal)
                        }
                        mDialog.dismiss()
                    }
                    .addOnFailureListener {
                        toastShort("Error!! "+it.message)
                    }
            }

            mDialogPayment.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debts_service)
        MyToolBar().show(this,"Cobranza de acopiadores",true)
        mIMaxProvider = IMaxProvider(this)
        mAuthProvider = AuthProvider()
        mClientBookingProvider = ClientBookingProvider()
        mDialog = loading(null)
        adapter = MyDebtAdapter(itemList,this)
        adapter.setListener(listener)

        itemDebtRV.setHasFixedSize(true)
        val ll = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        itemDebtRV.layoutManager = ll
        itemDebtRV.adapter = adapter

        getRetrofitDebt()

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun getRetrofitDebt(){
        mDialog.show()
        mIMaxProvider.getDebtPending(DataDebt(mAuthProvider.getId(),false))?.enqueue(object :
            Callback<ResponseDebt> {
            override fun onFailure(call: Call<ResponseDebt>, t: Throwable) {
                mDialog.dismiss()
                toastShort("error. no se pudo conectar con el servidor")
            }

            override fun onResponse(call: Call<ResponseDebt>, response: Response<ResponseDebt>) {
                if (response.body() != null){
                    if (response.body()?.success!!){
                        val myData = response.body()?.data
                        myData?.let {
                            itemList.clear()
                            itemList.addAll(it.filterNotNull())
                            adapter.notifyDataSetChanged()
                            montTotal = 0.0
                            for (item in it) montTotal += item.cs!!
                            textDebtTotal.text = DecimalFormat("S/ 0.00").format(montTotal)
                        }
                        mDialog.dismiss()
                    }else{
                        mDialog.dismiss()
                        toastShort("Error ${response.body()?.code}")
                    }
                }else{
                    mDialog.dismiss()
                    toastShort("Ocurrio un error. intetelo mas tarde...")
                }
            }

        })
    }
}