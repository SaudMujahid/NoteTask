package com.example.test.data.models

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val userName: String = "",
    val tasks: List<Task> = emptyList(),
    val subtasks: List<Subtask> = emptyList(),
    val notes: List<Note> = emptyList()
)
