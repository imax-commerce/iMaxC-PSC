package com.imaxcorp.imaxc.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.imaxcorp.imaxc.Constant.Companion.NOTIFICATION_CODE
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.channel.NotificationHelper
import com.imaxcorp.imaxc.receivers.AcceptReceiver
import com.imaxcorp.imaxc.receivers.CancelReceiver
import com.imaxcorp.imaxc.toastLong
import com.imaxcorp.imaxc.ui.delivery.NotificationViewActivity

class MyFirebaseMessagingClient : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        Log.d("NEW TOKEN-->", "AQUI:> $p0")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data["title"].toString()
            val body = remoteMessage.data["body"].toString()
            val idOrder = remoteMessage.data["idClient"].toString()
            val origin = remoteMessage.data["origin"].toString()
            val destine = remoteMessage.data["destination"].toString()
            val min = remoteMessage.data["min"].toString()
            val distance = remoteMessage.data["distance"].toString()

            if (it){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    Log.d("NOTIFY--> ", "Build version codes 0 >")
                    if (title.contains("SOLICITUD DE SERVICIO")){

                        showNotificationApiOreoActions(title,body, idOrder)
                        showNotificationActivity(idOrder, origin, destine, min, distance)
                    }else{
                        if (title.contains("SOLICITUD CANCELADA")) {
                            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancel(2)
                            showNotificationApiOreo(title,body)
                        }else{
                            showNotificationApiOreo(title, body)
                        }
                    }
                }else{
                    Log.d("NOTIFY--> ", "Build version codes 0 <")
                    if(title.contains("SOLICITUD DE SERVICIO")) {
                        showNotificationActions(title, body, idOrder)
                        showNotificationActivity(idOrder, origin, destine, min, distance)
                    } else{
                        if (title.contains("SOLICITUD CANCELADA")){
                            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancel(2)
                            showNotification(title, body)
                        }else{
                            showNotification(title, body)
                        }
                    }
                }
            } else{
                toastLong("Notificacion recivida corectamente, sin titulo...")
            }
        }
    }

    private fun showNotificationActivity(idClient: String, origin: String, destination: String, min: String, distance: String) {
        val pm = baseContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        if (!isScreenOn){
            val wakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,"AppName:MyLock"

            )
            wakeLock.acquire(10000)
        }
        val intent = Intent(baseContext, NotificationViewActivity::class.java)
        intent.putExtra("idClient", idClient)
        intent.putExtra("origin", origin)
        intent.putExtra("destination", destination)
        intent.putExtra("min", min)
        intent.putExtra("distance", distance)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or  Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showNotification(title: String,body:String) {
        val intent = PendingIntent.getActivity(baseContext,0, Intent(),PendingIntent.FLAG_ONE_SHOT)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationOldAPI(title,body,intent,sound)
        notificationHelper.getManager()?.notify(1,builder.build())
    }

    private fun showNotificationActions(title: String, body: String, idClient: String) {
        val acceptIntent =  Intent(this, AcceptReceiver::class.java)
        acceptIntent.putExtra("idClient", idClient)
        val acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val acceptAction = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "Aceptar",
            acceptPendingIntent
        ).build()
/*
        val cancelIntent =  Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idClient", idClient)
        val cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val cancelAction = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "Cancelar",
            cancelPendingIntent
        ).build()
*/
        val sound =  RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationOldAPIActions(title, body, sound, acceptAction)
        notificationHelper.getManager()?.notify(2, builder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showNotificationApiOreo(title: String,body:String) {
        val intent = PendingIntent.getActivity(baseContext,0, Intent(),PendingIntent.FLAG_ONE_SHOT)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotification(title,body,intent,sound)
        notificationHelper.getManager()?.notify(1,builder.build())
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showNotificationApiOreoActions(title: String, body: String, idClient: String) {
        val acceptIntent = Intent(this, AcceptReceiver::class.java)
        acceptIntent.putExtra("idClient", idClient)
        val acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val acceptAction =  Notification.Action.Builder(
            R.mipmap.ic_launcher,
            "Aceptar",
            acceptPendingIntent
        ).build()
/*
        val cancelIntent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idClient", idClient)
        val cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val cancelAction =  Notification.Action.Builder(
            R.mipmap.ic_launcher,
            "cancelar",
            cancelPendingIntent
        ) .build()
*/
        val sound =  RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationActions(title,body,sound,acceptAction)
        notificationHelper.getManager()?.notify(2, builder.build())
    }

}
