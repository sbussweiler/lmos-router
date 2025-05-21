package org.eclipse.lmos.routing.core.hybrid

import org.eclipse.lmos.routing.core.EmbeddingChatModelRoutingRequest
import org.eclipse.lmos.routing.core.Router
import org.eclipse.lmos.routing.core.semantic.EmbeddingRoutingResult
import org.eclipse.lmos.routing.core.semantic.EmbeddingRouter
import org.eclipse.lmos.routing.core.llm.ChatModelRouter

/**
 * [HybridRouter] combines [EmbeddingRouter] and [ChatModelRouter].
 * It first attempts semantic routing and falls back to LLM-based routing if needed.
 */
interface HybridRouter : Router {
    /**
     * Routes a query using semantic search, and optionally an LLM if no agent is found.
     *
     * @param query The user input.
     * @param tenant The tenant identifier.
     * @param conversationId The conversation context ID.
     * @return An [EmbeddingRoutingResult] containing the resolved agent.
     */
    fun resolveAgent(routingRequest: EmbeddingChatModelRoutingRequest): EmbeddingRoutingResult
}
