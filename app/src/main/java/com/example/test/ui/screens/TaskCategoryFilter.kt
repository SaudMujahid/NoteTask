package com.example.test.ui.screens

enum class TaskCategoryFilter(val label: String) {
    ALL("All Types"),
    HEALTH("HEALTH"),
    WORK("WORK"),
    MENTAL_HEALTH("MENTAL HEALTH"),
    OTHER("OTHER");

    companion object {
        fun fromTaskCategory(category: String): TaskCategoryFilter? {
            return entries.firstOrNull { it.label.equals(category, ignoreCase = true) }
        }
    }
}