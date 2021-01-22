package com.imaxcorp.imaxc.ui.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.Agencies
import com.imaxcorp.imaxc.data.AgencyData
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.ui.admin.adapter.AgencyAdapter

class AgenciasActivity : AppCompatActivity() {

    private var list: ArrayList<AgencyData> = ArrayList()
    private lateinit var db: DatabaseReference
    private lateinit var adapter: AgencyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agencias)

        MyToolBar().show(this,"Agencias",true)
        db = FirebaseDatabase.getInstance().reference.child("Agencias")



    }
}