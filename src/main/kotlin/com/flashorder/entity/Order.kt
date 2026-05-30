package com.flashorder.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("orders")
data class Order(
    @Id
    val id: Long? = null,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val status: String = "CREATED",
    val createdAt: Instant = Instant.now()
)
