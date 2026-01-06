package com.muatrenthenang.resfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Promotion
import kotlinx.coroutines.tasks.await

class PromotionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val promosRef = db.collection("promotions")

    suspend fun createPromotion(promotion: Promotion): Result<Boolean> {
        return try {
            val docRef = promosRef.document()
            val finalPromo = promotion.copy(id = docRef.id)
            docRef.set(finalPromo).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllPromotions(): Result<List<Promotion>> {
        return try {
            val snapshot = promosRef.get().await()
            val promos = snapshot.toObjects(Promotion::class.java)
            Result.success(promos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
