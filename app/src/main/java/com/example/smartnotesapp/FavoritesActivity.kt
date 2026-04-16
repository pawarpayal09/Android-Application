package com.example.smartnotesapp

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.*
import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.app.AlertDialog

class FavoritesActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var dbHelper: DatabaseHelper
    lateinit var favList: ArrayList<NoteModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        listView = findViewById(R.id.favListView)
        dbHelper = DatabaseHelper(this)

        loadFavorites()

        val favBtn = findViewById<ImageView>(R.id.favPageBtn)
        val recycleBtn = findViewById<ImageView>(R.id.recycleBtn)
        val menuBtn = findViewById<ImageView>(R.id.menuBtn)

        val session = getSharedPreferences("user_session", MODE_PRIVATE)

        favBtn.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        recycleBtn.setOnClickListener {
            startActivity(Intent(this, RecycleBinActivity::class.java))
        }

        menuBtn.setOnClickListener {
            showPopupMenu(menuBtn, session)
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        favList = dbHelper.getFavoriteNotes()

        val adapter = NoteAdapter(
            this,
            favList,
            dbHelper
        ) {
            loadFavorites()
        }

        listView.adapter = adapter
    }

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

    // PROFILE
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

    // SETTINGS
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
