package com.example.basichilt.retrofit

import com.example.basichilt.model.TranslateRequest
import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LibreTranslateApi {
    @POST("translate")
    fun translate(
        @Body body: TranslateRequest
    ): Observable<LibreTranslateResponse>
}

data class LibreTranslateResponse(
    @SerializedName("translatedText")
    val translatedText: String,

    @SerializedName("detectedLanguage")
    val detectedLanguage: DetectedLanguage? = null,

    @SerializedName("alternatives")
    val alternatives: List<String>? = null
)

data class DetectedLanguage(
    val confidence: Int,
    val language: String
)
