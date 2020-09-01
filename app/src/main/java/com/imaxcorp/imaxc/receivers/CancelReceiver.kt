package com.imaxcorp.imaxc.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.imaxcorp.imaxc.providers.ClientBookingProvider

class CancelReceiver : BroadcastReceiver() {

    private lateinit var mClientBookingProvider: ClientBookingProvider
    override fun onReceive(context: Context?, intent: Intent?) {

        val idClient = intent?.getStringExtra("idClient")

        idClient?.let {
            mClientBookingProvider = ClientBookingProvider()
            mClientBookingProvider.updateStatus(it,mapOf("status" to "cancel"))
        }

        val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
    }
}