package com.example.test.data.models

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
