package com.example.smartnotesapp

import android.os.Bundle
import android.widget.*
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class AddNoteActivity : AppCompatActivity() {

    lateinit var title: EditText
    lateinit var desc: EditText
    lateinit var saveBtn: Button
    lateinit var dbHelper: DatabaseHelper

    var noteId = -1   // 🔥 for edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        title = findViewById(R.id.titleEditText)
        desc = findViewById(R.id.descEditText)
        saveBtn = findViewById(R.id.saveBtn)

        val favBtn = findViewById<ImageView>(R.id.favPageBtn)
        val recycleBtn = findViewById<ImageView>(R.id.recycleBtn)
        val menuBtn = findViewById<ImageView>(R.id.menuBtn)

        val session = getSharedPreferences("user_session", MODE_PRIVATE)

        // ⭐ HEADER BUTTONS
        favBtn.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        recycleBtn.setOnClickListener {
            startActivity(Intent(this, RecycleBinActivity::class.java))
        }

        menuBtn.setOnClickListener {
            showPopupMenu(menuBtn, session)
        }

        dbHelper = DatabaseHelper(this)

        // 🔥 CHECK IF EDIT MODE
        noteId = intent.getIntExtra("id", -1)

        if (noteId != -1) {
            title.setText(intent.getStringExtra("title"))
            desc.setText(intent.getStringExtra("desc"))
            saveBtn.text = "Update Note"
        }

        saveBtn.setOnClickListener {

            val t = title.text.toString()
            val d = desc.text.toString()

            if (t.isEmpty() || d.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (noteId == -1) {
                // ➕ ADD NOTE
                dbHelper.insertNote(t, d)
                Toast.makeText(this, "Note Added", Toast.LENGTH_SHORT).show()
            } else {
                // ✏️ UPDATE NOTE
                dbHelper.updateNote(noteId, t, d)
                Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }

    // ⭐ POPUP MENU
    private fun showPopupMenu(view: View, session: SharedPreferences) {

        val popup = PopupMenu(this, view)
        popup.menu.add("Profile")
        popup.menu.add("Settings")
        popup.menu.add("Logout")

        popup.setOnMenuItemClickListener {

            when (it.title) {

                "Profile" -> showProfile()

                "Settings" -> showSettings()

                "Logout" -> {
                    session.edit().clear().apply()
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            true
        }
        popup.show()
    }

    // ⭐ PROFILE (FIXED)
    private fun showProfile() {

        val session = getSharedPreferences("user_session", MODE_PRIVATE)
        val username = session.getString("username", "")?.trim()

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

    // ⭐ SETTINGS (SIMPLE VERSION)
    private fun showSettings() {

        val options = arrayOf(
            "Clear All Notes",
            "About App"
        )

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {
                        val db = dbHelper.writableDatabase
                        db.execSQL("DELETE FROM notes")
                        Toast.makeText(this, "All notes deleted", Toast.LENGTH_SHORT).show()
                    }

                    1 -> {
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