package com.example.test.notification

object TaskEventBus : TaskSubject {
    private val observers = mutableListOf<TaskObserver>()

    @Synchronized
    override fun addObserver(observer: TaskObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    @Synchronized
    override fun removeObserver(observer: TaskObserver) {
        observers.remove(observer)
    }

    @Synchronized
    override fun notifyObservers(event: TaskEvent) {
        // Defensive copy so an observer unregistering itself doesn't crash the loop
        observers.toList().forEach { it.onTaskEvent(event) }
    }
}