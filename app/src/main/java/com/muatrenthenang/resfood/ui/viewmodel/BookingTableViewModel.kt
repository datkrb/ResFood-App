package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BookingTableViewModel : ViewModel() {

    // --- State Mock Data ---

    // 1. Branches
    // 1. Branches
    val branches = listOf(
        Branch("1", "ResFood Quận 1 - Bitexco", "2 Hải Triều, Bến Nghé, Q.1", 10.771576, 106.704987),
        Branch("2", "ResFood Thảo Điền", "12 Quốc Hương, Thảo Điền, Q.2", 10.801648, 106.736932),
        Branch("3", "ResFood Landmark 81", "720A Điện Biên Phủ, Q.Bình Thạnh", 10.795079, 106.721798)
    )

    // 2. Dates (Next 14 days)
    private val _dates = MutableStateFlow<List<LocalDate>>(emptyList())
    val dates: StateFlow<List<LocalDate>> = _dates.asStateFlow()

    // 3. Time Slots (17:00 - 22:00, every 15 mins)
    val timeSlots = generateTimeSlots()

    // --- Selection State ---
    
    private val _selectedBranch = MutableStateFlow<Branch?>(null)
    val selectedBranch: StateFlow<Branch?> = _selectedBranch.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow("18:00")
    val selectedTime: StateFlow<String> = _selectedTime.asStateFlow()

    private val _guestCountAdult = MutableStateFlow(2)
    val guestCountAdult: StateFlow<Int> = _guestCountAdult.asStateFlow()

    private val _guestCountChild = MutableStateFlow(0)
    val guestCountChild: StateFlow<Int> = _guestCountChild.asStateFlow()
    
    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    init {
        generateDates()
    }

    private fun generateDates() {
        val today = LocalDate.now()
        val dateList = mutableListOf<LocalDate>()
        for (i in 0..13) {
            dateList.add(today.plusDays(i.toLong()))
        }
        _dates.value = dateList
    }

    private fun generateTimeSlots(): List<String> {
        val slots = mutableListOf<String>()
        var hour = 17
        var min = 0
        while (hour < 22) {
            slots.add(String.format("%02d:%02d", hour, min))
            min += 15
            if (min == 60) {
                min = 0
                hour++
            }
        }
        return slots
    }

    // --- Actions ---

    fun selectBranch(branch: Branch) {
        _selectedBranch.value = branch
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        // Logic to refresh available tables could go here
    }

    fun selectTime(time: String) {
        _selectedTime.value = time
    }

    fun updateGuestAdult(delta: Int) {
        val newCount = (_guestCountAdult.value + delta).coerceIn(1, 20)
        _guestCountAdult.value = newCount
    }

    fun updateGuestChild(delta: Int) {
        val newCount = (_guestCountChild.value + delta).coerceIn(0, 10)
        _guestCountChild.value = newCount
    }
    
    fun updateNote(text: String) {
        _note.value = text
    }
}

// --- Models ---

data class Branch(val id: String, val name: String, val address: String, val lat: Double, val lng: Double)
