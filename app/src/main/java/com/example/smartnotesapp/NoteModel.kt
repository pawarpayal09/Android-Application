package com.example.smartnotesapp

data class NoteModel(
    val id: Int,
    val title: String,
    val description: String,
    val image: String,
    var isFavorite: Int = 0
)