package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.Booking
import com.example.data.model.BookingRequest
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
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
import kotlin.random.Random

class HotelBookingRepository(
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

  fun observeBookings(userId: String): Flow<List<Booking>> = flow {
    val path = "users/$userId/bookings"
    emitAll(
      db.collection("users")
        .document(userId)
        .collection("bookings")
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(Booking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  fun observeFavoriteHotelIds(userId: String): Flow<Set<String>> = flow {
    val path = "users/$userId/favorites"
    emitAll(
      db.collection("users")
        .document(userId)
        .collection("favorites")
        .snapshots()
        .map { snapshot -> snapshot.documents.map { it.id }.toSet() }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun createBooking(request: BookingRequest): Result<Booking> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid).collection("bookings").document()
    val bookingCode = "ATH-${Random.nextInt(1000, 9999)}${('A'..'Z').random()}"

    val payload =
      mapOf(
          "userId" to uid,
          "hotelId" to request.hotel.id,
          "hotelName" to request.hotel.name,
          "hotelCity" to request.hotel.city,
          "hotelImageKey" to request.hotel.imageKey,
          "roomType" to request.room.name,
          "guestName" to request.guestName.trim(),
          "guestPhone" to request.guestPhone?.trim()?.takeIf { it.isNotEmpty() },
          "specialRequests" to request.specialRequests?.trim()?.takeIf { it.isNotEmpty() },
          "checkInEpochDay" to request.checkInEpochDay,
          "checkOutEpochDay" to request.checkOutEpochDay,
          "nights" to request.nights.toLong(),
          "guestsCount" to request.guestsCount.toLong(),
          "roomsCount" to request.roomsCount.toLong(),
          "pricePerNight" to request.room.pricePerNight.toLong(),
          "taxesAndFees" to request.taxesAndFees.toLong(),
          "totalAmount" to request.totalAmount.toLong(),
          "bookingCode" to bookingCode,
          "status" to Booking.STATUS_CONFIRMED,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        .filterValues { it != null } as Map<String, Any>

    return try {
      docRef.set(payload).await()
      val saved =
        docRef
          .get()
          .await()
          .toObject(Booking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          ?: Booking(
            id = docRef.id,
            userId = uid,
            hotelId = request.hotel.id,
            hotelName = request.hotel.name,
            hotelCity = request.hotel.city,
            hotelImageKey = request.hotel.imageKey,
            roomType = request.room.name,
            guestName = request.guestName.trim(),
            guestPhone = request.guestPhone?.trim()?.takeIf { it.isNotEmpty() },
            specialRequests = request.specialRequests?.trim()?.takeIf { it.isNotEmpty() },
            checkInEpochDay = request.checkInEpochDay,
            checkOutEpochDay = request.checkOutEpochDay,
            nights = request.nights.toLong(),
            guestsCount = request.guestsCount.toLong(),
            roomsCount = request.roomsCount.toLong(),
            pricePerNight = request.room.pricePerNight.toLong(),
            taxesAndFees = request.taxesAndFees.toLong(),
            totalAmount = request.totalAmount.toLong(),
            bookingCode = bookingCode,
            status = Booking.STATUS_CONFIRMED,
          )
      Result.success(saved)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun cancelBooking(bookingId: String): Result<Unit> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
    return try {
      docRef
        .update(
          mapOf(
            "status" to Booking.STATUS_CANCELLED,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateBookingNotes(
    bookingId: String,
    guestPhone: String?,
    specialRequests: String?,
  ): Result<Unit> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
    val updates =
      mapOf(
        "guestPhone" to (guestPhone?.trim() ?: ""),
        "specialRequests" to (specialRequests?.trim() ?: ""),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    return try {
      docRef.update(updates).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun getBookingById(userId: String, bookingId: String): Result<Booking?> {
    val docRef = db.collection("users").document(userId).collection("bookings").document(bookingId)
    return try {
      val snapshot = docRef.get().await()
      Result.success(
        snapshot.toObject(Booking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun setFavorite(hotelId: String, isFavorite: Boolean): Result<Unit> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid).collection("favorites").document(hotelId)
    return try {
      if (isFavorite) {
        docRef
          .set(
            mapOf(
              "userId" to uid,
              "hotelId" to hotelId,
              "createdAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      } else {
        docRef.delete().await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(
        e,
        if (isFavorite) OperationType.CREATE else OperationType.DELETE,
        docRef.path,
      )
      Result.failure(e)
    }
  }
}
