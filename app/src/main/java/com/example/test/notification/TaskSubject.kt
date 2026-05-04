package com.example.test.notification

interface TaskSubject {
    fun addObserver(observer: TaskObserver)
    fun removeObserver(observer: TaskObserver)
    fun notifyObservers(event: TaskEvent)
}