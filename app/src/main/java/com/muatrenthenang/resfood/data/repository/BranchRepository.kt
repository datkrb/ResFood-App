package com.muatrenthenang.resfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Branch
import kotlinx.coroutines.tasks.await

/**
 * Repository để quản lý chi nhánh nhà hàng
 * App chỉ sử dụng 1 chi nhánh duy nhất
 */
class BranchRepository {
    private val db = FirebaseFirestore.getInstance()
    private val branchesRef = db.collection("branches")

    /**
     * Lấy chi nhánh chính (duy nhất) của nhà hàng
     * Nếu chưa có, sẽ tạo mới với thông tin mặc định
     */
    suspend fun getPrimaryBranch(): Result<Branch> {
        return try {
            val doc = branchesRef.document(Branch.PRIMARY_BRANCH_ID).get().await()
            
            if (doc.exists()) {
                val branch = doc.toObject(Branch::class.java)
                if (branch != null) {
                    branch.id = doc.id
                    Result.success(branch)
                } else {
                    // Doc exists but failed to parse, create new
                    val newBranch = createDefaultBranch()
                    Result.success(newBranch)
                }
            } else {
                // Branch doesn't exist, create default
                val newBranch = createDefaultBranch()
                Result.success(newBranch)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tạo chi nhánh mặc định
     */
    private suspend fun createDefaultBranch(): Branch {
        val defaultBranch = Branch.createDefault()
        branchesRef.document(Branch.PRIMARY_BRANCH_ID).set(defaultBranch).await()
        return defaultBranch
    }

    /**
     * Cập nhật thông tin chi nhánh
     */
    suspend fun updateBranch(branch: Branch): Result<Boolean> {
        return try {
            branchesRef.document(Branch.PRIMARY_BRANCH_ID).set(branch).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
