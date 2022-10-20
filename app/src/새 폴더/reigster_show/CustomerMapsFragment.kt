package com.example.reigster_show

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_deliveryman_showmap.*
import kotlinx.android.synthetic.main.activity_detail_orderpage.*
import kotlinx.android.synthetic.main.activity_select_lib_loc.*
import kotlinx.android.synthetic.main.fragment_customemap.*
import kotlinx.android.synthetic.main.fragment_customemap.view.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class CustomerMapsFragment : Fragment(), OnMapReadyCallback {
    var count: Int = 0
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var db2: FirebaseFirestore = FirebaseFirestore.getInstance()
    var polylineOptions = PolylineOptions().width(5f).color(Color.RED).clickable(true)
    var show_check = 0
    lateinit var customerpoint: LatLng
    lateinit var deliverypoint: LatLng
    private lateinit var mMap: GoogleMap
    private lateinit var mMap2: GoogleMap
    var latLngArrayList = ArrayList<com.google.android.gms.maps.model.LatLng>()
    lateinit var maker: Marker
    lateinit var polyline: Polyline
    lateinit var customview: View
    var distance: Double = 0.0
    var time: Int = 0
    var startthread = false
    var paintroute= false
    var starthandler= false
    var tcount: Int=0
    lateinit var mContext: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val customerview = inflater.inflate(R.layout.fragment_customemap, container, false)
        //var mapView = customerview.findViewById(R.id.customermapView) as MapView
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.customermapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        customerview.fc_btn_refresh.setOnClickListener {
            Log.d("프레그먼트", "경로 최신화 버튼 클릭")
            mMap.addPolyline(polylineOptions)
//            textTotalDistance.setText(distance.toString()+"km")
//            textTotalTime.setText(time.toString()+"분")
            //polyline=

        }
       val handler=object:Handler(){
            override fun handleMessage(msg: Message) {
                db.collection("customers").whereEqualTo("id", "test").get()
                    .addOnSuccessListener { doc ->
                        for (index in doc) {
                            customerpoint = LatLng(
                                index.data["xcnts"].toString().toDouble(),
                                index.data["ydnts"].toString().toDouble()
                            )
                            Log.d("프레그먼트", "customerpoint: " + customerpoint)

                            db.collection("DeliveryMan").whereEqualTo("id", "delivery").get()
                                .addOnSuccessListener { doc ->
                                    for (index in doc) {
                                        Log.d("프레그먼트", "deliverypoint x: " + index.data["xcnts"].toString().toDouble(),)
                                        Log.d("프레그먼트", "deliverypoint y: " + index.data["ydnts"].toString().toDouble(),)
                                        deliverypoint = LatLng(index.data["xcnts"].toString().toDouble(), index.data["ydnts"].toString().toDouble())
                                        Log.d(" 프레그먼트","startthread 트루")
                                    }
                                    mMap.addMarker(MarkerOptions().position(customerpoint).title("나").icon(BitmapDescriptorFactory.fromResource(R.drawable.userimg)))
                                    mMap.addMarker(MarkerOptions().position(deliverypoint).title("배달원").icon(BitmapDescriptorFactory.fromResource(R.drawable.deliveryimg)))
                                    val initLatLNG = customerpoint
                                    val initcameraPosition = CameraPosition.builder().target(initLatLNG).zoom(18.0f).build()
                                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(initcameraPosition))
                                    starthandler=true
                                    removeMessages(0)
                                }
                        }

                    }

            }
        }
        thread(start=true){
            while(true) {
                if (startthread) {
                    startthread=false
                    if(tcount>=1) {
                        handler?.sendEmptyMessage(0)
                        Thread.sleep(1000)
                        while(true) {
                            if(starthandler) {
                                Log.d("프레그먼트", "drawroute 실행")
                                drawroute(customerpoint, deliverypoint)
                                break;
                            }
                        }

                    }
                }
                if(paintroute){
                    Thread.sleep(1000)
                    requireActivity()!!.runOnUiThread {
                        Log.d("프레그먼트", "fc_btn_refresh.performClick() 실행")
                        customerview.fc_btn_refresh.performClick()
                        paintroute=false
                        Thread.sleep(100)
                    }
                }
                Thread.sleep(1000)
            }
        }
        Log.d("프레그먼트", "CustomerMapsFragment의 creativeView")
        //mapView.getMapAsync(this)

        return customerview
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onMapReady(googleMap: GoogleMap) {
//        MapsInitializer.initialize(this.getActivity());
        Log.d(" 프레그먼트","onMapReady")
        mMap=googleMap
        tcount++
        db.collection("Orders").whereEqualTo("customerid", "test")
            .get().addOnSuccessListener { doc ->
                Log.d("프레그먼트", "CustomerMapsFragment의 db1시작")
                for (index in doc) {
                    Log.d("프레그먼트", "CustomerMapsFragment의 db1 반복문 도는 중")
                    if (index.data["orderCheck"].toString().toBoolean() == true && !index.data["deliveryid"].toString().equals("default")){
                        startthread=true
                        //drawroute(customerpoint,deliverypoint)
                    }

                }
            }
    }
    fun getMapInfo() {
            if (startthread) {
                db.collection("Orders").whereEqualTo("customerid", "test")
                    .get().addOnSuccessListener { doc ->
                        Log.d("프레그먼트", "CustomerMapsFragment의 db1시작")
                        for (index in doc) {
                            Log.d("프레그먼트", "CustomerMapsFragment의 db1 반복문 도는 중")
                            if (index.data["orderCheck"].toString().toBoolean() == true && !index.data["deliveryid"].toString().equals("default")){
                                startthread = false
                                getPoint()
                                Thread.sleep(3000)

                            }
                        }

                    }
            }
    }
    fun getPoint(){
        db.collection("customers").whereEqualTo("id", "test").get()
            .addOnSuccessListener { doc ->
                for (index in doc) {
                    customerpoint = LatLng(
                        index.data["xcnts"].toString().toDouble(),
                        index.data["ydnts"].toString().toDouble()
                    )
                    Log.d("프레그먼트", "customerpoint: " + customerpoint)

                    db.collection("DeliveryMan").whereEqualTo("id", "delivery").get()
                        .addOnSuccessListener { doc ->
                            for (index in doc) {
                                Log.d("프레그먼트", "deliverypoint x: " + index.data["xcnts"].toString().toDouble(),)
                                Log.d("프레그먼트", "deliverypoint y: " + index.data["ydnts"].toString().toDouble(),)
                                deliverypoint = LatLng(index.data["xcnts"].toString().toDouble(), index.data["ydnts"].toString().toDouble())
                                Log.d(" 프레그먼트","startthread 트루")
                                startthread=false
                            }
                            mMap.addMarker(MarkerOptions().position(customerpoint).title("나").icon(BitmapDescriptorFactory.fromResource(R.drawable.userimg)))
                            mMap.addMarker(MarkerOptions().position(deliverypoint).title("배달원").icon(BitmapDescriptorFactory.fromResource(R.drawable.libraryimg)))
                            val initLatLNG = customerpoint
                            val initcameraPosition = CameraPosition.builder().target(initLatLNG).zoom(13.0f).build()
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(initcameraPosition))
                        }
                }
            }
    }
    fun drawroute(cp: LatLng, dp: LatLng) {
        Log.d("프레그먼트", "CustomerMapsFragment의 drawroute시작")
        val polylineOptions2 =
            PolylineOptions().width(5f).color(Color.RED).clickable(true)
        latLngArrayList.clear()
        Log.d("메시지", "스레드 실행")
        val urlString =
            "https://apis.openapi.sk.com/tmap/routes?version=1&format=json&callback=result&appKey=l7xx7c351c7720cb4c108b5c3f5cd5538575+&startX=" +
                    cp.longitude + "&startY=" + cp.latitude + "&endX=" + dp.longitude + "&endY=" + dp.latitude
        Log.d("프레그먼트",urlString)
        val values: ContentValues? = null
        var result: String = ""
        val relayTmapAPI = RelayTmapAPI()
        result = relayTmapAPI.request(urlString, values).toString()
        Log.d("메시지", "tmap 결과 값 저장")
        val root = JSONObject(result)
        //총 경로 횟수 featuresArray에 저장
        val featuresArray = root.getJSONArray("features")
        Log.d("메시지", "featuresArray 길이" + featuresArray.length())
        for (i in 0 until featuresArray.length()) {
            val featuresIndex = featuresArray[i] as JSONObject
            if (i == 0) {
                val property = featuresIndex.getJSONObject("properties")
                distance = property.getDouble("totalDistance") / 1000.0
                distance = Math.round(distance).toDouble()
                time = property.getInt("totalTime") / 60
                Log.d("거리와 시간", distance.toString() + " " + time.toString())
//                            textTotalTime.setText(time.toString()+"분")
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
                    Log.d("메시지", "좌표 :" + longitude + " " + latitude)
                }
            }
        }
        polylineOptions2.addAll(latLngArrayList)
        //var getTimeAndMeter = GetTimeAndMeter()
        //getTimeAndMeter.requsetInfo(customerpoint, deliverypoint)
            polylineOptions=polylineOptions2
            paintroute=true
        Log.d("메시지", "경로정보 저장")
    }
}
