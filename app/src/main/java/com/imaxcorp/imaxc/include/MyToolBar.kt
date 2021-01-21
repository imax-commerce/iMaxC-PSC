package com.imaxcorp.imaxc.include

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.imaxcorp.imaxc.R

class MyToolBar {

    private lateinit var toolbar: Toolbar
    fun show(activity: AppCompatActivity, title: String, back: Boolean) {

        toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.title = title
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(back)
    }

}