package com.example.basichilt.model

data class TranslateRequest(
    val q: String,
    val source: String,
    val target: String,
    val format: String = "text",
    val api_key: String = ""           // 空字符串即可免费使用
)