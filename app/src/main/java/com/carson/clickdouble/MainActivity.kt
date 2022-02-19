package com.carson.clickdouble

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by yujunyao on 1/19/22.
 */
class MainActivity: FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button1.setOnClickListener {
            startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }
        button2.setOnClickListener {

        }
//        while (true) {
//            Thread(Runnable {
//                Thread.sleep(10_000_000_000)
//            }).start()
//        }
    }

}