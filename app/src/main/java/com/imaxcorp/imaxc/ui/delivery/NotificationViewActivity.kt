package com.imaxcorp.imaxc.ui.delivery

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.google.firebase.database.*
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.HomeQuery
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.ClientBookingProvider
import com.imaxcorp.imaxc.providers.GeoFireProvider
import com.imaxcorp.imaxc.savePreferenceString
import com.imaxcorp.imaxc.toastLong
import com.imaxcorp.imaxc.toastShort
import kotlinx.android.synthetic.main.notification_view.*
import java.text.DecimalFormat
import java.util.*

class NotificationViewActivity : AppCompatActivity() {

    private lateinit var mGeoFireProvider: GeoFireProvider
    private lateinit var mAuthProvider: AuthProvider
    private var mMediaPlayer: MediaPlayer? = null
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private var mCounter = 45
    private lateinit var mTextViewCounter: TextView
    private var mHandler: Handler? = null
    private var mListener: ValueEventListener? = null

    private lateinit var idDocument: String

    val runnable = Runnable {
        mCounter -= 1
        mTextViewCounter.text = mCounter.toString()
        if (mCounter>0){
            initTimer()
        }else{
            cancelBooking()
        }
    }

    private fun initTimer(){
        mHandler = Handler()
        mHandler?.postDelayed(runnable, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_view)

        mClientBookingProvider = ClientBookingProvider()
        mTextViewCounter = findViewById(R.id.textViewCounter)
        mMediaPlayer = MediaPlayer.create(this,R.raw.ringtone)
        mMediaPlayer?.isLooping = true
        mMediaPlayer?.start()
        mClientBookingProvider = ClientBookingProvider()

        idDocument = intent.getStringExtra("idClient")!!
        textViewOrigin.text = intent.getStringExtra("origin")
        textViewDestination.text = intent.getStringExtra("destination")
        textViewMin.text = intent.getStringExtra("min")
        textViewDistance.text = intent.getStringExtra("distance")
        //por mientras
        val mClientBookingProvider = ClientBookingProvider()
        mClientBookingProvider.getPrice(idDocument).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val price = snapshot.value.toString().toDouble()
                    et_driver_game.visibility = View.VISIBLE
                    et_driver_game.text = getString(R.string.ganancia_driver,DecimalFormat("0.0").format(price*0.75)+"0")
                }else{
                    toastLong("no hay Price")
                }
            }

        } )
        val win = window
        win.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        initTimer()

        checkIfClientCancelBooking()
        btnAcceptBooking.setOnClickListener { acceptBooking(mClientBookingProvider.getClientBooking(idDocument))}
        btnCancelBooking.setOnClickListener { cancelBooking() }
    }

    private fun checkIfClientCancelBooking() {
        mListener = mClientBookingProvider.getClientBooking(idDocument).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()){
                    toastLong("El cliente cancelo la Solicitud")
                    if (mHandler!=null) mHandler?.removeCallbacks(runnable)

                    Intent(this@NotificationViewActivity, MainActivity::class.java).also{
                        startActivity(it)
                        finish()
                    }
                }
            }

        })
    }

    private fun cancelBooking(){
        if (mHandler != null) mHandler?.removeCallbacks(runnable)
        mClientBookingProvider = ClientBookingProvider()
        mClientBookingProvider.updateStatus(idDocument, mapOf("status" to "cancel"))

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }

    }

    private fun acceptBooking(postRef: DatabaseReference) {
        if (mHandler != null) mHandler?.removeCallbacks(runnable)

        mAuthProvider = AuthProvider()

        postRef.runTransaction(object: Transaction.Handler {
            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error!=null){
                    toastLong("error "+error.message)
                }else{
                    toastShort("Success :)")
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(2)
                    mGeoFireProvider = GeoFireProvider("active_drivers")
                    mGeoFireProvider.removeLocation(mAuthProvider.getId())
                    mGeoFireProvider.removeLocation(mAuthProvider.getId())

                    mClientBookingProvider.updateRoot(mapOf(
                        "/$idDocument/${mAuthProvider.getId()}" to true,
                        "/$idDocument/indexType/${mAuthProvider.getId()}/Domicilio" to "accept",
                        "/$idDocument/indexType/${mAuthProvider.getId()}/status" to true
                    ))
                    savePreferenceString("CONNECT","CONNECT","working")
                    Intent(this@NotificationViewActivity, MapDriverBookingActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .setAction(Intent.ACTION_RUN).putExtra("ID_DOC",idDocument).also{
                            startActivity(it)
                        }
                }
            }

            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val p = currentData.getValue(HomeQuery::class.java)
                    ?: return Transaction.success(currentData)

                if (p.status=="create"){
                    p.detail?.accept = Date()
                    p.detail?.idDriver = mAuthProvider.getId()
                    p.indexType = mapOf(
                        "Domicilio" to "accept",
                        mAuthProvider.getId() to mapOf(
                            "Domicilio" to true,
                            "status" to true
                        )
                    )
                    p.status = "accept"
                    p.indexType = mapOf(
                        "Domicilio" to "accept"
                    )
                }

                currentData.value = p
                return Transaction.success(currentData)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer!=null){
            if (mMediaPlayer!!.isPlaying){
                mMediaPlayer!!.pause()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mMediaPlayer!=null){
            if (mMediaPlayer!!.isPlaying){
                mMediaPlayer!!.start()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mMediaPlayer!=null){
            if (mMediaPlayer!!.isPlaying){
                mMediaPlayer!!.release()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mHandler != null ) mHandler?.removeCallbacks(runnable)

        if (mMediaPlayer != null){
            if (mMediaPlayer!!.isPlaying){
                mMediaPlayer!!.pause()
            }
        }

        if (mListener != null){
            mClientBookingProvider.getClientBooking(idDocument).removeEventListener(mListener!!)
        }
    }
}