package com.imaxcorp.imaxc.ui.courier.register

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.imaxcorp.imaxc.R
import kotlinx.android.synthetic.main.fragment_form_order.*
import kotlinx.android.synthetic.main.fragment_form_order.view.*

class FormOrderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_form_order, container, false)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        view.fabPackAdd.setOnClickListener {
            openFragmentPacket()
        }
        return view
    }


    private fun openFragmentPacket(){
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

    private fun backAnimate(){
        val ft = view!!.ffoFragment
        ft.animate().translationX(0f).setDuration(150)
            .setListener(object : AnimatorListenerAdapter() {
            }).start()
    }



}