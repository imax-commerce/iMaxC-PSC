package com.imaxcorp.imaxc.ui.setting

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.data.MyPackList
import com.imaxcorp.imaxc.data.PackBooking
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.loading
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.ClientProvider
import com.imaxcorp.imaxc.providers.DriverProvider
import com.imaxcorp.imaxc.toastShort
import com.imaxcorp.imaxc.ui.courier.register.adapter.OrderAdapter
import kotlinx.android.synthetic.main.activity_debts_detail.*
import java.text.DecimalFormat

class DebtsDetailActivity : AppCompatActivity() {

    private lateinit var idDoc: String
    private lateinit var mDialog: Dialog
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mClientProvider: ClientProvider
    private lateinit var mDriverProvider: DriverProvider
    private lateinit var mAuthProvider: AuthProvider
    private var shippingList: ArrayList<PackBooking> = ArrayList()
    private var mOrder: ClientBooking? = null
    private lateinit var nameClient: String
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debts_detail)
        MyToolBar().show(this,"Detalle de Servicio",true)
        mDialog = loading(null)
        mAuthProvider = AuthProvider()
        mDriverProvider = DriverProvider()
        mClientBookingProvider = ClientBookingProvider()
        mClientProvider = ClientProvider()
        idDoc = intent.getStringExtra("DOC")!!
        adapter = OrderAdapter(shippingList,this)
        txtDebtDRV.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        txtDebtDRV.adapter = adapter
        getOrderInfo()

    }

    private fun getOrderInfo() {
        mDialog.show()
        mClientBookingProvider.getClientBooking(idDoc)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    toastShort(error.message)
                    mDialog.dismiss()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        mOrder = snapshot.getValue(ClientBooking::class.java)

                        if (mOrder!=null){
                            val mPackList = snapshot.child("shipping").getValue(MyPackList::class.java)
                            if (mPackList != null) {
                                shippingList.addAll(mPackList.list!!.filterNotNull())
                            }
                            if (mAuthProvider.getId() == mOrder?.detail!!.idClient){
                                initView(false)
                            } else {
                                getClientInfo(mOrder!!.detail!!.idClient!!)
                            }
                        }else{
                            mDialog.dismiss()
                            toastShort("Error al obtener Datos")
                            setContentView(R.layout.activity_message)
                        }

                    }else {
                        mDialog.dismiss()
                        setContentView(R.layout.activity_message)
                        toastShort("No se encontro orden")
                    }
                }

            })

    }

    @SuppressLint("SetTextI18n")
    private fun initView(createClient: Boolean) {
        txtDebtDClient.text = if (createClient) nameClient else "iMax Courier"
        val aux = mOrder?.description!!.split("|")
        txtDebtDComer.text = aux[0] +" - "+aux[1]
        txtDebtDStand.text = if (mOrder?.express!!) "Servicio Express" else "Servicio Normal"
        txtDebtDMont.text = DecimalFormat("S/ 0.00").format(mOrder?.detail!!.price)
        if (mOrder?.detail!!.cargo) txtDebtDDescription.text = "Descripción de envio (Dio Para Flete ${DecimalFormat("S/ 0.00").format(mOrder?.detail!!.montCargo)})"
        adapter.notifyDataSetChanged()
        mDialog.dismiss()
    }

    private fun getClientInfo(idClient: String) {
        mClientProvider.getClient(idClient)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    mDialog.dismiss()
                    toastShort("Error al obtener Datos")
                    setContentView(R.layout.activity_message)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        nameClient = snapshot.child("name").value.toString()
                        initView(true)
                    }else{
                        initView(false)
                    }
                }

            })
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}