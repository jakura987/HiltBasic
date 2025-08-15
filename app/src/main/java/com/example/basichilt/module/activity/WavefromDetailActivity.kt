package com.example.basichilt.module.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.basichilt.R
import com.example.basichilt.module.ble.BtManager
import com.jakewharton.rxrelay2.PublishRelay
import com.uber.autodispose.android.ViewScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import com.uber.autodispose.autoDispose

class WavefromDetailActivity : AppCompatActivity() {

    private lateinit var tv: TextView
    private val relay = PublishRelay.create<String>() // 不回放（与原版一致）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Timber.forest().isEmpty()) Timber.plant(Timber.DebugTree())
        setContentView(R.layout.activity_wavefrom_detail)

        findViewById<Button>(R.id.wavefrom_back).setOnClickListener {
            startActivity((Intent(this@WavefromDetailActivity, WavefromActivity::class.java)))
        }

        tv = findViewById(R.id.tvValue)
        val fixed = intent.getBooleanExtra("fixed", false)


        if (fixed) {
            tv.post {
                subscribeCommon()// tv可能没被attact, 会执行autodispose，所以用post确保view attach
                relay.accept("FIRST (fixed) @ ${System.currentTimeMillis()}")
            }
        } else {
            subscribeCommon()
            relay.accept("FIRST (buggy) @ ${System.currentTimeMillis()}")
        }
    }


    //     relay
//    .observeOn(AndroidSchedulers.mainThread())   // A
//    .doOnSubscribe { … }                         // B 开始订阅后被调用 （记录）
//    .doOnDispose { … }                           // C 订阅被取消时调用 （记录）
//    .autoDispose(ViewScopeProvider.from(tv))     // D 自动绑定Rx链到某个Android生命周期 （避免在 UI 已销毁后还继续回调）（给订阅加保险）
//    .subscribe(onNext, onError)                  // E
    private fun subscribeCommon() {
        relay.observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                Timber.tag("demo").d("SUBSCRIBE attached=%s", tv.isAttachedToWindow)
            }
            .doOnDispose {
                Timber.tag("demo").d("DISPOSED (maybe not attached)")
            }
            .autoDispose(ViewScopeProvider.from(tv))
            .subscribe({ value ->
                tv.text = value
                Timber.tag("demo").d("RECEIVED: %s", value)
            }, { e ->
                // 防止 OnErrorNotImplementedException 崩溃
                Timber.tag("demo").w(e, "stream error (可能是 OutsideScope: 未 attach)")
            })
    }

}


