package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.muatrenthenang.resfood.data.model.User

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    //kiểm tra user
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Hàm đăng nhập
    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            // 1. Đăng nhập vào Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            // 2. Lấy thông tin chi tiết user từ Firestore để check role
            if (firebaseUser != null) {
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Không tìm thấy dữ liệu người dùng"))
                }
            } else {
                Result.failure(Exception("Lỗi đăng nhập: User null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm đăng ký chuẩn
    suspend fun register(email: String, pass: String, fullName: String): Result<Boolean> {
        return try {
            // 1. Tạo tài khoản Auth (Email + Pass)
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw Exception("Không lấy được thông tin User")
            val userId = user.uid

            // 2. Gửi email xác thực ngay lập tức
            // (Hàm này sẽ gửi 1 email từ Firebase đến hòm thư người dùng)
            user.sendEmailVerification().await()

            // 3. Chuẩn bị dữ liệu user để lưu Firestore
            val userMap = hashMapOf(
                "id" to userId,
                "email" to email,
                "fullName" to fullName,
                "role" to "customer", // Mặc định là khách hàng
                "createdAt" to System.currentTimeMillis()
            )

            // 4. Lưu vào Firestore Database
            db.collection("users").document(userId).set(userMap).await()

            Result.success(true)
        } catch (e: Exception) {
            // Nếu lỗi (ví dụ email đã tồn tại, mật khẩu yếu...), trả về lỗi để ViewModel hiển thị
            Result.failure(e)
        }
    }

    // Hàm gửi email đặt lại mật khẩu (Forgot Password)
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kiểm tra xem user đã đăng nhập chưa
    fun isUserLoggedIn(): Boolean {
        // Lưu ý: Nếu muốn chặt chẽ, có thể check thêm auth.currentUser?.isEmailVerified == true
        return auth.currentUser != null
    }

    // Hàm đăng xuất (cho nút Đăng xuất sau này)
    fun logout() {
        auth.signOut()
    }

    suspend fun loginWithGoogle(idToken: String): Result<Boolean> {
        return try {
            // 1. Tạo chứng chỉ từ Token của Google
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)

            // 2. Đăng nhập vào Firebase
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Lỗi đăng nhập Google")

            // 3. Kiểm tra xem user này đã có trong Database chưa?
            // (Nếu lần đầu đăng nhập bằng Google thì phải tạo dữ liệu user mới)
            val docSnapshot = db.collection("users").document(user.uid).get().await()

            if (!docSnapshot.exists()) {
                val userMap = hashMapOf(
                    "id" to user.uid,
                    "email" to user.email,
                    "fullName" to user.displayName, // Lấy tên từ Google
                    "role" to "customer",
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("users").document(user.uid).set(userMap).await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm lấy thông tin chi tiết User từ Firestore
    suspend fun getUserDetails(userId: String): Result<User> {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Dữ liệu user bị lỗi"))
                }
            } else {
                Result.failure(Exception("Không tìm thấy user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm lấy ID người dùng hiện tại
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}