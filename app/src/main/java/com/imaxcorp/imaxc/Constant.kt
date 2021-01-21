package com.imaxcorp.imaxc

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

interface Constant {
    companion object {
        val AUTH = FirebaseAuth.getInstance()
        val DB = FirebaseDatabase.getInstance().reference

        const val LOCATION_REQUEST_CODE = 1
        const val SETTINGS_REQUEST_CODE = 2
        const val CHANNEL_ID = "com.imaxcorp.imaxc"
        const val CHANNEL_NAME = "iMaxC"
        const val NOTIFICATION_CODE = 100
        const val DATA_LOGIN = "LOGIN"
        const val CALL_REQUEST_CODE = 4
        const val SHIPPING_RESULT = 404
        const val GALLERY_RESULT = 406

        const val CREDENTIAL_CA = "adm-ca1"         //asignar
        const val CREDENTIAL_ATT = "adm-att-1"      //Asignar y tomar foto
        const val CREDENTIAL_ATS = "adm-att-2"      //asignar
        const val CREDENTIAL_ROOT = "adm-root"
    }
}

enum class UserType {
    DRIVER,
    ADMIN,
    COLLECTOR
}