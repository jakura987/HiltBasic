package com.example.basichilt.module

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.basichilt.R
import com.example.basichilt.module.basicFun.BasicImplement
import com.example.basichilt.module.basicFun.BasicInterface
import com.example.basichilt.module.ble.BtManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var btImplement: BasicImplement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.button)
        btn.setOnClickListener {
            Toast.makeText(this, btImplement.sayHello(), Toast.LENGTH_SHORT).show()
        }

    }
}