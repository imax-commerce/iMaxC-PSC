package com.imaxcorp.imaxc.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.imaxcorp.imaxc.data.ClientBooking

class ClientBookingProvider {

    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("ClientBooking")

    fun create(clientOrder: ClientBooking) : Task<Void> {
        return mDatabase.child(clientOrder.detail!!.idClient!!).setValue(clientOrder)
    }

    fun pushOrder(map: Map<String,Any>): Task<Void> {
        return mDatabase.push().setValue(map)
    }

    fun updateStatus(idOrderClient: String, map: Map<String,Any>): Task<Void> {
        return mDatabase.child(idOrderClient).updateChildren(map)
    }

    fun update(idOrderClient: String, map: Map<String, Any?>) : Task<Void> {
        return mDatabase.child(idOrderClient).updateChildren(map)
    }

    fun updateRoot(map: Map<String,Any>): Task<Void> {
        return mDatabase.updateChildren(map)
    }
    fun updateDetail(idClientOrder: String, map: Map<String,Any>): Task<Void> {
        return mDatabase.child(idClientOrder).child("detail").updateChildren(map)
    }

    fun updateIdHistoryBooking(idOrderClient: String): Task<Void>  {
        val idPush = mDatabase.push().key;
        return mDatabase.child(idOrderClient).updateChildren(mapOf("idHistoryBooking" to idPush));
    }

    fun getStatus(idOrderClient: String): DatabaseReference {
        return mDatabase.child(idOrderClient).child("status");
    }

    fun getClientBooking(idOrderClient: String): DatabaseReference {
        return mDatabase.child(idOrderClient);
    }

    fun delete(idOrderClient: String): Task<Void>  {
        return mDatabase.child(idOrderClient).removeValue();
    }

    fun getBookingFree(): Query {
        return mDatabase.orderByChild("indexType/Agencia").equalTo("create")
    }

    fun getBookingPending(idOrder: String): Query {
        return mDatabase.orderByChild("indexType/$idOrder/status").equalTo(true)
    }

    fun getPacketBooking(idOrder: String) : DatabaseReference {
        return mDatabase.child(idOrder).child("shipping-list")
    }

}