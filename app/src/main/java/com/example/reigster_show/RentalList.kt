package com.example.reigster_show

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detail_orderpage.*
import kotlinx.android.synthetic.main.fragment_rental_list.*
import kotlinx.android.synthetic.main.bookitem.view.*
import kotlinx.android.synthetic.main.fragment_rental_list.view.*


class RentalList : Fragment() {

    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var customer_id=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rental_list, container, false)
        val adpater = BookListAdapter()
        customer_id= arguments?.getString("customer_id").toString()
        Log.d("아이디",customer_id+"!")
        view.recyclerView.adapter=adpater
        view.recyclerView.layoutManager=  LinearLayoutManager(context)
        // 검색 옵션 변수
        var searchOption = "name"
        view.purchase_btn.setOnClickListener {
            firestore.collection("customers").whereEqualTo("id",customer_id).get()
                .addOnSuccessListener {doc ->
                    for(index in doc){
                        val data= hashMapOf(
                            "customerid" to customer_id,
                            "deliveryid" to "default",
                            "orderCheck" to false,
                            "orderaddress" to index.data["address"],
                            "ordercomplete" to false,
                            "orderlib" to index.data["selectLib"]
                        )
                        firestore.collection("Orders").add(data)
                    }
                }


        }
/////////////////////////////////////////////////////////////////////////////////////////
        // 스피너 옵션에 따른 동작
        view.bookspinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                views : View?,
                position: Int,
                id: Long
            ) {
                if (view != null) {
                    when (view.bookspinner.getItemAtPosition(position)) {
                        "책제목" -> {
                            searchOption = "name"
                        }
                        "출판사" -> {
                            searchOption = "publisher"
                        }
                    }
                }
            }
        }

/////////////////////////////////////////////////////////////////////////////////////////
        // 검색 옵션에 따라 검색
        view.searchBtn.setOnClickListener {
            (recyclerView.adapter as BookListAdapter).search(
                searchWord.text.toString(),
                searchOption
            )
        }

        return view

    }
}
class BookListAdapter : RecyclerView.Adapter<BookListAdapter.BookHolder>() {
    // Book 클래스 ArrayList 생성성
    var BookList : ArrayList<Book> = arrayListOf()
    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    init {
        firestore.collection("BookList").get().addOnSuccessListener { result ->
            BookList.clear()
            Log.d("메시지", "db 불러오는 중")
            for (index in result) {
                Log.d("메시지", "db 반복문 시작")
                if (!index.data["orderCheck"].toString().toBoolean()) {
                    var selectOrders = index.toObject(Book::class.java)
                    BookList.add(selectOrders)
                }
            }
            notifyDataSetChanged()
        }
    }

    var clickBookList = ArrayList<String>()
    var distinctData = clickBookList.distinct()

    // xml파일을 inflate하여 ViewHolder를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.bookitem, parent, false)
        //return BookHolder(view)
        return BookHolder(view).apply {

            itemView.setOnClickListener {
                val curPos : Int = adapterPosition
                val profile: Book = BookList.get(curPos)

                clickBookList.add(profile.name.toString())


                distinctData = clickBookList.distinct()
                if(clickBookList.size == distinctData.size){
                    firestore?.collection("Basket")
                        ?.add(profile)

                    Toast.makeText(parent.context, "책제목: ${profile.name}\n출판사: ${profile.publisher}\n장바구니에 담았습니다!!", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(parent.context, "책제목: ${profile.name}\n출판사: ${profile.publisher}\n장바구니에 이미 담았습니다!!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        var viewHolder = (holder as BookHolder).itemView

        viewHolder.name.text = BookList[position].name
        viewHolder.publisher.text = BookList[position].publisher
    }


    // 리사이클러뷰의 아이템 총 개수 반환
    override fun getItemCount(): Int {
        return BookList.size
    }
    class BookHolder(itmview: View) : RecyclerView.ViewHolder(itmview) {
    }

    // 파이어스토어에서 데이터를 불러와서 검색어가 있는지 판단
    fun search(searchWord : String, option : String) {
        firestore?.collection("BookList")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            // ArrayList 비워줌
            BookList.clear()

            for (snapshot in querySnapshot!!.documents) {
                if (snapshot.getString(option)!!.contains(searchWord)) {
                    var item = snapshot.toObject(Book::class.java)
                    BookList.add(item!!)
                }
            }
            notifyDataSetChanged()
        }
    }


}

