package com.flashorder.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(SoldOutException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleSoldOut(ex: SoldOutException): Map<String, String> {
        return mapOf("error" to (ex.message ?: "Sold out"))
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneral(ex: Exception): Map<String, String> {
        return mapOf("error" to "Internal server error")
    }
}
