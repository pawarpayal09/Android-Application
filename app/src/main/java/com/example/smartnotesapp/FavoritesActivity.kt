package com.example.smartnotesapp

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

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
}