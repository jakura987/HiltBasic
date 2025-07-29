package com.example.basichilt.model

import com.google.gson.annotations.SerializedName

// 根对象
data class Translation(
    val status: Int,
    val content: Content
) {
    data class Content(
        val from: String,
        val to: String,
        val vendor: String,
        val out: String,
        @SerializedName("err_no") val errNo: Int    // 这里改成 "err_no"
    )

    // 定义 输出返回数据 的方法
    fun show() {
        println("Rxjava翻译结果：$status")
        println("Rxjava翻译结果：${content.from}")
        println("Rxjava翻译结果：${content.to}")
        println("Rxjava翻译结果：${content.vendor}")
        println("Rxjava翻译结果：${content.out}")
        println("Rxjava翻译结果：${content.errNo}")
    }
}
