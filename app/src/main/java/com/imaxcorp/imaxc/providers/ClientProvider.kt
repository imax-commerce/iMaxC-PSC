package com.imaxcorp.imaxc.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class  ClientProvider {

    private var db: DatabaseReference? = null

    init {
        if (db == null)
            db = FirebaseDatabase.getInstance().reference.child("Users").child("Clients")
    }

    fun createClient(client: Map<String,Any?>): Task<Void>? {
        return db?.child(client["id"].toString())?.setValue(client)
    }

    fun getClient(id: String): DatabaseReference? {
        return db?.child(id)
    }

    fun updateClient(data: Map<String,Any>, id: String): Task<Void>? {
        return db?.child(id)?.updateChildren(data)
    }
}