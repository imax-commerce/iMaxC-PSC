package com.imaxcorp.imaxc.ui.delivery

import android.Manifest
import android.annotation.SuppressLint
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
import com.imaxcorp.imaxc.Constant.Companion.LOCATION_REQUEST_CODE
import com.imaxcorp.imaxc.Constant.Companion.SETTINGS_REQUEST_CODE
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.providers.*
import com.imaxcorp.imaxc.services.DecodePoints
import kotlinx.android.synthetic.main.activity_map_driver_booking.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                        //Obtener ubicacion de usuario en tiempo real
                        /*
                        mMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.builder()
                                    .target(LatLng(location.latitude,location.longitude))
                                    .zoom(17f)
                                    .build()
                            ))
                        */
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

        idDocument = intent.getStringExtra("ID_DOC")!!
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

    }

    private fun finishBooking() {

        Intent(this, FinishDeliveryActivity::class.java).putExtra("ID_DOC",idDocument).also {
            startActivity(it)
            finish()
        }

    }

    private fun startBooking() {
        mClientBookingProvider.updateStatus(idDocument,mapOf("status" to "start"))
        btnStartBooking.text = "Finalizar envio"
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(destine).title("My Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_motoricy)))
        mMarker = mMap.addMarker(MarkerOptions().position(destine).title("Entregar Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destine)))
        drawRoute(destine)
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
                    if (mOrderClient.status == "accept") {
                        mMarker = mMap.addMarker(MarkerOptions().position(origin).title("Recoger Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destine)))
                        textViewClientBooking.text = "Ir a: ${mOrderClient.origin!!.address}"
                        textViewOriginClientBooking.text = "Nuemro del Contacto: ${mOrderClient.origin!!.phone}"
                        drawRoute(origin)
                    }else{
                        if (mOrderClient.status == "start") {
                            mMarker = mMap.addMarker(MarkerOptions().position(destine).title("Entregar Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destine)))
                            btnStartBooking.text = "Finalizar envio"
                            textViewClientBooking.text =
                                "Ir a: ${mOrderClient.destination!!.address}"
                            textViewOriginClientBooking.text =
                                "Nuemro del Contacto: ${mOrderClient.destination!!.phone}"
                            drawRoute(destine)
                        }
                    }
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