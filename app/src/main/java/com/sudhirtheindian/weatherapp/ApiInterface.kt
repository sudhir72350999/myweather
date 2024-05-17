package com.sudhirtheindian.weatherapp
import com.sudhirtheindian.weatherapp.data.citydata.WeatherApps
import com.sudhirtheindian.weatherapp.data.forecastModels.Forecast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiInterface {
//    GET request to a weather API endpoint.
    @GET("weather")
    fun getWeatherData(
        @Query("q") city: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Call<WeatherApps>

    @GET("forecast")
     fun getForecast(
        @Query ("q") city: String,
        @Query("appid") apiKey : String,
        @Query("units") units : String
    ) :Call<Forecast>



}
