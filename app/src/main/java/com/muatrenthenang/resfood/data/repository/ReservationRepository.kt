package com.muatrenthenang.resfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Reservation
import kotlinx.coroutines.tasks.await

class ReservationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val reservationsRef = db.collection("reservations")

    suspend fun createReservation(reservation: Reservation): Result<Boolean> {
        return try {
            val docRef = reservationsRef.document()
            val finalRes = reservation.copy(id = docRef.id)
            docRef.set(finalRes).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReservationsByDate(startOfDay: Long, endOfDay: Long): Result<List<Reservation>> {
        return try {
            // Providing simple query for now. Real implementations usually range query on 'time'
            // For simplicity, we get all and filter locally or limit to recent 100
             val snapshot = reservationsRef.orderBy("time").get().await()
             val all = snapshot.toObjects(Reservation::class.java)
             // Filter logic can be here or database side
             Result.success(all)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReservationStatus(id: String, status: String): Result<Boolean> {
        return try {
            reservationsRef.document(id).update("status", status).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
