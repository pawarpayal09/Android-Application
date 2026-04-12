package com.example.smartnotesapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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
}