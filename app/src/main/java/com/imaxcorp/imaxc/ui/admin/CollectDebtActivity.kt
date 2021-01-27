package com.imaxcorp.imaxc.ui.admin

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
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
import com.imaxcorp.imaxc.ui.admin.adapter.DebtCollectorAdapter
import com.imaxcorp.imaxc.ui.setting.adapter.MyDebtAdapter
import kotlinx.android.synthetic.main.action_bar_toolbar.*
import kotlinx.android.synthetic.main.activity_debts_service.*
import kotlinx.android.synthetic.main.item_payment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class CollectDebtActivity: AppCompatActivity() {
    private lateinit var mIMaxProvider: IMaxProvider
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mDialog: Dialog
    private var montTotal: Double = 0.0
    private lateinit var adapter: DebtCollectorAdapter
    private var itemList: ArrayList<ItemDebt> = ArrayList()
    private lateinit var searchView: SearchView
    private var actionMode: ActionMode? = null


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
        adapter = DebtCollectorAdapter()
        adapter.setListener(listener)

        itemDebtRV.setHasFixedSize(true)
        val ll = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        itemDebtRV.layoutManager = ll
        itemDebtRV.adapter = adapter

        getRetrofitDebt()

        adapter.onItemLongClick = {
            enableActionMode(it)
        }
        adapter.onItemClick = {
            enableActionMode(it)
        }

    }

    private fun enableActionMode(i:Int){
        if (actionMode == null){
            actionMode = startSupportActionMode(object : ActionMode.Callback{
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    toolbar.visibility = View.GONE
                    mode?.menuInflater?.inflate(R.menu.assigned_menu,menu)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean { return false }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    if (item?.itemId == R.id.selectDriver){
                        showDialog()
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
        }

        adapter.toggleSelection(i)
        val size = adapter.selectedItems.size()
        if (size == 0)
            actionMode?.finish()
        else{
            actionMode?.title = "$size"
            actionMode?.invalidate()
        }
    }

    private fun showDialog() {
        val mAlertDialog = AlertDialog.Builder(this,R.style.AlertDialogCustom)
        mAlertDialog.setTitle("Cobrando S/ 15.00 de un grupo de solicitudes")
        mAlertDialog.setMessage("Verifique y confirme para proceder ...")
        mAlertDialog.setPositiveButton("Confirmar",DialogInterface.OnClickListener { dialog, _ ->
            charge()
            dialog.dismiss()
        })
        mAlertDialog.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        })
        mAlertDialog.show()

    }

    private fun charge() {
        mDialog.show()
        val orders = itemList.filter { it.selected }
        val map = mutableMapOf<String, Any?>()
        orders.forEach {
            map["ClientBooking/${it.id}/detail/debtDriver"] = false
            map["ClientBooking/${it.id}/detail/paymentType"] = "cash"
            map["ClientBooking/${it.id}/detail/statusPayment"] = true
            map["ClientBooking/${it.id}/detail/idCharge"] = mAuthProvider.getId()
            map["ClientBooking/${it.id}/detail/charge"] = Date()
            map["ClientBooking/${it.id}/indexType/${it.idDriver}/status"] = false
        }

        mClientBookingProvider.getRootRef(map).addOnCompleteListener {
            if (it.isSuccessful && it.isComplete){
                actionMode?.finish()
                getRetrofitDebt()
            }else{
                toastShort("Error!! intentelo mas tarde")
                mDialog.dismiss()
            }
        }
    }

    private fun getRetrofitDebt(){
        if(!mDialog.isShowing) mDialog.show()
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
                            itemList.addAll(it)
                            adapter.setList(itemList)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_shearch,menu)
        val menuItem = menu?.findItem(R.id.searchItem)
        searchView = menuItem?.actionView as SearchView
        searchView.queryHint = "Buscar..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {return true}

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty())
                    filter(newText)
                else
                    filter("")
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.ac_reload -> getRetrofitDebt()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun filter(s: String){
        val listFilter = ArrayList<ItemDebt>()
        itemList.filterTo(listFilter){
            it.cc.toLowerCase().contains(s.toLowerCase())
                    || it.st.toLowerCase().contains(s.toLowerCase())
                    || it.driver.toLowerCase().contains(s.toLowerCase())
        }
        adapter.setList(listFilter)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}