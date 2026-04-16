package com.example.smartnotesapp

data class NoteModel(
    var id: Int,
    var title: String,
    var description: String,
    var image: String,
    var isFavorite: Int = 0
)