package com.muatrenthenang.resfood.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Model đại diện cho chi nhánh nhà hàng
 * Ứng dụng chỉ sử dụng 1 chi nhánh duy nhất với id = "main_branch"
 */
data class Branch(
    var id: String = "",
    var name: String = "",
    var address: Address = Address(),
    @get:PropertyName("table_count") @set:PropertyName("table_count") var tableCount: Int = 0,
    @get:PropertyName("max_capacity") @set:PropertyName("max_capacity") var maxCapacity: Int = 50,
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl: String? = null,
    var phone: String = "",
    @get:PropertyName("opening_hours") @set:PropertyName("opening_hours") var openingHours: String = "10:00 - 22:00",
    @get:PropertyName("shipping_fee") @set:PropertyName("shipping_fee") var shippingFee: Long = 15000
) {
    companion object {
        const val PRIMARY_BRANCH_ID = "main_branch"
        
        /**
         * Tạo branch mặc định với thông tin cơ bản
         */
        fun createDefault(): Branch = Branch(
            id = PRIMARY_BRANCH_ID,
            name = "ResFood Restaurant",
            address = Address(
                id = "restaurant_address",
                label = "Nhà hàng",
                addressLine = "123 Đường ABC",
                ward = "Phường 1",
                district = "Quận 1",
                city = "TP. Hồ Chí Minh",
                contactName = "ResFood",
                phone = "0123456789",
                isDefault = true,
                latitude = 10.7769,
                longitude = 106.7009
            ),
            tableCount = 10,
            maxCapacity = 50,
            phone = "0123456789",
            openingHours = "10:00 - 22:00"
        )
    }
}
