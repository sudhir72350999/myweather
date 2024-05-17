package com.sudhirtheindian.weatherapp.utils

import com.sudhirtheindian.weatherapp.ApiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    object RetrofitInstance {
//        Lazy initialization can be useful for performance optimization
        val api: ApiInterface by lazy {
            Retrofit.Builder()
                .baseUrl(Util.Base)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiInterface::class.java)
        }
    }
}