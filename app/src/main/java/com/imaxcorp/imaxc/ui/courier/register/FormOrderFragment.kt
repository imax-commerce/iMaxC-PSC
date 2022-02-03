package com.imaxcorp.imaxc.ui.courier.register

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.data.Driver
import com.imaxcorp.imaxc.data.MyPackList
import com.imaxcorp.imaxc.data.PackBooking
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.ClientProvider
import com.imaxcorp.imaxc.ui.courier.register.adapter.OrderAdapter
import kotlinx.android.synthetic.main.fragment_form_order.*
import kotlinx.android.synthetic.main.fragment_form_order.view.*
import kotlinx.android.synthetic.main.input_text.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FormOrderFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private var dataExists = false
    private lateinit var idDoc: String
    private lateinit var status: String
    private lateinit var userData: Driver
    private lateinit var mPackBookingList: ArrayList<PackBooking>
    private lateinit var mClientBooking: ClientBooking
    private lateinit var mPackAdapter: OrderAdapter
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mClientProvider: ClientProvider
    private var numberCargo = 0
    private var numberPacket = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_form_order, container, false)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ((context as RegisterOrderActivity).status=="start-init")
            (context as RegisterOrderActivity).setTitleTool("iMax Courier")
        mPackBookingList = ArrayList()
        mClientBookingProvider = ClientBookingProvider()
        mClientProvider = ClientProvider()
        val stringData = context!!.getPreference(Constant.DATA_LOGIN,"USER")
        userData = Gson().fromJson(stringData, Driver::class.java)

        ArrayAdapter.createFromResource(context!!,R.array.type_service,android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.sp_express.adapter = it
            view.sp_express.onItemSelectedListener = this
        }

        dataExists = (context as RegisterOrderActivity).dataExists
        if (dataExists) {
            mClientProvider.getClient((context as RegisterOrderActivity).idClient)
                ?.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val name: String = snapshot.child("name").value.toString()
                            view.et_col_name.setText(name)
                            view.et_col_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person,0,R.drawable.ic_baseline_phone,0)
                        }
                        else {
                            view.et_col_name.setText((context as RegisterOrderActivity).nameAttention)
                        }
                    }

                })
            //si no sirve arreglar aqui.
            view.et_col_name.setOnClickListener {
                (context as RegisterOrderActivity).openCall((context as RegisterOrderActivity).phoneAttention)
            }
            initView(view)
        }else {
            view.et_col_name.setText(userData.name)
            mClientBooking = ClientBooking()
            val comers = resources.getStringArray(R.array.commerces)
            val adapter = ArrayAdapter(context!!,R.layout.item_frase,comers)
            et_comers_name.threshold = 0
            et_comers_name.setAdapter(adapter)
            et_comers_name.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) et_comers_name.showDropDown()
            }
        }

        initPackAdapter(view)

        view.fabPackAdd.setOnClickListener {
            if (view.et_comers_name.text.isEmpty()){
                view.et_comers_name.error = "Complete el campo"
                view.et_comers_name.requestFocus()
                return@setOnClickListener
            }

            if (view.et_stand_name.text.isEmpty()){
                view.et_stand_name.error = "Complete el campo"
                view.et_stand_name.requestFocus()
                return@setOnClickListener
            }

            openFragmentPacket()
        }
        view.btnAction.setOnClickListener {
            writeBooking()
        }
    }
    //prepara la data par ael guardado en la db
    private fun writeBooking() {

        if (mPackBookingList.size==0){
            context!!.toastShort("Agrege envios antes de continuar")
            return
        }
        if (et_price_name.text.isEmpty() || et_price_name.text.toString().toDouble()<10.0){
            context!!.toastShort("Ingrese el monto del servicio(Monto minimo aceptado S/ 10.00)")
            et_price_name.error = "Campo obligatorio"
            et_price_name.requestFocus()
            return
        }
        if (numberCargo>0){
            val mDialog = Dialog(context!!)
            mDialog.setContentView(R.layout.input_text)
            mDialog.setCancelable(false)
            mDialog.title.text = "Monto recibido para pagar flete en agencias."
            mDialog.btnCloseDialog.setOnClickListener {
                mDialog.dismiss()
            }
            mDialog.btnRegisterDialog.setOnClickListener {
                if (mDialog.inputText.text.isNotEmpty()) {
                    saveData(mDialog.inputText.text.toString().toDouble(),true)
                    mDialog.dismiss()
                } else{
                    mDialog.inputText.error = "complete el campo"
                    mDialog.inputText.requestFocus()
                }
            }

            mDialog.show()
        }
        else {
            val mDialogAlert = AlertDialog.Builder(context!!)
            mDialogAlert.setCancelable(false)
            mDialogAlert.setMessage("Confirmar registro de paquetes")
            mDialogAlert.setPositiveButton("Confirmar",DialogInterface.OnClickListener { dialogInterface, i ->
                saveData(0.0,false)
                dialogInterface.dismiss()
            })
            mDialogAlert.setNegativeButton("Cerrar",DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })
            mDialogAlert.show()
        }
    }

    //escribe en la db
    private fun saveData(mount: Double, cargo: Boolean) {
        val mDialogLoad = context!!.loading("Registrando...")
        mDialogLoad.show()
        if (dataExists){
            val updates = mapOf(
                "/$idDoc/status" to "start-init",
                "/$idDoc/detail/price" to et_price_name.text.toString().toDouble(),
                "/$idDoc/detail/start_init" to Date(), 
                "/$idDoc/detail/numberShipping" to mPackBookingList.size,
                "/$idDoc/detail/numberPacket" to numberPacket,
                "/$idDoc/detail/montCargo" to  mount,
                "/$idDoc/detail/cargo" to cargo,
                "/$idDoc/detail/envoy" to 0,
                "/$idDoc/detail/debtDriver" to true,
                "/$idDoc/detail/debtService" to cb_debt_status.isChecked,
                "/$idDoc/indexType/${userData.id}/Agencia" to "start-init",
                "/$idDoc/indexType/Agencia" to "start_init",
                "/$idDoc/indexType/${userData.id}/debtService" to cb_debt_status.isChecked,
                "/$idDoc/indexType/shipping" to true,
                "/$idDoc/shipping/list" to mPackBookingList,
                "/$idDoc/detail/nameDriver" to userData.name
            )
            mClientBookingProvider.updateRoot(updates)
                .addOnCompleteListener {
                    if (it.isSuccessful && it.isComplete){
                        context!!.toastShort("Registrado Correctamente")
                        mDialogLoad.dismiss()
                        activity!!.onBackPressed()
                    }else{
                        mDialogLoad.dismiss()
                    }
                }
                .addOnFailureListener {
                    context!!.toastShort("Error!! "+it.message)
                }
        }else{
            val map = mapOf(
                "description" to et_comers_name.text.toString().trim()+"|"+et_stand_name.text.toString().trim(),
                "status" to "start-init",
                "payment" to "cash",
                "detail" to mapOf(
                    "accept" to Date(),
                    "create" to Date(),
                    "cargo" to cargo,
                    "debtService" to cb_debt_status.isChecked,
                    "debtDriver" to true,
                    "envoy" to 0,
                    "idClient" to "dM2iQG2sdhV90RHaZAvtkg1qrJc2",
                    "nameDriver" to userData.name,
                    "idDriver" to userData.id,
                    "km" to "En Paso",
                    "montCargo" to  mount,
                    "numberPacket" to numberPacket,
                    "numberShipping" to mPackBookingList.size,
                    "price" to et_price_name.text.toString().toDouble(),
                    "start_init" to Date()
                ),
                "origin" to mapOf(
                    "address" to et_comers_name.text.toString().trim()+" "+et_stand_name.text.toString().trim(),
                    "city" to "iMax Courier",
                    "contact" to userData.name,
                    "feature" to et_stand_name.text.toString().trim(),
                    "locality" to "iMax Courier",
                    "phone" to userData.phone,
                    "province" to "iMax Courier",
                    "street" to et_comers_name.text.toString().trim()
                ),
                "indexType" to mapOf(
                    "Agencia" to "start-init",
                    "shipping" to true,
                    userData.id to mapOf(
                        "Agencia" to "start-init",
                        "debtService" to cb_debt_status.isChecked,
                        "status" to true
                    )
                ),
                "shipping" to mapOf("list" to mPackBookingList),
                "typeService" to "Agencia",
                "express" to (context as RegisterOrderActivity).express,
                "domicile" to (context as RegisterOrderActivity).domicile
            )

            mClientBookingProvider.pushOrder(map)
                .addOnFailureListener {
                    context!!.toastShort("Error!! "+it.message  +" :(")
                }
                .addOnCompleteListener {
                    if (it.isComplete && it.isSuccessful) {
                        context!!.toastShort("Registrado Correctamente :)")
                        mDialogLoad.dismiss()
                        activity!!.onBackPressed()
                    }else{
                        mDialogLoad.dismiss()
                    }
                }
        }
    }
    //inicia el adaptador para el recicler view
    private fun initPackAdapter(view: View) {
        mPackAdapter = OrderAdapter(mPackBookingList,context as RegisterOrderActivity)
        val ll = LinearLayoutManager(activity)
        view.rv_pack.layoutManager = ll
        view.rv_pack.adapter = mPackAdapter
        if (!dataExists || status=="accept"){

            val mHelper = androidx.recyclerview.widget.ItemTouchHelper(
                ItemTouchHelper(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT)
            )
            mHelper.attachToRecyclerView(view.rv_pack)
        }

    }

    //inicia la vista de entrada para el usuarop
    private fun initView(view: View) {
        idDoc = (context as RegisterOrderActivity).idDoc
        status = (context as RegisterOrderActivity).status
        val expr = if ((context as RegisterOrderActivity).domicile) 2 else if ((context as RegisterOrderActivity).express) 1 else 0
        Log.e("valor de opcion ->", expr.toString())
        view.et_comers_name.isEnabled = false
        view.et_stand_name.isEnabled = false
        view.sp_express.setSelection(expr)
        view.sp_express.isEnabled = false
        val store = (context as RegisterOrderActivity).store.split("|")
        view.et_comers_name.setText(store[0])
        view.et_stand_name.setText(store[1])
        if (status=="accept"){
            view.btnAction.text = "Registrar envios"
        }else{
            view.et_price_name.setText(DecimalFormat("0.00").format((context as RegisterOrderActivity).price))
            view.et_price_name.isEnabled = false
            view.btnAction.visibility = View.GONE
            view.fabPackAdd.visibility = View.GONE
            if ((context as RegisterOrderActivity).debtDriver) {
                view.cb_debt_status.isClickable = false
                view.cb_debt_status.isChecked = true
            }else{
                view.cb_debt_status.visibility = View.GONE
            }
            initViewPack()
        }
    }
    //muestra los envios en caso de que ya esten creados
    private fun initViewPack() {
        mClientBookingProvider.getPacketBooking(idDoc).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val mList = snapshot.getValue(MyPackList::class.java)
                    if (mList != null) {
                        var mess = SimpleDateFormat("HH:mm dd-MM", Locale.US).format(mList.list?.get(0)?.create!!)
                        if ((context as RegisterOrderActivity).paymentCargo) {
                            mess += " (S/ ${(context as RegisterOrderActivity).cargoMount}0 flete)"
                            tv_detail.textSize = 14F
                        }
                        tv_detail.text = mess

                    }
                    mList?.list?.forEach {
                        mPackBookingList.add(it)
                        mPackAdapter.notifyDataSetChanged()
                    }

                }
            }

        })
    }

    private fun openFragmentPacket(){
        (context as RegisterOrderActivity).setIdView(1)
        val time: Long = 100
        Handler().postDelayed({
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
            transaction.add(R.id.fl_content,FormPacketFragment(),"FPF")
            transaction.addToBackStack("FPF")
            transaction.commit()

            val ft = view!!.ffoFragment
            ft.animate().translationX((-ft.width).toFloat()).setDuration(150)
                .setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) { }
                })
        },time)
    }

    fun backAnimate(){
        (context as RegisterOrderActivity).setTitleTool("Detalle de Orden")
        val ft = view!!.ffoFragment
        ft.animate().translationX(0f).setDuration(150)
            .setListener(object : AnimatorListenerAdapter() {
            }).start()
    }

    fun addPackets(pack: PackBooking){
        numberPacket += pack.packages
        if (pack.cargo) numberCargo++
        pack.create = Date()
        if (dataExists) pack.idOrder = idDoc
        pack.indexController = mutableMapOf(
            "attendName" to userData.name,
            "attendId" to userData.id
        )
        pack.comers = et_comers_name.text.toString().trim()
        pack.stand = et_stand_name.text.toString().trim()
        mPackBookingList.add(pack)
        mPackAdapter.notifyDataSetChanged()

    }

    inner class ItemTouchHelper(dragDirs: Int, swipeDirs: Int): androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
        dragDirs, swipeDirs
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            Collections.swap(mPackAdapter.listOrder,from,to)
            mPackAdapter.notifyItemMoved(from,to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (mPackAdapter.listOrder[viewHolder.adapterPosition].cargo){
                numberCargo-=1
            }
            numberPacket -= mPackAdapter.listOrder[viewHolder.adapterPosition].packages
            mPackAdapter.listOrder.removeAt(viewHolder.adapterPosition)
            mPackAdapter.notifyItemRemoved(viewHolder.adapterPosition)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //(context as RegisterOrderActivity).toastShort(sp_express.selectedItem.toString())
        when(position){
            0 -> {
                (context as RegisterOrderActivity).express = false
                (context as RegisterOrderActivity).domicile = false
            }
            1 -> {
                (context as RegisterOrderActivity).express = true
                (context as RegisterOrderActivity).domicile = false
            }
            2 -> {
                (context as RegisterOrderActivity).domicile = true
                (context as RegisterOrderActivity).express = false
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) { }
}