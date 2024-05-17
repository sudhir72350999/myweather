package com.sudhirtheindian.weatherapp.activities

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sudhirtheindian.weatherapp.ApiInterface
import com.sudhirtheindian.weatherapp.R
import com.sudhirtheindian.weatherapp.adapter.RvAdapter
import com.sudhirtheindian.weatherapp.data.citydata.WeatherApps
import com.sudhirtheindian.weatherapp.data.forecastModels.Forecast
import com.sudhirtheindian.weatherapp.data.forecastModels.ForecastData
import com.sudhirtheindian.weatherapp.databinding.ActivityMainBinding
import com.sudhirtheindian.weatherapp.databinding.BottomSheetLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.*

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_LOCATION = 100
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cityName: String = ""
    private lateinit var sheetLayoutBinding: BottomSheetLayoutBinding
    private lateinit var dialog: BottomSheetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        view binding enabled
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//      it  provides access to location-related services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        inflate() method of the BottomSheetLayoutBinding class
        sheetLayoutBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        dialog.setContentView(sheetLayoutBinding.root)

//   call method of fetchWeatherData
        fetchWeatherData(cityName)
        //   call method of  search the city name
        searchCity()

        binding.tvForecast.setOnClickListener {
            try {
//                open the bottomsheet dialog
                openDialog()
            } catch (e: Exception) {
                Toast.makeText(binding.tvForecast.context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the missing permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_LOCATION
            )
            return
        }
        // Request location updates
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations, this can be null.
                location?.let {
                    // Handle location here
                    val latitude = it.latitude
                    val longitude = it.longitude
                    // Get location name
                    val locationName = getLocationName(latitude, longitude)
                    // Update TextView with location name
                    binding.cityname.text = locationName

                    Log.d("LocationUpdate", "Latitude: $latitude, Longitude: $longitude")
                } ?: Log.e("LocationUpdate", "Last known location is null")
            }
            .addOnFailureListener { e ->
                Log.e("LocationUpdate", "Error getting last known location: ${e.message}")
            }
    }


    private fun openDialog() {
//        after opening dialog call the getforcast method
        getForecast()

             sheetLayoutBinding.rvForecast.apply {
            setHasFixedSize(true)
//                 show the data in grid layout horizontal view
            layoutManager = GridLayoutManager(this@MainActivity, 1, RecyclerView.HORIZONTAL, false)

        }

//         open the dialog with animation
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
    }


    private fun getForecast() {
        try {
            val retrofit = Retrofit.Builder()
//                it  is a converter factory for Retrofit
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .build()
                .create(ApiInterface::class.java)

            val response =
                retrofit.getForecast(cityName, "02f4e394b3a2c5551476d4a6c51af13e", "metric")
            response.enqueue(object : Callback<Forecast> {
                override fun onResponse(call: Call<Forecast>, response: Response<Forecast>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            val cityName = responseBody.city.name
                            binding.cityname.text = cityName
                            val forecastArray: ArrayList<ForecastData> =
                                ArrayList(responseBody.list)
                            val adapter = RvAdapter(forecastArray)
//         show the data in bottomsheet layout using adapter with city name
                            sheetLayoutBinding.rvForecast.adapter = adapter
                            sheetLayoutBinding.tvSheet.text = "Five days forecast in $cityName"
                        }
                    } else {
                        Log.e("Main", "Response not successful: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Forecast>, t: Throwable) {
                    Log.e("Main", "onFailure: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("Main", "Exception occurred: ${e.message}")
        }
    }

    private fun searchCity() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query != null) {
                    cityName = query
                }
//                after submit the city name call the fetchWeatherData method
                fetchWeatherData(cityName)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }


    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        // metric will convert this in string format
        val response =
            retrofit.getWeatherData(cityName, "02f4e394b3a2c5551476d4a6c51af13e", "metric")
        response.enqueue(object : Callback<WeatherApps> {
            override fun onResponse(call: Call<WeatherApps>, response: Response<WeatherApps>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min
                    val weatherDescription =
                        responseBody.weather.firstOrNull()?.description ?: "unknown"
                    val address = responseBody.name
                    val lat = responseBody.coord.lat
                    val lon = responseBody.coord.lon

                    binding.temperature.text = "$temperature °C"
                    binding.weather.text = condition
                    binding.maxtemp.text = "Max Temp: $maxTemp °C"
                    binding.mintemp.text = "Min Temp: $minTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.windspeed.text = "$windSpeed m/s"
                    binding.sunrise.text = "${time(sunrise)} "
                    binding.sunset.text = "${time(sunSet)}"
                    binding.sea.text = "$seaLevel hPa"
                    binding.condition.text = condition;
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityname.text = "$cityName"

//                     change the lottie animation after every condition
                    changeAnimationAccordingCondition(condition)
                }

            }


            override fun onFailure(call: Call<WeatherApps>, t: Throwable) {
                Log.d("Main", "onFailure: " + t.message)
            }


        })
    }

    private fun changeAnimationAccordingCondition(conditions: String) {
        when (conditions) {
//            condition for animation of weather
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Partialy Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
//        start the animation always
        binding.lottieAnimationView.playAnimation()
    }


    fun dayName(timestamp: Long): String {
//        convert the day in string format
        val sdf = SimpleDateFormat("EEEE", Locale.ENGLISH)
        return sdf.format((Date()))
    }

    fun date(): String {
        //        convert the date in string format
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }

    fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp * 1000)))
    }


    private fun getLocationName(latitude: Double, longitude: Double): String {
//        This Geocoder method returns a list of Address objects corresponding to the provided location name or address.
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                val locality = address.locality ?: "Unknown City"
                val toastMessage = "Locality: $locality"
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
                locality
            } else {
                "Unknown location"
            }

        } catch (e: Exception) {
            e.printStackTrace()
            "Error retrieving location"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location updates
                requestLocationUpdates()
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }
    }

}
