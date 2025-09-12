package com.example.basichilt.retrofit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.basichilt.R
import com.example.basichilt.module.MainActivity
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber


class RetrofitActivity : AppCompatActivity() {
    private val disposables = CompositeDisposable()

    @SuppressLint("AutoDispose")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_retrofit)

        val findViewById: Button = findViewById<Button>(R.id.back)
        findViewById.setOnClickListener {
            startActivity(Intent(this@RetrofitActivity, MainActivity::class.java))
        }

        val gson = GsonBuilder().setLenient().create()

        // 1) 实例化一个 Logging 拦截器，并设置打印级别
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // 打印请求／响应的头和体
        }

        // 2) 用它来创建 OkHttpClient
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()


        // 2) Retrofit 实例
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")  // MyMemory 基础 URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()


        // step5：创建 网络请求接口 的实例
        val api = retrofit.create(MyMemoryApi::class.java)

        // 发起翻译请求
        val observable: Observable<MyMemoryResponse> = api.translate(
            q = "大熊猫",
            langpair = "zh-CN|en"
        )


        val disposable: Disposable = observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resp ->
                if (resp.responseStatus == 200) {
                    val txt = resp.responseData.translatedText
                    Timber.tag("MyMemory").d("result: $txt")
                    Toast.makeText(this@RetrofitActivity, "$txt", Toast.LENGTH_SHORT).show()
                } else {
                    Timber.e("错误${resp.responseStatus}")
                }


            }, { err ->
                Timber.tag("RetrofitActivity").e(err, "Error fetching translation")
            }
            )

        disposables.addAll(disposable)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
