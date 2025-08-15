package com.example.basichilt.module

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.basichilt.BuildConfig
import com.example.basichilt.R
import com.example.basichilt.module.activity.WavefromActivity
import com.example.basichilt.module.basicFun.BasicImplement
import com.example.basichilt.module.basicFun.BasicInterface
import com.example.basichilt.module.ble.BtManager
import com.example.basichilt.module.contract.Contract
import com.example.basichilt.retrofit.RetrofitActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Contract.View {

    @Inject lateinit var presenter: Contract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("version name: ${BuildConfig.VERSION_NAME}")
        presenter.attach(this)

        findViewById<Button>(R.id.button).setOnClickListener {
            presenter.onButtonClicked()
        }

        findViewById<Button>(R.id.blueToothConnection).setOnClickListener{
            Toast.makeText(this@MainActivity, "blueToothConnect", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.retrofit_btn).setOnClickListener{
            Toast.makeText(this@MainActivity, "retro", Toast.LENGTH_SHORT).show()
            val intent : Intent = Intent(this@MainActivity, RetrofitActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.waveDemo_btn).setOnClickListener{
            val intent: Intent = Intent(this@MainActivity, WavefromActivity::class.java)
            startActivity(intent)
        }
    }



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