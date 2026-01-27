package com.muatrenthenang.resfood.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.muatrenthenang.resfood.data.model.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull

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

            // 2. Tạo doc user trong Firestore
            val newUser = User(
                id = userId,
                fullName = fullName,
                email = email,
                role = "customer"
            )
            db.collection("users").document(userId).set(newUser).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm đăng xuất
    fun logout() {
        auth.signOut()
    }

    // Hàm lấy ID user hiện tại
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Hàm lấy thông tin user từ Firestore
    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User không tồn tại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm reset mật khẩu
    suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kiểm tra xem email đã tồn tại trong hệ thống chưa (trừ email của user hiện tại)
     */
    suspend fun checkEmailExists(email: String, currentUserId: String): Boolean {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            
            // Nếu tìm thấy user với email này
            if (!querySnapshot.isEmpty) {
                // Kiểm tra xem có phải là email của chính user hiện tại không
                val existingUser = querySnapshot.documents.firstOrNull()
                existingUser?.id != currentUserId
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cập nhật thông tin user
     */
    suspend fun updateUser(
        userId: String,
        fullName: String,
        phone: String?,
        email: String
    ): Result<Boolean> {
        return try {
            // Kiểm tra email có bị trùng không (nếu email thay đổi)
            val currentUser = getUserProfile(userId).getOrNull()
            if (currentUser != null && currentUser.email != email) {
                val emailExists = checkEmailExists(email, userId)
                if (emailExists) {
                    return Result.failure(Exception("Email đã được sử dụng"))
                }
            }

            val updates = hashMapOf<String, Any>(
                "fullName" to fullName,
                "email" to email
            )
            if (phone != null) {
                updates["phone"] = phone
            }

            db.collection("users")
                .document(userId)
                .update(updates)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle theme preference
     */
    suspend fun toggleTheme(userId: String, isDarkTheme: Boolean): Result<Boolean> {
        return try {
            db.collection("users")
                .document(userId)
                .update("isDarkTheme", isDarkTheme)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload avatar lên ImgBB và cập nhật URL vào Firestore
     */
    suspend fun uploadAvatar(imageUri: Uri, context: android.content.Context): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("Người dùng chưa đăng nhập")
            
            // Đọc ảnh từ URI và tạo file tạm
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Không thể đọc ảnh")
            
            val tempFile = java.io.File(context.cacheDir, "temp_avatar_$userId.jpg")
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Tạo RequestBody và MultipartBody.Part
            val requestFile = okhttp3.RequestBody.create(
                "image/*".toMediaTypeOrNull(),
                tempFile
            )
            val imagePart = okhttp3.MultipartBody.Part.createFormData(
                "image",
                tempFile.name,
                requestFile
            )
            
            // Upload lên ImgBB
            val response = com.muatrenthenang.resfood.data.api.ImgBBUploader.api.uploadImage(
                apiKey = com.muatrenthenang.resfood.data.api.ImgBBUploader.getApiKey(),
                image = imagePart
            )
            
            // Xóa file tạm
            tempFile.delete()
            
            if (!response.success || response.data == null) {
                throw Exception("Upload ảnh thất bại")
            }
            
            val imageUrl = response.data.display_url
            
            // Cập nhật URL vào Firestore
            db.collection("users")
                .document(userId)
                .update("avatarUrl", imageUrl)
                .await()
            
            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Đổi mật khẩu người dùng
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: throw Exception("Người dùng chưa đăng nhập")
            
            // Re-authenticate user với mật khẩu hiện tại
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                user.email ?: throw Exception("Email không hợp lệ"),
                currentPassword
            )
            user.reauthenticate(credential).await()
            
            // Cập nhật mật khẩu mới
            user.updatePassword(newPassword).await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Đăng nhập bằng Google Sign-In
     */
    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            // 1. Tạo credential từ Google ID Token
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            
            // 2. Đăng nhập vào Firebase Auth
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Không lấy được thông tin user từ Google")
            
            val userId = firebaseUser.uid
            
            // 3. Kiểm tra xem user đã tồn tại trong Firestore chưa
            val userDoc = db.collection("users").document(userId).get().await()
            
            val user = if (userDoc.exists()) {
                // User đã tồn tại, lấy thông tin
                userDoc.toObject(User::class.java) ?: throw Exception("Không thể parse user data")
            } else {
                // User mới, tạo profile
                val newUser = User(
                    id = userId,
                    fullName = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: "",
                    role = "customer",
                    avatarUrl = firebaseUser.photoUrl?.toString(),
                    phone = firebaseUser.phoneNumber
                )
                
                // Lưu vào Firestore
                db.collection("users").document(userId).set(newUser).await()
                newUser
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}