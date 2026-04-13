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
    var mediaPlayer: MediaPlayer? = null

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

            val n = name.text.toString().trim()
            val u = username.text.toString().trim()
            val p = password.text.toString().trim()

            // ✅ VALIDATIONS

            if (n.isEmpty()) {
                name.error = "Enter full name"
                name.requestFocus()
                return@setOnClickListener
            }

            if (n.length < 3) {
                name.error = "Name must be at least 3 characters"
                name.requestFocus()
                return@setOnClickListener
            }

            // STRONG PASSWORD VALIDATION
            if (p.isEmpty()) {
                password.error = "Enter password"
                password.requestFocus()
                return@setOnClickListener
            }

            val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d).{6,}$")

            if (!passwordPattern.matches(p)) {
                password.error = "Password must contain:\n• 1 Uppercase\n• 1 Number\n• Min 6 characters"
                password.requestFocus()
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

            } else {
                Toast.makeText(this, "Username already exists!", Toast.LENGTH_SHORT).show()
            }
        }

        goToLogin.setOnClickListener {
            finish()
        }
    }

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