package com.imaxcorp.imaxc.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.Constant.Companion.AUTH
import com.imaxcorp.imaxc.include.MyToolBar
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        MyToolBar().show(this,"Login de Usuario", true)
    }

    fun sing(view: View){
        val email = txtEmail.text.toString()
        val password = txtPassword.text.toString()

        if (email.isEmpty()){
            txtEmail.error = "Ingrese su Correo"
            this.txtEmail.requestFocus()
            return
        }

        if (password.isEmpty()){
            this.txtPassword.error = "Ingrese su Contraseña"
            this.txtPassword.requestFocus()
            return
        }
        val dialog = loading("logging...")
        dialog.show()
        AUTH.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful && it.isComplete){
                    startActivity(
                        Intent(this, MainActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                }
                dialog.dismiss()
            }
            .addOnFailureListener {
                toastLong("Fail! ${it.message}")
            }
    }

    fun register(view: View){
        Intent(applicationContext, RegisterActivity::class.java).also {
            startActivity(it)
        }
    }
}