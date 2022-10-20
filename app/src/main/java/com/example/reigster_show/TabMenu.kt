package com.example.reigster_show

import android.content.ContentValues
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
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

class TabMenu : AppCompatActivity(){
    val binding by lazy { ActivityTabmenuBinding.inflate(layoutInflater) }
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var db2: FirebaseFirestore = FirebaseFirestore.getInstance()
    var polylineOptions = PolylineOptions().width(5f).color(Color.RED).clickable(true)
    var show_check = 0
    lateinit var customerpoint: LatLng
    lateinit var deliverypoint: LatLng
    var customerid=""
    var startthread = true
    var distance: Double = 0.0
    var time: Int = 0
    var latLngArrayList = ArrayList<com.google.android.gms.maps.model.LatLng>()
    var threadstart=true
    var check=2
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        var fragment1=RentalList()
        var fragment2=CustomerMapsFragment()
        var fragment3=EmptyOrderFragment()
        var list=listOf(fragment1, EmptyOrderFragment() , RentalList())
        val list1 = listOf(fragment1, fragment2, RentalList())
        if(intent.hasExtra("id")){
          customerid=intent.getStringExtra("id").toString()
            Log.d("메시지",customerid)
        }

        val titles = listOf("대여", "배달현황", "마이페이지")
        //쓰레드로 돌려
        val pagerAdapter = FragmentPagerAdapter(list, this)
        val args=Bundle()
        args.putString("customer_id",customerid)
        val args2=Bundle()
        args2.putString("customer_id",customerid)
        fragment1.arguments=args
        fragment2.arguments=args2

        binding.viewpager.adapter = pagerAdapter
                TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
                    tab.text = titles.get(position)
                }.attach()

            tabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if(tab.position==0){
                        fragment1.arguments=args
                        fragment2.arguments=args2

                    }
                    if(tab.position==1){
                        fragment1.arguments=args
                        fragment2.arguments=args2
                        db.collection("Orders").whereEqualTo("customerid",customerid )
                                    .get().addOnSuccessListener { doc ->
                                        Log.d("프레그먼트", "CustomerMapsFragment의 db1시작")
                                for (index in doc) {
                                    if(index.data["orderCheck"].toString().toBoolean() && !index.data["ordercomplete"].toString().toBoolean()){
                                        Log.d("프레그먼트", "분기점1")
                                        if(check!=1) {
                                            val pagerAdapter =
                                                FragmentPagerAdapter(list1, this@TabMenu)
                                            binding.viewpager.adapter = pagerAdapter
                                            Toast.makeText(getApplicationContext(), "배달중이에요.", Toast.LENGTH_SHORT).show()
                                        }
                                        check=1

                                    }
                                    else if(index.data["orderCheck"].toString().toBoolean() && index.data["ordercomplete"].toString().toBoolean()){
                                        Log.d("프레그먼트", "분기점2")
                                        if(check!=2) {

                                            val pagerAdapter =
                                                FragmentPagerAdapter(list, this@TabMenu)
                                            binding.viewpager.adapter = pagerAdapter
                                            Toast.makeText(getApplicationContext(), "배달현황이 없어요!!", Toast.LENGTH_SHORT).show()
                                        }
                                        check=2
                                    }
                                    else if(!index.data["orderCheck"].toString().toBoolean()){
                                        Log.d("프레그먼트", "분기점3")
                                        if(check!=2) {

                                                FragmentPagerAdapter(list, this@TabMenu)
                                            binding.viewpager.adapter = pagerAdapter
                                            Toast.makeText(getApplicationContext(), "배달현황이 없어요!!", Toast.LENGTH_SHORT).show()
                                        }
                                        check=2
                                    }
                                }
                            }
                    }

                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {


                }

            })
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