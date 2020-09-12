package com.imaxcorp.imaxc.ui.courier.register

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.imaxcorp.imaxc.Constant
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.Driver
import com.imaxcorp.imaxc.data.PackBooking
import com.imaxcorp.imaxc.getPreference
import com.imaxcorp.imaxc.providers.ClientPackProvider
import kotlinx.android.synthetic.main.fragment_form_order.*
import kotlinx.android.synthetic.main.fragment_form_order.view.*
import kotlinx.android.synthetic.main.input_text.*
import java.util.*
import kotlin.collections.ArrayList

class FormOrderFragment : Fragment() {
    private var dataExists = false
    private lateinit var idDoc: String
    private lateinit var status: String
    private lateinit var userData: Driver
    private lateinit var mPackBookingList: ArrayList<PackBooking>
    private lateinit var mPackAdapter: OrderAdapter

    private var numberCargo = 0

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
        mPackBookingList = ArrayList()
        val stringData = context!!.getPreference(Constant.DATA_LOGIN,"USER")
        userData = Gson().fromJson(stringData, Driver::class.java)
        view.et_col_name.setText(userData.name)
        dataExists = (context as RegisterOrderActivity).dataExists
        if (dataExists) initView(view)
        else {
            val comers = resources.getStringArray(R.array.commerces)
            val adapter = ArrayAdapter(context!!,R.layout.item_frase,comers)
            et_comers_name.setAdapter(adapter)
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

    private fun writeBooking() {
        val mClientPackProvider = ClientPackProvider()

        if (numberCargo<6){
            val mDialog = Dialog(context)
            mDialog.setContentView(R.layout.input_text)
            mDialog.setCancelable(false)
            mDialog.title.text = "Monto recibido para pagar flete en agencias."
            mDialog.btnCloseDialog.setOnClickListener {
                mDialog.dismiss()
            }
            mDialog.btnRegisterDialog.setOnClickListener {
                if (mDialog.inputText.text.isNotEmpty())
                    mDialog.dismiss()
                else{
                    mDialog.inputText.error = "complete el campo"
                    mDialog.inputText.requestFocus()
                }
            }
            mDialog.show()
        }
    }

    private fun initPackAdapter(view: View) {
        mPackAdapter = OrderAdapter(mPackBookingList,context as RegisterOrderActivity)
        val ll = LinearLayoutManager(activity)
        view.rv_pack.layoutManager = ll
        view.rv_pack.adapter = mPackAdapter
        if (!dataExists){

            val mHelper = androidx.recyclerview.widget.ItemTouchHelper(
                ItemTouchHelper(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT)
            )
            mHelper.attachToRecyclerView(view.rv_pack)
        }

    }

    private fun initView(view: View) {
        idDoc = (context as RegisterOrderActivity).idDoc
        status = (context as RegisterOrderActivity).status

        view.et_comers_name.isEnabled = false
        view.et_stand_name.isEnabled = false
        val store = (context as RegisterOrderActivity).store.split("|")
        view.et_comers_name.setText(store[0])
        view.et_stand_name.setText(store[1])
        if (status=="")
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
        if (pack.cargo!!) numberCargo++
        pack.create = Date()
        pack.idOrder = idDoc
        pack.indexController = mutableMapOf(
            "atention" to userData.name
        )

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
            if (mPackAdapter.listOrder[viewHolder.adapterPosition].cargo!!){
                numberCargo-=1
            }
            mPackAdapter.listOrder.removeAt(viewHolder.adapterPosition)
            mPackAdapter.notifyItemRemoved(viewHolder.adapterPosition)
        }
    }
}