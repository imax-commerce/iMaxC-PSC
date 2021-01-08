package com.imaxcorp.imaxc.ui.start

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.imaxcorp.imaxc.Constant.Companion.AUTH
import com.imaxcorp.imaxc.Constant.Companion.DATA_LOGIN
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.UserType
import com.imaxcorp.imaxc.data.Driver
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.DriverProvider
import com.imaxcorp.imaxc.savePreferenceObject
import com.imaxcorp.imaxc.ui.MessageActivity
import com.imaxcorp.imaxc.ui.admin.AdminActivity
import com.imaxcorp.imaxc.ui.courier.order.CourierActivity
import com.imaxcorp.imaxc.ui.delivery.MainActivity

class LaunchActivity : AppCompatActivity() {
    lateinit var mAnimation: LottieAnimationView
    private lateinit var mAuthProvider: AuthProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        mAuthProvider = AuthProvider()
        mAnimation = findViewById(R.id.animation)
        mAnimation.playAnimation()

        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message","Integración de Firebase Completada")
        analytics.logEvent("InitScreen",bundle)
    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({
            if (mAuthProvider.existSession()){
                val mDriverProvider = DriverProvider()
                mDriverProvider.getDriver(AUTH.currentUser!!.uid)?.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(error: DatabaseError) { }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val mDriver = snapshot.getValue(Driver::class.java) as Driver

                            savePreferenceObject(DATA_LOGIN,"USER",mDriver)

                            if (mDriver.available && mDriver.employee){
                                when(mDriver.typeUser){
                                    UserType.ADMIN.toString()->{
                                        startActivity(Intent(this@LaunchActivity, AdminActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                    }
                                    UserType.DRIVER.toString()->{
                                        startActivity(Intent(this@LaunchActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                    }
                                    UserType.COLLECTOR.toString()->{
                                        startActivity(Intent(this@LaunchActivity, CourierActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                    }
                                }

                            }else{
                                startActivity(Intent(this@LaunchActivity, MessageActivity::class.java)
                                    .putExtra("AVAILABLE",mDriver.available)
                                    .putExtra("EMPLOYEE",mDriver.employee)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                            }
                        }
                    }

                })
            }else{
                startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
        },2500)
    }
}