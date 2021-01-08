package com.imaxcorp.imaxc.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.openWeb
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val isAvailable = intent.getBooleanExtra("AVAILABLE",false)
        val isEmployee = intent.getBooleanExtra("EMPLOYEE",false)

        if (isAvailable){
            if (!isEmployee){
                textTitle.text = "Ya no erres empleado."
                textDescription.text = "Dejaste de formar parte de nuestra familia de iMax Courier, Gracias por haber sido parte de iMax Courier."
            }
        }else{
            textTitle.text = "Falta validar la cuenta."
            textDescription.text = "Aun no eres empleado de iMax Courier, puedes postular y formar parte de nuestra gran familia."
        }

        linkWeb.setOnClickListener {
            openWeb("http://imaxcommerce.com")
        }
    }
}