package com.example.reigster_show
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import android.location.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_deliveryman_showmap.*
import android.net.Uri
import android.telephony.SmsManager
import android.content.ContentValues
import android.graphics.Color
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.rotationMatrix
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Math.round
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread
class Deliveryman_ShowMap : AppCompatActivity(), OnMapReadyCallback {
    lateinit var locpoint: LatLng
    lateinit var customerpoint: LatLng
    lateinit var deliverypoint: LatLng

    var customer_id: String = ""
    var customer_phone: String = ""
    var customer_library: String = ""
    var delivery_id: String = ""
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var count: Int = 0
    var init_count: Int = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mMap: GoogleMap
    var latLngArrayList = ArrayList<com.google.android.gms.maps.model.LatLng>()
    lateinit var maker: Marker
    lateinit var curpolyline: Polyline
    val SMS_RECEIVE_PERMISSON = 99
    var polylineOptions=PolylineOptions().width(5f).color(Color.RED).clickable(true)
    var firststart=true
    val btnmHandler: Handler =  Handler()
    var check_thread:Boolean=true
    var startthread=false
    var distance:Double=0.0
    var init_route=0
    var time:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deliveryman_showmap)
        checkPermission()
        val locbundle = intent.getParcelableExtra<Bundle>("locbundle")
        if (locbundle != null) {
            locpoint = locbundle.getParcelable("libraryPoint")!!
            customerpoint = locbundle.getParcelable("customerPoint")!!
        }
        if (intent.hasExtra("customerid")&&intent.hasExtra("deliveryid") ) {
            customer_id = intent.getStringExtra("customerid").toString()
            delivery_id = intent.getStringExtra("deliveryid").toString()
            Log.d("????????? ?????????", "${delivery_id} "+customer_id)
        }
        val permissonCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        val permissions = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)
        if (permissonCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "SMS ???????????? ??????", Toast.LENGTH_SHORT).show()
            btn_deliveryStartsend.setOnClickListener {
                access_database(customer_id, 1)

            }
            btn_deliveryArrivesend.setOnClickListener {
                access_database(customer_id, 2)
            }
        } else {
            Toast.makeText(getApplicationContext(), "SMS ???????????? ??????", Toast.LENGTH_SHORT).show()
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECEIVE_SMS
                )
            ) {
                ActivityCompat.requestPermissions(this, permissions, SMS_RECEIVE_PERMISSON);
            } else {
                ActivityCompat.requestPermissions(this, permissions, SMS_RECEIVE_PERMISSON)
            }
        }
        btn_deliveryComplete.setOnClickListener {
           db.collection("Orders").whereEqualTo("customerid",customer_id)
               .get().addOnSuccessListener { doc->
                   for(index in doc){
                       if(index.data["ordercomplete"].toString().toBoolean()){
                           startthread=false
                          // db.collection("Orders").document(index.id).delete()
                           var initPage_intent= Intent(this, DeliveryOrderList::class.java)
                           startActivity(initPage_intent)
                           finish()
                       }
                       else{
                           Toast.makeText(getApplicationContext(), "?????? ??????????????? ????????? ??????????????????", Toast.LENGTH_LONG).show()
                       }

                   }

               }
        }
        btnUpdateroad.setOnClickListener {
            Log.d("?????????", "?????? ????????? ?????? ??????")
            if(init_route!=0){
                curpolyline.remove()
            }
            var tempPolylineOptions=polylineOptions
            curpolyline=mMap.addPolyline(tempPolylineOptions)
            init_route++
            textTotalDistance.setText(distance.toString()+"km")
            textTotalTime.setText(time.toString()+"???")
            //polyline=
        }
    }
    override fun onBackPressed() {
        //super.onBackPressed()
    }
    fun startProcess() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.deliverymapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    fun access_database(id: String, check: Int) {
        db.collection("customers").whereEqualTo("id", customer_id)
            .get().addOnSuccessListener { doc ->
                for (index in doc) {
                    customer_phone = index.data["phone"].toString()
                    customer_library = index.data["selectLib"].toString()
                }
                db.collection("Orders").whereEqualTo("customerid",customer_id)
                    .get().addOnSuccessListener {doc ->
                        for (index in doc) {
                            db.collection("Orders").document(index.id).update("deliveryid",delivery_id)
                        }
                    }
                try {
                    val sms = SmsManager.getDefault()

                    val phones = customer_phone

                    if (check == 1) {
                        sms.sendTextMessage(phones, null, "??????????????????.", null, null);
                    } else if (check == 2) {
                        sms.sendTextMessage(phones, null, "???????????? 3??? ??? ?????????.", null, null);
                    }
                    Toast.makeText(getApplicationContext(), "?????? ??????!", Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    Toast.makeText(getApplicationContext(), "?????? ??????!", Toast.LENGTH_LONG).show()
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace();
                }
            }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("?????????", "onMapReady")
        mMap = googleMap
        mMap.addMarker(MarkerOptions().position(customerpoint).title(customer_id).icon(BitmapDescriptorFactory.fromResource(R.drawable.userimg)))
        mMap.addMarker(MarkerOptions().position(locpoint).title(customer_library).icon(BitmapDescriptorFactory.fromResource(R.drawable.libraryimg)))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        updateLoaction()


        thread(start=true) {
            Thread.sleep(2000)
            while (true) {
                if(startthread) {

                    val polylineOptions2=PolylineOptions().width(5f).color(Color.RED).clickable(true)
                    latLngArrayList.clear()
                    Log.d("?????????", "????????? ??????")
                    val urlString = setUri()
                    val values: ContentValues? = null
                    var result: String = ""
                    val relayTmapAPI = RelayTmapAPI()
                    result = relayTmapAPI.request(urlString, values).toString()
                    Log.d("?????????", "tmap ?????? ??? ??????")
                    val root = JSONObject(result)
                    //??? ?????? ?????? featuresArray??? ??????
                    val featuresArray = root.getJSONArray("features")
                    Log.d("?????????", "featuresArray ??????" + featuresArray.length())

                    for (i in 0 until featuresArray.length()) {

                        val featuresIndex = featuresArray[i] as JSONObject
                        if(i==0){
                            val property=featuresIndex.getJSONObject("properties")
                            distance=property.getDouble("totalDistance")/1000.0
                            distance= (round(distance*100).toDouble())/100.0
                            time=property.getInt("totalTime")/60
                            Log.d("????????? ??????",distance.toString()+" "+time.toString())
//                            textTotalTime.setText(time.toString()+"???")
//                            textTotalDistance.setText(distance.toString()+"km")
                        }
                        val geometry = featuresIndex.getJSONObject("geometry")
                        val type = geometry.getString("type")
                        if (type == "LineString") {
                            val coordinatesArray = geometry.getJSONArray("coordinates")
                            for (j in 0 until coordinatesArray.length()) {
                                val pointArray = coordinatesArray[j] as JSONArray
                                val longitude = pointArray[0].toString().toDouble()
                                val latitude = pointArray[1].toString().toDouble()
                                latLngArrayList.add(
                                    com.google.android.gms.maps.model.LatLng(
                                        latitude,
                                        longitude
                                    )
                                )
                                Log.d("?????????", "?????? :" + longitude + " " + latitude)
                            }
                        }
                    }
                    var getTimeAndMeter=GetTimeAndMeter()
                    getTimeAndMeter.requsetInfo(customerpoint,locpoint)
                    polylineOptions2.addAll(latLngArrayList)

                    Thread.sleep(1000)
                    runOnUiThread {
                        //????????? ?????? ????????? ?????? ??????
                        Log.d("?????????", "???????????? ?????? ???")
                        polylineOptions=polylineOptions2
                        btnUpdateroad.performClick()
                        firststart=false
                    }
                    Thread.sleep(2000)
                    Log.d("?????????", "???????????? ??????")
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun updateLoaction() {
        Log.d("?????????","updateLoaction()"+" ?????????: "+delivery_id)
        val locationRequest = com.google.android.gms.location.LocationRequest.create()
        locationRequest.run {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for ((i, location) in it.locations.withIndex()) {
                        setLastLocation(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }
    fun setLastLocation(lastLocation: Location) {
        Log.d("?????????","setLastLocation")
        Log.d("?????????","${delivery_id}")


        db.collection("DeliveryMan").whereEqualTo("id",delivery_id).get().addOnSuccessListener { doc->
            for(index in doc){
                if (count != 0) maker.remove()
                val LatLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
                deliverypoint=LatLNG
                val markerOptions = MarkerOptions().position(LatLNG).title("??? ??????")
                val cameraPosition = CameraPosition.builder().target(LatLNG).zoom(15.0f).build()
                db.collection("DeliveryMan").document(index.id).update(mapOf(
                    "xcnts" to deliverypoint.latitude,
                    "ydnts" to deliverypoint.longitude
                ))
                maker = mMap.addMarker(markerOptions)
                maker.rotation
                maker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.deliveryimg))
                //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(LatLNG))
                deliverypoint = LatLNG
                    //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                val initLatLNG = LatLNG
                val initcameraPosition = CameraPosition.builder().target(initLatLNG).zoom(20.0f).build()
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(initcameraPosition))
                Log.d("?????????","?????? ??????")
                count++
                startthread=true
            }
        }
    }
    fun setUri(): String {
        val startName=URLEncoder.encode("?????????","UTF-8")
        val endName=URLEncoder.encode("?????????","UTF-8")
        val urlString =
            "https://apis.openapi.sk.com/tmap/routes?version=1&format=json&callback=result&appKey=l7xx7c351c7720cb4c108b5c3f5cd5538575+&startX=" +
                    customerpoint.longitude+"&startY="+ customerpoint.latitude + "&endX=" + deliverypoint.longitude + "&endY=" + deliverypoint.latitude
        //+"&startName="+startName+"&endName="+endName
        Log.d("TmapApi",urlString)
        return urlString
    }
    /////////////////////////
    val permissions =arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION)
    val PERM_LOCATION=99
    fun checkPermission(){
        var permitted_all=true
        for(permission in permissions){
            val result=ContextCompat.checkSelfPermission(this,permission)
            if(result!=PackageManager.PERMISSION_GRANTED){
                permitted_all=false
                requestPermissions()
                break
            }
        }
        if(permitted_all){
            startProcess()
        }
    }
    fun requestPermissions(){
        ActivityCompat.requestPermissions(this,permissions,PERM_LOCATION)
    }

    fun confrimAgain(){
        AlertDialog.Builder(this)
            .setTitle("?????? ??????")
            .setMessage("?????? ????????? ??????????????? ?????? ????????? ???????????????.")
            .setPositiveButton("?????????",{_,_->
                requestPermissions()
            }).setNegativeButton("?????????",{_,_->
                finish()
            })
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            99->{
                var granted_all=true
                for(result in grantResults){
                    if(result!=PackageManager.PERMISSION_GRANTED){
                        granted_all=false
                        break
                    }
                }
                if(granted_all){
                    startProcess()
                }
                else{
                    confrimAgain()
                }
            }
        }
    }
}


