package com.muatrenthenang.resfood.ui.viewmodel.admin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.api.ImgBBUploader
import com.muatrenthenang.resfood.data.model.Topping
import com.muatrenthenang.resfood.data.repository.ToppingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ToppingEditViewModel(
    private val repository: ToppingRepository = ToppingRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    // Topping State
    private val _topping = MutableStateFlow(Topping())
    val topping = _topping.asStateFlow()

    fun loadTopping(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTopping(id)
                .onSuccess { _topping.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun updateName(name: String) { _topping.value = _topping.value.copy(name = name) }
    fun updatePrice(price: Int) { _topping.value = _topping.value.copy(price = price) }
    fun updateImageUrl(url: String) { _topping.value = _topping.value.copy(imageUrl = url) }

    fun saveTopping(context: Context, imageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            
            val currentTopping = _topping.value
            if (currentTopping.name.isBlank()) {
                _error.value = "Tên topping không được để trống"
                _isSaving.value = false
                return@launch
            }

            // Upload image if selected
            if (imageUri != null) {
                val imageUrl = uploadImage(context, imageUri)
                if (imageUrl == null) {
                    _error.value = "Lỗi upload ảnh"
                    _isSaving.value = false
                    return@launch
                }
                _topping.value = _topping.value.copy(imageUrl = imageUrl)
            }

            val finalTopping = _topping.value
            val result = if (finalTopping.id.isBlank()) {
                repository.addTopping(finalTopping)
            } else {
                repository.updateTopping(finalTopping)
            }

            result.onSuccess { onSuccess() }
                .onFailure { _error.value = it.message }
            
            _isSaving.value = false
        }
    }
    
    private suspend fun uploadImage(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
                val bytes = inputStream.readBytes()
                inputStream.close()
                
                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)
                
                val response = ImgBBUploader.api.uploadImage(ImgBBUploader.getApiKey(), imagePart)
                if (response.success && response.data != null) {
                    response.data.url
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
