package com.flashorder.service

import com.flashorder.dto.CreateOrderRequest
import com.flashorder.dto.OrderResponse
import com.flashorder.entity.Order
import com.flashorder.exception.SoldOutException
import com.flashorder.repository.OrderRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val repository: OrderRepository
) {
    private val log = KotlinLogging.logger {}

    private val decrementScript = DefaultRedisScript(
        """
        local current = tonumber(redis.call('GET', KEYS[1]))
        if current == nil or current < tonumber(ARGV[1]) then
            return -1
        end
        return redis.call('DECRBY', KEYS[1], ARGV[1])
        """.trimIndent(),
        Long::class.java
    )

    suspend fun create(req: CreateOrderRequest): OrderResponse {
        val stockKey = "stock:${req.productId}"

        val remaining = redisTemplate.execute(
            decrementScript,
            listOf(stockKey),
            req.quantity.toString()
        ).awaitSingle()

        if (remaining < 0) {
            log.warn { "Sold out: productId=${req.productId}, requested=${req.quantity}" }
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