package com.sudhirtheindian.weatherapp.activities
import android.os.Handler
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.sudhirtheindian.weatherapp.R

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            // Code to execute after delay
            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
            finish()
        }, 3000)
    }

}