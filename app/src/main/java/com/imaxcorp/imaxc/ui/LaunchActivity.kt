package com.imaxcorp.imaxc.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.imaxcorp.imaxc.Constant.Companion.AUTH
import com.imaxcorp.imaxc.R

class LaunchActivity : AppCompatActivity() {
    lateinit var mAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
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
            if (AUTH.currentUser != null){
                startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }else{
                startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
        },2500)
    }
}