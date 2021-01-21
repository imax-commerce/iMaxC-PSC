package com.imaxcorp.imaxc.providers

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.imaxcorp.imaxc.data.Token

class TokenProvider {

    private var dbReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun create(idUser: String?) {
        idUser?.let {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { result ->
                val mToken = Token(result.token)
                val save = mapOf(
                    "tokens/$it" to mToken,
                    "Users/Drivers/$it/token" to mToken.tokenMessaging
                )
                dbReference.updateChildren(save)

            }
        }
    }
}