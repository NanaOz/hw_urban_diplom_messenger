package com.example.hw_urban_diplom_messenger.push

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Header

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("fcm/send")
    fun sendNotification(@Header("Authorization") bearerToken: String, @Body notification: Map<String, Any>): Call<ResponseBody>

}
