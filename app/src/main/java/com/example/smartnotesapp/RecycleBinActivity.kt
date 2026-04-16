package com.example.smartnotesapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences

class RecycleBinActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var dbHelper: DatabaseHelper
    lateinit var session: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_bin)

        listView = findViewById(R.id.recycleList)
        dbHelper = DatabaseHelper(this)
        session = getSharedPreferences("user_session", MODE_PRIVATE)

        // HEADER BUTTONS
        findViewById<ImageView>(R.id.favPageBtn).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        findViewById<ImageView>(R.id.recycleBtn).setOnClickListener {
            Toast.makeText(this, "Already in Recycle Bin", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.menuBtn).setOnClickListener {
            showPopupMenu(it)
        }

        loadRecycleData()
    }

    // LOAD DATA
    private fun loadRecycleData() {

        val notesList = dbHelper.getDeletedNotes()

        if (notesList.isEmpty()) {
            Toast.makeText(this, "Recycle Bin is empty", Toast.LENGTH_SHORT).show()
        }

        // USE RecycleAdapter (NOT NoteAdapter)
        val adapter = RecycleAdapter(this, notesList)

        listView.adapter = adapter

        // CLICK LISTENER (IMPORTANT)
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedNote = notesList[position]
            showOptionsDialog(selectedNote.id)
        }
    }

    // RESTORE
    private fun showOptionsDialog(noteId: Int) {

        val options = arrayOf("Restore Note", "Delete Permanently")

        android.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("Choose Action")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {
                        dbHelper.restoreNote(noteId)
                        Toast.makeText(this, "Note Restored", Toast.LENGTH_SHORT).show()
                        loadRecycleData()
                    }

                    1 -> {
                        dbHelper.deleteNote(noteId)
                        Toast.makeText(this, "Deleted Permanently", Toast.LENGTH_SHORT).show()
                        loadRecycleData()
                    }
                }
            }
            .show()
    }

    // POPUP MENU
    private fun showPopupMenu(view: View) {

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

    // PROFILE
    private fun showProfile() {

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

            android.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle("User Profile")
                .setMessage("Name: $name\nUsername: $user")
                .setPositiveButton("OK", null)
                .show()

        } else {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
        }

        cursor.close()
    }

    // SETTINGS
    private fun showSettings() {

        val options = arrayOf(
            "Notifications ON/OFF",
            "Clear All Notes",
            "About App"
        )

        android.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("Settings")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {
                        val pref = getSharedPreferences("app_settings", MODE_PRIVATE)
                        val current = pref.getBoolean("notifications", true)

                        pref.edit().putBoolean("notifications", !current).apply()

                        val status = if (!current) "ON" else "OFF"
                        Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
                    }

                    1 -> {
                        Toast.makeText(this, "Clear not implemented", Toast.LENGTH_SHORT).show()
                    }

                    2 -> {
                        android.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
                            .setTitle("About")
                            .setMessage("Smart Notes App\nDeveloped by Payal Pawar")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
            .show()
    }
}