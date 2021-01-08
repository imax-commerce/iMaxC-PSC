package com.imaxcorp.imaxc.include

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.imaxcorp.imaxc.R

class MyToolBar {

    fun show(activity: AppCompatActivity, title: String, back: Boolean) {

        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.title = title
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(back)

    }
}