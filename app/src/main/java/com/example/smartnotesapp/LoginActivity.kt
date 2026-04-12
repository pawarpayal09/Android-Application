package com.example.smartnotesapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.os.Handler

class LoginActivity : AppCompatActivity() {

    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var togglePassword: ImageView
    lateinit var loginBtn: Button
    lateinit var goToRegister: TextView
    lateinit var db: UserDatabaseHelper

    var isPasswordVisible = false
    var mediaPlayer: MediaPlayer? = null   // 🎵 audio player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        togglePassword = findViewById(R.id.togglePassword)
        loginBtn = findViewById(R.id.loginBtn)
        goToRegister = findViewById(R.id.goToRegister)

        db = UserDatabaseHelper(this)

        // 👁 PASSWORD TOGGLE
        togglePassword.setOnClickListener {
            if (isPasswordVisible) {
                password.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            isPasswordVisible = !isPasswordVisible
            password.setSelection(password.text.length)
        }

        loginBtn.setOnClickListener {

            val user = username.text.toString()
            val pass = password.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isValid = db.checkUser(user, pass)

            if (isValid) {

                val pref = getSharedPreferences("user_session", MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString("username", user)
                editor.apply()

                playSound(R.raw.login_success)

                // ⏱ WAIT FOR AUDIO TO COMPLETE
                Handler(mainLooper).postDelayed({

                    showLoginNotification()

                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()

                }, 2000)
            }
        }

        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // 🎵 FUNCTION TO PLAY SOUND
    private fun playSound(soundRes: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, soundRes)
        mediaPlayer?.start()
    }

    private fun showLoginNotification() {
        val channelId = "login_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Login Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Login Successful")
            .setContentText("Welcome to Smart Notes App")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}