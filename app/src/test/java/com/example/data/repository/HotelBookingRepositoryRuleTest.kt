package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.catalog.HotelCatalog
import com.example.data.model.Booking
import com.example.data.model.BookingRequest
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class HotelBookingRepositoryRuleTest : FirestoreEmulatorTestBase() {

  private fun sampleRequest(): BookingRequest {
    val hotel = HotelCatalog.hotels.first()
    val room = hotel.rooms.first()
    return BookingRequest(
      hotel = hotel,
      room = room,
      guestName = "आरव शर्मा",
      guestPhone = "9876543210",
      specialRequests = "अर्ली चेक-इन",
      checkInEpochDay = 20400L,
      checkOutEpochDay = 20402L,
      nights = 2,
      guestsCount = 2,
      roomsCount = 1,
    )
  }

  @Test
  fun createAndObserveBooking_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = HotelBookingRepository(firestore, auth)

    val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.createBooking(sampleRequest()) }
    assertTrue("Expected booking creation to succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)
    val booking = createResult.getOrThrow()
    assertTrue(booking.id.isNotBlank())
    assertEquals(Booking.STATUS_CONFIRMED, booking.status)

    val list =
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeBookings(uid).first { bookings -> bookings.any { it.id == booking.id } }
      }
    assertTrue(list.any { it.id == booking.id && it.hotelName == HotelCatalog.hotels.first().name })
  }

  @Test
  fun cancelBooking_authenticatedOwner_transitionsToCancelled() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = HotelBookingRepository(firestore, auth)

    val booking = withTimeout(DEFAULT_TIMEOUT_MS) { repo.createBooking(sampleRequest()).getOrThrow() }
    val cancelResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.cancelBooking(booking.id) }
    assertTrue("Expected cancelBooking to succeed: ${cancelResult.exceptionOrNull()}", cancelResult.isSuccess)

    val updated = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getBookingById(uid, booking.id).getOrThrow() }
    assertEquals(Booking.STATUS_CANCELLED, updated?.status)
  }

  @Test
  fun getBookingById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = HotelBookingRepository(firestore, auth)
    val booking = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.createBooking(sampleRequest()).getOrThrow() }

    signInTestUser(BOB_EMAIL)
    val bobRepo = HotelBookingRepository(firestore, auth)
    val result = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getBookingById(aliceUid, booking.id) }
    assertTrue(result.isFailure)
    val firestoreException = result.exceptionOrNull()?.unwrapFirestoreException()
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
  }

  @Test
  fun observeBookings_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = HotelBookingRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) { repo.observeBookings("some_uid").first() }
      fail("Expected PERMISSION_DENIED for unauthenticated caller")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  @Test
  fun toggleFavorite_ownerCanAddAndRemoveFavorite() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = HotelBookingRepository(firestore, auth)
    val hotelId = HotelCatalog.hotels.first().id

    val addRes = withTimeout(DEFAULT_TIMEOUT_MS) { repo.setFavorite(hotelId, true) }
    assertTrue(addRes.isSuccess)

    val favs =
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeFavoriteHotelIds(uid).first { it.contains(hotelId) }
      }
    assertTrue(favs.contains(hotelId))

    val removeRes = withTimeout(DEFAULT_TIMEOUT_MS) { repo.setFavorite(hotelId, false) }
    assertTrue(removeRes.isSuccess)
  }

  private companion object {
    const val ALICE_EMAIL = "alice.atithi@test.com"
    const val BOB_EMAIL = "bob.atithi@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
