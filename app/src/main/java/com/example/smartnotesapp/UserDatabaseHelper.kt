package com.example.smartnotesapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "UserDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, username TEXT UNIQUE, password TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun registerUser(name: String, username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()

        values.put("name", name.trim())
        values.put("username", username.trim())
        values.put("password", password.trim())

        val result = db.insert("users", null, values)
        return result != -1L
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE username=? AND password=?",
            arrayOf(username.trim(), password.trim())
        )

        val exists = cursor.moveToFirst()
        cursor.close()

        return exists
    }

    fun getUserDetails(username: String): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT name, username FROM users WHERE username=?",
            arrayOf(username)
        )
    }
}