package com.muatrenthenang.resfood.data.model

data class Table(
    val id: String = "",
    val name: String = "",
    val status: String = "EMPTY", // EMPTY, OCCUPIED, RESERVED
    val seats: Int = 4
)
