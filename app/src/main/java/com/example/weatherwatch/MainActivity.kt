package com.example.weatherwatch

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherwatch.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchWeatherData("lahore")
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null){
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, API_KEY, "metric")
        response.enqueue(object : Callback<WeatherDetails>{
            override fun onResponse(call: Call<WeatherDetails>, response: Response<WeatherDetails>) {
                val responseBody = response.body()
                if (response.isSuccessful){
                    val temperature = responseBody?.main?.temp?.let { Math.round(it) }
                    val humidity = responseBody?.main?.humidity
                    val windSpeed = responseBody?.wind?.speed
                    val sunrise = responseBody?.sys?.sunrise?.toLong()
                    val sunset = responseBody?.sys?.sunset?.toLong()
                    val condition = responseBody?.weather?.get(0)?.main
                    val seaLevel = responseBody?.main?.pressure
                    val maxTemp = responseBody?.main?.temp_max?.let { Math.round(it) }
                    val minTemp = responseBody?.main?.temp_min?.let { Math.round(it) }

                    binding.temprature.text = "$temperature°C"
                    binding.humidity.text = "$humidity%"
                    binding.windSpeed.text = "$windSpeed m/s"
                    binding.sunrise.text = timeFormat(sunrise!!)
                    binding.sunset.text = timeFormat(sunset!!)
                    binding.condition.text = "$condition"
                    binding.seaLevel.text = "$seaLevel hPa"
                    binding.maxTemp.text = "Max Temp: $maxTemp°C"
                    binding.minTemp.text = "Min Temp: $minTemp°C"
                    binding.cityName.text = "$cityName"
                    binding.weather.text = "$condition"
                    binding.day.text = dayFormat(System.currentTimeMillis())
                    binding.date.text = dateFormat()
                    binding.cityName.text = "$cityName"

                    changeBackground(condition)
                }
            }

            override fun onFailure(p0: Call<WeatherDetails>, p1: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun changeBackground(condition: String?) {
        when(condition){
            "Partially Clouds", "Haze", "Overcast", "Clouds", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.cloud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun dayFormat(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun dateFormat(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun timeFormat(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }
}