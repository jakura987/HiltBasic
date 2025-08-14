package com.example.basichilt.module.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

        // —— 复现“原版偶尔没图”的核心：未 attach 就订阅（绑定 ViewScope）——
        relay
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(ViewScopeProvider.from(tv))
            .subscribe({ value ->
                tv.text = value
            }, { e ->
                if (e is com.uber.autodispose.OutsideScopeException) {
                    Timber.tag("demo").w(e, "OutsideScope — view 未 attach，已忽略")
                } else {
                    Timber.tag("demo").e(e, "stream error")
                }
            })


        // 立刻发首帧：由于上面订阅可能已被立即 dispose（attached=false），这条会被丢弃
        relay.accept("FIRST @ ${System.currentTimeMillis()}")
    }


    private fun subscribeCommon() {
        relay
            .observeOn(AndroidSchedulers.mainThread())
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


