package com.muatrenthenang.resfood.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.TableReservation
import kotlinx.coroutines.tasks.await

class ReservationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val reservationsRef = db.collection("reservations")

    suspend fun createReservation(reservation: TableReservation): Result<String> {
        return try {
            val docRef = reservationsRef.document()
            val finalReservation = reservation.copy(id = docRef.id)
            docRef.set(finalReservation).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if the branch has enough capacity for the requested time.
     * Simple logic: Sum guests of confirmed/pending reservations within +/- 2 hours of requested time.
     */
    suspend fun checkAvailability(
        branchId: String,
        requestedTime: Timestamp,
        requestedGuests: Int,
        maxCapacity: Int
    ): Result<Boolean> {
        return try {
            // Define time window: +/- 2 hours (7200 seconds)
            val windowSeconds = 7200L
            val startWindow = Timestamp(requestedTime.seconds - windowSeconds, 0)
            val endWindow = Timestamp(requestedTime.seconds + windowSeconds, 0)

            val snapshot = reservationsRef
                .whereEqualTo("branch_id", branchId)
                .whereGreaterThanOrEqualTo("time_slot", startWindow)
                .whereLessThanOrEqualTo("time_slot", endWindow)
                .get()
                .await()

            var currentGuests = 0
            for (doc in snapshot.documents) {
                val status = doc.getString("status") ?: "PENDING"
                if (status != "CANCELLED") {
                    val adult = doc.getLong("guest_count_adult")?.toInt() ?: 0
                    val child = doc.getLong("guest_count_child")?.toInt() ?: 0
                    currentGuests += (adult + child)
                }
            }

            if (currentGuests + requestedGuests <= maxCapacity) {
                Result.success(true)
            } else {
                Result.success(false) // Not available
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReservationsByUser(userId: String): Result<List<TableReservation>> {
         return try {
            val snapshot = reservationsRef
                .whereEqualTo("user_id", userId)
                .get()
                .await()
            
            val list = snapshot.documents.mapNotNull { it.toObject(TableReservation::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
