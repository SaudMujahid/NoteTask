package com.example.test.notification

interface TaskObserver {
    fun onTaskEvent(event: TaskEvent)
}