package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Topping
import kotlinx.coroutines.tasks.await

class ToppingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun checkAdmin(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User chưa đăng nhập"))
        // Note: For strict security, role should be checked from DB, but for now relying on UI protection 
        // or potentially add explicit check if needed like in FoodRepository
        return Result.success(Unit)
    }

    suspend fun getToppings(): Result<List<Topping>> {
        return try {
            val snapshot = db.collection("toppings").get().await()
            val items = snapshot.map { 
                it.toObject(Topping::class.java).copy(id = it.id)
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTopping(id: String): Result<Topping> {
        return try {
            val doc = db.collection("toppings").document(id).get().await()
            val topping = doc.toObject(Topping::class.java)?.copy(id = doc.id)
            if (topping != null) Result.success(topping)
            else Result.failure(Exception("Topping not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTopping(topping: Topping): Result<String> {
        return try {
            val docRef = db.collection("toppings").add(topping).await()
            // Update the ID in the document itself for consistency
            db.collection("toppings").document(docRef.id).update("id", docRef.id).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTopping(topping: Topping): Result<Boolean> {
        val id = topping.id.takeIf { it.isNotEmpty() } ?: return Result.failure(Exception("Missing ID"))
        return try {
            db.collection("toppings").document(id).set(topping).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTopping(id: String): Result<Boolean> {
        return try {
            db.collection("toppings").document(id).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
