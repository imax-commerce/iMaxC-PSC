package com.imaxcorp.imaxc.ui.admin

import android.app.Dialog
import android.content.DialogInterface
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.data.*
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.providers.*
import kotlinx.android.synthetic.main.action_bar_toolbar.*
import kotlinx.android.synthetic.main.activity_shipping.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ShippingActivity : AppCompatActivity() {

    companion object {
        data class itemInfo(
            var id: String? = null,
            var token: String? = null
        )
    }

    private lateinit var searchView: SearchView
    private lateinit var listData: ArrayList<ShippingData>
    private lateinit var adapter: ShippingAssignAdapter
    private lateinit var mDialog: Dialog
    private lateinit var mNotificationProvider: NotificationProvider
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mIMaxProvider: IMaxProvider
    private lateinit var userData: Driver
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mDriverProvider: DriverProvider
    private var names: ArrayList<String> = ArrayList()
    private var info: ArrayList<itemInfo> = ArrayList()
    private lateinit var valueEventListener: ValueEventListener

    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping)
        MyToolBar().show(this,"Envios",true)
        mAuthProvider = AuthProvider()
        mIMaxProvider = IMaxProvider(this)
        mDriverProvider = DriverProvider()
        mNotificationProvider = NotificationProvider()
        mClientBookingProvider = ClientBookingProvider()
        mDialog = loading(null)
        val stringData = getPreference(Constant.DATA_LOGIN,"USER")
        userData = Gson().fromJson(stringData, Driver::class.java)

        listData = ArrayList()
        adapter = ShippingAssignAdapter()
        list_shipping.setHasFixedSize(true)
        list_shipping.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        list_shipping.adapter = adapter

        getRetrofit()
        getDriverList()

        adapter.onItemClick = {
            enableActionMode(it)
        }
        adapter.onItemLongClick = {
            enableActionMode(it)
        }

    }

    private fun getDriverList() {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                names.clear()
                info.clear()
                for (item in snapshot.children){
                    if (item.child("available").value as Boolean && item.child("employee").value as Boolean){
                        names.add(item.child("name").value.toString())
                        info.add(itemInfo(item.key,item.child("token").value.toString()))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Error --> ",error.message)
            }

        }
        mDriverProvider.getReference()?.orderByChild("typeUser")?.equalTo("DRIVER")?.addValueEventListener(valueEventListener)
    }

    override fun onDestroy() {
        mDriverProvider.getReference()?.orderByChild("typeUser")?.equalTo("DRIVER")?.removeEventListener(valueEventListener)
        super.onDestroy()
    }
    private fun showDriver(){
        val selected = listData.filter { it.selected }
        var index = -1
        if (selected.isNotEmpty()){
            val mAlertOpt = AlertDialog.Builder(this)
            mAlertOpt.setTitle("Motorizados")

            mAlertOpt.setSingleChoiceItems(names.toTypedArray(),-1,DialogInterface.OnClickListener { _, which ->
                index = which
            }).setPositiveButton("Asignar",DialogInterface.OnClickListener { dialog, _ ->
                if (index >= 0){
                    saveAssigned(index,selected)
                    dialog.dismiss()
                }else{
                    toastShort("Seleccione un Motorizado...")
                }
            }).setNegativeButton("Cancelar",DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })
            mAlertOpt.show()
        }

    }

    private fun enableActionMode(i: Int) {

        if (actionMode == null)
            actionMode  = startSupportActionMode(object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    toolbar.visibility = View.GONE
                    mode?.menuInflater?.inflate(R.menu.assigned_menu,menu)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return false
                }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    if (item?.itemId == R.id.selectDriver){
                        showDriver()
                        //mode?.finish()
                        return true
                    }

                    return false
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    adapter.selectedItems.clear()
                    adapter.getList().filter { it.selected }.forEach { it.selected = false }
                    adapter.notifyDataSetChanged()
                    actionMode = null
                    Handler().postDelayed({
                        toolbar.visibility = View.VISIBLE
                    },300)
                }

            })
        adapter.toggleSelection(i)
        val size = adapter.selectedItems.size()
        if (size == 0) {
            actionMode?.finish()
        }
        else {
            actionMode?.title = "$size"
            actionMode?.invalidate()
        }
    }

    private fun saveAssigned(index: Int,selected: List<ShippingData>) {
        mDialog.show()
        val time = Date()
        val map = mutableMapOf<String, Any?>()
        selected.forEach {
            map["ClientBooking/${it.idOrder}/shipping/list/${it.shippingCost}/idPost"] = info[index].id
            map["ClientBooking/${it.idOrder}/shipping/list/${it.shippingCost}/idOrder"] = it.idOrder
            map["ClientBooking/${it.idOrder}/shipping/list/${it.shippingCost}/assign"] = time
            map["Assigned/${info[index].id}/${it.idOrder}/${it.shippingCost}"] = it
        }

        mClientBookingProvider.getRootRef(map).addOnCompleteListener {
            if (it.isComplete && it.isSuccessful){
                sendNotify(info[index].token)
                actionMode?.finish()
                getRetrofit()
            }else{
                toastShort("Error al actualizar datos...")
                mDialog.dismiss()
            }
        }


    }

    private fun sendNotify(token: String?) {
        token?.let {
            val data = Data(
                "NUEVA ASIGNACION DE ENVIOS",
                "Comunicate con iMax para coordinar el recojo del envío"
            )
            val send = FCMSend(it,data)
            mNotificationProvider.sendNotification(send)?.enqueue(object : Callback<FCMResponse> {
                override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {
                    if (response.body() != null) {
                        if (response.body()?.success == 1) {
                            toastShort("Sen notifico al motorizado asignado")
                        }else toastShort("Error al enviar notificaciòn.")
                    }else toastShort("Error al enviar notificaciòn.")
                }

                override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                    toastShort("Error al enviar notificaciòn. ${t.message}")
                }

            })
        }
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