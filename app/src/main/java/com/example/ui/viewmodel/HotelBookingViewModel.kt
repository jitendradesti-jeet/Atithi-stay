package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.HotelCatalog
import com.example.data.model.Booking
import com.example.data.model.BookingRequest
import com.example.data.model.Hotel
import com.example.data.model.HotelCategory
import com.example.data.model.RoomOption
import com.example.data.repository.HotelBookingRepository
import com.example.data.repository.HotelRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>

  data class Success<T>(val data: T) : UiState<T>

  data class Error(val message: String) : UiState<Nothing>
}

enum class SortOption(val labelHi: String) {
  RECOMMENDED("अनुशंसित"),
  PRICE_LOW("कम कीमत"),
  RATING_HIGH("शीर्ष रेटिंग"),
}

data class StaySearchCriteria(
  val query: String = "",
  val selectedCategory: HotelCategory = HotelCategory.ALL,
  val sortOption: SortOption = SortOption.RECOMMENDED,
  val checkInEpochDay: Long = defaultCheckInEpochDay(),
  val checkOutEpochDay: Long = defaultCheckInEpochDay() + 2L,
  val guestsCount: Int = 2,
  val roomsCount: Int = 1,
) {
  val nights: Int
    get() = (checkOutEpochDay - checkInEpochDay).toInt().coerceAtLeast(1)

  companion object {
    fun defaultCheckInEpochDay(): Long {
      val cal = Calendar.getInstance()
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
      return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis) + 1L
    }
  }
}

sealed interface BookingActionState {
  data object Idle : BookingActionState

  data object Submitting : BookingActionState

  data class Confirmed(val booking: Booking) : BookingActionState

  data class Error(val message: String) : BookingActionState
}

