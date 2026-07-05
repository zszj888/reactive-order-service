package com.flashorder.controller

import com.flashorder.config.currentRequestContext
import com.flashorder.dto.CreateOrderRequest
import com.flashorder.dto.OrderResponse
import com.flashorder.service.OrderService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.coroutines.withLoggingContextAsync
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val service: OrderService
) {
    private val log = KotlinLogging.logger {}

    @PostMapping
    suspend fun create(
        @RequestBody req: CreateOrderRequest
    ): OrderResponse {
        val ctx = currentRequestContext()
        return withLoggingContextAsync(
            "userId" to req.userId.toString(),
            "productId" to req.productId.toString(),
            "correlationId" to ctx.correlationId,
            "conversationId" to ctx.conversationId
        ) {
            log.info { "Received order: userId=${req.userId}, productId=${req.productId}, correlationId=${ctx.correlationId}, conversationId=${ctx.conversationId}" }
            service.create(req)
        }
    }
}
