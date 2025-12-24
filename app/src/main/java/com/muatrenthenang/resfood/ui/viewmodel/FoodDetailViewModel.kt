package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Topping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.toMutableSet

class FoodDetailViewModel : ViewModel() {

    private val _food = MutableStateFlow<Food?>(null)
    val food: StateFlow<Food?> = _food.asStateFlow()

    private val _allToppings = MutableStateFlow<List<Topping>?>(null)
    val allToppings: StateFlow<List<Topping>?> = _allToppings.asStateFlow()

    private val _selectedToppings = MutableStateFlow<Set<Topping>>(emptySet())
    val selectedToppings: StateFlow<Set<Topping>> = _selectedToppings.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _totalPrice = MutableStateFlow(0)
    val totalPrice: StateFlow<Int> = _totalPrice.asStateFlow()

    // data tam de test
    val sampleFoods = listOf(
        Food(
            id = "1",
            name = "Khô gà đè tem",
            price = 36000,
            imageUrl = "https://scontent.fsgn5-5.fna.fbcdn.net/v/t39.30808-6/600260768_1155860166762988_5756592224410624860_n.jpg?_nc_cat=100&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeH7yzbChNMzkbgIT0ok3MBzozyO-tR3dzCjPI761Hd3MIDrCyNoTQw0KpUMtjiT1jpG3jvcLS0viKRcfr89xd51&_nc_ohc=wVz7QoDR-xMQ7kNvwFFMYjP&_nc_oc=Adn2rXWKD9rsIwO2t_7S1mlkhdb6A_FifOiNqC6lPsIMqS5TzK_M5pABEFtzZd_23RI&_nc_zt=23&_nc_ht=scontent.fsgn5-5.fna&_nc_gid=-4YJ9NjytchcwS0pWVwF1w&oh=00_AflyAtg8mRJx669GJ7A5IFRskqqSVPFR5jjRkZqMIC0png&oe=6950314B",
            calories = 360,
            rating = 3.6f,
            description = "Khô gà anh Độ, một sản phẩm của Streamer Đị Mi Xô đã ra mắt khá " +
                    "lâu nhưng vẫn được nhiều anh em tin dùng và ăn thử. Giá ưu đãi chỉ 36k " +
                    "và chỉ còn 18k khi bạn đến phố 120 Yên Lãng, Hà Nội. Hãy thử ngay! " +
                    "\nNừn ná na na anh mộ pi xi." +
                    "\nƯớc gì được ăn khô gà nguyên năm"
        ),
        Food(
            id = "2",
            name = "Phở Sài Gòn",
            price = 50000,
            imageUrl = "https://thanhnien.mediacdn.vn/uploaded/minhnguyet/2016_04_06/phosaigon_UAKG.jpg",
            calories = 410,
            rating = 4.9f,
            description = "Tương ớt, tương đen, ớt sa tế, giá, rau... Và dĩ nhiên không thể thiếu món tiết hột gà. Thịt bò viên chính là điểm giúp phở Lệ lấy lòng thực khách tứ phương. Đó là còn chưa nói đến lượng thịt của phở Lệ khá nhiều, đầy hơn những nơi khác nên chỉ cần ăn một tô là đã đủ no căng bụng"
        ),
        Food(
            id = "3",
            name = "Hủ tiếu Mỹ Tho",
            price = 55000,
            imageUrl = "https://cdn.tgdd.vn/Files/2017/05/09/980903/cach-nau-hu-tieu-my-tho-dam-chat-mien-tay-5_760x509.jpg",
            calories = 405,
            rating = 4.7f,
            description = "Hủ tiếu Mỹ Tho bao gồm thịt lát, thịt bằm, xương và gan heo, có tiệm còn thêm tôm khô vào để nước dùng ngọt hơn. Vị ngọt đậm đà từ nước hầm xương và các phụ gia khác khiến hủ tiếu Mỹ Tho đậm chất miền Tây Nam Bộ. Hủ tiếu Mỹ Tho thường được ăn kèm với phụ gia là giá sống, hành phi, chanh ớt, tiêu, nước tương. Nước chấm là nước tương, pha chút giấm và đường, có nơi sẽ có thêm tép mỡ và hành phi. Hủ tiếu Mỹ Tho ngoài nổi tiếng nhờ nước lèo thơm ngọt vị miền Tây, còn nổi tiếng nhờ cọng hủ tiếu trong và dai, khi nấu không bị bở, hay mềm đi, chỉ trừ khi nấu lâu quá"
        )
    )

    val listTopping = listOf(
        Topping(
            name = "Trứng lòng đào",
            price = 5000,
            imageUrl = "https://cdn.tgdd.vn/2021/11/CookRecipe/GalleryStep/thanh-pham-453.jpg"
        ),
        Topping(
            name = "Thịt nướng",
            price = 7000,
            imageUrl = "https://cdn.tgdd.vn/2021/11/CookRecipe/GalleryStep/thanh-pham-453.jpg"
        ),
        Topping(
            name = "Tốp mỡ",
            price = 3000,
            imageUrl = "https://cdn.tgdd.vn/2021/11/CookRecipe/GalleryStep/thanh-pham-453.jpg"
        ),
    )
    //

    fun loadFoodDetail(foodId: String) {
        _food.value = sampleFoods.find { it.id == foodId }
        _allToppings.value = listTopping

        updateTotalPrice()
    }

    fun increaseQuantity() {
        if (_quantity.value < 10){
            _quantity.update { it + 1 }
            updateTotalPrice()
        }
    }

    fun decreaseQuantity() {
        if (_quantity.value > 1) {
            _quantity.update { it - 1 }
            updateTotalPrice()
        }
    }

    fun onToppingSelected(topping: Topping, isSelected: Boolean) {
        _selectedToppings.update { currentSelected ->
            val newSelected = currentSelected.toMutableSet()
            if (isSelected) {
                newSelected.add(topping)
            } else {
                newSelected.remove(topping)
            }
            newSelected
        }
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val foodPrice = _food.value?.price ?: 0
        val currentQuantity = _quantity.value
        val toppingsPrice = _selectedToppings.value.sumOf { it.price }

        _totalPrice.value = (foodPrice * currentQuantity) + toppingsPrice
    }
}