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
import com.example.basichilt.module.contract.Contract
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Contract.View {

    @Inject lateinit var presenter: Contract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 注册自己为 View
        presenter.attach(this)

        findViewById<Button>(R.id.button).setOnClickListener {
            presenter.onButtonClicked()
        }
    }

    // 这里实现 Contract.View 的方法
    override fun showGreeting(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }
}


//@AndroidEntryPoint
//class MainActivity : AppCompatActivity() {
//
//    @Inject
//    lateinit var btImplement: BasicImplement
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//
//        val btn: Button = findViewById(R.id.button)
//        btn.setOnClickListener {
//            Toast.makeText(this, btImplement.sayHello(), Toast.LENGTH_SHORT).show()
//        }
//
//    }
//}