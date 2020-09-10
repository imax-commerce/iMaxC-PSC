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
    }
}

enum class UserType {
    DRIVER,
    ADMIN,
    COLLECTOR
}