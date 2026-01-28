package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.muatrenthenang.resfood.data.model.Branch
import com.muatrenthenang.resfood.data.model.TableReservation
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.data.repository.BranchRepository
import com.muatrenthenang.resfood.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

sealed class BookingState {
    object Idle : BookingState()
    object Loading : BookingState()
    data class Success(val reservationId: String) : BookingState()
    data class Error(val message: String) : BookingState()
}

class BookingTableViewModel : ViewModel() {

    private val reservationRepository = ReservationRepository()
    private val branchRepository = BranchRepository()
    private val authRepository = AuthRepository()

    // Branch (Single branch from Firestore)
    private val _branch = MutableStateFlow<Branch?>(null)
    val branch: StateFlow<Branch?> = _branch.asStateFlow()

    // Dates (Next 14 days)
    private val _dates = MutableStateFlow<List<LocalDate>>(emptyList())
    val dates: StateFlow<List<LocalDate>> = _dates.asStateFlow()

    // Time Selection Data
    val availableHours = (10..22).toList()
    val availableMinutes = listOf(0, 15, 30, 45)

    // --- Selection State ---
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedHour = MutableStateFlow(18)
    val selectedHour: StateFlow<Int> = _selectedHour.asStateFlow()

    private val _selectedMinute = MutableStateFlow(0)
    val selectedMinute: StateFlow<Int> = _selectedMinute.asStateFlow()

    private val _guestCountAdult = MutableStateFlow(2)
    val guestCountAdult: StateFlow<Int> = _guestCountAdult.asStateFlow()

    private val _guestCountChild = MutableStateFlow(0)
    val guestCountChild: StateFlow<Int> = _guestCountChild.asStateFlow()
    
    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState.asStateFlow()

    init {
        generateDates()
        loadBranch()
    }

    private fun loadBranch() {
        viewModelScope.launch {
            branchRepository.getPrimaryBranch().onSuccess { loadedBranch ->
                _branch.value = loadedBranch
            }
        }
    }

    private fun generateDates() {
        val today = LocalDate.now()
        val dateList = mutableListOf<LocalDate>()
        for (i in 0..13) {
            dateList.add(today.plusDays(i.toLong()))
        }
        _dates.value = dateList
    }

    // --- Actions ---

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectHour(hour: Int) {
        _selectedHour.value = hour
    }

    fun selectMinute(minute: Int) {
        _selectedMinute.value = minute
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

    fun confirmBooking() {
        val userId = authRepository.getCurrentUserId()
        val currentBranch = _branch.value

        if (userId == null) {
            _bookingState.value = BookingState.Error("Vui lòng đăng nhập để đặt bàn!")
            return
        }

        if (currentBranch == null) {
            _bookingState.value = BookingState.Error("Đang tải thông tin nhà hàng, vui lòng thử lại!")
            return
        }

        _bookingState.value = BookingState.Loading

        viewModelScope.launch {
            try {
                // Construct Date Time
                val dateTime = LocalDateTime.of(_selectedDate.value, LocalTime.of(_selectedHour.value, _selectedMinute.value))
                val date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
                val timestamp = Timestamp(date)

                val totalGuests = _guestCountAdult.value + _guestCountChild.value

                // 1. Check Availability using branch info from Firestore
                val availabilityResult = reservationRepository.checkAvailability(
                    branchId = currentBranch.id,
                    requestedTime = timestamp,
                    requestedGuests = totalGuests,
                    maxCapacity = currentBranch.maxCapacity
                )

                if (availabilityResult.isFailure) {
                    val error = availabilityResult.exceptionOrNull()
                    _bookingState.value = BookingState.Error("Lỗi kiểm tra bàn: ${error?.message}")
                    return@launch
                }

                if (availabilityResult.getOrNull() == true) {
                    // 2. Create Reservation
                    val reservation = TableReservation(
                        userId = userId,
                        branchId = currentBranch.id,
                        branchName = currentBranch.name,
                        timeSlot = timestamp,
                        guestCountAdult = _guestCountAdult.value,
                        guestCountChild = _guestCountChild.value,
                        note = _note.value
                    )

                    val createResult = reservationRepository.createReservation(reservation)
                    
                    if (createResult.isSuccess) {
                        _bookingState.value = BookingState.Success(createResult.getOrNull() ?: "")
                    } else {
                        _bookingState.value = BookingState.Error("Lỗi tạo đặt bàn: ${createResult.exceptionOrNull()?.message}")
                    }
                } else {
                    _bookingState.value = BookingState.Error("Xin lỗi, nhà hàng đã hết chỗ vào khung giờ này!")
                }

            } catch (e: Exception) {
                _bookingState.value = BookingState.Error("Lỗi không xác định: ${e.message}")
            }
        }
    }
    
    fun resetBookingState() {
        _bookingState.value = BookingState.Idle
    }
}
