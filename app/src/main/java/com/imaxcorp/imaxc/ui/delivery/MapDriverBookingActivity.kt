package com.imaxcorp.imaxc.ui.delivery

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.Constant.Companion.LOCATION_REQUEST_CODE
import com.imaxcorp.imaxc.Constant.Companion.SETTINGS_REQUEST_CODE
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.providers.*
import com.imaxcorp.imaxc.services.DecodePoints
import com.imaxcorp.imaxc.ui.start.LaunchActivity
import kotlinx.android.synthetic.main.activity_map_driver_booking.*
import kotlinx.android.synthetic.main.item_payment.*
import kotlinx.android.synthetic.main.toast_custom.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.*

class MapDriverBookingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var idDocument : String
    private lateinit var mMap: GoogleMap
    private lateinit var mMapFragment: SupportMapFragment
    private lateinit var mAuthProvider: AuthProvider
    private lateinit var mGeoFireProvider: GeoFireProvider
    private lateinit var mClientBookingProvider: ClientBookingProvider
    private lateinit var mTokenProvider: TokenProvider
    private lateinit var mNotificationProvider: NotificationProvider
    private lateinit var mClientProvider: ClientProvider
    private lateinit var mDriverProvider: DriverProvider

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocation: FusedLocationProviderClient
    private var mMarker: Marker? = null
    private lateinit var mCurrentLatLng: LatLng

    private lateinit var origin: LatLng
    private lateinit var destine: LatLng

    private lateinit var mGoogleApiProvider: GoogleApiProvider
    private lateinit var mPolylineList: List<LatLng>
    private lateinit var mPolylineOptions: PolylineOptions;

    private var mIsFirstTime = true
    private var mIsCloseToClient = false
    private lateinit var mOrderClient: ClientBooking
    private lateinit var mDialogLoad: Dialog
    private var isPayment = false
    private lateinit var message: String

    private var mLocationCallback: LocationCallback? = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                for (location in it.locations){
                    if (applicationContext != null){
                        mCurrentLatLng = LatLng(location.latitude,location.longitude)

                        if(mMarker != null) mMarker?.remove()

                        mMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(location.latitude,location.longitude))
                                .title("My Position")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_motoricy))
                        )
                        updateLocation()

                        if (mIsFirstTime) {
                            mIsFirstTime = false
                            getClientBooking()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_driver_booking)

        mDialogLoad = loading("loading...")
        mDialogLoad.show()
        idDocument = intent.getStringExtra("ID_DOC")!!
        mDriverProvider = DriverProvider()
        mAuthProvider = AuthProvider()
        mGeoFireProvider = GeoFireProvider("drivers_working")
        mTokenProvider = TokenProvider()
        mClientBookingProvider = ClientBookingProvider()
        mNotificationProvider = NotificationProvider()
        mClientProvider = ClientProvider()
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        mMapFragment = supportFragmentManager.findFragmentById(R.id.mapDriver) as SupportMapFragment
        mMapFragment.getMapAsync(this)

        mGoogleApiProvider = GoogleApiProvider(this)

        btnStartBooking.setOnClickListener {
            when(btnStartBooking.text.toString()) {
                "Iniciar envio" -> {
                    startBooking()
                }
                "Finalizar envio" -> {
                    finishBooking()
                }
            }
        }

        textViewClientBooking.setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val layout = inflater.inflate(R.layout.toast_custom, findViewById(R.id.layout_toast) ,false)
            layout.toastText.text = message
            val mToast = Toast(this)
            mToast.setGravity(Gravity.START,0,200)
            mToast.duration = Toast.LENGTH_LONG
            mToast.view = layout
            mToast.show()
        }
    }

    private fun payment(){
        val mDialogPayment = Dialog(this)
        mDialogPayment.setContentView(R.layout.item_payment)
        mDialogPayment.setCancelable(false)
        mDialogPayment.title.text = getString(R.string.priceCobrar,DecimalFormat("0.00").format(mOrderClient.detail?.price))
        mDialogPayment.btnCloseDialog.setOnClickListener {
            mDialogPayment.dismiss()
        }
        mDialogPayment.btnCashDialog.setOnClickListener {
            mDialogPayment.dismiss()
            mDialogLoad.show()
            val map = mapOf(
                "/$idDocument/detail/paymentType" to "cash",
                "/$idDocument/detail/statusPayment" to true,
                "/$idDocument/detail/paymentDate" to Date(),
                "/$idDocument/detail/paymentOrigin" to "origin"
            )

            mClientBookingProvider.updateRoot(map)
                .addOnCompleteListener {
                    if (it.isSuccessful && it.isComplete){
                        toastLong("Completado... :)")
                        isPayment = true
                        btnPayment.visibility = View.GONE
                        mDialogLoad.dismiss()
                    }else{
                        mDialogLoad.dismiss()
                    }
                }
                .addOnFailureListener {
                    toastShort("Error... "+it.message)
                }
        }
        mDialogPayment.btnTransferDialog.setOnClickListener {
            mDialogPayment.dismiss()
            mDialogLoad.show()
            val map = mapOf(
                "/$idDocument/detail/paymentType" to "transfer",
                "/$idDocument/detail/statusPayment" to true,
                "/$idDocument/detail/paymentDate" to Date(),
                "/$idDocument/detail/paymentOrigin" to "origin"
            )

            mClientBookingProvider.updateRoot(map)
                .addOnCompleteListener {
                    if (it.isSuccessful && it.isComplete){
                        toastLong("Completado... :)")
                        isPayment = true
                        btnPayment.visibility = View.GONE
                        mDialogLoad.dismiss()
                    }else{
                        mDialogLoad.dismiss()
                    }
                }
                .addOnFailureListener {
                    toastShort("Error... "+it.message)
                }
        }
        mDialogPayment.show()
    }
    private fun finishBooking() {

        Intent(this, FinishDeliveryActivity::class.java)
            .putExtra("ID_DOC",idDocument)
            .putExtra("PAYMENT",isPayment)
            .putExtra("PRICE",mOrderClient.detail?.price)
            .also {
            startActivity(it)
            finish()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun startBooking() {
        mDialogLoad.show()
        val map = mapOf(
            "/$idDocument/status" to "start",
            "/$idDocument/indexType/Domicilio" to "start",
            "/$idDocument/indexType/${mAuthProvider.getId()}/Domicilio" to "start",
            "/$idDocument/detail/start" to Date()
        )
        mClientBookingProvider.updateRoot(map)
            .addOnFailureListener {
                toastLong("Error. ${it.message}")
            }
            .addOnCompleteListener {
                if (it.isSuccessful && it.isComplete){
                    btnStartBooking.text = "Finalizar envio"
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(mCurrentLatLng).title("My Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_motoricy)))
                    mMarker = mMap.addMarker(MarkerOptions().position(destine).title("Entregar Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_destiny)))
                    drawRoute(destine)
                    btnPayment.visibility = View.GONE
                    btnCancelBooking.visibility = View.GONE
                    textViewClientBooking.text =
                        "Dir: ${mOrderClient.destination!!.street} ${mOrderClient.destination!!.feature} - ${mOrderClient.destination!!.locality}"
                    textViewOriginClientBooking.text =
                        "Contacto: ${mOrderClient.destination!!.phone}"
                    textViewOriginClientBooking.setOnClickListener { openCall(mOrderClient.destination?.phone!!) }
                    message = mOrderClient.destination!!.reference!!
                    mDialogLoad.dismiss()
                }else{
                    mDialogLoad.dismiss()
                }
            }
    }

    private fun updateLocation(){
        if (mAuthProvider.existSession())
            mCurrentLatLng.let {
                mGeoFireProvider.saveLocation(mAuthProvider.getId(), it)
            }
    }

    private fun getClientBooking() {
        mClientBookingProvider.getClientBooking(idDocument).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    mOrderClient = snapshot.getValue(ClientBooking::class.java) as ClientBooking
                    origin = LatLng(mOrderClient.origin!!.latLng!!.latitude,mOrderClient.origin!!.latLng!!.longitude)
                    destine = LatLng(mOrderClient.destination!!.latLng!!.latitude,mOrderClient.destination!!.latLng!!.longitude)
                    mOrderClient.detail?.statusPayment?.let {
                        isPayment = it
                    }
                    if (mOrderClient.status == "accept") {

                        mMarker = mMap.addMarker(MarkerOptions().position(origin).title("Recoger Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_origin)))
                        textViewClientBooking.text = "Dir: ${mOrderClient.origin!!.street} ${mOrderClient.origin!!.feature} - ${mOrderClient.origin!!.locality}"
                        textViewOriginClientBooking.text = "Contacto: ${mOrderClient.origin!!.phone}"
                        message = mOrderClient.origin!!.reference!!
                        drawRoute(origin)
                        textViewOriginClientBooking.setOnClickListener { openCall(mOrderClient.origin?.phone!!) }
                        btnCancelBooking.visibility = View.VISIBLE
                        btnCancelBooking.setOnClickListener { cancelBooking() }
                        btnPayment.visibility = View.VISIBLE
                        btnPayment.setOnClickListener {
                            payment()
                        }
                    }else{
                        if (mOrderClient.status == "start") {
                            mMarker = mMap.addMarker(MarkerOptions().position(destine).title("Entregar Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_destiny)))
                            btnStartBooking.text = "Finalizar envio"
                            textViewClientBooking.text =
                                "Dir: ${mOrderClient.destination!!.street} ${mOrderClient.destination!!.feature} - ${mOrderClient.destination!!.locality}"
                            textViewOriginClientBooking.text =
                                "Contacto: ${mOrderClient.destination!!.phone}"
                            message = mOrderClient.destination!!.reference!!
                            drawRoute(destine)
                            btnCancelBooking.visibility = View.GONE
                            btnPayment.visibility = View.GONE
                            textViewOriginClientBooking.setOnClickListener { openCall(mOrderClient.destination?.phone!!) }
                        }
                    }
                    mDialogLoad.dismiss()
                }
            }

        })
    }

    private fun drawRoute(position:LatLng){
        mGoogleApiProvider.getDirections(mCurrentLatLng,position)
            ?.enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {

                }
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    try {
                        val jsonObject = JSONObject(response.body())
                        val jsonArray = jsonObject.getJSONArray("routes")
                        val route = jsonArray.getJSONObject(0)
                        val polylines = route.getJSONObject("overview_polyline")
                        val points = polylines.getString("points")

                        mPolylineList = DecodePoints.decodePoly(points) as List<LatLng>
                        mPolylineOptions = PolylineOptions()
                        mPolylineOptions.color(Color.BLUE)
                        mPolylineOptions.width(10f)
                        mPolylineOptions.startCap(SquareCap())
                        mPolylineOptions.jointType(JointType.ROUND)
                        mPolylineOptions.addAll(mPolylineList)
                        mMap.addPolyline(mPolylineOptions)
//centra la vista
                        val constructor = LatLngBounds.Builder()
                        constructor.include(mCurrentLatLng)
                        constructor.include(position)

                        val bonus = constructor.build()
                        val width = resources.displayMetrics.widthPixels
                        val height = resources.displayMetrics.heightPixels
                        val padding = (height * 0.25).toInt() // 25% de espacio (padding) superior e inferior

                        val center = CameraUpdateFactory.newLatLngBounds(bonus, width, height, padding)
                        mMap.animateCamera(center)
                        /*
                        val legs = route.getJSONArray("legs")
                        val leg = legs.getJSONObject(0)
                        val distance = leg.getJSONObject("distance")
                        val duration = leg.getJSONObject("duration")
                        val distanceText = distance.getString("text")
                        val durationText = duration.getString("text")
*/
                    } catch (e: Exception){
                        Log.d("Error","Error encontrado: ${e.message}")
                    }
                }
            })
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.uiSettings.isZoomControlsEnabled = true

        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.smallestDisplacement = 5F

        startLocation()
    }
    private fun startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS()
            }
        }
    }

    private fun showAlertDialogNOGPS() {
        val builder =  AlertDialog.Builder(this)
        builder.setMessage("Por favor activa tu ubicacion para continuar")
            .setPositiveButton("Configuraciones", DialogInterface.OnClickListener() {_,_ ->
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE)
            }).create().show();
    }

    private fun gpsActived(): Boolean {
        var isActive = false
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true
        }
        return isActive
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Proporciona los permisos para continuar")
                    .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                    .setPositiveButton("OK", DialogInterface.OnClickListener() {_,_->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE);
                    })
                    .create()
                    .show();
            }
            else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE);
            }
        }
    }

    private fun cancelBooking() {
        val mAlertDialog = AlertDialog.Builder(this)
        mAlertDialog.setMessage("Esta Seguro que desea Cancelar el servicio?")
        mAlertDialog.setPositiveButton("SI",DialogInterface.OnClickListener { _, _ ->
            mDialogLoad.show()
            val map = mapOf(
                "/$idDocument/status" to "cancel",
                "/$idDocument/indexType/Domicilio" to "cancel",
                "/$idDocument/indexType/${mAuthProvider.getId()}/Domicilio" to "cancel",
                "/$idDocument/indexType/${mAuthProvider.getId()}/status" to false,
                "/$idDocument/detail/cancel" to Date(),
                "/$idDocument/${mAuthProvider.getId()}" to null,
                "/$idDocument/detail/obs" to "Cancelado por | ${mAuthProvider.getId()}"
            )

            mClientBookingProvider.updateRoot(map)
                .addOnFailureListener {
                    toastLong("Error. ${it.message}")
                }
                .addOnCompleteListener {
                    if (it.isComplete && it.isSuccessful){
                        mDriverProvider.updateDriver(mapOf(
                            "/${mAuthProvider.getId()}/online" to "free"
                        ))?.addOnCompleteListener {task->
                            if (task.isSuccessful && task.isComplete){
                                Intent(this,
                                    LaunchActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .setAction(Intent.ACTION_RUN).also { act->
                                        startActivity(act)
                                    }
                            }else{
                                mDialogLoad.dismiss()
                            }
                        }?.addOnFailureListener {exp->
                            toastLong("Error. ${exp.message}")
                        }
                    } else {
                        mDialogLoad.dismiss()
                    }
                }

        })
        mAlertDialog.setNegativeButton("NO",DialogInterface.OnClickListener { dialogInterface, _ ->
            dialogInterface.dismiss()
        })
        mAlertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived())  {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            showAlertDialogNOGPS();
        }
    }
}