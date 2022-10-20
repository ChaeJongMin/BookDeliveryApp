package com.example.reigster_show

import android.content.ContentValues
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.example.reigster_show.databinding.ActivityTabmenuBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_tabmenu.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class TabMenu : AppCompatActivity() {
    val binding by lazy { ActivityTabmenuBinding.inflate(layoutInflater) }
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var db2: FirebaseFirestore = FirebaseFirestore.getInstance()
    var polylineOptions = PolylineOptions().width(5f).color(Color.RED).clickable(true)
    var show_check = 0
    lateinit var customerpoint: LatLng
    lateinit var deliverypoint: LatLng
    var startthread = true
    var distance: Double = 0.0
    var time: Int = 0
    var latLngArrayList = ArrayList<com.google.android.gms.maps.model.LatLng>()


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val list = listOf(RentalList(), CustomerMapsFragment(), RentalList())
        val pagerAdapter = FragmentPagerAdapter(list, this)
        binding.viewpager.adapter = pagerAdapter
        val titles = listOf("A", "B", "C")
        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = titles.get(position)
        }.attach()


    }

}
class FragmentPagerAdapter(val fragmentList: List<Fragment>, fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList.get(position)
    }
}