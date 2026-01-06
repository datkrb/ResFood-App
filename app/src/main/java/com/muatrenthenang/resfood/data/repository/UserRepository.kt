package com.muatrenthenang.resfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    suspend fun getAllCustomers(): Result<List<User>> {
        return try {
            val snapshot = usersRef.whereEqualTo("role", "customer").get().await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
