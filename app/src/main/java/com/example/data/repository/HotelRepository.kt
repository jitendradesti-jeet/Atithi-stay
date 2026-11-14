package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.catalog.HotelCatalog
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.Hotel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HotelRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(
    context: Context
  ) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  fun observeHotels(): Flow<List<Hotel>> = flow {
    requireUserId()
    val path = HOTELS_COLLECTION
    emitAll(
      db.collection(HOTELS_COLLECTION)
        .whereEqualTo("isActive", true)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(Hotel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { if (it.featured) 1 else 0 }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun fetchHotels(): Result<List<Hotel>> {
    val path = HOTELS_COLLECTION
    return try {
      requireUserId()
      val snapshot =
        db.collection(HOTELS_COLLECTION).whereEqualTo("isActive", true).get().await()
      val hotels =
        snapshot
          .toObjects(Hotel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { if (it.featured) 1 else 0 }
      Result.success(hotels)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, path)
      Result.failure(e)
    }
  }

  suspend fun getHotelById(hotelId: String): Result<Hotel?> {
    val docRef = db.collection(HOTELS_COLLECTION).document(hotelId)
    return try {
      requireUserId()
      val snapshot = docRef.get().await()
      Result.success(
        snapshot.toObject(Hotel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun addHotel(hotel: Hotel): Result<Hotel> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef =
      if (hotel.id.isNotBlank()) {
        db.collection(HOTELS_COLLECTION).document(hotel.id)
      } else {
        db.collection(HOTELS_COLLECTION).document()
      }

    val payload = buildHotelPayload(hotel, uid)
    return try {
      docRef.set(payload).await()
      val saved =
        docRef
          .get()
          .await()
          .toObject(Hotel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          ?: hotel.copy(id = docRef.id, createdBy = uid)
      Result.success(saved)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun seedInitialHotelsIfEmpty(): Result<Unit> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    return try {
      val existingSnapshot =
        db.collection(HOTELS_COLLECTION).whereEqualTo("isActive", true).limit(1).get().await()
      if (!existingSnapshot.isEmpty) {
        return Result.success(Unit)
      }
      val batch = db.batch()
      HotelCatalog.hotels.forEach { hotel ->
        val docRef = db.collection(HOTELS_COLLECTION).document(hotel.id)
        batch.set(docRef, buildHotelPayload(hotel, uid))
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, HOTELS_COLLECTION)
      Result.failure(e)
    }
  }

  private fun buildHotelPayload(hotel: Hotel, uid: String): Map<String, Any> {
    val roomMaps =
      hotel.rooms.map { room ->
        mapOf(
          "id" to room.id,
          "name" to room.name,
          "bedType" to room.bedType,
          "maxGuests" to room.maxGuests,
          "pricePerNight" to room.pricePerNight,
          "breakfastIncluded" to room.breakfastIncluded,
          "highlights" to room.highlights,
        )
      }
    return mapOf(
      "name" to hotel.name,
      "location" to hotel.location,
      "price" to hotel.price,
      "description" to hotel.description,
      "city" to hotel.city,
      "state" to hotel.state,
      "categoryName" to hotel.categoryName,
      "rating" to hotel.rating,
      "reviewCount" to hotel.reviewCount,
      "imageKey" to hotel.imageKey,
      "amenities" to hotel.amenities,
      "checkInTime" to hotel.checkInTime,
      "checkOutTime" to hotel.checkOutTime,
      "rooms" to roomMaps,
      "featured" to hotel.featured,
      "isActive" to true,
      "createdBy" to uid,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
  }

  companion object {
    const val HOTELS_COLLECTION = "hotels"
  }
}
