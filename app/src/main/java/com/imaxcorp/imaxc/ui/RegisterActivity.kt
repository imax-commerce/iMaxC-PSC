package com.imaxcorp.imaxc.ui

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.data.Driver
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.DriverProvider
import kotlinx.android.synthetic.main.activity_register.*
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mDriverProvider: DriverProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        MyToolBar().show(this,"Registro de Conductor", true)
        mAuthProvider = AuthProvider()
        mDriverProvider = DriverProvider()
    }

    fun register(view: View) {
        val name = txtUserRegister.text.toString().trim()
        val email = txtEmailRegister.text.toString().trim()
        val password = txtPasswordRegister.text.toString().trim()
        val vehicle: String = txtVehicleRegister.text.toString().trim()
        val plate: String = txtVehicleBrandRegister.text.toString().trim()

        if (name.isEmpty()){
            txtUserRegister.error = "Ingrese su nombre y apellido"
            txtUserRegister.requestFocus()
            return
        }

        if (email.isEmpty()) {
            txtEmailRegister.error = "Ingrese su correo"
            txtEmailRegister.requestFocus()
            return
        }

        if (password.isEmpty()){
            txtPasswordRegister.error = "Ingrese una contraseña"
            txtPasswordRegister.requestFocus()
            return
        }

        if (password.length < 6){
            txtPasswordRegister.error = "Su contraseña debe de tener un minimo de 6 caracteres"
            txtPasswordRegister.requestFocus()
            return
        }

        if (vehicle.isEmpty()){
            txtVehicleRegister.error = "Ingrese la marca del Vehiculo"
            txtVehicleRegister.requestFocus()
            return
        }

        if (plate.isEmpty()){
            txtVehicleBrandRegister.error = "Ingrese la placa del vehiculo"
            txtVehicleBrandRegister.requestFocus()
            return
        }

        val loadProgress = loading("Registrando...")
        loadProgress.show()
        mAuthProvider.registerEmailPassword(email,password)
            .addOnCompleteListener {
                if (it.isComplete && it.isSuccessful){

                    Constant.AUTH.currentUser?.uid?.let { it1 ->
                        val drive = Driver()
                        drive.date = Date()
                        drive.dateString = SimpleDateFormat("dd-MM-yy HH:mm", Locale.US).format(
                            Date()
                        )
                        drive.email = email
                        drive.name = name
                        drive.typeUser = UserType.DRIVER.toString()
                        drive.id = it1
                        drive.vehicleBrand = vehicle
                        drive.vehiclePlate = plate
                        saveData(drive,loadProgress) }
                }else{
                    loadProgress.dismiss()
                }
            }
            .addOnFailureListener {
                toastLong("Error en Crear usuario!. ${it.message}")
            }

    }

    private fun saveData(driver: Driver, loadProgress: Dialog) {
        mDriverProvider.createDriver(driver)
            ?.addOnCompleteListener {
                if (it.isSuccessful && it.isComplete){
                    toastShort("Registro Exitoso")
                    loadProgress.dismiss()
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                }else{
                    loadProgress.dismiss()
                }
            }
            ?.addOnFailureListener {
                toastLong("Error en el registro de datos!. ${it.message}")
            }
    }
}