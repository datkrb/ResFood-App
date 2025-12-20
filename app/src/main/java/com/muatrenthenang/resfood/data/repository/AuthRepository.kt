package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance() // <-- BẮT BUỘC PHẢI CÓ DÒNG NÀY thì mới lưu Database được

    // Hàm đăng nhập
    suspend fun login(email: String, pass: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm đăng ký chuẩn (Đã xóa hàm cũ 2 tham số đi cho đỡ nhầm)
    suspend fun register(email: String, pass: String, fullName: String): Result<Boolean> {
        return try {
            // 1. Tạo tài khoản Auth (Email + Pass)
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = authResult.user?.uid ?: throw Exception("Không lấy được User ID")

            // 2. Chuẩn bị dữ liệu user
            val userMap = hashMapOf(
                "id" to userId,
                "email" to email,
                "fullName" to fullName,
                "role" to "customer", // Mặc định là khách hàng
                "createdAt" to System.currentTimeMillis()
            )

            // 3. Lưu vào Firestore Database (Cần biến db ở trên)
            db.collection("users").document(userId).set(userMap).await()

            Result.success(true)
        } catch (e: Exception) {
            // Nếu tạo Auth thành công mà lưu Database thất bại -> Có thể xóa Auth đi để user đăng ký lại (Option nâng cao)
            Result.failure(e)
        }
    }

    // Kiểm tra xem user đã đăng nhập chưa
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}