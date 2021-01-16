package com.imaxcorp.imaxc

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.imaxcorp.imaxc.ui.courier.order.CourierActivity
import kotlinx.android.synthetic.main.load.*
import kotlinx.android.synthetic.main.toast_custom.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun Context.toastShort(message: String, duration: Int = Toast.LENGTH_SHORT) : Toast{
    return Toast.makeText(this,message,duration).apply { show() }
}

fun Context.toastLong(message: String, duration: Int = Toast.LENGTH_LONG) : Toast{
    return Toast.makeText(this,message,duration).apply { show() }
}


fun Context.loading(message: String?): Dialog {


    val builder = Dialog(this)
    builder.setContentView(R.layout.load)
    builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    builder.setCancelable(false)
    if (message == null){
        builder.textLoad.visibility = View.GONE
    }else{
        builder.textLoad.text = message
    }
    return builder
}

fun Context.savePreferenceObject(file: String, key: String, value:Any?) {
    val preference = this.applicationContext.getSharedPreferences(file,MODE_PRIVATE).edit()
    value?.let {
        preference.putString(key,Gson().toJson(value)).apply()
    } ?: run {
        return@run
    }

}

fun Context.savePreferenceString(file: String, key: String, value:String?) {
    val preference = this.applicationContext.getSharedPreferences(file,MODE_PRIVATE).edit()
    value?.let {
        preference.putString(key,value).apply()
    } ?: run {
        return@run
    }

}

fun Context.getPreference(file: String,key: String) : String? {
    val preference = this.applicationContext.getSharedPreferences(file,MODE_PRIVATE)
    return preference.getString(key,null)

}

fun Context.deletePreference(file: String) {
    val preference = this.applicationContext.getSharedPreferences(file, MODE_PRIVATE).edit()
    preference.clear().apply()
}

fun Context.openWeb(url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
}

fun Context.oval(@ColorInt color: Int): ShapeDrawable {
    val oval = ShapeDrawable(OvalShape())
    with(oval) {
        paint.color = color
    }
    return oval
}

fun Activity.openCall(phone: String) {
    val call = Intent(Intent.ACTION_CALL)
    call.data = Uri.parse("tel:$phone")

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)){

                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE),Constant.CALL_REQUEST_CODE)
            }else{
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE),Constant.CALL_REQUEST_CODE)
            }
        }else{
            startActivity(call)
        }
    }else{
        startActivity(call)
    }
}

fun Activity.notificacion() {

    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : Notification.Builder
    val channelId = "com.imaxcorp.imaxc"
    val description = "Test notification"

    val intent = Intent(this,CourierActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT)
    val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val contentView = RemoteViews(packageName,R.layout.notification_layout)
    contentView.setTextViewText(R.id.tv_title,"Nueva Solicitud Disponible")
    contentView.setTextViewText(R.id.tv_content,SimpleDateFormat("dd-MM-yy HH:mm:ss",Locale.US).format(Date()))
    val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannel = NotificationChannel(channelId,description, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.GREEN
        notificationChannel.enableVibration(false)

        notificationManager.createNotificationChannel(notificationChannel)

        builder = Notification.Builder(this,channelId)
            .setContent(contentView)
            .setSmallIcon(R.drawable.icon_orders)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.icon_orders))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(sound)
    }else{

        builder = Notification.Builder(this)
            .setContent(contentView)
            .setSmallIcon(R.drawable.icon_orders)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.icon_orders))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(sound)
    }
    notificationManager.notify(1234,builder.build())
}

fun Activity.openGallery() {
    val mIntent = Intent(Intent.ACTION_GET_CONTENT)
    mIntent.type = "image/*"
    startActivityForResult(mIntent, Constant.GALLERY_RESULT)
}
