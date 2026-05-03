package com.example.test.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val category: String,
    val date: String, // Simplified as String for now
    val isChecked: Boolean = false,
    val isScheduled: Boolean = false,
    val scheduleStartMinutes: Int? = null,
    val scheduleEndMinutes: Int? = null
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isChecked: Boolean = false
)


@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val title: String = "",
    val content: String = "",
    val type: String = "NOTE",
    val color: String = "DEFAULT",
    val listItemsJson: String = "[]",
    val photoUris: String = "",
    val stickers: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
