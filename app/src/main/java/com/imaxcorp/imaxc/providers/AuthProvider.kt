package com.imaxcorp.imaxc.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthProvider {

    private var mAuth: FirebaseAuth? = null

    init {
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance()
        }
    }

    fun registerEmailPassword(email: String, password: String): Task<AuthResult> {
        return mAuth!!.createUserWithEmailAndPassword(email, password)
    }

    fun loginEmailPassword(email: String, password: String): Task<AuthResult> {
        return mAuth!!.signInWithEmailAndPassword(email,password)
    }

    fun logOut(){
        mAuth?.signOut()
    }

    fun getId(): String {
        return mAuth?.currentUser?.uid!!
    }

    fun existSession(): Boolean {
        var exist = false
        if (mAuth?.currentUser!=null){
            exist = true
        }
        return exist
    }
}