package com.imaxcorp.imaxc.ui.delivery

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.imaxcorp.imaxc.Constant.Companion.SHIPPING_RESULT
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.*
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.loading
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.IMaxProvider
import com.imaxcorp.imaxc.services.ClickListener
import com.imaxcorp.imaxc.toastShort
import com.imaxcorp.imaxc.ui.delivery.adapter.ShippingAdapter
import kotlinx.android.synthetic.main.activity_express.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpressActivity : AppCompatActivity() {

    private lateinit var adapter:  ShippingAdapter
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mIMaxProvider: IMaxProvider
    private lateinit var mDialog: Dialog
    private var itemList: ArrayList<ShippingData> = ArrayList()
    private val listener = object : ClickListener {
        override fun clickEvent(position: Int) {
            val data = adapter.getItem(position)
            Intent(this@ExpressActivity,DetailExpressActivity::class.java).putExtra("DOCUMENT",data.idOrder).also {
                startActivityForResult(it,SHIPPING_RESULT)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_express)

        MyToolBar().show(this,"Envios asignados",true)
        mAuthProvider = AuthProvider()
        mIMaxProvider = IMaxProvider(this)
        mDialog = loading(null)

        adapter = ShippingAdapter()
        adapter.setListener(listener)
        rv_shipping.setHasFixedSize(true)
        rv_shipping.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rv_shipping.adapter = adapter

        getRetrofit()

    }


    private fun getRetrofit() {
        val data = DataDebt()
        data.id = mAuthProvider.getId()
        mDialog.show()
        mIMaxProvider.getExpressOrder(data)?.enqueue(object : Callback<ResponseShipping> {
            override fun onResponse(
                call: Call<ResponseShipping>,
                response: Response<ResponseShipping>
            ) {
                response.body()?.data?.let {
                    itemList.clear()
                    itemList.addAll(it)
                    adapter.setList(itemList)
                }
                mDialog.dismiss()
            }

            override fun onFailure(call: Call<ResponseShipping>, t: Throwable) {
                mDialog.dismiss()
                Log.d("ERROR--->","${t.message}")
                toastShort("Error. ${t.message}")
            }

        })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.update_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_reload -> getRetrofit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}