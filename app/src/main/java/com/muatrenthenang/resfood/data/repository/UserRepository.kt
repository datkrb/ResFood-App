package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Address
import com.muatrenthenang.resfood.data.model.User
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersRef = db.collection("users")

    /**
     * Lấy ID của user đang đăng nhập
     */
    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Lấy tất cả khách hàng (cho admin)
     */
    suspend fun getAllCustomers(): Result<List<User>> {
        return try {
            val snapshot = usersRef.whereEqualTo("role", "customer").get().await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ADDRESS CRUD OPERATIONS ====================

    /**
     * Lấy danh sách địa chỉ của user hiện tại
     */
    suspend fun getAddresses(): Result<List<Address>> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
            val userDoc = usersRef.document(userId).get().await()
            
            if (!userDoc.exists()) {
                return Result.failure(Exception("Không tìm thấy user"))
            }

            // Lấy mảng addresses từ document
            @Suppress("UNCHECKED_CAST")
            val addressesList = userDoc.get("addresses") as? List<Map<String, Any>> ?: emptyList()
            
            val addresses = addressesList.map { map ->
                Address(
                    id = map["id"] as? String ?: UUID.randomUUID().toString(),
                    label = map["label"] as? String ?: "Nhà riêng",
                    addressLine = map["addressLine"] as? String ?: "",
                    ward = map["ward"] as? String ?: "",
                    district = map["district"] as? String ?: "",
                    city = map["city"] as? String ?: "",
                    contactName = map["contactName"] as? String ?: "",
                    phone = map["phone"] as? String ?: "",
                    isDefault = map["isDefault"] as? Boolean ?: false,
                    latitude = map["latitude"] as? Double,
                    longitude = map["longitude"] as? Double,
                    createdAt = (map["createdAt"] as? Long) ?: System.currentTimeMillis()
                )
            }
            
            Result.success(addresses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    /**
     * Thêm địa chỉ mới
     */
    suspend fun addAddress(address: Address): Result<Address> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            // Tạo ID mới nếu chưa có
            val newAddress = if (address.id.isBlank()) {
                address.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis())
            } else {
                address.copy(createdAt = System.currentTimeMillis())
            }
            
            // Lấy danh sách địa chỉ hiện tại
            val currentAddresses = getAddresses().getOrNull()?.toMutableList() ?: mutableListOf()
            
            // Nếu địa chỉ mới là mặc định, bỏ mặc định của các địa chỉ khác
            val updatedList = if (newAddress.isDefault) {
                currentAddresses.map { it.copy(isDefault = false) } + newAddress
            } else {
                // Nếu là địa chỉ đầu tiên, tự động đặt làm mặc định
                if (currentAddresses.isEmpty()) {
                    listOf(newAddress.copy(isDefault = true))
                } else {
                    currentAddresses + newAddress
                }
            }
            
            // Lưu lại vào Firestore
            val addressMaps = updatedList.map { addr -> addressToMap(addr) }
            usersRef.document(userId).update("addresses", addressMaps).await()
            
            Result.success(newAddress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật địa chỉ
     */
    suspend fun updateAddress(address: Address): Result<Address> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            // Lấy danh sách địa chỉ hiện tại
            val currentAddresses = getAddresses().getOrNull()?.toMutableList() 
                ?: return Result.failure(Exception("Không thể lấy danh sách địa chỉ"))
            
            // Tìm và cập nhật địa chỉ
            val updatedList = if (address.isDefault) {
                // Nếu địa chỉ này là mặc định, bỏ mặc định của các địa chỉ khác
                currentAddresses.map { addr ->
                    if (addr.id == address.id) address
                    else addr.copy(isDefault = false)
                }
            } else {
                currentAddresses.map { addr ->
                    if (addr.id == address.id) address else addr
                }
            }
            
            // Lưu lại vào Firestore
            val addressMaps = updatedList.map { addr -> addressToMap(addr) }
            usersRef.document(userId).update("addresses", addressMaps).await()
            
            Result.success(address)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa địa chỉ
     */
    suspend fun deleteAddress(addressId: String): Result<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            // Lấy danh sách địa chỉ hiện tại
            val currentAddresses = getAddresses().getOrNull()?.toMutableList()
                ?: return Result.failure(Exception("Không thể lấy danh sách địa chỉ"))
            
            val deletedAddress = currentAddresses.find { it.id == addressId }
            val filteredList = currentAddresses.filter { it.id != addressId }
            
            // Nếu xóa địa chỉ mặc định, đặt địa chỉ đầu tiên còn lại làm mặc định
            val finalList = if (deletedAddress?.isDefault == true && filteredList.isNotEmpty()) {
                filteredList.mapIndexed { index, addr ->
                    if (index == 0) addr.copy(isDefault = true) else addr
                }
            } else {
                filteredList
            }
            
            // Lưu lại vào Firestore
            val addressMaps = finalList.map { addr -> addressToMap(addr) }
            usersRef.document(userId).update("addresses", addressMaps).await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đặt địa chỉ làm mặc định
     */
    suspend fun setDefaultAddress(addressId: String): Result<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            // Lấy danh sách địa chỉ hiện tại
            val currentAddresses = getAddresses().getOrNull()?.toMutableList()
                ?: return Result.failure(Exception("Không thể lấy danh sách địa chỉ"))
            
            // Cập nhật địa chỉ mặc định
            val updatedList = currentAddresses.map { addr ->
                addr.copy(isDefault = addr.id == addressId)
            }
            
            // Lưu lại vào Firestore
            val addressMaps = updatedList.map { addr -> addressToMap(addr) }
            usersRef.document(userId).update("addresses", addressMaps).await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy địa chỉ mặc định của user hiện tại
     */
    suspend fun getDefaultAddress(): Result<Address?> {
        return try {
            val addresses = getAddresses().getOrNull() ?: emptyList()
            val defaultAddress = addresses.find { it.isDefault } ?: addresses.firstOrNull()
            Result.success(defaultAddress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy địa chỉ theo ID
     */
    suspend fun getAddressById(addressId: String): Result<Address?> {
        return try {
            val addresses = getAddresses().getOrNull() ?: emptyList()
            val address = addresses.find { it.id == addressId }
            Result.success(address)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    /**
     * Chuyển Address object thành Map để lưu Firestore
     */
    private fun addressToMap(address: Address): Map<String, Any?> {
        return mapOf(
            "id" to address.id,
            "label" to address.label,
            "addressLine" to address.addressLine,
            "ward" to address.ward,
            "district" to address.district,
            "city" to address.city,
            "contactName" to address.contactName,
            "phone" to address.phone,
            "isDefault" to address.isDefault,
            "latitude" to address.latitude,
            "longitude" to address.longitude,
            "createdAt" to address.createdAt
        )
    }
}
