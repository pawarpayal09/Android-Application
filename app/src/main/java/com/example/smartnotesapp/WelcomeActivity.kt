package com.example.smartnotesapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    lateinit var progressBar: ProgressBar
    lateinit var loadingText: TextView

    var progressValue = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)

        val handler = Handler(Looper.getMainLooper())

        Thread {
            while (progressValue < 100) {

                progressValue += 5

                if (progressValue > 100) progressValue = 100   // safety check

                handler.post {
                    progressBar.progress = progressValue
                    loadingText.text = "Loading... $progressValue%"
                }

                Thread.sleep(150)
            }

            handler.post {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

        }.start()
    }
}