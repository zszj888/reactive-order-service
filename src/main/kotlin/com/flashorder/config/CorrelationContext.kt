package com.flashorder.config

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext

data class RequestContext(
    val correlationId: String,
    val conversationId: String
)

suspend fun currentRequestContext(): RequestContext {
    val ctx = currentCoroutineContext()
    val reactorCtx = ctx[ReactorContext.Key]?.context ?: return RequestContext("", "")
    return RequestContext(
        correlationId = reactorCtx.getOrDefault(CorrelationFilter.CORRELATION_ID_KEY, "") ?: "",
        conversationId = reactorCtx.getOrDefault(CorrelationFilter.CONVERSATION_ID_KEY, "") ?: ""
    )
}
