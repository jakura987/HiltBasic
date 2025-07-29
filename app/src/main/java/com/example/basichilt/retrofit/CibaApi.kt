package com.example.basichilt.retrofit

import com.example.basichilt.model.Translation
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST


interface CibaApi {
    @FormUrlEncoded
    @POST("ajax.php")
    fun getTranslation(
        @Field("a") action: String,
        @Field("f") from:   String,
        @Field("t") to:     String,
        @Field("w") word:   String
    ): Observable<Translation>


    @GET("ajax.php?a=fy&f=auto&t=auto&w=hi%20world")
    fun getCall(): Observable<Translation>
}

