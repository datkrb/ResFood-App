package com.muatrenthenang.resfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Address
import com.muatrenthenang.resfood.data.model.Branch
import kotlinx.coroutines.tasks.await

class BranchRepository {
    private val db = FirebaseFirestore.getInstance()
    private val branchesRef = db.collection("branches")

    suspend fun getBranches(): Result<List<Branch>> {
        return try {
            val snapshot = branchesRef.get().await()
            val branches = snapshot.documents.mapNotNull { it.toObject(Branch::class.java) }
            Result.success(branches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
