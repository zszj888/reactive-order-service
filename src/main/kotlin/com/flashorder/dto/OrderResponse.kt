package com.flashorder.dto

data class OrderResponse(
    val orderId: Long,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val status: String
)
