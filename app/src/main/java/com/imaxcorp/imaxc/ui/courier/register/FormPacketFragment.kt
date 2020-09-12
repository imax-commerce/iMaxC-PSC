package com.imaxcorp.imaxc.ui.courier.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.PackBooking
import com.imaxcorp.imaxc.ui.courier.order.CourierActivity
import kotlinx.android.synthetic.main.fragment_form_packet.view.*

class FormPacketFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_form_packet, container, false)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (context as RegisterOrderActivity).setTitleTool("Agregar Envio")

        view.btnAddPacket.setOnClickListener {
            savePack(view)
        }
    }

    private fun savePack(view: View) {
        if (view.et_destine_name.text.isEmpty()){
            view.et_destine_name.error = "Complete el campo"
            view.et_destine_name.requestFocus()
            return
        }
        if (view.et_agency_name.text.isEmpty()){
            view.et_agency_name.error = "Complete el campo"
            view.et_agency_name.requestFocus()
            return
        }
        if (view.et_number_pack.text.isEmpty() || view.et_number_pack.text.toString().toInt()<=0){
            view.et_number_pack.error = "Complete el campo"
            view.et_number_pack.requestFocus()
            return
        }

        val mPack = PackBooking()
        mPack.agency = view.et_agency_name.text.toString().trim()
        mPack.destine = view.et_destine_name.text.toString().trim()
        mPack.packages = view.et_number_pack.text.toString().toInt()
        mPack.cargo = view.cb_cargo.isChecked
        mPack.gui = !view.cb_gui.isChecked
        mPack.domicile = view.cb_home.isChecked

        val fragment = activity!!.supportFragmentManager.findFragmentByTag("FOF") as FormOrderFragment
        fragment.addPackets(mPack)
        backPressed()
    }

    fun backPressed(){
        (context as RegisterOrderActivity).setIdView(0)
        activity!!.supportFragmentManager.popBackStack()
        val fragment = activity!!.supportFragmentManager.findFragmentByTag("FOF") as FormOrderFragment
        fragment.backAnimate()
    }


}