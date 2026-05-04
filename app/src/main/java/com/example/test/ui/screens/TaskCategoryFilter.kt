package com.example.test.ui.screens

enum class TaskCategoryFilter(val label: String) {
    ALL("All Types"),
    Personal("Personal"),
    WORK("WORK"),
    University("University"),
    OTHER("OTHER");

    companion object {
        fun fromTaskCategory(category: String): TaskCategoryFilter? {
            return entries.firstOrNull { it.label.equals(category, ignoreCase = true) }
        }
    }
}