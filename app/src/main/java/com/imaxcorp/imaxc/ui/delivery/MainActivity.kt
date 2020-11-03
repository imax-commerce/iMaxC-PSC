package com.imaxcorp.imaxc.ui.delivery

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.imaxcorp.imaxc.*
import com.imaxcorp.imaxc.R
import com.imaxcorp.imaxc.data.ClientBooking
import com.imaxcorp.imaxc.include.MyToolBar
import com.imaxcorp.imaxc.providers.AuthProvider
import com.imaxcorp.imaxc.providers.DriverProvider
import com.imaxcorp.imaxc.providers.GeoFireProvider
import com.imaxcorp.imaxc.providers.TokenProvider
import com.imaxcorp.imaxc.ui.start.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mTokenProvider: TokenProvider
    private lateinit var mMap: GoogleMap
    private lateinit var mMapFragment: SupportMapFragment
    private var mAuthProvider = AuthProvider()
    private lateinit var mDialog: Dialog

    private lateinit var mListener: ValueEventListener
    private var mMarker: Marker? = null
    private var isConnect = "off"
    private var mIsConnect = false
    private var mListMarker: ArrayList<Marker> = ArrayList()
    private var mCurrentLatLng: LatLng? = null
    private lateinit var mGeoFireProvider: GeoFireProvider
    private lateinit var mGeoFireProviderWorking: GeoFireProvider
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocation: FusedLocationProviderClient
    private lateinit var mDriverProvider: DriverProvider

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ItemAdapter
    private var mIsFirstTime = true
    private lateinit var listenerOnline : DatabaseReference

    private var mLocationCallback: LocationCallback? = object : LocationCallback()  {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                for (location in it.locations){
                    if (applicationContext != null){
                        mCurrentLatLng = LatLng(location.latitude,location.longitude)
                        //Obtener ubicacion de usuario en tiempo real
                        mMap.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                            CameraPosition.builder()
                                .target(LatLng(location.latitude,location.longitude))
                                .zoom(17f)
                                .build()
                        ))
                        if (mIsFirstTime){
                            mIsFirstTime = false
                            getActiveDrive()
                        }
                        if (isConnect=="free")
                            updateLocation()
                        if (isConnect=="working")
                            updateWorking()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyToolBar().show(this,"Conductor",false)
        mDriverProvider = DriverProvider()

        mDialog = loading("loading...")
        mGeoFireProvider = GeoFireProvider("active_drivers")
        mGeoFireProviderWorking = GeoFireProvider("drivers_working")
        mAuthProvider = AuthProvider()
        mTokenProvider = TokenProvider()
        // con esta propiedad podemos detener o iniciar la ubicacion de forma comveniente
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mMapFragment.getMapAsync(this)
        mRecyclerView = findViewById(R.id.contentPending)
        val ll = LinearLayoutManager(this)
        mRecyclerView.layoutManager = ll

        btmMapAction.setOnClickListener {
            if (isConnect=="working"){
                toastLong("Tienes solicitudes Pendientes. terminalos antes de continuar")
                return@setOnClickListener
            }
            if (mIsConnect){
                disconnect()
            }else{
                startLocation()
            }

        }
        generateToken()

    }

    private fun statusConnect() {
        if (isConnect=="free" || isConnect=="working"){
            startLocation()
        }
    }

    private fun disconnect(){
        mDialog.show()
        mDriverProvider.updateDriver(mapOf(
            "/${mAuthProvider.getId()}/online" to "off"
        ))?.addOnCompleteListener {

            if (it.isSuccessful && it.isComplete){
                btmMapAction.text = "Conectarse"
                mIsConnect = false
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@addOnCompleteListener
                }
                mMap.isMyLocationEnabled = false
                mFusedLocation.removeLocationUpdates(mLocationCallback)
                mGeoFireProvider.removeQuery()
                mMap.clear()
                if (mAuthProvider.existSession())
                    mGeoFireProvider.removeLocation(mAuthProvider.getId())
            }
            mDialog.dismiss()
        }?.addOnFailureListener {
            toastLong("${it.message}")
            mDialog.dismiss()
        }

    }

    private fun startLocation(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (gpsActive()){
                    mDialog.show()
                    mDriverProvider.updateDriver(mapOf(
                        "/${mAuthProvider.getId()}/online" to if (isConnect == "off") "free" else isConnect
                    ))?.addOnCompleteListener {
                        if (it.isSuccessful && it.isComplete){
                            btmMapAction.text = "Desconectarse"
                            mIsConnect = true
                            mIsFirstTime = true
                            mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
                            mMap.isMyLocationEnabled = true
                            if (mDialog.isShowing) mDialog.dismiss()
                        }
                    }?.addOnFailureListener {
                        toastLong("${it.message}")
                    }

                }else
                    showGPS()
            }else{
                checkLocationPermission()
            }
        }else{
            if (gpsActive()){
                mIsConnect = true
                mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
                mMap.isMyLocationEnabled = true
                if (mDialog.isShowing) mDialog.dismiss()
            }else showGPS()
        }
    }

    private fun getActiveDrive(){
        mCurrentLatLng?.let {
            mGeoFireProvider.getActiveOrder(it).addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyExited(key: String?) {
                    //Conductores que desconectan
                    for (marker in mListMarker){
                        if (marker.tag != null) {
                            if (marker.tag == key){
                                marker.remove()
                                mListMarker.remove(marker)
                                return
                            }
                        }
                    }
                }

                override fun onGeoQueryError(error: DatabaseError?) {
                }

                override fun onGeoQueryReady() {
                }

                override fun onKeyEntered(key: String?, location: GeoLocation) {
                    //marcadores de conductores que se conecten
                    for (marker in mListMarker){
                        if (marker.tag != null) {
                            if (marker.tag == key){
                                return
                            }
                        }
                    }

                    val driverLagLng = LatLng(location.latitude,location.longitude)
                    val marker = mMap.addMarker(MarkerOptions().position(driverLagLng)
                        .title("Solicitud Disponible")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_orders))
                    )
                    marker.tag = key
                    mListMarker.add(marker)
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {
                    //actualizar la posision de cada conductor
                    for (marker in mListMarker){
                        if (marker.tag != null) {
                            if (marker.tag == key){
                                location?.let { it1 ->
                                    marker.position = LatLng(it1.latitude,it1.longitude)
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun updateLocation(){
        if (mAuthProvider.existSession())
            mCurrentLatLng?.let {
                mGeoFireProvider.saveLocation(mAuthProvider.getId(), it)
            }
    }

    private fun updateWorking(){
        if (mAuthProvider.existSession())
            mCurrentLatLng?.let {
                mGeoFireProviderWorking.saveLocation(mAuthProvider.getId(), it)
            }
    }


    override fun onStart() {
        super.onStart()
        if (!::listenerOnline.isInitialized){
            listenerOnline = mDriverProvider.getChildOnline(mAuthProvider.getId())!!
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    isConnect = dataSnapshot.value as String
                    if (isConnect == "acopiando"){
                        desvincular()
                    }
                    if(mIsFirstTime) statusConnect()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    toastLong(databaseError.message)
                }
            }
            mListener = listenerOnline.addValueEventListener(postListener)
        }
        val query = FirebaseDatabase.getInstance().reference.child("ClientBooking")
            .orderByChild(mAuthProvider.getId())
            .equalTo(true)

        val options = FirebaseRecyclerOptions.Builder<ClientBooking>()
            .setQuery(query,ClientBooking::class.java)
            .build()
        mAdapter = ItemAdapter(options, this)
        mRecyclerView.adapter = mAdapter
        mAdapter.startListening()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style_retro))
        } catch (e: Resources.NotFoundException) {
            Log.e("TAG", "Can't find style. Error: ", e)
        }
        mMap.uiSettings.isZoomControlsEnabled = true
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.smallestDisplacement = 5F
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-12.049545,-77.027862)))

        mMap.setOnMarkerClickListener {marker ->
            Intent(this@MainActivity, AcceptActivity::class.java).putExtra("CONNECT",isConnect).putExtra("DOC",marker.tag.toString()).also {
                startActivity(it)
            }
            false
        }

    }

    private fun desvincular(){
        if (::listenerOnline.isInitialized && ::mListener.isInitialized)
            listenerOnline.removeEventListener(mListener)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::listenerOnline.isInitialized && ::mListener.isInitialized)
            listenerOnline.removeEventListener(mListener)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            Constant.LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if (gpsActive()){
                            mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
                            // aparce punto de mi ubicacion se pone aqui para que no se caiga la aplicacion
                            // si no tenemos permisos o si no esta activado el gps
                            mMap.isMyLocationEnabled = true
                        }else
                            showGPS()
                    }else{
                        checkLocationPermission()
                    }
                }else checkLocationPermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            Constant.SETTINGS_REQUEST_CODE -> {
                if (gpsActive()) {
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
                    mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
                    mMap.isMyLocationEnabled = true
                }else{
                    showGPS()
                }
            }
        }
    }

    private fun showGPS(){
        AlertDialog.Builder(this)
            .setMessage("Por favor tu GPS para continuar")
            .setPositiveButton("Configuar", DialogInterface.OnClickListener { dialog, which ->
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),Constant.SETTINGS_REQUEST_CODE)
            }).create().show()
    }

    private fun gpsActive(): Boolean {
        var isActive = false
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isActive = true
        }
        return isActive
    }

    private fun checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                AlertDialog.Builder(this)
                    .setTitle("Proporciona los permisos para continuar")
                    .setMessage("Esta aplicación requiere de los pérmisos de ubicacion para poder utilizarse.")
                    .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            Constant.LOCATION_REQUEST_CODE)
                    })
                    .create().show()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constant.LOCATION_REQUEST_CODE)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drive_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_out -> {
                out()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun out(){
        disconnect()
        mAuthProvider.logOut()
        deletePreference(getString(R.string.preference_user))
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun generateToken(){
        mTokenProvider.create(mAuthProvider.getId())

    }

}