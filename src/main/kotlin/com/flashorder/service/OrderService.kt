package com.flashorder.service

import com.flashorder.dto.CreateOrderRequest
import com.flashorder.dto.OrderResponse
import com.flashorder.entity.Order
import com.flashorder.exception.SoldOutException
import com.flashorder.repository.OrderRepository
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val repository: OrderRepository
) {

    suspend fun create(req: CreateOrderRequest): OrderResponse {
        val stockKey = "stock:${req.productId}"

        val stock = redisTemplate
            .opsForValue()
            .get(stockKey)
            .awaitSingle()

        if (stock == null || stock.toInt() < req.quantity) {
            throw SoldOutException("Insufficient stock for product ${req.productId}")
        }

        val saved = repository.save(
            Order(
                userId = req.userId,
                productId = req.productId,
                quantity = req.quantity,
                status = "CREATED"
            )
        )

        return OrderResponse(
            orderId = saved.id!!,
            userId = saved.userId,
            productId = saved.productId,
            quantity = saved.quantity,
            status = saved.status
        )
    }
}
