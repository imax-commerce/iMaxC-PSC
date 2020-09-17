package com.imaxcorp.imaxc

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.load.*

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
