package com.flashorder.config

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.UUID

@Component
@Order(-1)
class CorrelationFilter : WebFilter {

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-Id"
        const val CONVERSATION_ID_HEADER = "X-Conversation-Id"
        const val CORRELATION_ID_KEY = "correlationId"
        const val CONVERSATION_ID_KEY = "conversationId"
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val correlationId = exchange.request.headers
            .getFirst(CORRELATION_ID_HEADER) ?: UUID.randomUUID().toString()
        val conversationId = exchange.request.headers
            .getFirst(CONVERSATION_ID_HEADER) ?: UUID.randomUUID().toString()

        exchange.attributes[CORRELATION_ID_KEY] = correlationId
        exchange.attributes[CONVERSATION_ID_KEY] = conversationId

        val mdcContext = MDCContext(
            mapOf(
                CORRELATION_ID_KEY to correlationId,
                CONVERSATION_ID_KEY to conversationId
            )
        )

        return mono(mdcContext) {
            chain.filter(exchange).awaitFirstOrNull()
        }.contextWrite { ctx ->
            ctx.put(CORRELATION_ID_KEY, correlationId)
                .put(CONVERSATION_ID_KEY, conversationId)
        }
    }
}
