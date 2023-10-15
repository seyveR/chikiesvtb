package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        supportActionBar?.hide()

        val image = findViewById<ImageView>(R.id.SplashScreenImage)
        image.alpha = 0f
        image.animate().setDuration(1500).alpha(1f).withEndAction(){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}