package com.example.reigster_show


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_intro.*
import kotlin.concurrent.thread


class Intro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        textIntros.setText("도서배달")
        textLoading.setText("로딩 중.......")
        val handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                    textLoading.setText("로딩 완료!")
                     startLogin()
              finish()
            }
        }
            thread(start=true)
            {
                Thread.sleep(1000)
                handler?.sendEmptyMessage(0)
            }
        }

    override fun onPause() {
        super.onPause()
        finish()
    }
    fun startLogin(){
        val start_intent=Intent(this,login::class.java)
        startActivity(start_intent)
    }
}