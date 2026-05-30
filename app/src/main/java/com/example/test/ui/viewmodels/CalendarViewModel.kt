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

enum class CalendarViewMode {
    YEAR, MONTH, DAY
}

class CalendarViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()

    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _weekDates = MutableStateFlow<List<Date>>(emptyList())
    val weekDates: StateFlow<List<Date>> = _weekDates.asStateFlow()

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks.asStateFlow()

    private val _calendarDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val calendarDays: StateFlow<List<CalendarDay>> = _calendarDays.asStateFlow()

    private val _tasksForSelectedDate = MutableStateFlow<List<Task>>(emptyList())
    val tasksForSelectedDate: StateFlow<List<Task>> = _tasksForSelectedDate.asStateFlow()

    // Collapsible sections for Day View
    private val _isTodaysTasksExpanded = MutableStateFlow(true)
    val isTodaysTasksExpanded: StateFlow<Boolean> = _isTodaysTasksExpanded.asStateFlow()

    private val _isScheduleExpanded = MutableStateFlow(true)
    val isScheduleExpanded: StateFlow<Boolean> = _isScheduleExpanded.asStateFlow()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        generateWeekDates()
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { taskList ->
                _allTasks.value = taskList
                generateCalendarDays()
                updateTasksForSelectedDate()
            }
        }
    }

    // View Mode Navigation
    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
        if (mode == CalendarViewMode.DAY) {
            generateWeekDates()
        }
    }

    fun navigateToMonth(month: Int) {
        val newMonth = Calendar.getInstance()
        newMonth.set(Calendar.YEAR, _currentYear.value)
        newMonth.set(Calendar.MONTH, month)
        _currentMonth.value = newMonth
        _viewMode.value = CalendarViewMode.MONTH
        generateCalendarDays()
    }

    fun navigateToDayView() {
        _viewMode.value = CalendarViewMode.DAY
        generateWeekDates()
    }

    fun goBack(): Boolean {
        return when (_viewMode.value) {
            CalendarViewMode.DAY -> {
                _viewMode.value = CalendarViewMode.MONTH
                true
            }
            CalendarViewMode.MONTH -> {
                _viewMode.value = CalendarViewMode.YEAR
                true
            }
            CalendarViewMode.YEAR -> false // Return to home
        }
    }

    // Collapsible sections
    fun toggleTodaysTasksExpanded() {
        _isTodaysTasksExpanded.value = !_isTodaysTasksExpanded.value
    }

    fun toggleScheduleExpanded() {
        _isScheduleExpanded.value = !_isScheduleExpanded.value
    }

    // Date Selection
    fun selectDate(date: Date) {
        _selectedDate.value = date
        updateTasksForSelectedDate()
        generateWeekDates()
    }

    fun selectDateAndNavigateToDay(date: Date) {
        _selectedDate.value = date
        _viewMode.value = CalendarViewMode.DAY
        updateTasksForSelectedDate()
        generateWeekDates()
    }

    // Month/Year Navigation
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

    fun nextYear() {
        _currentYear.value += 1
    }

    fun previousYear() {
        _currentYear.value -= 1
    }

    fun goToToday() {
        val today = Calendar.getInstance()
        _currentMonth.value = today
        _selectedDate.value = today.time
        _currentYear.value = today.get(Calendar.YEAR)
        generateCalendarDays()
        generateWeekDates()
        updateTasksForSelectedDate()
    }

    // Generate week dates for Day View (7 days centered around selected date)
    private fun generateWeekDates() {
        val selectedCal = Calendar.getInstance()
        selectedCal.time = _selectedDate.value
        
        // Start from 3 days before selected date
        val startCal = selectedCal.clone() as Calendar
        startCal.add(Calendar.DAY_OF_MONTH, -3)
        
        val dates = mutableListOf<Date>()
        for (i in 0..6) {
            dates.add(startCal.time)
            startCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        _weekDates.value = dates
    }

    // Generate calendar days for Month View
    private fun generateCalendarDays() {
        val month = _currentMonth.value.clone() as Calendar
        month.set(Calendar.DAY_OF_MONTH, 1)
        
        val firstDayOfWeek = month.get(Calendar.DAY_OF_WEEK)
        
        val startCalendar = month.clone() as Calendar
        startCalendar.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - 1))
        
        val days = mutableListOf<CalendarDay>()
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        for (i in 0 until 42) {
            val currentDay = startCalendar.clone() as Calendar
            val dayDate = currentDay.time
            
            val isCurrentMonth = currentDay.get(Calendar.MONTH) == month.get(Calendar.MONTH)
            val isToday = currentDay.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                         currentDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            
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
