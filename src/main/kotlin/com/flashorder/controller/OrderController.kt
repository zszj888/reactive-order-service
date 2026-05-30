package com.flashorder.controller

import com.flashorder.dto.CreateOrderRequest
import com.flashorder.dto.OrderResponse
import com.flashorder.service.OrderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val service: OrderService
) {

    @PostMapping
    suspend fun create(
        @RequestBody req: CreateOrderRequest
    ): OrderResponse {
        return service.create(req)
    }
}
