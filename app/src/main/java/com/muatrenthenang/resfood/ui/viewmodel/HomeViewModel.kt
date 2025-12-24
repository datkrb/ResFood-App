package com.muatrenthenang.resfood.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tapas
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.CategoryItem
import com.muatrenthenang.resfood.data.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.muatrenthenang.resfood.ui.screens.home.HomeUiState

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {

            // danh mục món ăn
            val categoryData = listOf(
                CategoryItem(icon = Icons.Default.Restaurant, name = "Món chính"),
                CategoryItem(icon = Icons.Default.Tapas, name = "Món phụ"),
                CategoryItem(icon = Icons.Default.EmojiFoodBeverage, name = "Nước uống"),
                CategoryItem(icon = Icons.Default.Fastfood, name = "Tráng miệng")
            )

            // danh sách món ăn (chưa phân theo danh mục)
//            val foodData = listOf(
//                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
//                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
//                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
//                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
//                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
//                )


            // data sample to test
            val foodData = listOf(
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
                            "\nƯớc gì được ăn khô gà nguyên năm",

                ),
                Food(
                    id = "2",
                    name = "Phở Sài Gòn",
                    price = 50000,
                    imageUrl = "https://thanhnien.mediacdn.vn/uploaded/minhnguyet/2016_04_06/phosaigon_UAKG.jpg",
                    calories = 410,
                    rating = 4.9f,
                    description = "tương ớt, tương đen, ớt sa tế, giá, rau... Và dĩ nhiên không thể thiếu món tiết hột gà. Thịt bò viên chính là điểm giúp phở Lệ lấy lòng thực khách tứ phương. Đó là còn chưa nói đến lượng thịt của phở Lệ khá nhiều, đầy hơn những nơi khác nên chỉ cần ăn một tô là đã đủ no căng bụng"
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

            _uiState.value = HomeUiState(
                categories = categoryData,
                foods = foodData
            )
        }
    }
}
