package com.imaxcorp.imaxc.ui.admin

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.Agencies
import com.imaxcorp.imaxc.data.AgencyData
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.openCall
import com.imaxcorp.imaxc.openGoogleMaps
import com.imaxcorp.imaxc.services.ClickListener
import com.imaxcorp.imaxc.services.OnClickListener
import com.imaxcorp.imaxc.toastShort
import com.imaxcorp.imaxc.ui.admin.adapter.AgencyAdapter
import kotlinx.android.synthetic.main.activity_agencias.*
import java.util.*
import kotlin.collections.ArrayList

class AgenciasActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private var list: ArrayList<AgencyData> = ArrayList()
    private lateinit var db: DatabaseReference
    private lateinit var adapter: AgencyAdapter
    private lateinit var valueEventListener: ValueEventListener

    private var listener = object : ClickListener {
        override fun clickEvent(position: Int) {
            val item = adapter.getItem(position)
            showOptions(item)
        }

    }

    private fun showOptions(item: AgencyData) {
        val alertDialog = AlertDialog.Builder(this,R.style.AlertDialogCustom)
        alertDialog.setTitle(item.agencia!!.toUpperCase(Locale.ROOT))
        var index = 0
        item.telefonos?.let {
            alertDialog.setSingleChoiceItems(item.telefonos!!.toTypedArray(),0,DialogInterface.OnClickListener { _, which ->
                index = which
            }).setNegativeButton("Llamar", DialogInterface.OnClickListener { dialog, _ ->
                openCall(it[index])
            })
        }
        item.latLng?.let{
            alertDialog.setPositiveButton("Como LLegar",DialogInterface.OnClickListener { dialog, _ ->
                openGoogleMaps(it)
            })
        }

        alertDialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agencias)

        MyToolBar().show(this,"Agencias",true)
        db = FirebaseDatabase.getInstance().reference.child("Agencias")

        adapter = AgencyAdapter()
        adapter.setListener(listener)

        rv_agency.setHasFixedSize(true)
        rv_agency.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rv_agency.adapter = adapter

        initListener()


    }

    private fun initListener() {

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (postSnapshot in snapshot.children) {
                    list.add(postSnapshot.getValue(AgencyData::class.java)!!)
                }
                if (list.isNotEmpty()) adapter.setList(list)
                Log.d("DATA---> ", "${list.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                toastShort(error.message)
            }

        }
        db.orderByChild("agencia").addValueEventListener(valueEventListener)
    }

    override fun onDestroy() {
        db.orderByChild("agencia").removeEventListener(valueEventListener)
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_shearch,menu)
        menu?.findItem(R.id.ac_reload)?.isVisible = false
        val menuItem = menu?.findItem(R.id.searchItem)
        searchView = menuItem?.actionView as SearchView
        searchView.queryHint = "Buscar Agencias..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()) filter(newText)
                else filter("")
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun filter(s: String){
        val listFilter = ArrayList<AgencyData>()
        list.filterTo(listFilter) {
            it.agencia!!.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))
        }
        adapter.setList(listFilter)
    }
}