class HotelBookingViewModel(
  private val hotelRepository: HotelRepository,
  private val repository: HotelBookingRepository,
  private val currentUserId: String,
) : ViewModel() {

  private val _searchCriteria = MutableStateFlow(StaySearchCriteria())
  val searchCriteria: StateFlow<StaySearchCriteria> = _searchCriteria.asStateFlow()

  private val _bookingActionState = MutableStateFlow<BookingActionState>(BookingActionState.Idle)
  val bookingActionState: StateFlow<BookingActionState> = _bookingActionState.asStateFlow()

  private val _actionMessage = MutableStateFlow<String?>(null)
  val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

  init {
    viewModelScope.launch {
      hotelRepository.seedInitialHotelsIfEmpty().onFailure { error ->
        Log.w(TAG, "Failed to seed initial hotels", error)
      }
    }
  }

  val hotelsState: StateFlow<UiState<List<Hotel>>> =
    hotelRepository
      .observeHotels()
      .map<List<Hotel>, UiState<List<Hotel>>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing hotels", rootError)
        emit(UiState.Error(rootError.message ?: "होटल लोड करने में समस्या आई"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val filteredHotels: StateFlow<List<Hotel>> =
    combine(hotelsState, _searchCriteria) { state, criteria ->
        val sourceHotels = (state as? UiState.Success)?.data ?: emptyList()
        val q = criteria.query.trim().lowercase(Locale.getDefault())
        val base =
          sourceHotels.filter { hotel ->
            val matchesCategory =
              criteria.selectedCategory == HotelCategory.ALL ||
                hotel.category == criteria.selectedCategory
            val matchesQuery =
              q.isEmpty() ||
                hotel.name.lowercase(Locale.getDefault()).contains(q) ||
                hotel.location.lowercase(Locale.getDefault()).contains(q) ||
                hotel.city.lowercase(Locale.getDefault()).contains(q) ||
                hotel.state.lowercase(Locale.getDefault()).contains(q)
            matchesCategory && matchesQuery
          }
        when (criteria.sortOption) {
          SortOption.RECOMMENDED -> base.sortedByDescending { if (it.featured) 1 else 0 }
          SortOption.PRICE_LOW -> base.sortedBy { it.price }
          SortOption.RATING_HIGH -> base.sortedByDescending { it.rating }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList(),
      )

  val bookingsState: StateFlow<UiState<List<Booking>>> =
    repository
      .observeBookings(currentUserId)
      .map<List<Booking>, UiState<List<Booking>>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing bookings", rootError)
        emit(UiState.Error(rootError.message ?: "बुकिंग लोड करने में समस्या आई"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val favoriteHotelIds: StateFlow<Set<String>> =
    repository
      .observeFavoriteHotelIds(currentUserId)
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w(TAG, "Error observing favorites", firestoreError ?: error)
        emit(emptySet())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptySet(),
      )

  val favoriteHotels: StateFlow<List<Hotel>> =
    combine(hotelsState, favoriteHotelIds) { state, ids ->
        val sourceHotels = (state as? UiState.Success)?.data ?: HotelCatalog.hotels
        sourceHotels.filter { it.id in ids }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList(),
      )

  val bookingStats: StateFlow<Pair<Int, Int>> =
    combine(bookingsState, favoriteHotelIds) { bookingsUi, favs ->
        val activeCount =
          (bookingsUi as? UiState.Success)?.data?.count {
            it.status == Booking.STATUS_CONFIRMED
          } ?: 0
        activeCount to favs.size
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = 0 to 0,
      )

  fun findHotelById(hotelId: String): Hotel? {
    val liveHotels = (hotelsState.value as? UiState.Success)?.data.orEmpty()
    return liveHotels.firstOrNull { it.id == hotelId } ?: HotelCatalog.findById(hotelId)
  }

  fun updateQuery(query: String) {
    _searchCriteria.update { it.copy(query = query) }
  }

  fun selectCategory(category: HotelCategory) {
    _searchCriteria.update { it.copy(selectedCategory = category) }
  }

  fun selectSortOption(option: SortOption) {
    _searchCriteria.update { it.copy(sortOption = option) }
  }

  fun updateDates(checkInEpochDay: Long, checkOutEpochDay: Long) {
    val safeCheckOut =
      if (checkOutEpochDay > checkInEpochDay) checkOutEpochDay else checkInEpochDay + 1L
    _searchCriteria.update {
      it.copy(checkInEpochDay = checkInEpochDay, checkOutEpochDay = safeCheckOut)
    }
  }

  fun updateGuestsAndRooms(guests: Int, rooms: Int) {
    _searchCriteria.update {
      it.copy(guestsCount = guests.coerceIn(1, 16), roomsCount = rooms.coerceIn(1, 8))
    }
  }

  fun toggleFavorite(hotelId: String) {
    val currentlyFav = hotelId in favoriteHotelIds.value
    viewModelScope.launch {
      val result = repository.setFavorite(hotelId, !currentlyFav)
      if (result.isFailure) {
        _actionMessage.value = "पसंदीदा अपडेट नहीं हो सका"
      }
    }
  }

  fun bookHotel(
    hotel: Hotel,
    room: RoomOption,
    guestName: String,
    guestPhone: String?,
    specialRequests: String?,
  ) {
    if (guestName.isBlank()) {
      _bookingActionState.value = BookingActionState.Error("कृपया मेहमान का नाम दर्ज करें")
      return
    }
    val criteria = _searchCriteria.value
    val request =
      BookingRequest(
        hotel = hotel,
        room = room,
        guestName = guestName,
        guestPhone = guestPhone,
        specialRequests = specialRequests,
        checkInEpochDay = criteria.checkInEpochDay,
        checkOutEpochDay = criteria.checkOutEpochDay,
        nights = criteria.nights,
        guestsCount = criteria.guestsCount,
        roomsCount = criteria.roomsCount,
      )
    _bookingActionState.value = BookingActionState.Submitting
    viewModelScope.launch {
      val result = repository.createBooking(request)
      result
        .onSuccess { booking ->
          _bookingActionState.value = BookingActionState.Confirmed(booking)
        }
        .onFailure { error ->
          _bookingActionState.value =
            BookingActionState.Error(error.localizedMessage ?: "बुकिंग पूरी नहीं हो सकी, पुनः प्रयास करें")
        }
    }
  }

  fun resetBookingActionState() {
    _bookingActionState.value = BookingActionState.Idle
  }

  fun cancelBooking(bookingId: String) {
    viewModelScope.launch {
      val result = repository.cancelBooking(bookingId)
      _actionMessage.value =
        if (result.isSuccess) {
          "बुकिंग सफलतापूर्वक रद्द कर दी गई"
        } else {
          "बुकिंग रद्द नहीं हो सकी, कृपया पुनः प्रयास करें"
        }
    }
  }

  fun updateBookingNotes(bookingId: String, guestPhone: String?, specialRequests: String?) {
    viewModelScope.launch {
      val result = repository.updateBookingNotes(bookingId, guestPhone, specialRequests)
      _actionMessage.value =
        if (result.isSuccess) {
          "मेहमान विवरण अपडेट हो गया"
        } else {
          "विवरण अपडेट नहीं हो सका"
        }
    }
  }

  fun clearActionMessage() {
    _actionMessage.value = null
  }

  private companion object {
    const val TAG = "HotelBookingVM"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}

object FormatUtils {
  private val inrFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
  private val hindiMonths =
    arrayOf(
      "जनवरी",
      "फ़रवरी",
      "मार्च",
      "अप्रैल",
      "मई",
      "जून",
      "जुलाई",
      "अगस्त",
      "सितंबर",
      "अक्टूबर",
      "नवंबर",
      "दिसंबर",
    )

  fun formatInr(amount: Int): String = "₹${inrFormat.format(amount)}"

  fun formatInr(amount: Long): String = "₹${inrFormat.format(amount)}"

  fun formatEpochDay(epochDay: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = hindiMonths[cal.get(Calendar.MONTH)]
    return "$day $month"
  }

  fun formatEpochDayFull(epochDay: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = hindiMonths[cal.get(Calendar.MONTH)]
    val year = cal.get(Calendar.YEAR)
    return "$day $month $year"
  }
}
