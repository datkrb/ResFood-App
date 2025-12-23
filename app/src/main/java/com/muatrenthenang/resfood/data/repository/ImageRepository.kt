

package com.muatrenthenang.resfood.data.repository

import android.content.Context
import android.util.Log
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo


object ImageRepository {
    private var inited = false
    private fun initCloudinary(context: Context) {
        if (inited) return
        val config: HashMap<String, String> = hashMapOf(
            "cloud_name" to "dd6hyrrdf",
        )
        MediaManager.init(context, config)
        inited = true
    }

    /**
     * Upload ảnh lên Cloudinary.
     * @param context Context của ứng dụng
     * @param uri Đường dẫn ảnh cần upload
     * @param folder Thư mục lưu ảnh trên Cloudinary (mặc định: "foods/")
     * @param onResult Callback trả về url ảnh hoặc thông báo lỗi
     */
    fun uploadImage(
        context: Context,
        uri: Uri,
        folder: String = "foods/",
        onResult: (url: String?, errorMessage: String?) -> Unit
    ) {
        initCloudinary(context)
        MediaManager.get().upload(uri)
            .unsigned("resfood_preset")
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("ImageRepository", "Bắt đầu upload: $uri")
                }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url")?.toString()
                    Log.d("ImageRepository", "Đã upload ảnh: $uri -> $url")
                    onResult(url, null)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    val msg = "Lỗi upload ảnh: $uri - ${error?.description}"
                    Log.e("ImageRepository", msg)
                    onResult(null, msg)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
}
