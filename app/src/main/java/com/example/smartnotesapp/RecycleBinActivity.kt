package com.example.smartnotesapp

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class RecycleBinActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var db: DatabaseHelper
    private lateinit var list: ArrayList<NoteModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_bin)

        listView = findViewById(R.id.recycleList)
        db = DatabaseHelper(this)

        loadData()
    }

    private fun loadData() {
        list = db.getDeletedNotes()

        val titles = ArrayList<String>()
        for (note in list) {
            titles.add(note.title)
        }

        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            titles
        )

        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->

            val note = list[position]

            AlertDialog.Builder(this)
                .setTitle("Recycle Bin")
                .setMessage("Restore or Delete permanently?")
                .setPositiveButton("Restore") { _, _ ->
                    db.restoreNote(note.id)
                    loadData()
                }
                .setNegativeButton("Delete Permanently") { _, _ ->
                    db.deleteNote(note.id)
                    loadData()
                }
                .show()
        }
    }
}