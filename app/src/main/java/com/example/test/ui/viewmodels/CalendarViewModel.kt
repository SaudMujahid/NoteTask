package com.example.test.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.models.Task
import com.example.test.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val tasks: List<Task> = emptyList()
)

class CalendarViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks.asStateFlow()

    private val _calendarDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val calendarDays: StateFlow<List<CalendarDay>> = _calendarDays.asStateFlow()

    private val _tasksForSelectedDate = MutableStateFlow<List<Task>>(emptyList())
    val tasksForSelectedDate: StateFlow<List<Task>> = _tasksForSelectedDate.asStateFlow()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun setUser(userId: Long) {
        viewModelScope.launch {
            taskRepository.getTasksForUser(userId).collect { taskList ->
                _allTasks.value = taskList
                generateCalendarDays()
                updateTasksForSelectedDate()
            }
        }
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
        updateTasksForSelectedDate()
    }

    fun nextMonth() {
        val newMonth = _currentMonth.value.clone() as Calendar
        newMonth.add(Calendar.MONTH, 1)
        _currentMonth.value = newMonth
        generateCalendarDays()
    }

    fun previousMonth() {
        val newMonth = _currentMonth.value.clone() as Calendar
        newMonth.add(Calendar.MONTH, -1)
        _currentMonth.value = newMonth
        generateCalendarDays()
    }

    fun goToToday() {
        _currentMonth.value = Calendar.getInstance()
        _selectedDate.value = Date()
        generateCalendarDays()
        updateTasksForSelectedDate()
    }

    private fun generateCalendarDays() {
        val month = _currentMonth.value.clone() as Calendar
        month.set(Calendar.DAY_OF_MONTH, 1)
        
        val firstDayOfWeek = month.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Start from Sunday of the week containing the 1st
        val startCalendar = month.clone() as Calendar
        startCalendar.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - 1))
        
        val days = mutableListOf<CalendarDay>()
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        // Generate 42 days (6 weeks) for consistent grid
        for (i in 0 until 42) {
            val currentDay = startCalendar.clone() as Calendar
            val dayDate = currentDay.time
            
            val isCurrentMonth = currentDay.get(Calendar.MONTH) == month.get(Calendar.MONTH)
            val isToday = currentDay.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                         currentDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            
            // Get tasks for this date
            val dateStr = dateFormatter.format(dayDate)
            val tasksForDate = _allTasks.value.filter { task ->
                task.date == dateStr
            }
            
            days.add(
                CalendarDay(
                    date = dayDate,
                    dayOfMonth = currentDay.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = isCurrentMonth,
                    isToday = isToday,
                    tasks = tasksForDate
                )
            )
            
            startCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        _calendarDays.value = days
    }

    private fun updateTasksForSelectedDate() {
        val selectedDateStr = dateFormatter.format(_selectedDate.value)
        _tasksForSelectedDate.value = _allTasks.value.filter { task ->
            task.date == selectedDateStr
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTask(task.id, !task.isChecked)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
}