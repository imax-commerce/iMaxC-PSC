package com.imaxcorp.imaxc.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.imaxcorp.imaxc.data.PackBooking

class ClientPackProvider {

    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("PacketBooking")

    fun createSet(id:String, data: Map<String, Any>) : Task<Void> {
        return mDatabase.child(id).setValue(data)
    }

    fun createPush(packBooking: PackBooking) : Task<Void> {
        return mDatabase.push().setValue(packBooking)
    }
}