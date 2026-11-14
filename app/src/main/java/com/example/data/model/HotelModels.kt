package com.example.data.model

import com.example.data.catalog.HotelCatalog
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class HotelCategory(val labelHi: String) {
  ALL("सभी"),
  PALACE("रॉयल पैलेस"),
  BEACH("बीच रिसॉर्ट"),
  MOUNTAIN("पहाड़ी विला"),
  LUXURY("लक्ज़री स्टे"),
}

@IgnoreExtraProperties
data class RoomOption(
  val id: String = "",
  val name: String = "",
  val bedType: String = "",
  val maxGuests: Int = 2,
  val pricePerNight: Int = 0,
  val breakfastIncluded: Boolean = true,
  val highlights: List<String> = emptyList(),
)

@IgnoreExtraProperties
data class Hotel(
  @DocumentId val id: String = "",
  val name: String = "",
  val location: String = "",
  val price: Long = 0L,
  val description: String = "",
  val city: String = "",
  val state: String = "",
  val categoryName: String = HotelCategory.LUXURY.name,
  val rating: Double = 4.5,
  val reviewCount: Long = 0L,
  val imageKey: String = HotelCatalog.KEY_SUITE,
  val amenities: List<String> = emptyList(),
  val checkInTime: String = "2:00 PM",
  val checkOutTime: String = "11:00 AM",
  val rooms: List<RoomOption> = emptyList(),
  val featured: Boolean = false,
  val isActive: Boolean = true,
  val createdBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  @get:Exclude
  val category: HotelCategory
    get() = HotelCategory.entries.firstOrNull { it.name == categoryName } ?: HotelCategory.LUXURY

  @get:Exclude
  val startingPrice: Int
    get() = price.toInt()

  @get:Exclude
  val imageResId: Int
    get() = HotelCatalog.drawableForKey(imageKey)
}

@IgnoreExtraProperties
data class Booking(
  @DocumentId val id: String = "",
  val userId: String = "",
  val hotelId: String = "",
  val hotelName: String = "",
  val hotelCity: String = "",
  val hotelImageKey: String = "",
  val roomType: String = "",
  val guestName: String = "",
  val guestPhone: String? = null,
  val specialRequests: String? = null,
  val checkInEpochDay: Long = 0L,
  val checkOutEpochDay: Long = 0L,
  val nights: Long = 1L,
  val guestsCount: Long = 2L,
  val roomsCount: Long = 1L,
  val pricePerNight: Long = 0L,
  val taxesAndFees: Long = 0L,
  val totalAmount: Long = 0L,
  val bookingCode: String = "",
  val status: String = STATUS_CONFIRMED,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  companion object {
    const val STATUS_CONFIRMED = "confirmed"
    const val STATUS_CANCELLED = "cancelled"
  }
}

@IgnoreExtraProperties
data class FavoriteHotel(
  @DocumentId val id: String = "",
  val userId: String = "",
  val hotelId: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

data class BookingRequest(
  val hotel: Hotel,
  val room: RoomOption,
  val guestName: String,
  val guestPhone: String?,
  val specialRequests: String?,
  val checkInEpochDay: Long,
  val checkOutEpochDay: Long,
  val nights: Int,
  val guestsCount: Int,
  val roomsCount: Int,
) {
  val roomSubtotal: Int
    get() = room.pricePerNight * nights * roomsCount

  val taxesAndFees: Int
    get() = (roomSubtotal * 0.12).toInt()

  val totalAmount: Int
    get() = roomSubtotal + taxesAndFees
}
