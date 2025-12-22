package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoriteItem(
    val id: String,
    val name: String,
    val restaurant: String,
    val imageUrl: String?,
    val price: Long,
    val rating: Double,
    val reviews: Int,
    val isAvailable: Boolean = true,
    var isFavorite: Boolean = true
)

class FavoritesViewModel : ViewModel() {
    private val _items = MutableStateFlow(
        listOf(
            FavoriteItem(
                id = "pho_bo",
                name = "Phở Bò Đặc Biệt",
                restaurant = "Phở 24",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC-SECwSb8wJ_avPwbbZd5sAP-CzQ3nsQ06vPv65Mq1UkOyHfNfgaomKcPS10U6XPJGO7SdPLWeR-zdjDehKyfUJvSd9k-fWMcOu4GHD0zl6bmv6_-MLPSvySvvrfbnBBAjSkfZKt7_rwzAO2nbquk_x0-YoQrF75sbsWP3LxYvl0ET2G-7_xg09sOCcx-8wqQVYzEuyLvFFg46MVuDAESRbGV3508wD5099bWdsiOV4TV_mqJ25C35-vEPhP0TbnL7S-0iRqXj2pA",
                price = 65000L,
                rating = 4.8,
                reviews = 1200,
                isAvailable = true
            ),
            FavoriteItem(
                id = "com_tam",
                name = "Cơm Tấm Sườn Bì",
                restaurant = "Cơm Tấm Cali",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAqdXYtfPiAlIHJVtdp3vDKsS1TTrTcclvzvbg3bE6XX8z2puPZeoQ9_GcHo074uAScWDXMWP1CuBA0fs6drydLA32kv_B0hemUAv0oo4ct_WlqJrneCXa1QfxxjRS6AQMCBArRhVXISEZKC-ZKTbBbqmfGuHHrZ2fJVgsmOt_QcJ6YtH3fC1G_pJ3eQ79_zP3nI-StgMVRDWtsWeU95albGrRBTalc6ZchZm26QFfzrVz5CfxzpjYrwibZjFnknQewYOhZbqcO31I",
                price = 55000L,
                rating = 4.5,
                reviews = 850,
                isAvailable = true
            ),
            FavoriteItem(
                id = "bun_bo",
                name = "Bún Bò Huế",
                restaurant = "Bún Bò Gánh",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCgqWrxCswCG_7zddk78gSQHaHuCyu5OWWDKtJEQV3bYvWrhRv4NdXArVZb-qKqc96x8q9IZ9iztqZcx373Bki7kg7kC6OqBnPEKzN8uWlUMG81amHuYgJGvmjkLYmgdwgjxh8M75mfzBUUB-VD7L2yuncH61REGeoYrvUFEdm982VWuPjMLqcy9yDSX4b_WPH8ff330Vu34z6vwFW2oq1lF65aQoEnJTUKVhYNQ7x-Asqo3Cakb-Hkh92TrjrWNq0EpEQH6odbqwE",
                price = 60000L,
                rating = 4.2,
                reviews = 430,
                isAvailable = false
            ),
            FavoriteItem(
                id = "banh_xeo",
                name = "Bánh Xèo Miền Tây",
                restaurant = "Ăn Là Ghiền",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCp68WQs2ezLvWYHeS-G2xyMU25Y7kAIfnUxdh29tZfbqXHfBVpPHyVel09vYM2GGHL53_RVY66KN9KiEGGxrZlmPsriBB9esMkKuONl1fPLlO8uNG2BQVdDNOl2cWeMi2BTrcTKGbrzZhYZwjx4ouo8gd7-Po2-0Lem9l8QfkSGOb12cVjNVHtizmkmZgtge_eEm21mBqTghHpo_ljzCtQuEo-ZrQPopD-fuGmTsZQJGtNt6Dcg-C4lwkllPliwPQe-kuKzUemaII",
                price = 45000L,
                rating = 4.9,
                reviews = 2100,
                isAvailable = true
            )
        )
    )
    val items = _items.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult = _actionResult.asStateFlow()

    fun toggleFavorite(id: String) {
        _items.value = _items.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    // Remove item from favorites
    fun removeFavorite(id: String) {
        _items.value = _items.value.filterNot { it.id == id }
        _actionResult.value = "Đã xóa khỏi danh sách yêu thích"
    }

    fun formatCurrency(value: Long): String {
        return java.text.DecimalFormat("###,###").format(value) + "đ"
    }

    fun addToCart(id: String) {
        val item = _items.value.firstOrNull { it.id == id }
        viewModelScope.launch {
            if (item == null) return@launch
            if (!item.isAvailable) {
                _actionResult.value = "Sản phẩm hiện không có"
                return@launch
            }
            _actionResult.value = "Đã thêm \"${item.name}\" vào giỏ hàng"
        }
    }

    fun clearResult(){ _actionResult.value = null }
}