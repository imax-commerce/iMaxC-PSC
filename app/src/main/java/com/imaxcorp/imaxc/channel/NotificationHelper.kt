package com.imaxcorp.imaxc.channel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.imaxcorp.imaxc.Constant.Companion
import com.imaxcorp.imaxc.R

class NotificationHelper(base: Context) : ContextWrapper(base) {

    private var manager: NotificationManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannel(){
        val notificationChannel: NotificationChannel =
            NotificationChannel(Companion.CHANNEL_ID,Companion.CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.lightColor = Color.GRAY
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getManager()?.createNotificationChannel(notificationChannel)
    }

    fun getManager(): NotificationManager? {
        if (manager == null){
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return manager
    }
    //para versiones superiores al api 26
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getNotification(title: String, body: String, intent: PendingIntent, soundUri: Uri) : Notification.Builder {
        return Notification.Builder(applicationContext,Companion.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
                //(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +applicationContext.packageName +"/"+R.raw.ringtone))
            .setSound(soundUri)
            .setContentIntent(intent)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(Notification.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getNotificationActions(
        title: String,
        body: String,
        soundUri: Uri,
        acceptAction: Notification.Action,
        cancelAction: Notification.Action
    ) : Notification.Builder {
        return Notification.Builder(applicationContext,Companion.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(acceptAction)
            .addAction(cancelAction)
            .setStyle(Notification.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    //para versiones inferiores al api 26
    fun getNotificationOldAPI(title: String, body: String, intent: PendingIntent, soundUri: Uri) : NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext,Companion.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(intent)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    fun getNotificationOldAPIActions(
        title: String,
        body: String,
        soundUri: Uri,
        acceptAction: NotificationCompat.Action,
        cancelAction: NotificationCompat.Action
    ) : NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext,Companion.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(acceptAction)
            .addAction(cancelAction)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }
}
