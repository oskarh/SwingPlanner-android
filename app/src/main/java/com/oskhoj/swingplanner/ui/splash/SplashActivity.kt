package com.oskhoj.swingplanner.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.oskhoj.swingplanner.MainActivity
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(1000)
        startActivity<MainActivity>()
        finish()
    }
}
