package com.example.smartnotesapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var addBtn: Button
    lateinit var dbHelper: DatabaseHelper
    lateinit var emptyText: TextView

    lateinit var notesList: ArrayList<NoteModel>
    var filteredList = ArrayList<NoteModel>()

    lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        addBtn = findViewById(R.id.addNoteBtn)
        searchView = findViewById(R.id.searchView)
        emptyText = findViewById(R.id.emptyText)

        dbHelper = DatabaseHelper(this)

        addBtn.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        loadNotes()

        // 🔍 SEARCH FUNCTION
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                filteredList.clear()

                if (newText.isNullOrEmpty()) {
                    filteredList.addAll(notesList)
                } else {
                    for (note in notesList) {
                        if (note.title.contains(newText, true) ||
                            note.description.contains(newText, true)
                        ) {
                            filteredList.add(note)
                        }
                    }
                }

                updateList(filteredList)
                return true
            }
        })

        // 🗑 DELETE NOTE
        listView.setOnItemLongClickListener { _, _, position, _ ->

            val note = filteredList[position]

            AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes") { _, _ ->
                    dbHelper.deleteNote(note.id)
                    loadNotes()
                    showNotification("Note Deleted", "Your note was deleted")
                }
                .setNegativeButton("No", null)
                .show()

            true
        }

        // 📄 CLICK NOTE
        listView.setOnItemClickListener { _, _, position, _ ->
            val note = filteredList[position]
            Toast.makeText(this, note.title, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    // ✅ LOAD NOTES
    private fun loadNotes() {
        notesList = dbHelper.getAllNotes()
        filteredList.clear()
        filteredList.addAll(notesList)

        updateList(filteredList)
    }

    // ✅ UPDATE LIST VIEW
    private fun updateList(list: ArrayList<NoteModel>) {

        val adapter = NoteAdapter(
            this,
            list,
            dbHelper
        ) {
            loadNotes()
        }

        listView.adapter = adapter

        // ✅ SHOW / HIDE EMPTY MESSAGE
        if (list.isEmpty()) {
            emptyText.visibility = TextView.VISIBLE
            listView.visibility = ListView.GONE
        } else {
            emptyText.visibility = TextView.GONE
            listView.visibility = ListView.VISIBLE
        }
    }

    // 🔔 NOTIFICATION
    private fun showNotification(title: String, message: String) {

        val channelId = "notes_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notes Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        manager.notify(1, notification)
    }
}