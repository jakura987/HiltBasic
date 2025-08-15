package com.example.basichilt.retrofit

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryApi {
    /**
     * q：要翻译的文本
     * langpair：源语言|目标语言，比如 "en|zh-CN"
     **/
    @GET("get")
    fun translate(
        @Query("q") q: String,
        @Query("langpair") langpair: String
    ): Observable<MyMemoryResponse>
}


data class MyMemoryResponse(
    @SerializedName("responseData")
    val responseData: ResponseData,
    @SerializedName("responseStatus")
    val responseStatus: Int
)

data class ResponseData(
    @SerializedName("translatedText")
    val translatedText: String,
    @SerializedName("match")
    val match: Float
)