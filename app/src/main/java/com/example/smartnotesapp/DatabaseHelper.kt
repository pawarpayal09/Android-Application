package com.example.smartnotesapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "NotesDB", null, 3) {   // 🔼 version updated

    override fun onCreate(db: SQLiteDatabase) {

        val createTable = """
    CREATE TABLE notes (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT,
        description TEXT,
        image TEXT,
        isDeleted INTEGER DEFAULT 0,
        isFavorite INTEGER DEFAULT 0   
    )
""".trimIndent()

        db.execSQL(createTable)

        val createUserTable = """
    CREATE TABLE users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE,
        password TEXT
    )
""".trimIndent()

        db.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE notes ADD COLUMN isFavorite INTEGER DEFAULT 0")
        }
    }

    // Insert normal note
    fun insertNote(title: String, description: String) {
        val db = writableDatabase
        val values = ContentValues()

        values.put("title", title)
        values.put("description", description)
        values.put("image", "")
        values.put("isDeleted", 0)

        db.insert("notes", null, values)
        db.close()
    }

    // Insert image note
    fun insertNote(title: String, description: String, image: String) {
        val db = writableDatabase
        val values = ContentValues()

        values.put("title", title)
        values.put("description", description)
        values.put("image", image)
        values.put("isDeleted", 0)

        db.insert("notes", null, values)
        db.close()
    }

    // Get active notes
    fun getAllNotes(): ArrayList<NoteModel> {
        val list = ArrayList<NoteModel>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM notes WHERE isDeleted=0", null)

        if (cursor.moveToFirst()) {
            do {
                val note = NoteModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(5)   // ⭐ ADD THIS LINE
                )
                list.add(note)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    // Recycle bin
    fun getDeletedNotes(): ArrayList<NoteModel> {
        val list = ArrayList<NoteModel>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM notes WHERE isDeleted=1", null)

        if (cursor.moveToFirst()) {
            do {
                val note = NoteModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
                )
                list.add(note)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun updateNote(id: Int, title: String, desc: String) {
        val db = writableDatabase
        val values = ContentValues()

        values.put("title", title)
        values.put("description", desc)

        db.update("notes", values, "id=?", arrayOf(id.toString()))
        db.close()
    }

    fun moveToTrash(id: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("isDeleted", 1)

        db.update("notes", values, "id=?", arrayOf(id.toString()))
        db.close()
    }

    fun restoreNote(id: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("isDeleted", 0)

        db.update("notes", values, "id=?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteNote(id: Int) {
        val db = writableDatabase
        db.delete("notes", "id=?", arrayOf(id.toString()))
        db.close()
    }

    // Register User
    fun registerUser(username: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put("username", username)
        values.put("password", password)

        val result = db.insert("users", null, values)
        db.close()

        return result != -1L
    }

    // Login Check
    fun loginUser(username: String, password: String): Boolean {
        val db = this.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE username=? AND password=?",
            arrayOf(username, password)
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUser(username: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM users WHERE username=?", arrayOf(username))
    }

    // Mark as favorite
    fun toggleFavorite(id: Int, status: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("isFavorite", status)
        db.update("notes", values, "id=?", arrayOf(id.toString()))
        db.close()
    }

    // Get only favorite notes
    fun getFavoriteNotes(): ArrayList<NoteModel> {
        val list = ArrayList<NoteModel>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM notes WHERE isFavorite=1 AND isDeleted=0", null)

        if (cursor.moveToFirst()) {
            do {
                val note = NoteModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(5)   // ⭐ ADD THIS LINE
                )
                list.add(note)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }
}