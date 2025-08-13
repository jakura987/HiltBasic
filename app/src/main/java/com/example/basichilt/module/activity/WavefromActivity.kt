package com.example.basichilt.module.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.basichilt.R
import com.example.basichilt.module.MainActivity

class WavefromActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wavefrom)

        findViewById<Button>(R.id.btn_buggy).setOnClickListener {
            startActivity(Intent(this, WavefromDetailActivity::class.java)
                .putExtra("buggy", true))
        }
        findViewById<Button>(R.id.btn_fixed).setOnClickListener {
            startActivity(Intent(this, WavefromDetailActivity::class.java)
                .putExtra("buggy", false))
        }
    }
}
