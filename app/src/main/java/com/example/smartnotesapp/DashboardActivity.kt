package com.example.smartnotesapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.util.*
import android.view.View
import android.content.SharedPreferences

class DashboardActivity : AppCompatActivity() {
        private val CAMERA_REQUEST = 200
        private val CAMERA_PERMISSION_CODE = 201

        lateinit var notesCard: Button
        lateinit var cameraCard: Button
        lateinit var audioCard: Button
        lateinit var tableCard: Button

        lateinit var session: SharedPreferences
        lateinit var db: DatabaseHelper

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_dashboard)
            supportActionBar?.hide()

            db = DatabaseHelper(this)
            session = getSharedPreferences("user_session", MODE_PRIVATE)

            findViewById<ImageView>(R.id.recycleBtn).setOnClickListener {
                startActivity(Intent(this, RecycleBinActivity::class.java))
            }

            findViewById<ImageView>(R.id.favPageBtn).setOnClickListener {
                startActivity(Intent(this, FavoritesActivity::class.java))
            }

            val menuBtn = findViewById<ImageView>(R.id.menuBtn)
            menuBtn.setOnClickListener { showPopupMenu(menuBtn) }

            notesCard = findViewById(R.id.notesCard)
            cameraCard = findViewById(R.id.cameraCard)
            audioCard = findViewById(R.id.audioCard)
            tableCard = findViewById(R.id.tableCard)

            notesCard.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
            }

            cameraCard.setOnClickListener { openCamera() }
            audioCard.setOnClickListener { startVoiceInput() }

            tableCard.setOnClickListener {
                val options = arrayOf("To-Do List", "Set Reminder")

                AlertDialog.Builder(this)
                    .setTitle("Choose Option")
                    .setItems(options) { _, which ->
                        if (which == 0) showTableDialog()
                        else showReminderDialog()
                    }
                    .show()
            }
        }

        // ⭐ MENU
        private fun showPopupMenu(view: View) {

            val popup = PopupMenu(this, view)
            popup.menu.add("Profile")
            popup.menu.add("Settings")
            popup.menu.add("Logout")

            popup.setOnMenuItemClickListener {

                when (it.title) {

                    "Profile" -> showProfile()

                    "Settings" -> showSettings()   // ✅ FIX ADDED

                    "Logout" -> {
                        val editor = session.edit()
                        editor.clear()
                        editor.apply()

                        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
                true
            }
            popup.show()
        }

        // ⭐ PROFILE (WORKING)
        private fun showProfile() {

            val username = session.getString("username", "")

            if (username.isNullOrEmpty()) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                return
            }

            val userDB = UserDatabaseHelper(this)
            val cursor = userDB.getUserDetails(username)

            if (cursor.moveToFirst()) {

                val name = cursor.getString(0)
                val user = cursor.getString(1)

                AlertDialog.Builder(this)
                    .setTitle("User Profile")
                    .setMessage("Name: $name\nUsername: $user")
                    .setPositiveButton("OK", null)
                    .show()

            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            }

            cursor.close()
        }

        // ✅ FORCE FULL REFRESH
        private fun restartApp() {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // 📷 CAMERA
        private fun openCamera() {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST)
            }
        }

        // 🎤 VOICE
        private fun startVoiceInput() {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note...")

            try {
                startActivityForResult(intent, 100)
            } catch (e: Exception) {
                Toast.makeText(this, "Voice not supported", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 🎤 VOICE NOTE
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (!result.isNullOrEmpty()) {
                val spokenText = result[0]

                AlertDialog.Builder(this)
                    .setTitle("Save Voice Note")
                    .setMessage(spokenText)
                    .setPositiveButton("Save") { _, _ ->
                        db.insertNote("Voice Note", spokenText)
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        // 📷 CAMERA IMAGE (🔥 FIX ADDED)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            val photo = data?.extras?.get("data") as Bitmap

            val base64Image = bitmapToBase64(photo)

            AlertDialog.Builder(this)
                .setTitle("Save Image Note")
                .setMessage("Do you want to save this image?")
                .setPositiveButton("Save") { _, _ ->

                    db.insertNote("Image Note", base64Image, base64Image)

                    Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

        private fun bitmapToBase64(bitmap: Bitmap): String {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        }

    // 📊 TABLE
    private fun showTableDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("To-Do List")

        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setPadding(20, 20, 20, 20)

        val tasks = ArrayList<Pair<CheckBox, EditText>>()

        // Create 5 rows (you can increase later)
        for (i in 1..5) {

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(0, 10, 0, 10)

            val checkBox = CheckBox(this)

            val editText = EditText(this)
            editText.hint = "Enter task $i"
            editText.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            row.addView(checkBox)
            row.addView(editText)

            tasks.add(Pair(checkBox, editText))
            mainLayout.addView(row)
        }

        builder.setView(mainLayout)

        builder.setPositiveButton("Save") { _, _ ->

            val result = StringBuilder()

            for ((checkBox, editText) in tasks) {

                val text = editText.text.toString().trim()

                if (text.isNotEmpty()) {
                    if (checkBox.isChecked) {
                        result.append("✔ ").append(text).append("\n")
                    } else {
                        result.append("✘ ").append(text).append("\n")
                    }
                }
            }

            if (result.isNotEmpty()) {
                db.insertNote("To-Do List", result.toString())
                Toast.makeText(this, "To-Do Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No tasks entered", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // 🔔 REMINDER DIALOG
    private fun showReminderDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Reminder")

        val input = EditText(this)
        input.hint = "Enter reminder text"
        builder.setView(input)

        builder.setPositiveButton("Next") { _, _ ->

            val message = input.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    calendar.set(year, month, day)

                    TimePickerDialog(
                        this,
                        { _, hour, minute ->

                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)

                            setAlarm(calendar, message)

                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // 🔔 SET ALARM (FIXED)
    private fun setAlarm(calendar: Calendar, message: String) {

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ✅ FIX FOR ANDROID 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Enable exact alarm permission in settings", Toast.LENGTH_LONG).show()
                return
            }
        }

        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("msg", message)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ SAFE METHOD
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllNotes() {
        val dbWritable = db.writableDatabase
        dbWritable.execSQL("DELETE FROM notes")
    }

    private fun showSettings() {

        val options = arrayOf(
            "Notifications ON/OFF",
            "Clear All Notes",
            "About App"
        )

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->

                when (which) {

                    // 🔔 NOTIFICATIONS
                    0 -> {
                        val pref = getSharedPreferences("app_settings", MODE_PRIVATE)
                        val current = pref.getBoolean("notifications", true)

                        pref.edit().putBoolean("notifications", !current).apply()

                        val status = if (!current) "ON" else "OFF"
                        Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
                    }

                    // 🗑 CLEAR NOTES
                    1 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Confirm")
                            .setMessage("Delete all notes?")
                            .setPositiveButton("Yes") { _, _ ->
                                clearAllNotes()
                                Toast.makeText(this, "All notes deleted", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }

                    // ℹ ABOUT
                    2 -> {
                        AlertDialog.Builder(this)
                            .setTitle("About Smart Notes")
                            .setMessage(
                                "Smart Notes App\n\n" +
                                        "Developed by: Payal Pawar\n" +
                                        "Features:\n• Notes\n• Voice\n• Camera\n• Reminders\n• To-Do List"
                            )
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
            .show()
    }
}