package com.example.basichilt.retrofit

import com.example.basichilt.model.Translation
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface GetRequestInterface {
    @GET("ajax.php")
    fun getTranslation(
        @Query("a") action: String = "fy",
        @Query("f") from: String = "auto",
        @Query("t") to: String = "auto",
        @Query("w") word: String
    ): Observable<Translation>
}
