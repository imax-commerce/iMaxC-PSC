package com.imaxcorp.imaxc.ui.admin

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.Constant.Companion.CREDENTIAL_ATS
import com.imaxcorp.imaxc.Constant.Companion.CREDENTIAL_ATT
import com.imaxcorp.imaxc.Constant.Companion.CREDENTIAL_CA
import com.imaxcorp.imaxc.Constant.Companion.CREDENTIAL_ROOT
import com.imaxcorp.imaxc.Constant.Companion.SHIPPING_RESULT
import com.imaxcorp.imaxc.data.*
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.providers.*
import com.imaxcorp.imaxc.services.ClickListener
import com.imaxcorp.imaxc.ui.delivery.DetailExpressActivity
import com.imaxcorp.imaxc.ui.delivery.adapter.ShippingAdapter
import kotlinx.android.synthetic.main.action_bar_toolbar.*
import kotlinx.android.synthetic.main.activity_shipping.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ShippingSendActivity : AppCompatActivity() {


    private lateinit var searchView: SearchView
    private lateinit var listData: ArrayList<ShippingData>
    private lateinit var adapter: ShippingAdapter
    private lateinit var mDialog: Dialog
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mIMaxProvider: IMaxProvider
    private lateinit var mDriverProvider: DriverProvider

    private val listener = object : ClickListener {
        override fun clickEvent(position: Int) {
            val data = adapter.getItem(position)
            Intent(this@ShippingSendActivity,DetailExpressActivity::class.java)
                .putExtra("DOCUMENT",data.idOrder)
                .putExtra("INDICE",data.shippingCost).also {
                    startActivityForResult(it,SHIPPING_RESULT)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping)
        MyToolBar().show(this,"Envios",true)
        mAuthProvider = AuthProvider()
        mIMaxProvider = IMaxProvider(this)
        mDriverProvider = DriverProvider()
        mDialog = loading(null)
       // val stringData = getPreference(Constant.DATA_LOGIN,"USER")
        //userData = Gson().fromJson(stringData, Driver::class.java)

        listData = ArrayList()
        adapter = ShippingAdapter()
        adapter.setListener(listener)
        list_shipping.setHasFixedSize(true)
        list_shipping.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        list_shipping.adapter = adapter

        getRetrofit()

    }

    private fun getRetrofit() {
        val data = DataDebt()
        data.id = mAuthProvider.getId()
        if(!mDialog.isShowing) mDialog.show()
        mIMaxProvider.getShippingOrder(data)?.enqueue(object : Callback<ResponseShipping> {
            override fun onResponse(
                call: Call<ResponseShipping>,
                response: Response<ResponseShipping>
            ) {
                response.body()?.data?.let {
                    listData.clear()
                    listData.addAll(it)
                    adapter.setList(listData)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            SHIPPING_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    getRetrofit()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.ac_reload -> getRetrofit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_shearch,menu)
        val menuItem = menu?.findItem(R.id.searchItem)
        searchView = menuItem?.actionView as SearchView
        searchView.queryHint = "Buscar Envio..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {return true}

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()){
                    filter(newText)
                }else{
                    filter("")
                }
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun filter(s: String) {
        val listFilter = ArrayList<ShippingData>()
        listData.filterTo(listFilter) {
            it.agency.toLowerCase().contains(s.toLowerCase())
                    || it.destine.toLowerCase().contains(s.toLowerCase())
                    || it.stand.toLowerCase().contains(s.toLowerCase())
                    || it.comers.toLowerCase().contains(s.toLowerCase())
                    || it.express.toLowerCase().contains(s.toLowerCase())
        }
        adapter.setList(listFilter)
    }
}