package com.example.test.data.models

data class ListItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)