package com.example.smartnotesapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler

class RegisterActivity : AppCompatActivity() {

    lateinit var name: EditText
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var togglePassword: ImageView
    lateinit var registerBtn: Button
    lateinit var goToLogin: TextView
    lateinit var db: UserDatabaseHelper

    var isPasswordVisible = false
    var mediaPlayer: MediaPlayer? = null   // 🎵 audio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        name = findViewById(R.id.regName)
        username = findViewById(R.id.regUsername)
        password = findViewById(R.id.regPassword)
        togglePassword = findViewById(R.id.togglePassword)
        registerBtn = findViewById(R.id.registerBtn)
        goToLogin = findViewById(R.id.goToLogin)

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

        registerBtn.setOnClickListener {

            val n = name.text.toString()
            val u = username.text.toString()
            val p = password.text.toString()

            if (n.isEmpty() || u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = db.registerUser(n, u, p)

            if (success) {

                playSound(R.raw.register_success)

                Handler(mainLooper).postDelayed({

                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()

                }, 2000)
            }

        }

        goToLogin.setOnClickListener {
            finish()
        }
    }

    // 🎵 PLAY SOUND
    private fun playSound(soundRes: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, soundRes)
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}