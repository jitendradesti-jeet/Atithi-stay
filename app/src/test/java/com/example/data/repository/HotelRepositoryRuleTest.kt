package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Hotel
import com.example.data.model.HotelCategory
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class HotelRepositoryRuleTest : FirestoreEmulatorTestBase() {

  private fun sampleHotel(id: String): Hotel =
    Hotel(
      id = id,
      name = "रॉयल लेक व्यू होटल",
      location = "फतेह सागर रोड, उदयपुर, राजस्थान",
      price = 6400L,
      description = "झील के शांत किनारे और पारंपरिक राजस्थानी आतिथ्य वाला बुटीक होटल।",
      city = "उदयपुर",
      state = "राजस्थान",
      categoryName = HotelCategory.PALACE.name,
      rating = 4.8,
      reviewCount = 210L,
      featured = true,
    )

  @Test
  fun addAndFetchHotels_authenticatedUser_succeeds() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = HotelRepository(firestore, auth)
    val hotelId = "hotel_${UUID.randomUUID().toString().replace("-", "").take(12)}"

    val addResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.addHotel(sampleHotel(hotelId)) }
    assertTrue("Expected addHotel to succeed: ${addResult.exceptionOrNull()}", addResult.isSuccess)
    val saved = addResult.getOrThrow()
    assertEquals(hotelId, saved.id)
    assertEquals("रॉयल लेक व्यू होटल", saved.name)
    assertEquals("फतेह सागर रोड, उदयपुर, राजस्थान", saved.location)
    assertEquals(6400L, saved.price)

    val fetched = withTimeout(DEFAULT_TIMEOUT_MS) { repo.fetchHotels().getOrThrow() }
    assertTrue(fetched.any { it.id == hotelId && it.name == "रॉयल लेक व्यू होटल" })

    val single = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getHotelById(hotelId).getOrThrow() }
    assertNotNull(single)
    assertEquals(hotelId, single?.id)
  }

  @Test
  fun observeHotels_authenticatedUser_emitsUpdates() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = HotelRepository(firestore, auth)
    val hotelId = "hotel_${UUID.randomUUID().toString().replace("-", "").take(12)}"

    withTimeout(DEFAULT_TIMEOUT_MS) { repo.addHotel(sampleHotel(hotelId)).getOrThrow() }
    val emitted =
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeHotels().first { hotels -> hotels.any { it.id == hotelId } }
      }
    assertTrue(emitted.any { it.id == hotelId })
  }

  @Test
  fun seedInitialHotelsIfEmpty_authenticatedUser_populatesCatalog() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = HotelRepository(firestore, auth)

    val seedResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.seedInitialHotelsIfEmpty() }
    assertTrue("Expected seeding to succeed: ${seedResult.exceptionOrNull()}", seedResult.isSuccess)

    val hotels = withTimeout(DEFAULT_TIMEOUT_MS) { repo.fetchHotels().getOrThrow() }
    assertTrue(hotels.isNotEmpty())
  }

  @Test
  fun observeHotels_unauthenticatedUser_failsWithIllegalStateOrPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = HotelRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) { repo.observeHotels().first() }
      fail("Expected failure for unauthenticated caller")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertTrue(
        e is IllegalStateException ||
          firestoreException?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
      )
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice.hotels@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
