package com.imaxcorp.imaxc.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.imaxcorp.imaxc.data.Driver

class DriverProvider {

    private var db: DatabaseReference? = null

    init {
        if (db == null)
            db = FirebaseDatabase.getInstance().reference.child("Users").child("Drivers")
    }

    fun createDriver(driver: Driver): Task<Void>? {
        return driver.id?.let { db?.child(it)?.setValue(driver) } ?: run { null }
    }

    fun getDriver(id: String): DatabaseReference? {
        return db?.child(id)
    }

    fun updateDriver(map: Map<String,Any>): Task<Void>? {
        return db?.updateChildren(map)
    }

    fun getChildOnline(id: String) : DatabaseReference? {
        return db?.child(id)?.child("online")
    }

    fun updateClient(data: Map<String,Any>, id: String): Task<Void>? {
        return db?.child(id)?.updateChildren(data)
    }

    fun getReference(): DatabaseReference? {
        return db
    }

}