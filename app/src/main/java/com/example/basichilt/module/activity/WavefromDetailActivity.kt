package com.example.basichilt.module.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.basichilt.R
import com.jakewharton.rxrelay2.PublishRelay
import com.uber.autodispose.android.ViewScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import com.uber.autodispose.autoDispose

class WavefromDetailActivity : AppCompatActivity() {

    private lateinit var tv: TextView
    private val relay = PublishRelay.create<String>() // 不回放

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Timber.forest().isEmpty()) Timber.plant(Timber.DebugTree())
        setContentView(R.layout.activity_wavefrom_detail)

        tv = findViewById(R.id.tvValue)

        val buggy = intent.getBooleanExtra("buggy", true)

        // 进入页面就演示一次
        if (buggy) {
            subscribeBuggy()                 // ❌ 未 attach 就订阅（高概率 attached=false）
            relay.accept("FIRST @ ${System.currentTimeMillis()}") // 这条会丢
        } else {
            tv.post {                        // ✅ attach 之后再订阅+发射
                subscribeFixed()
                relay.accept("FIRST @ ${System.currentTimeMillis()}")
            }
        }

        findViewById<Button>(R.id.btnBuggyOnce).setOnClickListener {
            subscribeBuggy()
            relay.accept("BUGGY @ ${System.currentTimeMillis()}") // 大概率丢
        }
        findViewById<Button>(R.id.btnFixedOnce).setOnClickListener {
            tv.post {
                subscribeFixed()
                relay.accept("FIXED @ ${System.currentTimeMillis()}") // 一定能到
            }
        }
        findViewById<Button>(R.id.btnEmit).setOnClickListener {
            relay.accept("EMIT @ ${System.currentTimeMillis()}")
        }
    }

    // ❌ 演示：未 attach 就订阅，AutoDispose 会立即 dispose
    private fun subscribeBuggy() {
        relay
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                Timber.tag("demo").d("SUBSCRIBE attached=%s", tv.isAttachedToWindow)
            }
            .doOnDispose {
                Timber.tag("demo").d("DISPOSED (maybe not attached)")
            }
            .autoDispose(ViewScopeProvider.from(tv))
            .subscribe { value ->
                tv.text = value
                Timber.tag("demo").d("RECEIVED: %s", value)
            }
    }

    // ✅ 正确：等 attach 后订阅（通过 post 确保 isAttachedToWindow=true）
    private fun subscribeFixed() {
        relay
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                Timber.tag("demo").d("SUBSCRIBE attached=%s", tv.isAttachedToWindow)
            }
            .doOnDispose {
                Timber.tag("demo").d("DISPOSED")
            }
            .autoDispose(ViewScopeProvider.from(tv))
            .subscribe { value ->
                tv.text = value
                Timber.tag("demo").d("RECEIVED: %s", value)
            }
    }
}


