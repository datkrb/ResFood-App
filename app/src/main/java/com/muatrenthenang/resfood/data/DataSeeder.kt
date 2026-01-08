package com.muatrenthenang.resfood.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.*
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class DataSeeder {
    private val db = FirebaseFirestore.getInstance()

    suspend fun seedAll() {
        seedFoods()
        seedTables()
        seedPromotions()
        seedUsers()
        seedOrders()
    }

    private suspend fun seedFoods() {
        val ref = db.collection("foods")
        if (ref.limit(1).get().await().isEmpty) {
            val foods = listOf(
                Food(name = "Beefsteak Sốt Tiêu", description = "Bò Mỹ thượng hạng sốt tiêu đen", price = 250000, imageUrl = "https://img.freepik.com/free-photo/grilled-beef-steak-dark-wooden-surface_1150-44344.jpg", isAvailable = true),
                Food(name = "Pizza Hải Sản", description = "Tôm, mực, nghêu, phô mai mozzarella", price = 180000, imageUrl = "https://img.freepik.com/free-photo/top-view-pepperoni-pizza-with-mushroom-sausages-bell-pepper-olive-corn-black-wooden_140725-12283.jpg", isAvailable = true),
                Food(name = "Mì Ý Carbonara", description = "Sốt kem trứng, thịt xông khói", price = 120000, imageUrl = "https://img.freepik.com/free-photo/pasta-carbonara-with-bacon-parmesan-cheese_140725-12154.jpg", isAvailable = true),
                Food(name = "Salad Cá Ngừ", description = "Rau tươi, cá ngừ ngâm dầu", price = 85000, imageUrl = "https://img.freepik.com/free-photo/tuna-salad-with-fresh-vegetables-bowl_1150-44675.jpg", isAvailable = true),
                Food(name = "Khoai Tây Chiên", description = "Chiên giòn rắc phô mai", price = 45000, imageUrl = "https://img.freepik.com/free-photo/crispy-french-fries-ketchup-mayonnaise_1150-26588.jpg", isAvailable = true),
                Food(name = "Coca Cola", description = "Lon 330ml", price = 20000, imageUrl = "https://img.freepik.com/free-photo/fresh-cola-drink-glass_144627-16201.jpg", isAvailable = true),
                Food(name = "Mojito Chanh Dây", description = "Thơm mát sảng khoái", price = 45000, imageUrl = "https://img.freepik.com/free-photo/fresh-mojito-cocktail-lime-mint-glass_140725-728.jpg", isAvailable = true),
                Food(name = "Bò Lúc Lắc", description = "Khoai tây chiên, bò lúc lắc", price = 150000, imageUrl = null, isAvailable = false) // Out of stock
            )
            foods.forEach { food ->
                val doc = ref.document()
                doc.set(food.copy(id = doc.id)).await()
            }
        }
    }

    private suspend fun seedTables() {
        val ref = db.collection("tables")
        if (ref.limit(1).get().await().isEmpty) {
            val tables = listOf(
                Table(name = "Bàn 01", seats = 2, status = "EMPTY"),
                Table(name = "Bàn 02", seats = 4, status = "OCCUPIED"),
                Table(name = "Bàn 03", seats = 4, status = "RESERVED"),
                Table(name = "Bàn 04", seats = 6, status = "EMPTY"),
                Table(name = "Bàn 05", seats = 8, status = "EMPTY"),
                Table(name = "Bàn 06", seats = 2, status = "EMPTY"),
                Table(name = "Bàn VIP 1", seats = 10, status = "RESERVED"),
                Table(name = "Bàn Sân Vườn 1", seats = 4, status = "EMPTY")
            )
            tables.forEach { table ->
                val doc = ref.document()
                doc.set(table.copy(id = doc.id)).await()
            }
        }
    }

    private suspend fun seedPromotions() {
        val ref = db.collection("promotions")
        if (ref.limit(1).get().await().isEmpty) {
            val promos = listOf(
                Promotion(name = "Giảm giá khai trương [Tự động]", code = "OPEN50", discountType = 0, discountValue = 50, startDate = Timestamp.now(), endDate = getFutureTimestamp(30)),
                Promotion(name = "Freeship đơn 200k [Tự động]", code = "FREESHIP", discountType = 1, discountValue = 15000, startDate = Timestamp.now(), endDate = getFutureTimestamp(365)),
                Promotion(name = "Giảm 20% cho thành viên mới", code = "NEWMEMBER", discountType = 0, discountValue = 20, startDate = Timestamp.now(), endDate = getFutureTimestamp(60))
            )
            promos.forEach { promo ->
                val doc = ref.document()
                doc.set(promo.copy(id = doc.id)).await()
            }
        }
    }

    private suspend fun seedUsers() {
        val ref = db.collection("users")
        // Check for ANY customer
        if (ref.whereEqualTo("role", "customer").limit(1).get().await().isEmpty) {
            val users = listOf(
                User(fullName = "Nguyễn Văn A", email = "vana@gmail.com", phone = "0901234567", address = "123 Lê Lợi, Q1", rank = "Vàng", points = 1500),
                User(fullName = "Trần Thị B", email = "thib@gmail.com", phone = "0909888777", address = "456 Nguyễn Huệ, Q1", rank = "Bạc", points = 200),
                User(fullName = "Lê Văn C", email = "vanc@gmail.com", phone = "0912345678", address = "789 Cách Mạng Tháng 8, Q3", rank = "Kim Cương", points = 5000),
                User(fullName = "Phạm Thị D", email = "thid@gmail.com", phone = "0987654321", address = "101 Điện Biên Phủ, BT", rank = "Bạc", points = 50)
            )
            users.forEach { user ->
                val doc = ref.document() // Auto-ID, no Auth
                doc.set(user.copy(id = doc.id)).await()
            }
        }
    }

    private suspend fun seedOrders() {
        val ref = db.collection("orders")
        if (ref.limit(1).get().await().isEmpty) {
             val pastOrders = listOf(
                OrderHelper.create("COMPLETED", "Beefsteak Sốt Tiêu", 2, 500000, 24),
                OrderHelper.create("COMPLETED", "Pizza Hải Sản", 1, 180000, 48),
                OrderHelper.create("COMPLETED", "Mì Ý Carbonara", 3, 360000, 2),
                OrderHelper.create("PENDING", "Salad Cá Ngừ", 1, 85000, 0),
                OrderHelper.create("PROCESSING", "Coca Cola", 4, 80000, 0),
                OrderHelper.create("COMPLETED", "Bò Lúc Lắc", 1, 150000, 120), // 5 days ago
                OrderHelper.create("CANCELLED", "Khoai Tây Chiên", 2, 90000, 1)
            )
             pastOrders.forEach { order ->
                val doc = ref.document()
                doc.set(order.copy(id = doc.id)).await()
            }
        }
    }

    private fun getFutureTimestamp(days: Int): Timestamp {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return Timestamp(cal.time)
    }

    object OrderHelper {
        fun create(status: String, foodName: String, qty: Int, total: Int, hoursAgo: Int): Order {
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR_OF_DAY, -hoursAgo)
            
            val item = OrderItem(
                foodId = "mock_food_id",
                foodName = foodName,
                price = (total/qty),
                quantity = qty,
                note = if(qty > 1) "Giao nhanh" else ""
            )
            
            return Order(
                userName = "Khách Seed",
                userPhone = "090xxxxxxx",
                address = "Địa chỉ Mock",
                items = listOf(item),
                total = total,
                subtotal = total,
                status = status,
                createdAt = Timestamp(cal.time)
            )
        }
    }
}
