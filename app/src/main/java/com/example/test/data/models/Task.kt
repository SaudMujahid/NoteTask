package com.example.test.data.models

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)
