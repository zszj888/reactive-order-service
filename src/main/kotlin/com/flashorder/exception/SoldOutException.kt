package com.flashorder.exception

class SoldOutException(
    message: String = "Product is sold out"
) : RuntimeException(message)
